package de.V10lator.V10lift;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitScheduler;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;
import de.V10lator.V10lift.API.Cuboid;
import de.V10lator.V10lift.API.WGMan;

class VLCE implements CommandExecutor {
    private final V10lift plugin;

    VLCE(V10lift plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String command,
        String[] args) {
        if (!plugin.hasPerm(sender, "v10lift.build") &&
            !plugin.hasPerm(sender, "v10lift.admin")) {
            sender.sendMessage(ChatColor.RED + "Jij mag dit niet doen!");
            return true;
        }
        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }
        //PLAYERS ONLY
        if (args[0].equalsIgnoreCase("create")) {
            String player = ((Player) sender).getName();
            if (plugin.builds.containsKey(player)) {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "De lift heeft een naam nodig!");
                    return true;
                }
                HashSet < LiftBlock > blocks = plugin.builds.get(player);
                if (blocks.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "Voeg eerst blokken toe!");
                    return true;
                }
                StringBuilder sb = new StringBuilder(args[1]);
                for (int i = 2; i < args.length; i++) {
                    sb.append(" ");
                    sb.append(args[i]);
                }
                int l = sb.length();
                if (l > 15)
                    sb.delete(16, l);
                String lift = sb.toString().trim();
                if (!plugin.api.createNewLift(player, lift))
                    sender.sendMessage(ChatColor.RED + "Oeps! Deze naam bestaat al!");
                TreeSet < LiftBlock > blcks = plugin.lifts.get(lift).blocks;
                for (LiftBlock lb: blocks)
                    plugin.api.addBlockToLift(blcks, lb);
                plugin.api.sortLiftBlocks(lift);
                plugin.builds.remove(player);
                sender.sendMessage(ChatColor.GREEN + "Lift \"" + ChatColor.YELLOW + lift + ChatColor.GREEN + "\" gemaakt!");
                ((Player) sender).performCommand("v10lift edit " + lift);
            } else {
                plugin.builds.put(player, new HashSet < LiftBlock > ());
                sender.sendMessage(ChatColor.GOLD + "Ok, voeg nu alle blokken van de cabine toe door met rechtermuisknop op de blokken te drukken.");
                sender.sendMessage(ChatColor.GOLD + "Typ dan: /v10lift create <NAAM>");
            }
            //PLAYERS ONLY
        } else if (args[0].equalsIgnoreCase("offlinefix")) {
            String player = ((Player) sender).getName();
            if (!plugin.editors.containsKey(player)) {
                sender.sendMessage(ChatColor.RED + "Start editor modus eerst!");
                return true;
            }
            Lift lift = plugin.lifts.get(plugin.editors.get(player));
            lift.offline = false;
            for (LiftBlock lb: lift.offlineInputs) {
                lb.active = false;
                plugin.api.setOffline(plugin.editors.get(player), lb.active);
                Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] De speler heeft de lift op niet-offline gezet!");
            }
            //PLAYERS ONLY
        } else if (args[0].equalsIgnoreCase("delete")) {
            String player = ((Player) sender).getName();
            if (args.length < 2) {
                sendHelp(sender, "delete");
                return true;
            }
            StringBuilder sb = new StringBuilder(args[1]);
            for (int i = 2; i < args.length; i++)
                sb.append(" ").append(args[i]);
            String lift = sb.toString();
            if (!plugin.lifts.containsKey(lift)) {
                sender.sendMessage(ChatColor.RED + "Lift \"" + ChatColor.YELLOW + lift + ChatColor.RED + "\" bestaat niet!");
                return true;
            }
            if (!plugin.lifts.get(lift).owners.contains(player) && !plugin.hasPerm(sender, "v10lift.admin")) {
                sender.sendMessage(ChatColor.RED + "Jij bent niet de eigenaar van die lift!");
                return true;
            }
            plugin.api.removeLift(lift);
            sender.sendMessage(ChatColor.GOLD + "Lift verwijderd.");
        } else if (args[0].equalsIgnoreCase("start")) {
            // /aplift start <NAAM> <Verdieping>
            if (args.length < 3) {
                sendHelp(sender, "start");
                return true;
            }

            if (!plugin.lifts.containsKey(args[1])) {
                Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] De lift bestaat niet!");
                return false;
            }

            Lift l = plugin.lifts.get(args[1]);

            if (!l.floors.containsKey(args[2])) {
                Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] De vloer van de lift bestaat niet!");
                return false;
            }

            Floor f = l.floors.get(args[2]);

            plugin.api.addToQueue(args[1], f, args[2]);
            sender.sendMessage(ChatColor.GOLD + "Lift gestart.");
        } else if (args[0].equalsIgnoreCase("setspeed")) {
            // /aplift setspeed <NAAM> <SPEED>
            if (args.length < 3) {
                sendHelp(sender, "speed");
                return true;
            }

            if (!plugin.lifts.containsKey(args[1])) {
                Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] De lift bestaat niet!");
                return false;
            }

            Lift l = plugin.lifts.get(args[1]);

            try {
                l.speed = Integer.valueOf(args[2]);
            } catch (NumberFormatException ex) {
                Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] Die speed is geen getal!");
                return false;
            }
            sender.sendMessage(ChatColor.GOLD + "Lift speed aangepast.");
        } else if (args[0].equalsIgnoreCase("stop")) {
            // /aplift stop <NAAM>
            if (args.length < 2) {
                sendHelp(sender, "stop");
                return true;
            }

            if (!plugin.lifts.containsKey(args[1])) {
                Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] De lift bestaat niet!");
                return false;
            }

            Lift l = plugin.lifts.get(args[1]);

            if (!l.queue.isEmpty()) {
                l.queue.clear();
            }

            if (plugin.movingTasks.get(args[1]) == null) {
                Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] De lift bevat geen moving tasks!");
                return false;
            }

            Bukkit.getScheduler().cancelTask(plugin.movingTasks.get(args[1]));

            plugin.movingTasks.remove(args[1]);

            sender.sendMessage(ChatColor.GOLD + "Lift gestopt.");
            //PLAYERS ONLY
        } else if (args[0].equalsIgnoreCase("abort")) {
            String player = ((Player) sender).getName();
            boolean abort = false;

            if (plugin.builds.containsKey(player)) {
                plugin.builds.remove(player);
                abort = true;
            }
            if (plugin.whoisReq.contains(player)) {
                plugin.whoisReq.remove(player);
                abort = true;
            }
            if (plugin.inputEdits.containsKey(player)) {
                plugin.inputEdits.remove(player);
                abort = true;
            }
            if (plugin.inputRemoves.contains(player)) {
                plugin.inputRemoves.remove(player);
                abort = true;
            }
            if (plugin.offlineEdits.contains(player)) {
                plugin.offlineEdits.remove(player);
                abort = true;
            }
            if (plugin.offlineRemoves.contains(player)) {
                plugin.offlineRemoves.remove(player);
                abort = true;
            }
            if (plugin.builder.contains(player)) {
                plugin.builder.remove(player);
                plugin.api.sortLiftBlocks(plugin.editors.get(player));
                abort = true;
            }
            if (plugin.ropeEdits.containsKey(player)) {
                plugin.ropeEdits.remove(player);
                abort = true;
            }
            if (plugin.ropeRemoves.contains(player)) {
                plugin.ropeRemoves.remove(player);
                abort = true;
            }
            if (plugin.doorEdits.containsKey(player)) {
                plugin.doorEdits.remove(player);
                abort = true;
            }
            if (abort)
                sender.sendMessage(ChatColor.GOLD + "Geannuleerd.");
            else
                sender.sendMessage(ChatColor.RED + "Oeps! Je kunt niks annuleren.");
            //PLAYERS ONLY
        } else if (args[0].equalsIgnoreCase("edit")) {
            String player = ((Player) sender).getName();
            if (args.length < 2) {
                if (!plugin.editors.containsKey(player)) {
                    sendHelp(sender, "edit");
                    return true;
                }
                String lift = plugin.editors.get(player);
                if (!plugin.lifts.containsKey(lift)) {
                    plugin.editors.remove(player);
                    sender.sendMessage(ChatColor.RED + "Lift niet gevonden!");
                    return true;
                }
                Lift li = plugin.lifts.get(lift);
                plugin.editors.remove(player);
                plugin.inputEdits.remove(player);
                plugin.inputRemoves.remove(player);
                plugin.offlineEdits.remove(player);
                plugin.offlineRemoves.remove(player);
                if (plugin.builder.contains(player)) {
                    plugin.builder.remove(player);
                    plugin.api.sortLiftBlocks(plugin.editors.get(player));
                }
                plugin.ropeEdits.remove(player);
                plugin.ropeRemoves.remove(player);
                plugin.doorEdits.remove(player);
                BlockState bs;
                Server s = plugin.getServer();
                Sign sign;
                for (LiftBlock lb: li.blocks) {
                    bs = s.getWorld(lb.world).getBlockAt(lb.x, lb.y, lb.z).getState();
                    if (!(bs instanceof Sign))
                        continue;
                    sign = (Sign) bs;
                    if (!sign.getLine(0).equalsIgnoreCase(plugin.signText))
                        continue;
                    sign.setLine(3, "");
                    sign.update();
                }
                Iterator < LiftSign > liter = li.signs.iterator();
                while (liter.hasNext()) {
                    LiftSign ls = liter.next();
                    bs = s.getWorld(ls.world).getBlockAt(ls.x, ls.y, ls.z).getState();
                    if (!(bs instanceof Sign)) {
                        s.getLogger().info("[" + plugin.getName() + "] Verkeerde sign verwijderd op: " + ls.x + ", " + ls.y + ", " + ls.z + " in world " + ls.world);
                        liter.remove();
                        continue;
                    }
                    sign = (Sign) bs;
                    sign.setLine(3, ls.oldText);
                    sign.update();
                    ls.oldText = null;
                }
                sender.sendMessage(ChatColor.GOLD + "Editor uitgezet!");
                return true;
            } else if (plugin.editors.containsKey(player)) {
                sender.sendMessage(ChatColor.RED + "Oeps! Je bent nog in editor modus!");
                return true;
            }
            StringBuilder sb = new StringBuilder(args[1]);
            for (int i = 2; i < args.length; i++) {
                sb.append(" ");
                sb.append(args[i]);
            }
            String lift = sb.toString();
            if (!plugin.lifts.containsKey(lift)) {
                sender.sendMessage(ChatColor.RED + "Lift \"" + ChatColor.YELLOW + lift + ChatColor.RED + "\" bestaat niet!");
                return true;
            }
            Lift li = plugin.lifts.get(lift);
            if (!li.owners.contains(player) && !plugin.hasPerm(sender, "v10lift.admin")) {
                sender.sendMessage(ChatColor.RED + "Jij bent niet de eigenaar van deze lift!");
                return true;
            }
            plugin.editors.put(player, lift);
            BlockState bs;
            Server s = plugin.getServer();
            Sign sign;
            for (LiftBlock lb: li.blocks) {
                bs = s.getWorld(lb.world).getBlockAt(lb.x, lb.y, lb.z).getState();
                if (!(bs instanceof Sign))
                    continue;
                sign = (Sign) bs;
                if (!sign.getLine(0).equalsIgnoreCase(plugin.signText))
                    continue;
                sign.setLine(3, ChatColor.RED + "Onderhoud");
                sign.update();
            }
            Iterator < LiftSign > liter = li.signs.iterator();
            while (liter.hasNext()) {
                LiftSign ls = liter.next();
                bs = s.getWorld(ls.world).getBlockAt(ls.x, ls.y, ls.z).getState();
                if (!(bs instanceof Sign)) {
                    s.getLogger().info("[" + plugin.getName() + "] Verkeerde sign verwijderd op: " + ls.x + ", " + ls.y + ", " + ls.z + " in wereld " + ls.world);
                    liter.remove();
                    continue;
                }
                sign = (Sign) bs;
                ls.oldText = sign.getLine(3);
                sign.setLine(3, ChatColor.RED + "Onderhoud");
                sign.update();
            }
            sender.sendMessage(ChatColor.GREEN + "Editor aangezet!");
            //PLAYERS ONLY
        } else if (args[0].equalsIgnoreCase("floor")) {
            String player = ((Player) sender).getName();
            if (!plugin.editors.containsKey(player)) {
                sender.sendMessage(ChatColor.RED + "Zet eerst de editor modus aan!");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Geen vloernaam opgegeven!");
                sendHelp(sender, "floor");
                return true;
            }
            String lift = plugin.editors.get(player);
            if (args[1].equalsIgnoreCase("add")) {
                Block b = ((Player) sender).getLocation().getBlock();
                StringBuilder sb = new StringBuilder(args[2]);
                for (int i = 3; i < args.length; i++) {
                    sb.append(" ");
                    sb.append(args[i]);
                }
                String floor = sb.toString();

                int ret = plugin.api.addNewFloor(lift, floor, new Floor(b.getY() - 1, b.getWorld().getName()));
                if (ret == 0)
                    sender.sendMessage(ChatColor.GREEN + "Vloer \"" + ChatColor.YELLOW + floor + ChatColor.GREEN + "\" aangemaakt!");
                else if (ret == -2)
                    sender.sendMessage(ChatColor.RED + "Je kunt niet 2 vloeren met dezelfde naam aanmaken!");
                else if (ret == -3)
                    sender.sendMessage(ChatColor.RED + "Je kunt niet 2 vloeren op dezelfde hoogte aanmaken!");
                else
                    sender.sendMessage(ChatColor.RED + "Internal error!");
            } else if (args[1].equalsIgnoreCase("del")) {
                StringBuilder sb = new StringBuilder(args[2]);
                for (int i = 3; i < args.length; i++) {
                    sb.append(" ");
                    sb.append(args[i]);
                }
                if (plugin.api.removeFloor(lift, sb.toString()))
                    sender.sendMessage(ChatColor.GOLD + "Vloer verwijderd!");
                else
                    sender.sendMessage(ChatColor.RED + "Vloer \"" + ChatColor.YELLOW + sb.toString() + ChatColor.RED + "\" niet gevonden!");
            } else if (args[1].equalsIgnoreCase("rename")) {
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Geen vloernaam opgegeven!");
                    sendHelp(sender, "floor");
                    return true;
                }
                StringBuilder sb = new StringBuilder(args[2]);
                String floor = sb.toString();
                int i = 3;
                Lift li = plugin.lifts.get(lift);
                while (!li.floors.containsKey(floor)) {
                    sb.append(" ").append(args[i]);
                    floor = sb.toString();
                    i++;
                    if (i >= args.length) {
                        sender.sendMessage(ChatColor.RED + "Vloer \"" + ChatColor.YELLOW + floor + ChatColor.RED + "\" niet gevonden!");
                        return true;
                    }
                }
                if (args.length < i + 1) {
                    sender.sendMessage(ChatColor.RED + "Geen vloernaam opgegeven!");
                    sendHelp(sender, "floor");
                    return true;
                }
                sb = new StringBuilder(args[i]);
                i++;
                for (; i < args.length; i++)
                    sb.append(" ").append(args[i]);
                int ret = plugin.api.renameFloor(lift, floor, sb.toString());
                if (ret == -4)
                    sender.sendMessage(ChatColor.RED + "Je kunt geen 2 vloeren met dezelfde naam aanmaken!");
                else if (ret < 0)
                    sender.sendMessage(ChatColor.RED + "Internal error!");
                else
                    sender.sendMessage(ChatColor.GREEN + "Vloernaam aangepast!");
            } else {
                sendHelp(sender, "floor");
                return true;
            }
            //PLAYERS ONLY
        } else if (args[0].equalsIgnoreCase("input")) {
            String player = ((Player) sender).getName();
            if (!plugin.editors.containsKey(player)) {
                sender.sendMessage(ChatColor.RED + "Start editor modus eerst!");
                return true;
            }
            if (args.length < 2) {
                sendHelp(sender, "input");
                return true;
            }
            Lift lift = plugin.lifts.get(plugin.editors.get(player));
            if (args[1].equalsIgnoreCase("add")) {
                String floor = null;
                if (args.length < 3) {
                    Block b = ((Player) sender).getLocation().getBlock();
                    Floor f = new Floor(b.getY() - 1, b.getWorld().getName());
                    if (!lift.floors.containsValue(f)) {
                        sender.sendMessage(ChatColor.RED + "Automatische vloerdetectie niet gevonden!");
                        return true;
                    }
                    for (Entry < String, Floor > e: lift.floors.entrySet()) {
                        Floor fl = e.getValue();
                        if (fl.equals(f)) {
                            floor = e.getKey();
                            break;
                        }
                    }
                } else {
                    StringBuilder sb = new StringBuilder(args[2]);
                    for (int i = 3; i < args.length; i++) {
                        sb.append(" ");
                        sb.append(args[i]);
                    }
                    floor = sb.toString();
                    if (!lift.floors.containsKey(floor)) {
                        sender.sendMessage(ChatColor.RED + "Vloer \"" + ChatColor.YELLOW + floor + ChatColor.RED + "\" niet gevonden!");
                        return true;
                    }
                }
                if (plugin.inputEdits.containsKey(player) ||
                    plugin.inputRemoves.contains(player)) {
                    sender.sendMessage(ChatColor.RED + "Je bent nog een input aan het aanpassen!");
                    return true;
                }
                plugin.inputEdits.put(player, floor);
                sender.sendMessage(ChatColor.GOLD + "Klik nu met rechtermuisknop op het inputblok!");
            } else if (args[1].equalsIgnoreCase("del")) {
                if (lift.inputs.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "Er is geen input om te verwijderen!");
                    return true;
                }
                if (plugin.inputEdits.containsKey(player) ||
                    plugin.inputRemoves.contains(player)) {
                    sender.sendMessage(ChatColor.RED + "Je bent nog een input aan het aanpassen!");
                    return true;
                }
                plugin.inputRemoves.add(player);
                sender.sendMessage(ChatColor.GOLD + "Klik nu met rechtermuisknop op het inputblok!");
            } else {
                sendHelp(sender, "input");
                return true;
            }
            //PLAYERS ONLY
        } else if (args[0].equalsIgnoreCase("offline")) {
            String player = ((Player) sender).getName();
            if (!plugin.editors.containsKey(player)) {
                sender.sendMessage(ChatColor.RED + "Start editor modus eerst!");
                return true;
            }
            if (args.length < 2) {
                sendHelp(sender, "offline");
                return true;
            }
            Lift lift = plugin.lifts.get(plugin.editors.get(player));
            if (args[1].equalsIgnoreCase("add")) {
                if (plugin.offlineEdits.contains(player) ||
                    plugin.offlineRemoves.contains(player)) {
                    sender.sendMessage(ChatColor.RED + "Je bent nog een offline input aan het aanpassen!");
                    return true;
                }
                plugin.offlineEdits.add(player);
                sender.sendMessage(ChatColor.GOLD + "Klik nu met rechtermuisknop op het offline inputblok!");
            } else if (args[1].equalsIgnoreCase("del")) {
                if (lift.offlineInputs.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "Geen offline inputblok om te verwijderen!");
                    return true;
                }
                if (plugin.offlineEdits.contains(player) ||
                    plugin.offlineRemoves.contains(player)) {
                    sender.sendMessage(ChatColor.RED + "Je bent nog een offline input aan het aanpassen!");
                    return true;
                }
                plugin.offlineRemoves.add(player);
                sender.sendMessage(ChatColor.GOLD + "Klik nu met rechtermuisknop op het offline inputblok!");
            } else {
                sendHelp(sender, "offline");
                return true;
            }
            //PLAYERS ONLY
        } else if (args[0].equalsIgnoreCase("rename")) {
            String player = ((Player) sender).getName();
            if (!plugin.editors.containsKey(player)) {
                sender.sendMessage(ChatColor.RED + "Start editor modus eerst!");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Geen naam opgegeven!");
                return true;
            }
            StringBuilder sb = new StringBuilder(args[1]);
            for (int i = 2; i < args.length; i++) {
                sb.append(" ");
                sb.append(args[i]);
            }
            int l = sb.length();
            if (l > 15)
                sb.delete(16, l);
            String nn = sb.toString().trim();
            plugin.api.renameLift(plugin.editors.get(player), nn);
            plugin.editors.put(player, nn);
            sender.sendMessage(ChatColor.GREEN + "Lift hernoemd!");
            //PLAYERS ONLY
        } else if (args[0].equalsIgnoreCase("build") && args.length == 1) {
            String player = ((Player) sender).getName();
            //Niet worldedit modus!
            if (!plugin.editors.containsKey(player)) {
                sender.sendMessage(ChatColor.RED + "Start editor modus eerst!");
                return true;
            }
            if (plugin.builder.contains(player)) {
                plugin.builder.remove(player);
                plugin.api.sortLiftBlocks(plugin.editors.get(player));
                sender.sendMessage(ChatColor.GREEN + "Bouwmodus uitgezet!");
                return true;
            }
            plugin.builder.add(player);
            sender.sendMessage(ChatColor.GOLD + "Klik nu met rechtermuisknop op de liftblokken!");
            sender.sendMessage(ChatColor.GOLD + "Doe daarna /v10lift build om het op te slaan!");
            //PLAYERS ONLY
        } else if (args[0].equalsIgnoreCase("build") &&
            args.length == 2 &&
            args[1].equalsIgnoreCase("worldedit")) {
            String player = ((Player) sender).getName();
            //Worldedit modus
            if (!plugin.editors.containsKey(player)) {
                sender.sendMessage(ChatColor.RED + "Start editor modus eerst!");
                return true;
            }
            if (!plugin.builder.contains(player)) {
                plugin.builder.add(player);
            }
            Player p = (Player) sender;
            Selection s = WGMan.wep.getSelection(p);
            if (s == null) {
                sender.sendMessage(ChatColor.RED + "Start editor modus eerst!");
                return true;
            }
            Vector pos1 = s.getNativeMinimumPoint();
            Vector pos2 = s.getNativeMaximumPoint();

            Cuboid cuboid = new Cuboid(pos1, pos2, p.getWorld());
            List < Block > blocks = cuboid.blockList();

            Bukkit.getLogger().severe("WG DING!");
            Bukkit.getLogger().severe(blocks.toString());
            Boolean gelukt = true;
            Integer misluktaantal = 0;
            for (Block bl: blocks) {
            	
            	if (bl.getType() == Material.AIR) {
            		continue;
            	}

                Bukkit.getLogger().severe("Stap 6");
                int ret = plugin.api.switchBlockAtLift(plugin.editors.get(player), bl);
                switch (ret) {
                    case 0:
                    	continue;
                    case 1:
                        gelukt = false;
                        misluktaantal = misluktaantal + 1;
                        continue;
                    case -2:
                        gelukt = false;
                        misluktaantal = misluktaantal + 1;
                        continue;
                    default:
                        gelukt = false;
                        misluktaantal = misluktaantal + 1;
                        continue;
                }
            }
            if (gelukt == true && misluktaantal == 0) {
                sender.sendMessage(ChatColor.GREEN + "Blokken toegevoegd aan de lift.");
            } else {
                sender.sendMessage(ChatColor.GREEN + "Blokken niet (allemaal) toegevoegd aan de lift.");
            }

            if (plugin.builder.contains(player)) {
                plugin.builder.remove(player);
                plugin.api.sortLiftBlocks(plugin.editors.get(player));
                sender.sendMessage(ChatColor.GREEN + "Bouwmodus uitgezet!");
                return true;
            }
            //PLAYERS ONLY
        } else if (args[0].equalsIgnoreCase("rope")) {
            String player = ((Player) sender).getName();
            if (!plugin.editors.containsKey(player)) {
                sender.sendMessage(ChatColor.RED + "Start editor modus eerst!");
                return true;
            }
            if (args.length < 2) {
                sendHelp(sender, "rope");
                return true;
            }
            if (args[1].equalsIgnoreCase("add")) {
                if (plugin.ropeEdits.containsKey(player) || plugin.ropeRemoves.contains(player)) {
                    sender.sendMessage(ChatColor.RED + "Je bent de noodtrap nog aan het aanpassen.");
                    return true;
                }
                plugin.ropeEdits.put(player, null);
                sender.sendMessage(ChatColor.GOLD + "Klik nu met rechtermuisknop op het begin en het einde van de noodtrap.");
            } else if (args[1].equalsIgnoreCase("del")) {
                if (plugin.ropeEdits.containsKey(player) || plugin.ropeRemoves.contains(player)) {
                    sender.sendMessage(ChatColor.RED + "Je bent de noodtrap nog aan het aanpassen.");
                    return true;
                }
                plugin.ropeRemoves.add(player);
                sender.sendMessage(ChatColor.GOLD + "Klik nu met rechtermuisknop op de noodtrap.");
            }
            //PLAYERS ONLY
        } else if (args[0].equalsIgnoreCase("door")) {
            String player = ((Player) sender).getName();
            if (!plugin.editors.containsKey(player)) {
                sender.sendMessage(ChatColor.RED + "Start editor modus eerst!");
                return true;
            }
            if (plugin.doorEdits.containsKey(player)) {
                plugin.doorEdits.remove(player);
                sender.sendMessage(ChatColor.RED + "Deur editor modus uitgezet!");
                return true;
            }
            Lift lift = plugin.lifts.get(plugin.editors.get(player));
            String floor = null;
            if (args.length < 3) {
                Location loc = ((Player) sender).getLocation();
                Floor f = new Floor(loc.getBlockY() - 1, loc.getWorld().getName());
                if (!lift.floors.containsValue(f)) {
                    sender.sendMessage(ChatColor.RED + "Automatische vloerdetectie mislukt!");
                    return true;
                }
                for (Entry < String, Floor > e: lift.floors.entrySet()) {
                    Floor fl = e.getValue();
                    if (fl.equals(f)) {
                        floor = e.getKey();
                        break;
                    }
                }
            } else {
                StringBuilder sb = new StringBuilder(args[2]);
                for (int i = 3; i < args.length; i++) {
                    sb.append(" ");
                    sb.append(args[i]);
                }
                floor = sb.toString();
                if (!lift.floors.containsKey(floor)) {
                    sender.sendMessage(ChatColor.RED + "Vloer \"" + ChatColor.YELLOW + floor + ChatColor.RED + "\" niet gevonden!");
                    return true;
                }
            }
            plugin.doorEdits.put(player, floor);
            sender.sendMessage(ChatColor.GOLD + "Klik nu met rechtermuisknop op de deurblokken!");
            sender.sendMessage(ChatColor.GOLD + "Doe daarna /v10lift door om het op te slaan!");
            //PLAYERS ONLY
        } else if (args[0].equalsIgnoreCase("whitelist")) {
            String player = ((Player) sender).getName();
            if (!plugin.editors.containsKey(player)) {
                sender.sendMessage(ChatColor.RED + "Start editor modus eerst!");
                return true;
            }
            if (args.length < 3) {
                sendHelp(sender, "whitelist");
                return true;
            }
            Lift lift = plugin.lifts.get(plugin.editors.get(player));
            Player wp = plugin.getServer().getPlayer(args[2]);
            String wpn;
            if (wp == null)
                wpn = args[2].toLowerCase();
            else
                wpn = wp.getName().toLowerCase();
            String floor = null;
            if (args.length < 4) {
                Block b = ((Player) sender).getLocation().getBlock();
                Floor f = new Floor(b.getY() - 1, b.getWorld().getName());
                if (!lift.floors.containsValue(f)) {
                    sender.sendMessage(ChatColor.RED + "Automatische vloerdetectie mislukt!");
                    return true;
                }
                for (Entry < String, Floor > e: lift.floors.entrySet()) {
                    Floor fl = e.getValue();
                    if (fl.equals(f)) {
                        floor = e.getKey();
                        break;
                    }
                }
            } else {
                StringBuilder sb = new StringBuilder(args[3]);
                for (int i = 4; i < args.length; i++) {
                    sb.append(" ");
                    sb.append(args[i]);
                }
                floor = sb.toString();
                if (!lift.floors.containsKey(floor)) {
                    sender.sendMessage(ChatColor.RED + "Vloer \"" + ChatColor.YELLOW + floor + ChatColor.RED + "\" niet gevonden!");
                    return true;
                }
            }
            Floor f = lift.floors.get(floor);
            if (args[1].equalsIgnoreCase("add")) {
                if (f.whitelist.contains(wpn))
                    sender.sendMessage(ChatColor.RED + "Whitelist bevat " + wpn + " al!");
                else {
                    f.whitelist.add(wpn);
                    sender.sendMessage(ChatColor.GREEN + wpn + " toegevoegd!");
                }
            } else if (args[1].equalsIgnoreCase("del")) {
                if (!f.whitelist.contains(wpn))
                    sender.sendMessage(ChatColor.RED + "Whitelist bevat " + wpn + " nog niet!");
                else {
                    f.whitelist.remove(wpn);
                    sender.sendMessage(ChatColor.GREEN + wpn + " verwijderd!");
                    if (f.whitelist.isEmpty())
                        sender.sendMessage(ChatColor.YELLOW + "Whitelist is leeg, daarom wordt hij uitgezet!");
                }
            }
            //PLAYERS ONLY
        } else if (args[0].equalsIgnoreCase("whois")) {
            String player = ((Player) sender).getName();
            if (args.length < 2) {
                plugin.whoisReq.add(player);
                sender.sendMessage(ChatColor.GOLD + "Klik nu met rechtermuisknop op het blok dat u wilt checken!");
            } else {
                String lift;
                if (args.length == 2)
                    lift = args[1];
                else {
                    StringBuilder sb = new StringBuilder(args[1]);
                    for (int i = 2; i < args.length; i++)
                        sb.append(' ').append(args[i]);
                    lift = sb.toString();
                }
                if (!plugin.api.isLift(lift))
                    sender.sendMessage(ChatColor.RED + "Lift \"" + ChatColor.GOLD + lift + ChatColor.RED + "\" niet gevonden!");
                else
                    plugin.api.sendLiftInfo(((Player) sender), lift);
            }
            //PLAYERS ONLY SPEED COMMAND
        } else if (args[0].equalsIgnoreCase("speed")) {
            String player = ((Player) sender).getName();
            if (!plugin.editors.containsKey(player)) {
                sender.sendMessage(ChatColor.RED + "Start editor modus eerst!");
                return true;
            }
            if (args.length < 2) {
                sendHelp(sender, "speed");
                return true;
            }
            Lift lift = plugin.lifts.get(plugin.editors.get(player));
            try {
                lift.speed = Integer.parseInt(args[1]);
                if (lift.speed < 1)
                    lift.speed = 1;
                sender.sendMessage(ChatColor.GREEN + "Nieuwe liftsnelheid: " + lift.speed);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Verkeerde snelheid: " + args[1]);
                sendHelp(sender, "speed");
            }
            //PLAYERS ONLY SPEED COMMAND
        } else if (args[0].equalsIgnoreCase("sound")) {
            String player = ((Player) sender).getName();
            if (!plugin.editors.containsKey(player)) {
                sender.sendMessage(ChatColor.RED + "Start editor modus eerst!");
                return true;
            }
            Lift lift = plugin.lifts.get(plugin.editors.get(player));
            if (lift.sound) {
                lift.sound = false;
                sender.sendMessage(ChatColor.GOLD + "Sound modus uitgezet!");
            } else {
                lift.sound = true;
                sender.sendMessage(ChatColor.GOLD + "Sound modus aangezet!");
            }
            //PLAYERS ONLY
        } else if (args[0].equalsIgnoreCase("realistic")) {
            String player = ((Player) sender).getName();
            if (!plugin.editors.containsKey(player)) {
                sender.sendMessage(ChatColor.RED + "Start editor modus eerst!");
                return true;
            }
            Lift lift = plugin.lifts.get(plugin.editors.get(player));
            if (lift.realistic) {
                lift.realistic = false;
                sender.sendMessage(ChatColor.GOLD + "Realistische modus uitgezet!");
            } else {
                lift.realistic = true;
                sender.sendMessage(ChatColor.GOLD + "Realistische modus aangezet!");
            }
        } else if (args[0].equalsIgnoreCase("help")) {
            if (args.length < 2)
                sendHelp(sender);
            else
                sendHelp(sender, args[1]);
        } else if (args[0].equalsIgnoreCase("reset")) {
            if (!plugin.hasPerm(sender, "v10lift.admin")) {
                sender.sendMessage(ChatColor.RED + "Jij mag dit commando niet uitvoeren!");
                return true;
            }
            System.out.print("DEBUG: Resetting");
            BukkitScheduler bs = plugin.getServer().getScheduler();
            for (Entry < String, Lift > e: plugin.lifts.entrySet()) {
                String lift = e.getKey();
                System.out.print("DEBUG: Stopping " + lift);
                if (plugin.movingTasks.containsKey(lift))
                    bs.cancelTask(plugin.movingTasks.get(lift));
                System.out.print("DEBUG: Removing queue of " + lift);
                e.getValue().queue = null;
                System.out.print("DEBUG: Re-sorting lift blocks of " + lift);
                plugin.api.sortLiftBlocks(lift);
            }
            System.out.print("DEBUG: Clearing movingTasks");
            plugin.movingTasks.clear();
            System.out.print("DEBUG: Reloading config");
            plugin.reloadConfig();
            System.out.print("DEBUG: Loading...");
            plugin.load();
            System.out.print("DEBUG: Saving config...");
            plugin.saveConfig();
            sender.sendMessage(ChatColor.YELLOW + "Gereset!");
        } else if (args[0].equalsIgnoreCase("repair")) {
            if (!plugin.hasPerm(sender, "v10lift.repair.master")) {
                sender.sendMessage(ChatColor.RED + "Jij mag dit commando niet uitvoeren!");
                return true;
            }
            if (args.length < 2) {
                sendHelp(sender, "repair");
                return true;
            }
            StringBuilder sb = new StringBuilder(args[1]);
            for (int i = 2; i < args.length; i++)
                sb.append(" ").append(args[i]);
            String lift = sb.toString();
            if (!plugin.lifts.containsKey(lift)) {
                sender.sendMessage(ChatColor.RED + "Lift \"" + ChatColor.YELLOW + lift + ChatColor.RED + "\" bestaat niet!");
                return true;
            }
            if (!plugin.api.isDefective(lift)) {
                sender.sendMessage("Deze lift is niet kapot!");
                return true;
            }
            Player p = (Player) sender;
            if (p.getGameMode() == GameMode.SURVIVAL && plugin.masterAmount > 0) {
                PlayerInventory pi = p.getInventory();
                if (!pi.contains(new ItemStack(plugin.masterItem), plugin.masterAmount)) {
                    sender.sendMessage(ChatColor.RED + "Je hebt nodig: " + plugin.masterAmount + "x " + plugin.masterItem.toString().replace('_', ' ').toLowerCase());
                    return true;
                }
                pi.remove(new ItemStack(plugin.masterItem, plugin.masterAmount));
            }
            plugin.api.setDefective(lift, false);
            sender.sendMessage(ChatColor.GREEN + "Lift gerepareerd!");
        } else
            sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sendHelp(sender, null);
    }

    private void sendHelp(CommandSender sender, String section) {
        if (section == null)
            section = "main";

        if (section.equals("create")) {
            sender.sendMessage("Use /v10lift create");
            sender.sendMessage("to create a new lift.");
        } else if (section.equals("delete")) {
            sender.sendMessage("Use /v10lift delete your lift");
            sender.sendMessage("to delete \"your lift\".");
        } else if (section.equals("edit")) {
            sender.sendMessage("Use /v10lift edit \"my lift\"");
            sender.sendMessage("to remove/delete floors and input blocks from the lift \"my lift\".");
        } else if (section.equals("floor")) {
            sender.sendMessage("Use /v10lift floor add floor name");
            sender.sendMessage("to create a new floor called \"floor name\".");
            sender.sendMessage("Use /v10lift floor del floor name");
            sender.sendMessage("to delete the floor called \"floor name\".");
            sender.sendMessage("Use /v10lift floor rename floor name new name");
            sender.sendMessage("to rename the floor \"floor name\" to \"new name\".");
        } else if (section.equalsIgnoreCase("input")) {
            sender.sendMessage("Use /v10lift input add <floor name>");
            sender.sendMessage("to create a new input for the floor \"floor name\".");
            sender.sendMessage("If no floor name is given it will try to use the floor you're standing at.");
            sender.sendMessage("Use /v10lift input del");
            sender.sendMessage("to delete an input block.");
        } else if (section.equalsIgnoreCase("offline")) {
            sender.sendMessage("Use /v10lift offline add");
            sender.sendMessage("to create a new offline input.");
            sender.sendMessage("Use /v10lift offline del");
            sender.sendMessage("to delete a offline input block.");
        } else if (section.equals("build")) {
            sender.sendMessage("Use /v10lift build");
            sender.sendMessage("to start or stop the building mode.");
            sender.sendMessage("In that mode you can change the blocks of a still builded lift.");
        } else if (section.equals("rename")) {
            sender.sendMessage("Use /v10lift rename <new name>");
            sender.sendMessage("to rename the lift to \"new name\"");
        } else if (section.equals("rope")) {
            sender.sendMessage("Use /v10lift rope add");
            sender.sendMessage("to add a rope to your lift.");
            sender.sendMessage("Use /v10lift rope del");
            sender.sendMessage("to remove a rope from your lift.");
        } else if (section.equals("door")) {
            sender.sendMessage("Use /v10lift door floor name");
            sender.sendMessage("to edit a door for the floor\"floor name\".");
            sender.sendMessage("Defaults to the floor you are standing at if no floor name given.");
            sender.sendMessage("Use /v10lift door");
            sender.sendMessage("to stop the door edit mode.");
        } else if (section.equalsIgnoreCase("abort")) {
            sender.sendMessage("Use /v10lift abort");
            sender.sendMessage("to cancel your actuall lift creation, (offline) input or floor edit.");
        } else if (section.equalsIgnoreCase("whitelist")) {
            sender.sendMessage("Use /v10lift whitelist add player <floor name>");
            sender.sendMessage("to add a player to the whitelist of floor \"floor name\".");
            sender.sendMessage("Use /v10lift del player <floor name>");
            sender.sendMessage("to remove a player from the whitelist of floor \"floor name\".");
            sender.sendMessage("If no floor name is given it will try to use the floor you're standing at.");
        } else if (section.equalsIgnoreCase("speed")) {
            sender.sendMessage("Use /v10lift sound");
            sender.sendMessage("to toggle sound mode.");
        } else if (section.equalsIgnoreCase("speed")) {
            sender.sendMessage("Use /v10lift speed X");
            sender.sendMessage("to set the lift speed to X ticks (1 second = 20 ticks).");
        } else if (section.equalsIgnoreCase("realistic")) {
            sender.sendMessage("Use /v10lift realistic");
            sender.sendMessage("to toggle realistic mode.");
        } else if (section.equalsIgnoreCase("repair")) {
            sender.sendMessage("Use /v10lift repair lift name");
            sender.sendMessage("to repair the lift \"lift name\"");
        } else if (section.equalsIgnoreCase("start")) {
            sender.sendMessage("Use /v10lift start lift name floor name");
            sender.sendMessage("to let the lift move to that floor");
        } else if (section.equalsIgnoreCase("repair")) {
            sender.sendMessage("Use /v10lift stop lift name");
            sender.sendMessage("to stop the lift");
        } else {
            sender.sendMessage("Use /v10lift create");
            sender.sendMessage("/v10lift delete");
            sender.sendMessage("/v10lift edit");
            sender.sendMessage("/v10lift floor");
            sender.sendMessage("/v10lift input");
            sender.sendMessage("/v10lift offline");
            sender.sendMessage("/v10lift build");
            sender.sendMessage("/v10lift rope");
            sender.sendMessage("/v10lift door");
            sender.sendMessage("/v10lift abort");
            sender.sendMessage("/v10lift whois");
            sender.sendMessage("/v10lift sound");
            sender.sendMessage("/v10lift speed");
            sender.sendMessage("/v10lift realistic");
            sender.sendMessage("/v10lift whitelist");
            if (plugin.hasPerm(sender, "v10lift.admin"))
                sender.sendMessage("/v10lift reset");
            if (plugin.hasPerm(sender, "v10lift.repair.master"))
                sender.sendMessage("/v10lift repair");
            sender.sendMessage("/v10lift start");
            sender.sendMessage("/v10lift stop");
            sender.sendMessage("For more help try /v10lift help command");
        }
    }
}