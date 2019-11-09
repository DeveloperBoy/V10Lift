package de.V10lator.V10lift.WorldEdit;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;

import de.V10lator.V10lift.API.Cuboid;

public class Converter_v1_13_R1 {
	
	public static Cuboid getSelectionAsCuboid(Player p) {
		LocalSession l = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p));
		Region s;
        try {
            s = l.getSelection(l.getSelectionWorld());
        } catch (IncompleteRegionException e) {
        	p.sendMessage(ChatColor.RED + "You have to make a selection to do this.");
            return null;
        }
        Location pos1 = new Location(p.getLocation().getWorld(), s.getMinimumPoint().getX(), s.getMinimumPoint().getY(), s.getMinimumPoint().getZ());
        Location pos2 = new Location(p.getLocation().getWorld(), s.getMaximumPoint().getX(), s.getMaximumPoint().getY(), s.getMaximumPoint().getZ());

        Cuboid cuboid = new Cuboid(pos1, pos2);
        return cuboid;
	}

}
