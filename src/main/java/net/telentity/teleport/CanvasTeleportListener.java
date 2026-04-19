package net.telentity.teleport;

import io.canvasmc.canvas.event.EntityPostTeleportAsyncEvent;
import io.canvasmc.canvas.event.EntityTeleportAsyncEvent;
import net.telentity.api.TeHandle;
import net.telentity.api.registrable.RegiStore;
import net.telentity.api.tools.EntityShowHide;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CanvasTeleportListener extends AbstractTeleportListener {

    /** Entities collected PRE-teleport, keyed by player UUID. */
    private final Map<UUID, Map<Entity, Set<TeHandle>>> pendingEntities = new ConcurrentHashMap<>();

    public CanvasTeleportListener(Plugin plugin, RegiStore regiStore, EntityShowHide showHide) {
        super(plugin, regiStore, showHide);
    }

    /** PRE-teleport: player is still at source — safe to scan nearby/leashed entities. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPreTeleport(EntityTeleportAsyncEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        final var entities = collectEntities(player, event.getFrom(), event.getTo(), event.getCause());
        if (entities != null && !entities.isEmpty()) {
            pendingEntities.put(player.getUniqueId(), entities);
        }
    }

    /** POST-teleport: player is at destination — process the cached entities. */
    @EventHandler
    public void onPostTeleport(EntityPostTeleportAsyncEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        final var entities = pendingEntities.remove(player.getUniqueId());
        if (entities == null || entities.isEmpty()) return;
        processTeleport(player, event.getTo(), entities);
    }
}
