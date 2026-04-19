package net.telentity.api;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TeHandle {
    @NotNull
    String getPermissionName();

    @NotNull
    String getReasonDescription();

    @NotNull
    List<? extends Entity> getEntitiesToTeleport(@NotNull Player player);

    void beforeTeleport(@NotNull Player player, @NotNull Entity entity);

    void afterTeleport(@NotNull Player player, @NotNull Entity entity);

    /**
     * How many ticks after the entity's teleport to call {@link #afterTeleport}.
     * Handlers that depend on other handlers having already run (e.g. passengers
     * waiting for the vehicle to remount the player) should return a higher value.
     */
    default long afterTeleportDelayTicks() {
        return 3L;
    }
}