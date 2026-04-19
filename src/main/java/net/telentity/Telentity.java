package net.telentity;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import net.telentity.api.registrable.RegiStore;
import net.telentity.api.tools.EntityTools;
import net.telentity.store.MainRegiStore;
import net.telentity.teleport.CanvasTeleportListener;
import net.telentity.teleport.PaperTeleportListener;
import net.telentity.teleport.handler.LeashTeleportHandle;
import net.telentity.teleport.handler.NearbySittableTeleportHandle;
import net.telentity.teleport.handler.VehiclePassengerTeleportHandle;
import net.telentity.teleport.handler.VehicleTeleportHandle;
import net.telentity.teleport.prevent.TelentityPermissions;
import net.telentity.toolkit.MainEntityTools;
import net.telentity.unmount.UnmountResolver;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import static net.telentity.toolkit.VersionTools.isCanvas;

public final class Telentity extends JavaPlugin {

    private static PlatformScheduler scheduler;

    public static PlatformScheduler getScheduler() {
        return Telentity.scheduler;
    }

    @Override
    public void onEnable() {
        FoliaLib foliaLib = new FoliaLib(this);
        Telentity.scheduler = foliaLib.getScheduler();

        final var regiStore = new MainRegiStore(this);
        final var entityTools = new MainEntityTools(this);
        final var unmount = new UnmountResolver(entityTools, this).getUnmount();
        final var vehicleHandle = new VehicleTeleportHandle(unmount, entityTools);
        final var passengerHandle = new VehiclePassengerTeleportHandle(vehicleHandle, entityTools);

        regiStore.getTeleportHandleStore().register(vehicleHandle);
        regiStore.getTeleportHandleStore().register(passengerHandle);
        regiStore.getTeleportHandleStore().register(new LeashTeleportHandle());
        regiStore.getTeleportHandleStore().register(new NearbySittableTeleportHandle());
        regiStore.getTeleportPreventorStore().register(new TelentityPermissions());

        final var rsp = getServer().getServicesManager();
        rsp.register(RegiStore.class, regiStore, this, ServicePriority.Highest);
        rsp.register(EntityTools.class, entityTools, this, ServicePriority.Highest);

        if (isCanvas()) {
            new CanvasTeleportListener(this, regiStore, entityTools.getEntityShowHide());
        } else {
            if (foliaLib.isFolia()) {
                getLogger().info("Folia is known to have unreliable fire teleport events. We recommend using Canvas if the plugin is not behaving as expected (https://github.com/PaperMC/Folia/issues/330)");
            }
            new PaperTeleportListener(this, regiStore, entityTools.getEntityShowHide());
        }

        getLogger().info("Thanks for using this fork of Telentity!");
        getLogger().info("If you have any questions or suggestions, feel free to ask us!");
        getLogger().info("Github: https://github.com/WolfYangFan/TelentityX");
    }

    @Override
    public void onDisable() {
        getServer().getPluginManager().getPermissions().forEach(permission -> {
            if (permission.getName().startsWith("telentity")) {
                getServer().getPluginManager().removePermission(permission.getName());
            }
        });
        getLogger().info("Thanks for using this fork of Telentity!");
    }
}
