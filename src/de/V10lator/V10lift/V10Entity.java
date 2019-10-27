package de.V10lator.V10lift;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

class V10Entity {
    final Entity e;
    private final Location loc;
    final int y;
    short step = 0;

    V10Entity(Entity e, Location loc, int y) {
        this.e = e;
        this.loc = loc;
        this.y = y;
    }

    void moveUp() {
        if (e == null || e.isDead())
            return;
        loc.setY(y + step);
        e.teleport(loc);
    }

    void moveDown() {
        if (e == null || e.isDead())
            return;
        loc.setY(y - step);
        e.teleport(loc);
    }

    public int hashCode() {
        return 31 * 1 + ((e == null) ? 0 : e.getUniqueId().hashCode());
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        UUID uuid;
        if (obj instanceof V10Entity) {
            Entity ent = ((V10Entity) obj).e;
            if (ent == null || ent.isDead()) {
                if (e == null || e.isDead())
                    return true;
                return false;
            }
            uuid = ent.getUniqueId();
        } else if (obj instanceof Entity) {
            Entity ent = (Entity) obj;
            if (ent.isDead()) {
                if (e == null || e.isDead())
                    return true;
                return false;
            }
            uuid = ((Entity) obj).getUniqueId();
        } else
            return false;
        if (e == null || e.isDead())
            return false;
        if (uuid == e.getUniqueId())
            return true;
        return false;
    }
}