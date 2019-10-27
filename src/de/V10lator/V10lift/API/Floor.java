package de.V10lator.V10lift.API;

import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * This class is to handle floors in a API save way.
 * @author V10lator
 *
 */
public class Floor {
    private final int y;
    private final String world;

    /**
     * To create a new floor.
     * @param y - The height of the floor.
     * @param world - The name of the world the floor is in.
     * @throws Exception - If the given world can't be found.
     */
    public Floor(int y, String world) throws Exception {
        this.y = y;
        if (Bukkit.getWorld(world) == null)
            throw new Exception("World not found!");
        this.world = world;
    }

    /**
     * To get the height of the floor.
     * @return The height.
     */
    public int getY() {
        return y;
    }

    /**
     * To get the world the floor is in.
     * This may be null if the world is unloaded.
     * @return The world.
     */
    public World getWorld() {
        return Bukkit.getWorld(world);
    }

    /**
     * To get the name of the world the floor is in.
     * This will never be null.
     * @return The worlds name.
     */
    public String getWorldName() {
        return world;
    }
}