package net.telentity.teleport.handler;

import net.telentity.api.TeHandle;
import net.telentity.api.tools.EntityPassengerTools;
import net.telentity.api.tools.EntityTools;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VehiclePassengerTeleportHandle implements TeHandle {

    private final VehicleTeleportHandle vehicleTeleportHandle;
    private final EntityPassengerTools passengerTools;

    public VehiclePassengerTeleportHandle(VehicleTeleportHandle vehicleTeleportHandle, EntityTools entityTools) {
        this.vehicleTeleportHandle = vehicleTeleportHandle;
        this.passengerTools = entityTools.getEntityPassengerTools();
    }

    @Override
    public @NotNull String getPermissionName() {
        return "passenger";
    }

    @Override
    public @NotNull String getReasonDescription() {
        return "Entity is a passenger of the teleporting player's vehicle.";
    }

    @Override
    public @NotNull List<? extends Entity> getEntitiesToTeleport(@NotNull Player player) {
        final var vehicle = vehicleTeleportHandle.getEntitiesToTeleport(player);
        return vehicle.isEmpty() ? vehicle : passengerTools.getPassengers(vehicle.getFirst()).toList();
    }

    @Override
    public void beforeTeleport(@NotNull Player player, @NotNull Entity entity) {
        // unused here, but a good example of what this does is in the LeashTeleportHandle
    }

    @Override
    public void afterTeleport(@NotNull Player player, @NotNull Entity entity) {
        // player.getVehicle() may be null here if VehicleTeleportHandle.afterTeleport hasn't
        // remounted the player yet (race condition). Retrieve the vehicle directly via the
        // handle which falls back to lastUnmount() when the player isn't mounted yet.
        final var vehicles = vehicleTeleportHandle.getEntitiesToTeleport(player);
        if (vehicles.isEmpty()) return;
        passengerTools.addPassenger(vehicles.getFirst(), entity);
    }
}