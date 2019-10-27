package de.V10lator.V10lift;

import java.io.Serializable;

class LiftSign implements Serializable {
    private static final long serialVersionUID = 1L;
    String world;
    int x;
    int y;
    int z;
    String oldText = null;

    final byte type;
    byte state;

    LiftSign(String world, int x, int y, int z, byte type, byte state) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
        this.state = state;
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
        if (!(obj instanceof LiftSign)) {
            if (!(obj instanceof LiftBlock))
                return false;
            LiftBlock other = (LiftBlock) obj;
            if (world.equals(other.world) &&
                x == other.x &&
                y == other.y &&
                z == other.z)
                return true;
            return false;
        }
        LiftSign other = (LiftSign) obj;
        if (world.equals(other.world) &&
            x == other.x &&
            y == other.y &&
            z == other.z)
            return true;
        return false;
    }
}