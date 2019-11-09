package de.V10lator.V10lift.API;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.V10lator.V10lift.WorldEdit.Converter_v1_10_R1;
import de.V10lator.V10lift.WorldEdit.Converter_v1_11_R1;
import de.V10lator.V10lift.WorldEdit.Converter_v1_12_R1;
import de.V10lator.V10lift.WorldEdit.Converter_v1_13_R1;
import de.V10lator.V10lift.WorldEdit.Converter_v1_13_R2;
import de.V10lator.V10lift.WorldEdit.Converter_v1_14_R1;
import de.V10lator.V10lift.WorldEdit.Converter_v1_9_R1;
import de.V10lator.V10lift.WorldEdit.Converter_v1_9_R2;

public class ConverterUtil {
	static String version;
	
	public ConverterUtil(Plugin pl) {
		version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		Bukkit.getLogger().info("[V10Lift] Your version: " + version);
		
		switch (version) {
	        case "v1_10_R1": {
	        	Bukkit.getLogger().info("[V10Lift] Loading support for v1_10_R1!");
	        	break;
	        }
	        case "v1_11_R1": {
	        	Bukkit.getLogger().info("[V10Lift] Loading support for v1_11_R1!");
	        	break;
	        }
	        case "v1_12_R1": {
	        	Bukkit.getLogger().info("[V10Lift] Loading support for v1_12_R1!");
	        	break;
	        }
	        case "v1_13_R1": {
	        	Bukkit.getLogger().info("[V10Lift] Loading support for v1_13_R1!");
	        	break;
	        }
	        case "v1_13_R2": {
	        	Bukkit.getLogger().info("[V10Lift] Loading support for v1_13_R2!");
	        	break;
	        }
	        case "v1_14_R1": {
	        	Bukkit.getLogger().info("[V10Lift] Loading support for v1_14_R1!");
	        	break;
	        }
	        case "v1_9_R1": {
	        	Bukkit.getLogger().info("[V10Lift] Loading support for v1_9_R1!");
	        	break;
	        }
	        case "v1_9_R2": {
	        	Bukkit.getLogger().info("[V10Lift] Loading support for v1_9_R2!");
	        	break;
	        }
	        default:
	        	Bukkit.getLogger().severe("[V10Lift] Version not supported! Plugin will now disable...");
	        	pl.getPluginLoader().disablePlugin(pl);
	        	break;
		}
	}
	
	public Cuboid getSelectionAsCuboid(Player p) {
        switch (version) {
            case "v1_10_R1": {
                return Converter_v1_10_R1.getSelectionAsCuboid(p);
            }
            case "v1_11_R1": {
            	return Converter_v1_11_R1.getSelectionAsCuboid(p);
            }
            case "v1_12_R1": {
            	return Converter_v1_12_R1.getSelectionAsCuboid(p);
            }
            case "v1_13_R1": {
            	return Converter_v1_13_R1.getSelectionAsCuboid(p);
            }
            case "v1_13_R2": {
            	return Converter_v1_13_R2.getSelectionAsCuboid(p);
            }
            case "v1_14_R1": {
            	return Converter_v1_14_R1.getSelectionAsCuboid(p);
            }
            case "v1_9_R1": {
            	return Converter_v1_9_R1.getSelectionAsCuboid(p);
            }
            case "v1_9_R2": {
            	return Converter_v1_9_R2.getSelectionAsCuboid(p);
            }
            default:
                return null;
        }
    }
}