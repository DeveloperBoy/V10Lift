package de.V10lator.V10lift;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import de.V10lator.V10lift.API.XMaterial;

public class V10lift_API {
    private final V10lift plugin;

    private final double version = 2.0D;
    final HashSet < Material > forbidden = new HashSet < Material > ();

	V10lift_API(V10lift plugin) {
        this.plugin = plugin;

        //TODO Add all types!
        forbidden.add(XMaterial.ACACIA_DOOR.parseMaterial());
        forbidden.add(XMaterial.BIRCH_DOOR.parseMaterial());
        forbidden.add(XMaterial.DARK_OAK_DOOR.parseMaterial());
        forbidden.add(XMaterial.IRON_DOOR.parseMaterial());
        forbidden.add(XMaterial.JUNGLE_DOOR.parseMaterial());
        forbidden.add(XMaterial.OAK_DOOR.parseMaterial());
        forbidden.add(XMaterial.SPRUCE_DOOR.parseMaterial());
        
        forbidden.add(XMaterial.BLACK_BED.parseMaterial());
        forbidden.add(XMaterial.BLUE_BED.parseMaterial());
        forbidden.add(XMaterial.BROWN_BED.parseMaterial());
        forbidden.add(XMaterial.CYAN_BED.parseMaterial());
        forbidden.add(XMaterial.GRAY_BED.parseMaterial());
        forbidden.add(XMaterial.GREEN_BED.parseMaterial());
        forbidden.add(XMaterial.LIGHT_BLUE_BED.parseMaterial());
        forbidden.add(XMaterial.LIGHT_GRAY_BED.parseMaterial());
        forbidden.add(XMaterial.LIME_BED.parseMaterial());
        forbidden.add(XMaterial.MAGENTA_BED.parseMaterial());
        forbidden.add(XMaterial.ORANGE_BED.parseMaterial());
        forbidden.add(XMaterial.PINK_BED.parseMaterial());
        forbidden.add(XMaterial.PURPLE_BED.parseMaterial());
        forbidden.add(XMaterial.RED_BED.parseMaterial());
        forbidden.add(XMaterial.WHITE_BED.parseMaterial());
        forbidden.add(XMaterial.YELLOW_BED.parseMaterial());
        
        forbidden.add(XMaterial.ACACIA_SAPLING.parseMaterial());
        forbidden.add(XMaterial.BAMBOO_SAPLING.parseMaterial());
        forbidden.add(XMaterial.BIRCH_SAPLING.parseMaterial());
        forbidden.add(XMaterial.DARK_OAK_SAPLING.parseMaterial());
        forbidden.add(XMaterial.JUNGLE_SAPLING.parseMaterial());
        forbidden.add(XMaterial.OAK_SAPLING.parseMaterial());
        forbidden.add(XMaterial.SPRUCE_SAPLING.parseMaterial());
        
        forbidden.add(XMaterial.TNT.parseMaterial());
        
        forbidden.add(XMaterial.PISTON.parseMaterial());
        forbidden.add(XMaterial.PISTON_HEAD.parseMaterial());
        forbidden.add(XMaterial.MOVING_PISTON.parseMaterial());
        forbidden.add(XMaterial.STICKY_PISTON.parseMaterial());
    }

    /**
     * Returns the versions number of the API.
     * The major number only changes if there's a breakage,
     * while the minor number changes with every bugfix/addon.
     * 
     * @return double
     */
    public double getVersion() {
        return version;
    }

    /**
     * Returns the lifts position at the Y axis.
     * 
     * @param lift - The lift to get the position from.
     * @return The Y coordinate.
     */
    public int getY(String lift) {
        if (lift == null || !plugin.lifts.containsKey(lift))
            return -1;
        return plugin.lifts.get(lift).y;
    }

    /**
     * Returns the world the lift is in.
     * 
     * @param lift - The lift to get the world from.
     * @return The world
     */
    public World getWorld(String lift) {
        if (lift == null || !plugin.lifts.containsKey(lift))
            return null;
        return plugin.getServer().getWorld(plugin.lifts.get(lift).world);
    }

    /**
     * Returns the queue of a lift. This queue is not editable!
     * 
     * @param lift + The lift to get the queue from.
     * @return A Map: The Key is the floors name while the Value is the floor itself.
     */
    public LinkedHashMap < String, Floor > getQueue(String lift) {
        if (lift != null && plugin.lifts.containsKey(lift)) {
            LinkedHashMap < String, Floor > floors = new LinkedHashMap < String, Floor > ();
            Lift l = plugin.lifts.get(lift);
            if (l.queue != null) {
                Floor f;
                try {
                    for (Entry < String, Floor > e: l.queue.entrySet()) {
                        f = e.getValue();
                        floors.put(e.getKey(), new Floor(f.y, f.world));
                    }
                } catch (Exception e) {
                    plugin.getServer().getLogger().info("[" + plugin.getName() + "] API Error: Can't convert queue for lift \"" + lift + "\"!");
                    e.printStackTrace();
                    return null;
                }
            }
            return floors;
        }
        return null;
    }

    /**
     * To rename a lift-
     * @param lift - The old name of the lift.
     * @param newName - The new name of the lift.
     */
    public void renameLift(String lift, String newName) {
        if (lift == null || newName == null || !plugin.lifts.containsKey(lift))
            return;
        Lift l = plugin.lifts.get(lift);
        plugin.lifts.remove(lift);
        plugin.lifts.put(newName, l);
        World w;
        BlockState bs;
        Sign si;
        Server se = plugin.getServer();
        for (LiftSign s: l.signs) {
            w = se.getWorld(s.world);
            if (w == null)
                continue;
            bs = w.getBlockAt(s.x, s.y, s.z).getState();
            if (!(bs instanceof Sign))
                continue;
            si = (Sign) bs;
            si.setLine(1, newName);
            si.update();
        }
    }

    /**
     * Sets the queue for a lift. This overrides previous queues.
     * 
     * @param lift - The lift to set the queue.
     * @param queue - The queue.
     * @return True if the queue was setted.
     */
    public boolean setQueue(String lift, LinkedHashMap < String, Floor > queue) {
        if (lift == null || queue == null || !plugin.lifts.containsKey(lift))
            return false;
        Lift l = plugin.lifts.get(lift);
        l.queue = new LinkedHashMap < String, Floor > ();
        for (Entry < String, Floor > e: queue.entrySet())
            addToQueue(lift, e.getValue(), e.getKey());
        return true;
    }

    /**
     * To add an entry to a lifts queue.
     * The entry will not be set if it's already presented in the queue.
     * For the floors name we will use the original name of the floor or,
     * if there is no floor, a (very xD) random sequence.
     * @param lift - The lift.
     * @param floor - The floor to add to the queue.
     * @return True if the entry was added.
     */
    public boolean addToQueue(String lift, int y, World world) {
        return addToQueue(lift, y, world, null);
    }

    /**
     * To add an entry to a lifts queue.
     * The entry will not be set if it's already presented in the queue.
     * For the floors name we will use floorName. floorName may be null.
     * @param lift - The lift.
     * @param floor - The floor to add to the queue.
     * @return True if the entry was added.
     */
    public boolean addToQueue(String lift, int y, World world, String floorName) {
        return addToQueue(lift, new Floor(y, world.getName()), floorName);
    }

    /**
     * To add an entry to a lifts queue.
     * The entry will not be set if it's already presented in the queue.
     * For the floors name we will use floorName. floorName may be null.
     * @param lift - The lift.
     * @param floor - The floor to add to the queue.
     * @return True if the entry was added.
     */
    public boolean addToQueue(String lift, de.V10lator.V10lift.API.Floor floor, String floorName) {
        if (floor == null)
            return false;
        return addToQueue(lift, new Floor(floor.getY(), floor.getWorldName()), floorName);
    }

    /**
     * To add an entry to a lifts queue.
     * The entry will not be set if it's already presented in the queue.
     * For the floors name we will use floorName. floorName may be null.
     * @param lift - The lift.
     * @param floor - The floor to add to the queue.
     * @return True if the entry was added.
     */
    boolean addToQueue(String lift, Floor floor, String floorName) {
        if (lift == null ||
            !plugin.lifts.containsKey(lift) ||
            floor == null) {
        	Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] That lift doesn't exists.");
            return false;
        }
        Lift l = plugin.lifts.get(lift);
        if (l.queue == null) 
            l.queue = new LinkedHashMap < String, Floor > ();
        if (!l.queue.containsValue(floor)) {
        	Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] The elevator doesn't navigate to that floor yet!");
            if (floorName == null) {
            	Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] The floor name is null!");
                floorName = ChatColor.MAGIC + "-----";
                for (Entry < String, Floor > e: plugin.lifts.get(lift).floors.entrySet())
                    if (e.getValue().equals(floor)) {
                        floorName = e.getKey();
                        floor = e.getValue();
                        break;
                    }
            }
            l.queue.put(floorName, floor);
            Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] The elevator has now received the queue!");
            startLift(lift);
            Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] The elevator is started!");
            return true;
        }
        return false;
    }

    /**
     * Add a new floor to a lift. This is not needed for addToQueue
     * @param lift - The lift to add a new floor to.
     * @param floorName - The name of the new floor.
     * @param floor - The floor.
     * @return 0 it the floor was added or -1 if the input is weird (null or lift not found, for example) or -2 if a floor with that name exists or -3 if a floor at that height exists.
     */
    public int addNewFloor(String lift, String floorName, de.V10lator.V10lift.API.Floor floor) {
        if (floor == null || floor.getWorld() == null)
            return -1;
        return addNewFloor(lift, floorName, new Floor(floor.getY(), floor.getWorldName()));
    }

    int addNewFloor(String lift, String floorName, Floor floor) {
        if (lift == null ||
            floorName == null ||
            !plugin.lifts.containsKey(lift) ||
            floor == null ||
            floor.y < 0)
            return -1;
        if (floor.y > plugin.getServer().getWorld(floor.world).getMaxHeight())
            return -1;
        if (floorName.length() > 13)
            floorName = floorName.substring(0, 13).trim();
        Lift li = plugin.lifts.get(lift);
        if (li.floors.containsKey(floorName))
            return -2;
        if (li.floors.containsValue(floor))
            return -3;
        li.floors.put(floorName, floor);
        sortFloors(li);
        return 0;
    }

    /**
     * Remove a floor from the lift.
     * This will remove all input and door blocks for that floor, too.
     * @param lift - The lift to remove the floor from.
     * @param floorName - The name of the floor to remove.
     * @return True if the floor was removed.
     */
    public boolean removeFloor(String lift, String floorName) {
        if (lift == null || floorName == null || !plugin.lifts.containsKey(lift))
            return false;
        Lift li = plugin.lifts.get(lift);
        if (!li.floors.containsKey(floorName))
            return false;
        li.floors.remove(floorName);
        Iterator < LiftBlock > iter = li.inputs.iterator();
        while (iter.hasNext())
            if (iter.next().floor.equals(floorName))
                iter.remove();
        return true;
    }

    /**
     * Rename a floor.
     * @param lift - The lift.
     * @param oldName - The floor name.
     * @param newName - The new floor name.
     * @return 0 if the floor got renamed or -1 if one of the parameters was null or -2 if the lift wasn't found or -3 if the floor wasn't found or -4 if a floor with the new name still exists.
     */
    @SuppressWarnings("unlikely-arg-type")
    public int renameFloor(String lift, String oldName, String newName) {
        if (lift == null || oldName == null || newName == null)
            return -1;
        if (!plugin.lifts.containsKey(lift))
            return -2;
        Lift li = plugin.lifts.get(lift);
        if (!li.floors.containsKey(oldName))
            return -3;
        if (newName.length() > 13)
            newName = newName.substring(0, 13).trim();
        if (li.floors.containsValue(newName))
            return -4;
        Floor f = li.floors.get(oldName);
        li.floors.remove(oldName);
        li.floors.put(newName, f);
        sortFloors(li);
        Iterator < LiftBlock > liter = li.inputs.iterator();
        LiftBlock lb;
        ArrayList < LiftBlock > newBlocks = new ArrayList < LiftBlock > ();
        while (liter.hasNext()) {
            lb = liter.next();
            if (lb.floor.equals(oldName)) {
                liter.remove();
                newBlocks.add(new LiftBlock(lb.world, lb.x, lb.y, lb.z, newName));
            }
        }
        for (LiftBlock nlb: newBlocks)
            li.inputs.add(nlb);
        return 0;
    }

    /**
     * Returns the whitelist of a given floor. This list is editable!
     * @param lift - The lift.
     * @param floor - The floor
     * @return A set with the player names.
     */
    public HashSet < String > getWhitelist(String lift, String floor) {
        HashSet < String > ret = null;
        if (lift != null && plugin.lifts.containsKey(lift) && floor != null) {
            Lift li = plugin.lifts.get(lift);
            if (li.floors.containsKey(floor))
                ret = li.floors.get(floor).whitelist;
        }
        return ret;
    }

    /**
     * Returns true if the given lift is defective.
     * @param lift - The lift.
     * @return True if the lift is defective.
     */
    public boolean isDefective(String lift) {
        if (lift == null || !plugin.lifts.containsKey(lift))
            return true;
        return plugin.lifts.get(lift).defective;
    }

    /**
     * Sets the given lift defective or repairs it, depending on the given state.
     * @param lift - The lift.
     * @param state - true to set the lift defective, false to repair it.
     */
    public void setDefective(String lift, boolean state) {
        if (lift != null && plugin.lifts.containsKey(lift)) {
            Lift l = plugin.lifts.get(lift);
            boolean oldState = l.defective;
            if (oldState == state)
                return;
            l.defective = state;
            Iterator < LiftSign > liter = l.signs.iterator();
            Server server = plugin.getServer();
            if (state) {
                int max = l.signs.size();
                if (max == 0)
                    return;
                LiftSign ls;
                if (max == 1)
                    ls = liter.next();
                else {
                    int r = plugin.rand.nextInt(max);
                    for (int i = 0; i < r; i++)
                        liter.next();
                    ls = liter.next();
                }
                BlockState bs = server.getWorld(ls.world).getBlockAt(ls.x, ls.y, ls.z).getState();
                if (!(bs instanceof Sign)) {
                    server.getLogger().info("[" + plugin.getName() + "] Wrong sign removed at: " + ls.x + ", " + ls.y + ", " + ls.z + " in world " + ls.world);
                    liter.remove();
                    return;
                }
                Sign s = (Sign) bs;
                ls.oldText = s.getLine(3);
                s.setLine(3, plugin.defectiveText);
                s.update();

                for (LiftBlock lb: l.blocks) {
                    bs = server.getWorld(lb.world).getBlockAt(lb.x, lb.y, lb.z).getState();
                    if (!(bs instanceof Sign))
                        continue;
                    s = (Sign) bs;
                    l.signText = s.getLine(3);
                    s.setLine(3, ChatColor.MAGIC + "Defect!");
                    s.update();
                }
            } else {
                LiftSign ls;
                BlockState bs;
                Sign s;
                while (liter.hasNext()) {
                    ls = liter.next();
                    bs = plugin.getServer().getWorld(ls.world).getBlockAt(ls.x, ls.y, ls.z).getState();
                    if (!(bs instanceof Sign)) {
                        plugin.getServer().getLogger().info("[" + plugin.getName() + "] Wrong sign removed at: " + ls.x + ", " + ls.y + ", " + ls.z + " in world " + ls.world);
                        liter.remove();
                        continue;
                    }
                    s = (Sign) bs;
                    if (s.getLine(3).equals(plugin.defectiveText)) {
                        s.setLine(3, ls.oldText);
                        s.update();
                        ls.oldText = null;
                        for (LiftBlock lb: l.blocks) {
                            bs = server.getWorld(lb.world).getBlockAt(lb.x, lb.y, lb.z).getState();
                            if (!(bs instanceof Sign))
                                continue;
                            s = (Sign) bs;
                            s.setLine(3, l.signText);
                            s.update();
                            l.signText = null;
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Sets the given lift defective or repairs it, depending on the given state.
     * @param lift - The lift.
     * @param state - true to set the lift defective, false to repair it.
     */
    public void setOffline(String lift, boolean state) {
        if (lift != null && plugin.lifts.containsKey(lift)) {
            Lift l = plugin.lifts.get(lift);
            if (l.offline == state)
                return;
            l.offline = state;
            Iterator < LiftSign > liter = l.signs.iterator();
            Server s = plugin.getServer();
            BlockState bs;
            Sign sign;
            if (state) {
                for (LiftBlock lb: l.blocks) {
                    bs = s.getWorld(lb.world).getBlockAt(lb.x, lb.y, lb.z).getState();
                    if (!(bs instanceof Sign))
                        continue;
                    sign = (Sign) bs;
                    if (!sign.getLine(0).equalsIgnoreCase(plugin.signText))
                        continue;
                    sign.setLine(3, ChatColor.RED + "Disabled");
                    sign.update();
                }
                while (liter.hasNext()) {
                    LiftSign ls = liter.next();
                    bs = s.getWorld(ls.world).getBlockAt(ls.x, ls.y, ls.z).getState();
                    if (!(bs instanceof Sign)) {
                        s.getLogger().info("[" + plugin.getName() + "] Wrong sign removed at: " + ls.x + ", " + ls.y + ", " + ls.z + " in world " + ls.world);
                        liter.remove();
                        continue;
                    }
                    sign = (Sign) bs;
                    ls.oldText = sign.getLine(3);
                    sign.setLine(3, ChatColor.RED + "Disabled");
                    sign.update();
                }
            } else {
                for (LiftBlock lb: l.blocks) {
                    bs = s.getWorld(lb.world).getBlockAt(lb.x, lb.y, lb.z).getState();
                    if (!(bs instanceof Sign))
                        continue;
                    sign = (Sign) bs;
                    if (!sign.getLine(0).equalsIgnoreCase(plugin.signText))
                        continue;
                    sign.setLine(3, "");
                    sign.update();
                }
                while (liter.hasNext()) {
                    LiftSign ls = liter.next();
                    bs = s.getWorld(ls.world).getBlockAt(ls.x, ls.y, ls.z).getState();
                    if (!(bs instanceof Sign)) {
                        s.getLogger().info("[" + plugin.getName() + "] Wrong sign removed on: " + ls.x + ", " + ls.y + ", " + ls.z + " in world " + ls.world);
                        liter.remove();
                        continue;
                    }
                    sign = (Sign) bs;
                    sign.setLine(3, ls.oldText);
                    sign.update();
                    ls.oldText = null;
                }
            }
        }
    }

    /**
     * Creates a new lift.
     * Use addBlockToLift(String lift, Block block) after!
     * @param player - The name of the lift owner.
     * @param lift - The name of the new lift.
     * @return true if the lift was created or false if there was an error.
     */
    public boolean createNewLift(String player, String lift) {
        if (player == null || lift == null || plugin.lifts.containsKey(lift))
            return false;
        plugin.lifts.put(lift, new Lift(player, plugin.defaultSpeed, plugin.defaultRealistic));
        return true;
    }

    /**
     * Adds a block to a lift.
     * Use sortLiftBlocks(String lift) after!
     * @param lift - The lift.
     * @param block - The Block to add.
     * @return 0 if the block was added successfully or -1 if the lift doesn't exist or the block is null or -2 if the material is forbidden or -3 if the block is still a block of the lift.
     */
    public int addBlockToLift(String lift, Block block) {
        if (lift == null || block == null)
            return -1;
        return addBlockToLift(plugin.lifts.get(lift).blocks, block);
    }

    @SuppressWarnings("deprecation")
	int addBlockToLift(Set < LiftBlock > blocks, Block block) {
        Material type = block.getType();
        LiftBlock tlb;
        if (type.toString().contains("SIGN"))
            tlb = new LiftBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), type, block.getData(), ((Sign) block.getState()).getLines());
        else
            tlb = new LiftBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), type, block.getData());
        return addBlockToLift(blocks, tlb);
    }

    int addBlockToLift(Set < LiftBlock > blocks, LiftBlock block) {
        if (forbidden.contains(block.type))
            return -2;
        if (blocks.contains(block))
            return -3;
        blocks.add(block);
        return 0;
    }

    /**
     * Removes a block from a lift.
     * Use sortLiftBlocks(String lift) after!
     * @param lift - The lift.
     * @param block - The Block to remove.
     * @return 0 if the block was removed successfully or -1 if the lift doesn't exist or the block is null or -2 if the block is not a block of the lift.
     */
    public int removeBlockFromLift(String lift, Block block) {
        return removeBlockFromLift(plugin.lifts.get(lift).blocks, block);
    }

    @SuppressWarnings("deprecation")
	int removeBlockFromLift(Set < LiftBlock > blocks, Block block) {
        if (blocks == null || block == null)
            return -1;
        Material type = block.getType();
        LiftBlock tlb;
        if (type.toString().contains("SIGN"))
            tlb = new LiftBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), type, block.getData(), ((Sign) block.getState()).getLines());
        else
            tlb = new LiftBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), type, block.getData());
        if (!blocks.contains(tlb))
            return -2;
        blocks.remove(tlb);
        return 0;
    }

    /**
     * Adds or removes a block from a lift.
     * Use sortLiftBlocks(String lift) after!
     * @param lift - The lift.
     * @param block - The Block to add or remove.
     * @return 0 if the block was added successfully or 1 if the block was removed successfully or -1 if the lift doesn't exist or the block is null or -2 if the material is forbidden.
     */
    public int switchBlockAtLift(String lift, Block block) {
        if (lift == null || !plugin.lifts.containsKey(lift))
            return -1;
        return switchBlockAtLift(plugin.lifts.get(lift).blocks, block);
    }

    @SuppressWarnings("deprecation")
	int switchBlockAtLift(Set < LiftBlock > blocks, Block block) {
        if (blocks == null || block == null)
            return -1;
        Material type = block.getType();
        if (forbidden.contains(type))
            return -2;
        LiftBlock tlb;
        if (type.toString().contains("SIGN"))
            tlb = new LiftBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), type, block.getData(), ((Sign) block.getState()).getLines());
        else
            tlb = new LiftBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), type, block.getData());
        if (blocks.contains(tlb)) {
            blocks.remove(tlb);
            return 1;
        }
        blocks.add(tlb);
        return 0;
    }

    /**
     * Sorts the blocks of a lift.
     * Use this after they have been modified.
     * @param lift - The lift.
     */
    public void sortLiftBlocks(String lift) {
        if (lift != null && plugin.lifts.containsKey(lift))
            sortLiftBlocks(plugin.lifts.get(lift));
    }

    void sortLiftBlocks(Lift lift) {
        if (lift.world == null)
            lift.world = lift.blocks.first().world;
        World world = plugin.getServer().getWorld(lift.world);
        if (world == null)
            return;
        lift.y = world.getMaxHeight();
        for (LiftBlock lb: lift.blocks)
            if (lb.y < lift.y) {
                lift.y = lb.y;
                lift.world = lb.world;
            }
    }

    /**
     * Adds a rope to a lift.
     * @param lift - The lift.
     * @param world - The World the rope is in.
     * @param x - The coordinate of the rope at the X axis.
     * @param minY - The lowest rope block at the Y axis.
     * @param maxY - The highest rope block at the Y axis.
     * @param z - The coordinate of the rope at the Z axis.
     * @return 0 if the rope was added successfully or -1 if the lift doesn't exist or a parameter is null or -2 if the rope isn't made of one material only or -3 if a part of the rope is still registred as a rope or -4 if the rope material is blacklisted.
     */
    public int addRope(String lift, World world, int x, int minY, int maxY, int z) {
        return addRope(lift, world, x, minY, maxY, z, world);
    }

    /**
     * Adds a rope to a lift.
     * @param lift - The lift.
     * @param startWorld - The World the upper end of the rope is in.
     * @param x - The coordinate of the rope at the X axis.
     * @param minY - The lowest rope block at the Y axis.
     * @param maxY - The highest rope block at the Y axis.
     * @param z - The coordinate of the rope at the Z axis.
     * @param endWorld - The World the lower end of the rope is in.
     * @return 0 if the rope was added successfully or -1 if the lift doesn't exist or a parameter is null or -2 if the rope isn't made of one material only or -3 if a part of the rope is still registred as a rope or -4 if the rope material is blacklisted or -5 if a multiworld rope was requested but there's a V10verlap error.
     */
    public int addRope(String lift, World startWorld, int x, int minY, int maxY, int z, World endWorld) {
        if (lift == null || !plugin.lifts.containsKey(lift) || startWorld == null || endWorld == null)
            return -1;
        boolean change;
        boolean v10verlap;

        if (startWorld.equals(endWorld)) {
            change = minY > maxY;
            v10verlap = false;
        } else {
            if (plugin.v10vAPI == null)
                return -5;
            change = true;
            for (World world = startWorld; world != null; world = plugin.v10vAPI.getLowerWorld(world)) {
                if (world.equals(endWorld)) {
                    change = false;
                    break;
                }
            }
            if (change) {
                change = false;
                for (World world = startWorld; world != null; world = plugin.v10vAPI.getUpperWorld(world)) {
                    if (world.equals(endWorld)) {
                        change = true;
                        break;
                    }
                }
                if (!change)
                    return -5;
            }
            v10verlap = true;
        }
        if (change) {
            int ty = minY;
            World tw = endWorld;
            minY = maxY;
            endWorld = startWorld;
            maxY = ty;
            startWorld = tw;
        }
        Block block = startWorld.getBlockAt(x, minY, z);
        if (isRope(block))
            return -3;
        Material m = block.getType();
        if (forbidden.contains(m))
            return -4;
        if (!v10verlap) {
            for (int i = minY + 1; i <= maxY; i++) {
                block = startWorld.getBlockAt(x, i, z);
                if (isRope(block))
                    return -3;
                if (block.getType() != m)
                    return -2;
            }
        } else {
            for (int i = plugin.v10vAPI.getMinY(startWorld); i <= maxY; i++) {
                block = startWorld.getBlockAt(x, i, z);
                if (isRope(block))
                    return -3;
                if (block.getType() != m)
                    return -2;
            }
            int my;
            change = false;
            for (World world = plugin.v10vAPI.getLowerWorld(startWorld); !change; world = plugin.v10vAPI.getLowerWorld(world)) {
                if (world.equals(endWorld)) {
                    my = minY;
                    change = true;
                } else
                    my = plugin.v10vAPI.getMinY(world);
                for (; my <= plugin.v10vAPI.getMaxY(world); my++) {
                    block = startWorld.getBlockAt(x, my, z);
                    if (isRope(block))
                        return -3;
                    if (block.getType() != m)
                        return -2;
                }
            }
        }
        plugin.lifts.get(lift).ropes.add(new V10Rope(m, startWorld.getName(), endWorld.getName(), x, minY, maxY, z));
        return 0;
    }

    /**
     * Removes a rope from a lift.
     * @param lift - The lift.
     * @param block - a piece of the rope.
     * @return True if the rope was removed.
     */
    public boolean removeRope(String lift, Block block) {
        if (lift == null || block == null || !plugin.lifts.containsKey(lift) || !containsRope(lift, block))
            return false;
        String world = block.getWorld().getName();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        V10Rope rope;
        Iterator < V10Rope > riter = plugin.lifts.get(lift).ropes.iterator();
        HashSet < String > worlds = new HashSet < String > ();
        while (riter.hasNext()) {
            rope = riter.next();
            if (x != rope.x ||
                z != rope.z)
                continue;
            if (rope.startWorld.equals(rope.endWorld)) {
                if (world.equals(rope.startWorld))
                    if (y >= rope.minY && y <= rope.maxY) {
                        riter.remove();
                        return true;
                    }
                continue;
            }
            if (plugin.v10vAPI == null) {
                plugin.getLogger().info("[" + plugin.getName() + "] Multiworld lift found, but V10verlap is not installed!");
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                return false;
            }

            worlds.add(rope.startWorld);
            World tw;
            while ((tw = plugin.v10vAPI.getLowerWorld(world)) != null) {
                worlds.add(tw.getName());
                if (worlds.contains(rope.endWorld))
                    break;
            }
            if (worlds.contains(world)) {
                if (world.equals(rope.startWorld)) {
                    if (y <= rope.maxY) {
                        riter.remove();
                        return true;
                    }
                } else if (world.equals(rope.endWorld)) {
                    if (y >= rope.minY && y <= rope.maxY) {
                        riter.remove();
                        return true;
                    }
                } else {
                    worlds.remove(rope.startWorld);
                    worlds.remove(rope.endWorld);
                    if (worlds.contains(world)) {
                        riter.remove();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Shows if a block is part of a rope.
     * @param lift - The lift to check.
     * @param block - a piece of the rope.
     * @return True if the block is a part of the rope at the lift.
     */
    public boolean containsRope(String lift, Block block) {
        if (lift == null || block == null || !plugin.lifts.containsKey(lift))
            return false;
        Lift l = plugin.lifts.get(lift);
        if (l.ropes.isEmpty())
            return false;
        String world = block.getWorld().getName();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        HashSet < String > worlds = new HashSet < String > ();
        for (V10Rope rope: l.ropes) {
            if (x != rope.x ||
                z != rope.z)
                continue;
            if (rope.startWorld.equals(rope.endWorld)) {
                if (world.equals(rope.startWorld))
                    if (y >= rope.minY && y <= rope.maxY)
                        return true;
                continue;
            }
            if (plugin.v10vAPI == null) {
                plugin.getLogger().info("[" + plugin.getName() + "] Multiworld lift found, but V10verlap is not installed!");
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                return false;
            }
            worlds.add(rope.startWorld);
            World tw;
            while ((tw = plugin.v10vAPI.getLowerWorld(world)) != null) {
                worlds.add(tw.getName());
                if (worlds.contains(rope.endWorld))
                    break;
            }
            if (worlds.contains(world)) {
                if (world.equals(rope.startWorld)) {
                    if (y <= rope.maxY)
                        return true;
                } else if (world.equals(rope.endWorld)) {
                    if (y >= rope.minY && y <= rope.maxY)
                        return true;
                } else {
                    worlds.remove(rope.startWorld);
                    worlds.remove(rope.endWorld);
                    if (worlds.contains(world))
                        return true;
                }
            }
            worlds.clear();
        }
        return false;
    }

    /**
     * Shows if a block is part of a rope.
     * @param block - a piece of the rope.
     * @return True if the block is a part of the rope of any lift.
     */
    public boolean isRope(Block block) {
        for (String lift: plugin.lifts.keySet())
            if (containsRope(lift, block))
                return true;
        return false;
    }

    /**
     * To completely remove a lift. Cannot be undone!
     * @param lift - The lift.
     * @return True if the lift was removed successfully.s
     */
    public boolean removeLift(String lift) {
        if (!plugin.lifts.containsKey(lift))
            return false;
        Iterator < Entry < String, String >> iter = plugin.editors.entrySet().iterator();
        Entry < String, String > entry;
        HashSet < String > activeEdits = new HashSet < String > ();
        while (iter.hasNext()) {
            entry = iter.next();
            if (entry.getValue().equals(lift)) {
                activeEdits.add(entry.getKey());
                iter.remove();
            }
        }
        for (String pn: activeEdits) {
            plugin.inputEdits.remove(pn);
            plugin.inputRemoves.remove(pn);
            plugin.offlineEdits.remove(pn);
            plugin.offlineRemoves.remove(pn);
            plugin.builder.remove(pn);
            plugin.ropeEdits.remove(pn);
            plugin.ropeRemoves.remove(pn);
            plugin.doorEdits.remove(pn);
        }
        if (plugin.movingTasks.containsKey(lift)) {
            plugin.getServer().getScheduler().cancelTask(plugin.movingTasks.get(lift));
            plugin.movingTasks.remove(lift);
        }
        plugin.lifts.remove(lift);
        return true;
    }


    private void sortFloors(Lift lift) {
        ArrayList < Entry < String, Floor >> as = new ArrayList < Entry < String, Floor >> (lift.floors.entrySet());
        Collections.sort(as, new Comparator < Entry < String, Floor >> () {
            public int compare(Entry < String, Floor > e1, Entry < String, Floor > e2) {
                if (plugin.v10vAPI == null)
                    return ((Integer) e1.getValue().y).compareTo(((Integer) e2.getValue().y));
                String world = e1.getValue().world;
                String cworld = e2.getValue().world;
                if (world.equals(cworld))
                    return ((Integer) e1.getValue().y).compareTo(((Integer) e2.getValue().y));
                World tw;
                int c = 0;
                while (true) {
                    tw = plugin.v10vAPI.getUpperWorld(world);
                    if (tw == null)
                        break;
                    c++;
                    world = tw.getName();
                    if (world.equals(cworld))
                        return c;
                }
                c = 0;
                while (true) {
                    tw = plugin.v10vAPI.getLowerWorld(world);
                    if (tw == null)
                        break;
                    c--;
                    world = tw.getName();
                    if (world.equals(cworld))
                        return c;
                }
                return 0;
            }
        });
        Iterator < Entry < String, Floor >> iter = as.iterator();
        lift.floors.clear();
        Entry < String, Floor > e;
        while (iter.hasNext()) {
            e = iter.next();
            lift.floors.put(e.getKey(), e.getValue());
        }
    }

    void startLift(String lift) {
        if (!plugin.movingTasks.containsKey(lift)) {
        	Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] The elevator doesn't contain that movingtask!");
            Lift l = plugin.lifts.get(lift);
            plugin.movingTasks.put(lift,
                plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,
                    new MoveLift(plugin, lift, l.speed), l.speed, l.speed));
            Bukkit.getLogger().severe("[" + plugin.getName() + " Debug] The lift has also started successfully!");
        }
    }

    void sendLiftInfo(Player player, String l) {
        sendLiftInfo(player, l, plugin.lifts.get(l));
    }

    void sendLiftInfo(Player player, String l, Lift lift) {
        if (!lift.owners.contains(player.getName()) && !plugin.hasPerm(player, "v10lift.admin"))
            player.sendMessage(ChatColor.RED + "You can't check this elevator!");
        else {
            player.sendMessage(ChatColor.GOLD + "Elevator: " + ChatColor.YELLOW + l);
            player.sendMessage(ChatColor.GOLD + "Settings:");
            player.sendMessage(ChatColor.GREEN + "  Speed: " + ChatColor.YELLOW + lift.speed);
            player.sendMessage(ChatColor.GREEN + "  Realistic Mode: " + ChatColor.YELLOW + lift.realistic);
            player.sendMessage(ChatColor.GREEN + "  Malfunction: " + ChatColor.YELLOW + plugin.api.isDefective(l));
            player.sendMessage(ChatColor.GOLD + "Floors:");
            if (lift.floors.isEmpty())
                player.sendMessage(ChatColor.RED + "None.");
            else {
                for (Entry < String, Floor > entry: lift.floors.entrySet()) {
                    player.sendMessage(ChatColor.GREEN + "  " + entry.getKey() + ":");
                    Floor f = entry.getValue();
                    player.sendMessage(ChatColor.YELLOW + "    World: " + ChatColor.GREEN + f.world);
                    player.sendMessage(ChatColor.YELLOW + "    Height: " + ChatColor.GREEN + f.y);
                    player.sendMessage(ChatColor.YELLOW + "    Whitelist:");
                    if (f.whitelist.isEmpty())
                        player.sendMessage(ChatColor.GOLD + "      None.");
                    else {
                        ChatColor color = ChatColor.DARK_PURPLE;
                        Iterator < String > iter = f.whitelist.iterator();
                        StringBuilder sb = new StringBuilder();
                        sb.append("      ").append(color).append(iter.next());
                        while (iter.hasNext()) {
                            if (color == ChatColor.DARK_PURPLE)
                                color = ChatColor.LIGHT_PURPLE;
                            else
                                color = ChatColor.DARK_PURPLE;
                            sb.append(ChatColor.AQUA).append(", ").append(color).append(iter.next());
                        }
                        player.sendMessage(sb.toString());
                    }
                }
            }
        }
    }

    /**
     * To check if a lift exists
     * @param lift - The lift.
     * @return True if the exists, false otherwise.
     */
    public boolean isLift(String lift) {
        return plugin.lifts.containsKey(lift);
    }

    /**
     * To close a lifts door.
     * @param lift - The lift.
     * @return true if the door was closed successfully, false otherwise.
     */
    public boolean closeDoor(String lift) {
        return (lift == null || !plugin.lifts.containsKey(lift)) ? false : closeDoor(plugin.lifts.get(lift));
    }

	boolean closeDoor(Lift lift) {
        boolean blocked = false;
        Server s = plugin.getServer();
        Block block;
        Location loc;
        if (lift.doorOpen == null)
            return true;
        if (lift.realistic) {
            for (LiftBlock db: lift.doorOpen.doorBlocks) {
                block = s.getWorld(db.world).getBlockAt(db.x, db.y, db.z);
                for (Entity ent: block.getChunk().getEntities()) {
                    loc = ent.getLocation();
                    if (loc.getBlockX() == db.x &&
                        loc.getBlockY() == db.y &&
                        loc.getBlockZ() == db.z) {
                        blocked = true;
                        break;
                    }
                }
                if (blocked)
                    break;
            }
        }
        if (!blocked) {
            for (LiftBlock db: lift.doorOpen.doorBlocks) {
                block = s.getWorld(db.world).getBlockAt(db.x, db.y, db.z);
                block.setType(db.type);
            }
            lift.doorOpen = null;
            if (lift.doorCloser != null)
                lift.doorCloser.stop();
        }
        return !blocked;
    }

    /**
     * To check if a lift has a open door.
     * @param lift - The lift.
     * @return True if the door is open, false otherwise.
     */
    public boolean hasDoorOpen(String lift) {
        return (lift == null || !plugin.lifts.containsKey(lift)) ? false : plugin.lifts.get(lift).doorOpen != null;
    }

    /**
     * To open a lifts door.
     * @param lift - The lift.
     * @return True if the door was opened successfully, false otherwise.
     */
    public boolean openDoor(String lift) {
        if (lift == null || !plugin.lifts.containsKey(lift))
            return false;
        Lift l = plugin.lifts.get(lift);
        if (l.queue != null)
            return false;
        Floor f = null;
        for (Floor fl: l.floors.values())
            if (fl.y == l.y && fl.world.equals(l.world)) {
                f = fl;
                break;
            }
        return f == null ? false : openDoor(plugin.lifts.get(lift), lift, f);
    }

    boolean openDoor(Lift lift, String ln, Floor floor) {
        if (lift.doorOpen != null && !closeDoor(lift))
            return false;

        Server s = plugin.getServer();
        for (LiftBlock db: floor.doorBlocks)
            s.getWorld(db.world).getBlockAt(db.x, db.y, db.z).setType(Material.AIR);
        lift.doorOpen = floor;
        if (lift.realistic) {
            lift.doorCloser = new DoorCloser(plugin, ln);
            lift.doorCloser.setPid(plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, lift.doorCloser, plugin.doorCloseTime, plugin.doorCloseTime));
        }
        return true;
    }
}