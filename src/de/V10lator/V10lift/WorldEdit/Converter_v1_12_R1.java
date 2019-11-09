package de.V10lator.V10lift.WorldEdit;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.sk89q.worldedit.bukkit.selections.Selection;

import de.V10lator.V10lift.V10lift;
import de.V10lator.V10lift.API.Cuboid;

public class Converter_v1_12_R1 {
	
	public static Cuboid getSelectionAsCuboid(Player p) {
		Selection s = V10lift.worldedit.getSelection(p);
        if (s == null) {
            p.sendMessage(ChatColor.RED + "You have to make a selection to do this.");
            return null;
        }
        Location pos1 = new Location(p.getLocation().getWorld(), s.getNativeMinimumPoint().getX(), s.getNativeMinimumPoint().getY(), s.getNativeMinimumPoint().getZ());
        Location pos2 = new Location(p.getLocation().getWorld(), s.getNativeMaximumPoint().getX(), s.getNativeMaximumPoint().getY(), s.getNativeMaximumPoint().getZ());

        Cuboid cuboid = new Cuboid(pos1, pos2);
        return cuboid;
	}

}
