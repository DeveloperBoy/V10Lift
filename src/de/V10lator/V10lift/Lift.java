package de.V10lator.V10lift;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TreeSet;


/*
 * We're  re-using this class since v0.4.3.
 * This breaks compatibility for v0.1 cause there
 * this file is used, too. Then it got replaced by NewLift
 * and this one was only used to convert 0.1 lifts (savefile
 * compatibility).
 */
class Lift implements Serializable {
    private static final long serialVersionUID = 2L;

    final HashSet < String > owners;
    HashSet < String > whitelist;
    final TreeSet < LiftBlock > blocks = new TreeSet < LiftBlock > ();
    int y;
    String world;
    final LinkedHashMap < String, Floor > floors = new LinkedHashMap < String, Floor > ();
    final HashSet < LiftSign > signs = new HashSet < LiftSign > ();
    final HashSet < LiftBlock > inputs = new HashSet < LiftBlock > ();
    LinkedHashMap < String, Floor > queue = null;
    Floor doorOpen = null;
    final ArrayList < V10Entity > toMove = new ArrayList < V10Entity > ();
    int counter = 0;
    boolean defective = false;
    int speed = 16;
    boolean realistic = true;
    boolean offline = false;
    boolean sound = true;
    HashSet < LiftBlock > offlineInputs = new HashSet < LiftBlock > ();
    DoorCloser doorCloser = null;

    final HashSet < V10Rope > ropes = new HashSet < V10Rope > ();
    String signText = null;

    Lift(HashSet < String > owners, int speed, boolean realistic) {
        this.owners = owners;
        this.speed = speed;
        this.realistic = realistic;
    }

    Lift(String owner, int speed, boolean realistic) {
        HashSet < String > hs = new HashSet < String > ();
        hs.add(owner);
        owners = hs;
        this.speed = speed;
        this.realistic = realistic;
    }
}