package net.telentity.teleport;

import net.telentity.Telentity;
import net.telentity.api.TeHandle;
import net.telentity.api.tools.EntityShowHide;
import net.telentity.api.registrable.RegiStore;
import net.telentity.store.TeStore;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Set;

public abstract class AbstractTeleportListener implements Listener {

    protected final Plugin plugin;
    protected final RegiStore regiStore;
    protected final EntityShowHide entityShowHide;

    protected AbstractTeleportListener(Plugin plugin, RegiStore regiStore, EntityShowHide showHide) {
        this.plugin = plugin;
        this.regiStore = regiStore;
        this.entityShowHide = showHide;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Collects the entities to teleport for a given player teleport event.
     * Must be called while the player is still at {@code from} (pre-teleport).
     * Returns {@code null} if the teleport should be ignored.
     */
    protected Map<Entity, Set<TeHandle>> collectEntities(Player player, Location from, Location to,
                                                         PlayerTeleportEvent.TeleportCause cause) {
        if (to == null) return null;
        final World fromWorld = from.getWorld();
        final World toWorld = to.getWorld();
        if (fromWorld == null || toWorld == null) return null;
        final boolean sameWorld = fromWorld.equals(toWorld);
        switch (cause) {
            case DISMOUNT, SPECTATE, EXIT_BED -> { return null; }
            case UNKNOWN -> { if (sameWorld && from.distanceSquared(to) <= 3.5) { return null; } }
        }
        return ((TeStore) regiStore.getTeleportHandleStore()).collect(player, to);
    }

    /** Full single-phase path used by Paper (event fires pre-teleport). */
    protected void handleTeleport(Player player, Location from, Location to, PlayerTeleportEvent.TeleportCause cause) {
        final var entities = collectEntities(player, from, to, cause);
        if (entities == null) return;
        processTeleport(player, to, entities);
    }

    /** Schedules the actual entity teleportation. Safe to call post-teleport. */
    protected void processTeleport(Player player, Location to, Map<Entity, Set<TeHandle>> entities) {
        final World toWorld = to.getWorld();
        if (toWorld == null) return;
        final World fromWorld = player.getWorld();
        final boolean sameWorld = fromWorld.equals(toWorld);

        // Register destination chunk on the region that owns the destination location
        Telentity.getScheduler().runAtLocation(to, (t) -> {
            final var toChunk = toWorld.getChunkAt(to);
            regiStore.getChunkEnforcer().register(toChunk);

            entities.forEach((entity, handlers) ->
                // Per-entity chunk access must run on the entity's own region thread
                Telentity.getScheduler().runAtEntity(entity, (t2) -> {
                    final var chunk = entity.getWorld().getChunkAt(entity.getLocation());
                    regiStore.getChunkEnforcer().register(chunk);
                    handlers.forEach(handler -> handler.beforeTeleport(player, entity));
                    beforeTeleport(player, entity, sameWorld);
                    entity.teleportAsync(to);
                    Telentity.getScheduler().runAtEntityLater(entity, (t3) -> {
                        handlers.forEach(handler -> handler.afterTeleport(player, entity));
                        afterTeleport(player, entity, sameWorld);
                        regiStore.getChunkEnforcer().unregister(chunk);
                    }, 3);
                })
            );

            Telentity.getScheduler().runAtLocationLater(to, (t2) -> regiStore.getChunkEnforcer().unregister(toChunk), 4);
        });
    }

    protected void beforeTeleport(Player player, Entity entity, boolean refresh) {
        regiStore.getEntityEnforcer().register(entity);
        entity.eject();
        entity.leaveVehicle();
        entity.setFallDistance(-Float.MAX_VALUE);
        if (refresh) entityShowHide.hideEntity(player, entity);
    }

    protected void afterTeleport(Player player, Entity entity, boolean refresh) {
        entity.setFallDistance(-Float.MAX_VALUE);
        regiStore.getEntityEnforcer().unregister(entity);
        if (refresh) entityShowHide.showEntity(player, entity);
    }
}

