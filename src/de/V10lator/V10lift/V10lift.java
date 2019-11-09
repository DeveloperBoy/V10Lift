package de.V10lator.V10lift;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import de.V10lator.V10lift.API.ConverterUtil;
import de.V10lator.V10verlap.V10verlap;
import de.V10lator.V10verlap.V10verlap_API;

public class V10lift extends JavaPlugin {
    HashMap < String, Lift > lifts;
    final HashMap < String, HashSet < LiftBlock >> builds = new HashMap < String, HashSet < LiftBlock >> ();
    final HashMap < String, String > editors = new HashMap < String, String > ();
    final HashMap < String, String > inputEdits = new HashMap < String, String > ();
    final HashSet < String > inputRemoves = new HashSet < String > ();
    final HashSet < String > offlineEdits = new HashSet < String > ();
    final HashSet < String > offlineRemoves = new HashSet < String > ();
    final HashSet < String > builder = new HashSet < String > ();
    final HashMap < String, LiftBlock > ropeEdits = new HashMap < String, LiftBlock > ();
    final HashSet < String > ropeRemoves = new HashSet < String > ();
    final HashMap < String, String > doorEdits = new HashMap < String, String > ();
    final HashSet < String > whoisReq = new HashSet < String > ();
    HashMap < String, Integer > movingTasks = new HashMap < String, Integer > ();
    String signText;
    final String defectiveText = ChatColor.RED + "" + ChatColor.MAGIC + "Defect";
    double chanceOfDefect;
    Material repairItem;
    int repairAmount;
    Material masterItem;
    int masterAmount;
    int defaultSpeed;
    boolean defaultRealistic;
    final Random rand = new Random();
    private final AtomicBoolean saveLock = new AtomicBoolean(false);
    boolean dontSave = true;
    long doorCloseTime = 5 * 20;
    public static WorldEditPlugin worldedit;
    public static ConverterUtil cutil;
    //TODO: List of experimental features...
    /*
     * Since 0.2.2:
     * - Freeze lifts in unloaded chunks.
     * Since v0.4.13:
     * - Automatic door closing (DoorCloser.class)
     */
    V10lift_API api;

    V10verlap_API v10vAPI = null;

    @SuppressWarnings("unchecked")
    public void onEnable() {
        if (!setupPlugins()) {
            Bukkit.getLogger().severe(String.format("[%s] - Disabled due to no WorldGuard dependency found!", new Object[] {
                getDescription().getName()
            }));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Server server = getServer();
        PluginManager pm = server.getPluginManager();
        Logger log = server.getLogger();
        PluginDescriptionFile pdf = getDescription();
        File cfg = new File(getDataFolder(), "lifts.sav");
        api = new V10lift_API(this);
        try {
            if (!cfg.exists())
                lifts = new HashMap < String, Lift > ();
            else {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(cfg));
                Object o = in .readObject(); in .close();
                if (o == null || !(o instanceof Object[])) {
                    log.info("Can't read savefile!");
                    pm.disablePlugin(this);
                    return;
                }
                Object[] oa = (Object[]) o;
                int sv = (Integer) oa[0];
                if (sv < 6) {
                    log.info("Old savefile detected!");
                    if (sv < 2) {
                        log.info("Sorry, since v0.4.3 we don't support that old savefiles anymore.");
                        pm.disablePlugin(this);
                    }
                    if (sv < 5) {
                        log.info("Converting lifts and floors...");
                        HashMap < String, NewLift > ol = (HashMap < String, NewLift > ) oa[1];
                        lifts = new HashMap < String, Lift > ();
                        Iterator < String > fiter;
                        int ret;
                        ArrayList < String > toRename = new ArrayList < String > ();
                        for (Entry < String, NewLift > e: ol.entrySet()) {
                            NewLift l = e.getValue();
                            Lift nl = new Lift(l.owners, defaultSpeed, defaultRealistic);
                            for (LiftBlock lb: l.blocks)
                                nl.blocks.add(lb);
                            nl.y = l.y;
                            nl.world = l.world;
                            api.sortLiftBlocks(nl);
                            nl.whitelist = l.whitelist;
                            for (Entry < String, Floor > e2: l.floors.entrySet())
                                nl.floors.put(e2.getKey(), e2.getValue());
                            for (LiftSign s: l.signs)
                                nl.signs.add(s);
                            for (LiftBlock i: l.inputs)
                                nl.inputs.add(i);
                            nl.queue = l.queue;
                            nl.doorOpen = l.doorOpen;
                            if (sv >= 3)
                                nl.counter = l.counter;
                            if (sv >= 4) {
                                nl.speed = l.speed;
                                nl.realistic = l.realistic;
                            }
                            String ln = e.getKey();
                            lifts.put(ln, nl);
                            fiter = nl.floors.keySet().iterator();
                            while (fiter.hasNext()) {
                                String name = fiter.next();
                                if (name.length() > 13)
                                    toRename.add(name);
                            }
                            if (!toRename.isEmpty()) {
                                fiter = toRename.iterator();
                                while (fiter.hasNext()) {
                                    String name = fiter.next();
                                    ret = api.renameFloor(ln, name, name);
                                    if (ret < 0) {
                                        log.info("Can't rename \"" + name + "\": " + ret);
                                        pm.disablePlugin(this);
                                        return;
                                    }
                                }
                                toRename.clear();
                            }
                        }
                    }
                    if (sv < 6) {
                        log.info("Updating Lifts...");
                        lifts = (HashMap < String, Lift > ) oa[1];
                        for (Lift l: lifts.values())
                            if (l.offlineInputs == null)
                                l.offlineInputs = new HashSet < LiftBlock > ();
                    }
                } else
                    lifts = (HashMap < String, Lift > ) oa[1];
            }
        } catch (Exception e) {
            log.info("Can't read savefile!");
            e.printStackTrace();
            pm.disablePlugin(this);
            return;
        }
        load();
        saveConfig();

        V10verlap v10verlap = (V10verlap) pm.getPlugin("V10verlap");
        if (v10verlap != null) {
            v10vAPI = v10verlap.getAPI();
            double v10v = v10vAPI.getVersion();
            if (v10v < 1.2D) {
                log.info("V10verlap API outdated (>= 1.2 needed, " + v10v + " found!");
                log.info("Disabling V10verlap support");
                v10vAPI = null;
            } else if (v10v >= 2.0D) {
                log.info("V10verlap API to new (< 2.0 needed, " + v10v + " found!");
                log.info("Disabling V10verlap support");
                v10vAPI = null;
            }
        }
        
        cutil = new ConverterUtil(this);
        setupPlugins();

        pm.registerEvents(new VLL(this), this);
        getCommand("v10lift").setExecutor(new VLCE(this));
        BukkitScheduler bs = server.getScheduler();
        Lift lift;
        for (Entry < String, Lift > e: lifts.entrySet()) {
            lift = e.getValue();
            if (lift.queue != null) {
                if (lift.queue.isEmpty())
                    lift.queue = null;
                else
                    api.startLift(e.getKey());
            }
        }

        bs.scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                save(true);
            }
        }, 36000L, 36000L);
        dontSave = false;
        log.info("v" + pdf.getVersion() + " enabled!");
    }

    public void onDisable() {
        Server s = getServer();
        s.getScheduler().cancelTasks(this);
        save(false);
        s.getLogger().info("[" + getName() + "] disabled!");
    }

    void load() {
        Configuration config = getConfig();

        signText = config.getString("SignText", "[v10lift]");
        chanceOfDefect = config.getDouble("DefectRate", 0.0D);
        repairItem = Material.valueOf(config.getString("RepairItem", "REDSTONE"));
        repairAmount = config.getInt("RepairAmount", 5);
        masterItem = Material.valueOf(config.getString("MasterRepairItem", "DIAMOND"));
        masterAmount = config.getInt("MasterRepairAmount", 10);
        defaultSpeed = config.getInt("DefaultSpeed", 16);
        defaultRealistic = config.getBoolean("DefaultRealistic", true);
        doorCloseTime = config.getInt("DoorCloseTime", (int)(doorCloseTime / 20)) * 20;
        config.set("SignText", signText);
        config.set("DefectRate", chanceOfDefect);
        config.set("RepairItem", repairItem.name());
        config.set("RepairAmount", repairAmount);
        config.set("MasterRepairItem", masterItem.name());
        config.set("MasterRepairAmount", masterAmount);
        config.set("DefaultSpeed", defaultSpeed);
        config.set("DefaultRealistic", defaultRealistic);
        config.set("DoorCloseTime", doorCloseTime / 20);
    }

    void save(boolean async) {
        if (dontSave)
            return;
        Object[] tw = new Object[2];
        tw[0] = 6;
        tw[1] = lifts;
        while (!saveLock.compareAndSet(false, true))
            continue;
        try {
            File f = new File(getDataFolder(), "lifts.sav");
            if (!f.exists()) {
                getDataFolder().mkdirs();
                f.createNewFile();
            }
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
            out.writeObject(tw);
            AsyncSave as = new AsyncSave(out);
            if (async)
                getServer().getScheduler().scheduleSyncDelayedTask(this, as);
            else
                as.run();
        } catch (Exception e) {
            saveLock.set(false);
            getServer().getLogger().info("[" + getName() + "] can't write save file!");
            e.printStackTrace();
        }
    }

    boolean hasPerm(CommandSender cs, String perm) {
        if (cs.hasPermission(perm))
            return true;
        for (int i = perm.lastIndexOf("."); i > 1; i = perm.lastIndexOf(".")) {
            perm = perm.substring(0, i + 1) + "*";
            if (cs.hasPermission(perm))
                return true;
            perm = perm.substring(0, perm.length() - 2);
        }
        return cs.hasPermission("*");
    }

    private class AsyncSave implements Runnable {
        private final ObjectOutputStream out;

        private AsyncSave(ObjectOutputStream out) {
            this.out = out;
        }

        public void run() {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            saveLock.set(false);
        }
    }

    public V10lift_API getAPI() {
        return api;
    }

    private boolean setupPlugins() {
        if (hasWorldEditOnServer()) {
            worldedit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
            return true;
        }
        return false;
    }

    private static boolean hasWorldEditOnServer() {
        return Bukkit.getPluginManager().getPlugin("WorldEdit") != null;
    }
}