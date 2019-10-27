package de.V10lator.V10lift;

import java.io.Serializable;
import java.util.Map;

import org.bukkit.Material;

class LiftBlock implements Comparable < LiftBlock > , Serializable {
    private static final long serialVersionUID = 1L;
    String world;
    final int x;
    int y;
    final int z;

    //For cabine blocks:
    final Material type;
    byte data;
    final String[] lines;

    //For inputs:
    final String floor;
    boolean active = false;

    //For chests:
    Map < String, Object > [] serializedItemStacks = null;

    LiftBlock(String world, int x, int y, int z, String floor) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = null;
        this.data = 0;
        this.lines = null;
        this.floor = floor;
    }

    LiftBlock(String world, int x, int y, int z, Material type, byte data) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
        this.data = data;
        this.lines = null;
        this.floor = null;
    }

    LiftBlock(String world, int x, int y, int z, Material type, byte data, String[] lines) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
        this.data = data;
        this.lines = lines;
        this.floor = null;
    }

    public int compareTo(LiftBlock lb) {
        int ret = ((Integer) y).compareTo(lb.y);
        if (ret == 0)
            ret = ((Integer) x).compareTo(lb.x);
        if (ret == 0)
            ret = ((Integer) z).compareTo(lb.z);
        return ret;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((world == null) ? 0 : world.hashCode());
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof LiftBlock)) {
            if (!(obj instanceof LiftSign))
                return false;
            LiftSign other = (LiftSign) obj;
            if (world.equals(other.world) &&
                x == other.x &&
                y == other.y &&
                z == other.z)
                return true;
            return false;
        }
        LiftBlock other = (LiftBlock) obj;
        if (world.equals(other.world) &&
            x == other.x &&
            y == other.y &&
            z == other.z)
            return true;
        return false;
    }
}