package net.telentity.teleport;

import io.canvasmc.canvas.event.EntityPostTeleportAsyncEvent;
import io.canvasmc.canvas.event.EntityTeleportAsyncEvent;
import net.telentity.api.registrable.RegiStore;
import net.telentity.api.tools.EntityShowHide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;

public class CanvasTeleportListener extends AbstractTeleportListener {

    public CanvasTeleportListener(Plugin plugin, RegiStore regiStore, EntityShowHide showHide) {
        super(plugin, regiStore, showHide);
    }

    @EventHandler
    public void onPlayerTeleport(EntityTeleportAsyncEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        handleTeleport(player, event.getFrom(), event.getTo(), event.getCause());
    }

}
