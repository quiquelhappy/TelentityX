package net.telentity.teleport;

import net.telentity.api.registrable.RegiStore;
import net.telentity.api.tools.EntityShowHide;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

public class PaperTeleportListener extends AbstractTeleportListener {

    public PaperTeleportListener(Plugin plugin, RegiStore regiStore, EntityShowHide showHide) {
        super(plugin, regiStore, showHide);
    }

    @EventHandler
    private void onPlayerTeleport(PlayerTeleportEvent event) {
        handleTeleport(event.getPlayer(), event.getFrom(), event.getTo(), event.getCause());
    }
}