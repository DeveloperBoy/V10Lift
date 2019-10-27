package de.V10lator.V10lift;

import java.io.Serializable;

import org.bukkit.Material;

class V10Rope implements Serializable {
    private static final long serialVersionUID = 1L;

    final Material id;
    final String startWorld;
    final String endWorld;
    final int x;
    final int minY;
    final int maxY;
    final int z;
    String currentWorld;
    int currently;

    V10Rope(Material id, String startWorld, String endWorld, int x, int minY, int maxY, int z) {
        this.id = id;
        this.startWorld = startWorld;
        this.endWorld = endWorld;
        this.x = x;
        this.minY = minY;
        this.maxY = maxY;
        this.z = z;
        this.currently = minY;
        this.currentWorld = endWorld;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + startWorld.hashCode();
        result = prime * result + endWorld.hashCode();
        result = prime * result + x;
        result = prime * result + minY;
        result = prime * result + maxY;
        result = prime * result + z;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof V10Rope))
            return false;
        V10Rope other = (V10Rope) obj;
        return startWorld.equals(other.startWorld) &&
            endWorld.equals(other.endWorld) &&
            x == other.x &&
            minY == other.minY &&
            maxY == other.maxY &&
            z == other.z;
    }
}