package de.V10lator.V10lift;

import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;


class VLL implements Listener {
    private final V10lift plugin;

    VLL(V10lift plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        String[] lines = event.getLines();
        if (!lines[0].equalsIgnoreCase(plugin.signText))
            return;
        Player player = event.getPlayer();
        if (lines[1].isEmpty()) {
            player.sendMessage(ChatColor.RED + "No lift name given!");
            return;
        }
        if (!plugin.lifts.containsKey(lines[1])) {
            player.sendMessage(ChatColor.RED + "Lift \"" + ChatColor.YELLOW + lines[1] + ChatColor.RED + "\" doesn't exists!");
            event.setCancelled(true);
            return;
        }
        Lift lift = plugin.lifts.get(lines[1]);
        if (!lift.owners.contains(player.getName()) && !plugin.hasPerm(player, "v10lift.admin")) {
            player.sendMessage(ChatColor.RED + "You can't do this!");
            event.setCancelled(true);
            return;
        }
        byte type;
        if (lift.floors.containsKey(lines[2])) {
            type = 1;
            event.setLine(3, ChatColor.GRAY + lines[2]);
        } else
            type = 0;
        event.setLine(2, "");

        Block block = event.getBlock();
        lift.signs.add(new LiftSign(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), type, (byte) 0));
        player.sendMessage(ChatColor.GREEN + "Lift sign created!");
    }

    @SuppressWarnings("unlikely-arg-type")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (plugin.api.isRope(block)) {
            event.getPlayer().sendMessage(ChatColor.RED + "You can't do this! (Remove the emergency stairs first!)");
            event.setCancelled(true);
            return;
        }
        LiftBlock tlb = new LiftBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), null);
        Lift lift;
        for (Entry < String, Lift > e: plugin.lifts.entrySet()) {
            lift = e.getValue();
            if (lift.blocks.contains(tlb)) {
            	event.getPlayer().sendMessage(ChatColor.RED + "You can't do this! (Remove the lift first!)");
                event.setCancelled(true);
                return;
            }

            for (Floor f: lift.floors.values()) {
                if (f.doorBlocks.contains(tlb)) {
                	event.getPlayer().sendMessage(ChatColor.RED + "You can't do this! (Remove the door first!)");
                    event.setCancelled(true);
                    return;
                }
            }

            if (!(block.getState() instanceof Sign))
                continue;
            if (!lift.signs.contains(tlb))
                continue;
            Player p = event.getPlayer();
            if (!lift.owners.contains(p.getName()) && !plugin.hasPerm(p, "v10lift.admin")) {
                p.sendMessage(ChatColor.RED + "You can't do this!");
                event.setCancelled(true);
            } else {
                lift.signs.remove(tlb);
                event.getPlayer().sendMessage(ChatColor.YELLOW + "Lift sign removed!");
            }
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractButton(PlayerInteractEvent event) {
    	Action action = event.getAction();
    	Block block = event.getClickedBlock();
        Material button = event.getClickedBlock().getType();
        if(action == Action.RIGHT_CLICK_BLOCK && (button.toString().contains("_BUTTON") || button == Material.LEVER)) {
        	Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] It was a button!");
        	String world = block.getWorld().getName();
	        int x = block.getX();
	        int y = block.getY();
	        int z = block.getZ();
	        for (Entry < String, Lift > e: plugin.lifts.entrySet()) {
	            Lift lift = e.getValue();
	            for (LiftBlock lb: lift.offlineInputs) {
	                if (world.equals(lb.world) &&
	                    x == lb.x &&
	                    y == lb.y &&
	                    z == lb.z) {
	                	Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] Location matches an elevator!");
	                    if (lb.active == false) {
	                        lb.active = true;
	                        plugin.api.setOffline(e.getKey(), lb.active);
	                        Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] The lever has put the elevator on offline!");
	                    } else {
	                    	lb.active = false;
	                    	plugin.api.setOffline(e.getKey(), lb.active);
	                    	Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] The lever has put the elevator on non-offline!");
	                    }
	                    return;
	                }
	            }
	            lift.offline = false;
	            if (lift.offline) {
	            	Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] The elevator is offline!");
	                return;
	            }
	            for (LiftBlock lb: lift.inputs) {
	                if (world.equals(lb.world) &&
	                    x == lb.x &&
	                    y == lb.y &&
	                    z == lb.z) {
	                	Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] The button worked!");
                        plugin.api.addToQueue(e.getKey(), lift.floors.get(lb.floor), lb.floor);
	                    return;
	                }
	            }
	        }
        }
    }
    @SuppressWarnings("unlikely-arg-type")
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
    	EquipmentSlot hand = event.getHand();
    	if (hand == null || !hand.equals(EquipmentSlot.HAND))
        	return;
        Player player = event.getPlayer();
        String pn = player.getName();
        if (plugin.builds.containsKey(pn)) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;
            event.setCancelled(true);
            int ret = plugin.api.switchBlockAtLift(plugin.builds.get(pn), event.getClickedBlock());
            switch (ret) {
                case 0:
                    player.sendMessage(ChatColor.GREEN + "Block added to the elevator.");
                    break;
                case 1:
                    player.sendMessage(ChatColor.GOLD + "Block removed from the elevator.");
                    break;
                case -2:
                    player.sendMessage(ChatColor.RED + "The material \"" + ChatColor.YELLOW + event.getClickedBlock().getType().toString().replace('_', ' ').toLowerCase() + ChatColor.RED + "\" cannot be used!");
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Internal API error: " + ret);
            }
        } else if (plugin.inputEdits.containsKey(pn)) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;
            Block block = event.getClickedBlock();
            LiftBlock tlb = new LiftBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), plugin.inputEdits.get(pn));
            Lift lift = plugin.lifts.get(plugin.editors.get(pn));
            event.setCancelled(true);
            if (lift.inputs.contains(tlb)) {
                player.sendMessage(ChatColor.RED + "This block has already been chosen as an input. Choose another block!");
                return;
            }
            lift.inputs.add(tlb);
            plugin.inputEdits.remove(pn);
            player.sendMessage(ChatColor.GREEN + "Input created!");
        } else if (plugin.inputRemoves.contains(pn)) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;
            Block block = event.getClickedBlock();
            LiftBlock tlb = new LiftBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), null);
            Lift lift = plugin.lifts.get(plugin.editors.get(pn));
            event.setCancelled(true);
            if (lift.inputs.contains(tlb)) {
                lift.inputs.remove(tlb);
                plugin.inputRemoves.remove(pn);
                player.sendMessage(ChatColor.GREEN + "Input removed!");
                return;
            }
            player.sendMessage(ChatColor.RED + "This block is not an input. Choose another block!");
        } else if (plugin.offlineEdits.contains(pn)) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;
            Block block = event.getClickedBlock();
            LiftBlock tlb = new LiftBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), null);
            Lift lift = plugin.lifts.get(plugin.editors.get(pn));
            event.setCancelled(true);
            if (lift.offlineInputs.contains(tlb)) {
                player.sendMessage(ChatColor.RED + "This block is already registered as an offline input. Choose another block!");
                return;
            }
            lift.offlineInputs.add(tlb);
            plugin.offlineEdits.remove(pn);
            player.sendMessage(ChatColor.GREEN + "Offline input created!");
        } else if (plugin.offlineRemoves.contains(pn)) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;
            Block block = event.getClickedBlock();
            LiftBlock tlb = new LiftBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), null);
            Lift lift = plugin.lifts.get(plugin.editors.get(pn));
            event.setCancelled(true);
            if (lift.offlineInputs.contains(tlb)) {
                lift.offlineInputs.remove(tlb);
                plugin.offlineRemoves.remove(pn);
                player.sendMessage(ChatColor.GREEN + "Offline input removed!");
                return;
            }
            player.sendMessage(ChatColor.RED + "This block is not an offline input. Choose another block!");
        } else if (plugin.builder.contains(pn)) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;
            event.setCancelled(true);
            int ret = plugin.api.switchBlockAtLift(plugin.editors.get(pn), event.getClickedBlock());
            switch (ret) {
                case 0:
                    player.sendMessage(ChatColor.GREEN + "Block added to the elevator.");
                    break;
                case 1:
                    player.sendMessage(ChatColor.GOLD + "Block removed from the elevator.");
                    break;
                case -2:
                    player.sendMessage(ChatColor.RED + "The material \"" + ChatColor.YELLOW + event.getClickedBlock().toString().replace('_', ' ').toLowerCase() + ChatColor.RED + "\" is not supported!");
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Internal API error: " + ret);
            }
        } else if (plugin.ropeEdits.containsKey(pn)) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;
            event.setCancelled(true);
            LiftBlock start = plugin.ropeEdits.get(pn);
            Block now = event.getClickedBlock();
            if (start == null) {
                player.sendMessage(ChatColor.GOLD + "Now click with your right mouse button on the end of the emergency stairs!");
                plugin.ropeEdits.put(pn, new LiftBlock(now.getWorld().getName(), now.getX(), now.getY(), now.getZ(), null));
            } else if (start.equals(new LiftBlock(now.getWorld().getName(), now.getX(), now.getY(), now.getZ(), null))) {
                plugin.ropeEdits.put(pn, null);
                player.sendMessage(ChatColor.GOLD + "Start removed!");
                player.sendMessage(ChatColor.GOLD + "Now click with your right mouse button on the end of the emergency stairs!");
            } else {
                if (start.x != now.getX() || start.z != now.getZ()) {
                    player.sendMessage(ChatColor.RED + "An emergency staircase can only go up!");
                    return;
                }
                int ret = plugin.api.addRope(plugin.editors.get(pn), plugin.getServer().getWorld(start.world), start.x, now.getY(), start.y, start.z, now.getWorld());
                switch (ret) {
                    case 0:
                        player.sendMessage(ChatColor.GREEN + "Emergency stage created.");
                        break;
                    case -2:
                        player.sendMessage(ChatColor.RED + "An emergency staircase may only be made of the same material.");
                        break;
                    case -3:
                        player.sendMessage(ChatColor.RED + "Part of the emergency staircase is already part of another emergency staircase.");
                        break;
                    case -4:
                        player.sendMessage(ChatColor.RED + "The emergency staircase is built of blacklisted blocks.");
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + "Internal API error: " + ret);
                }
                plugin.ropeEdits.remove(pn);
            }
        } else if (plugin.ropeRemoves.contains(pn)) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;
            event.setCancelled(true);
            Block block = event.getClickedBlock();
            if (plugin.api.forbidden.contains(block.getType())) {
                player.sendMessage(ChatColor.RED + block.getType().toString().replace('_', ' ').toLowerCase() + ChatColor.RED + "\" is niet gesupport!");
                return;
            }
            String lift = plugin.editors.get(pn);
            if (!plugin.api.containsRope(lift, block)) {
                player.sendMessage(ChatColor.RED + "This block is not part of the emergency staircase.");
                return;
            }
            plugin.api.removeRope(lift, block);
            plugin.ropeRemoves.remove(pn);
            player.sendMessage(ChatColor.GOLD + "Noodtrap verwijderd!");
        } else if (plugin.doorEdits.containsKey(pn)) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;
            Block block = event.getClickedBlock();
            event.setCancelled(true);
            if (plugin.api.forbidden.contains(block.getType())) {
                player.sendMessage(ChatColor.RED + block.getType().toString().replace('_', ' ').toLowerCase() + ChatColor.RED + "\" is currently not supported!");
                return;
            }
            @SuppressWarnings("deprecation")
			LiftBlock tlb = new LiftBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), block.getType(), block.getData());
            Lift lift = plugin.lifts.get(plugin.editors.get(pn));
            Floor floor = lift.floors.get(plugin.doorEdits.get(pn));
            if (floor.doorBlocks.contains(tlb)) {
                floor.doorBlocks.remove(tlb);
                player.sendMessage(ChatColor.GOLD + "Door removed!");
                return;
            }
            floor.doorBlocks.add(tlb);
            player.sendMessage(ChatColor.GREEN + "Door created!");
        } else if (plugin.whoisReq.contains(pn)) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;
            Block block = event.getClickedBlock();
            LiftBlock tlb = new LiftBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), null);
            Lift lift;
            event.setCancelled(true);
            plugin.whoisReq.remove(pn);
            for (Entry < String, Lift > e: plugin.lifts.entrySet()) {
                lift = e.getValue();
                if (lift.blocks.contains(tlb) || lift.inputs.contains(tlb) || lift.signs.contains(tlb) || lift.ropes.contains(tlb) || lift.offlineInputs.contains(tlb)) {
                    plugin.api.sendLiftInfo(player, e.getKey(), lift);
                    return;
                }
            }
            player.sendMessage(ChatColor.YELLOW + "This block is not part of the elevator.");
        } else {
            Action a = event.getAction();
            if (a != Action.RIGHT_CLICK_BLOCK &&
                a != Action.LEFT_CLICK_BLOCK)
                return;
            BlockState bs = event.getClickedBlock().getState();
            if (!(bs instanceof Sign))
                return;
            Sign sign = (Sign) bs;
            if (!sign.getLine(0).equalsIgnoreCase(plugin.signText))
                return;
            String lift = sign.getLine(1);
            if (!plugin.lifts.containsKey(lift))
                return;
            Lift l = plugin.lifts.get(lift);
            if (l.offline) {
                event.setCancelled(true);
                return;
            }
            if (l.defective) {
                if (sign.getLine(3).equals(plugin.defectiveText) && plugin.hasPerm(player, "v10lift.repair") && a == Action.RIGHT_CLICK_BLOCK) {
                    if (player.getGameMode() == GameMode.SURVIVAL && plugin.repairAmount > 0) {
                        ItemStack is = player.getInventory().getItemInMainHand();
                        int am = is.getAmount();
                        if (is.getType() != plugin.repairItem ||
                            am < plugin.repairAmount) {
                            player.sendMessage(ChatColor.RED + "You need: " + plugin.repairAmount + "x " + plugin.repairItem.toString().replace('_', ' ').toLowerCase());
                            return;
                        }
                        is.setAmount(am - plugin.repairAmount);
                        player.getInventory().setItemInMainHand(is);
                    }
                    plugin.api.setDefective(lift, false);
                }
                event.setCancelled(true);
                return;
            }

            if (!l.blocks.contains(new LiftBlock(sign.getWorld().getName(), sign.getX(), sign.getY(), sign.getZ(), null)))
                return;
            if (plugin.editors.containsValue(l))
                return;
            event.setCancelled(true);
            if (l.defective)
                return;
            String f = ChatColor.stripColor(sign.getLine(3));
            if (a == Action.RIGHT_CLICK_BLOCK) {
                Iterator < String > iter = l.floors.keySet().iterator();
                if (!l.floors.containsKey(f)) {
                    if (!iter.hasNext()) {
                        player.sendMessage(ChatColor.RED + "This elevator has no floors!");
                        return;
                    }
                    f = iter.next();
                }
                while (iter.hasNext()) {
                    if (iter.next().equals(f))
                        break;
                }
                if (!iter.hasNext())
                    iter = l.floors.keySet().iterator();
                String f2 = iter.next();
                Floor floor = l.floors.get(f2);
                if (l.y == floor.y)
                    sign.setLine(3, ChatColor.GREEN + f2);
                else if (!floor.whitelist.isEmpty() && !floor.whitelist.contains(pn.toLowerCase()) && !plugin.hasPerm(player, "v10lift.admin"))
                    sign.setLine(3, ChatColor.RED + f2);
                else
                    sign.setLine(3, ChatColor.YELLOW + f2);
                sign.update();
            } else {
                if (!l.floors.containsKey(f)) {
                    player.sendMessage(ChatColor.RED + "Floor not found!");
                    return;
                }
                Floor floor = l.floors.get(f);
                if (!floor.whitelist.isEmpty() && !floor.whitelist.contains(pn.toLowerCase()) && !plugin.hasPerm(player, "v10lift.admin")) {
                    player.sendMessage(ChatColor.RED + "You can't go to that floor!");
                    event.setCancelled(true);
                    return;
                }
                plugin.api.addToQueue(lift, l.floors.get(f), f);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() != DamageCause.SUFFOCATION)
            return;
        Entity e = event.getEntity();
        Location loc;
        if (e instanceof LivingEntity)
            loc = ((LivingEntity) e).getEyeLocation();
        else
            loc = e.getLocation();
        String world = loc.getWorld().getName();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        for (Lift lift: plugin.lifts.values()) {
            for (LiftBlock lb: lift.blocks) {
                if (world.equals(lb.world) &&
                    x == lb.x &&
                    y == lb.y &&
                    z == lb.z) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}