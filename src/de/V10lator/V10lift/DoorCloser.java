package de.V10lator.V10lift;

class DoorCloser implements Runnable {
    private final V10lift plugin;
    private final String lift;
    private int pid;

    DoorCloser(V10lift plugin, String lift) {
        this.plugin = plugin;
        this.lift = lift;
    }

    void setPid(int pid) {
        this.pid = pid;
    }

    void stop() {
        plugin.getServer().getScheduler().cancelTask(pid);
        if (plugin.lifts.containsKey(lift))
            plugin.lifts.get(lift).doorCloser = null;
    }

    public void run() {
        if (plugin.api.closeDoor(lift))
            stop();
    }
}