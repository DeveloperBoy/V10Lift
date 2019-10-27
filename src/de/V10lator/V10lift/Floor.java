package de.V10lator.V10lift;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;


class Floor implements Serializable {
    private static final long serialVersionUID = 1L;

    final int y;
    final String world;
    final ArrayList < LiftBlock > doorBlocks = new ArrayList < LiftBlock > ();
    final HashSet < String > whitelist = new HashSet < String > ();

    Floor(int y, String world) {
        this.y = y;
        this.world = world;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((world == null) ? 0 : world.hashCode());
        result = prime * result + y;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Floor other = (Floor) obj;
        if (world == null) {
            if (other.world != null)
                return false;
        } else if (!world.equals(other.world))
            return false;
        if (y != other.y)
            return false;
        return true;
    }
}