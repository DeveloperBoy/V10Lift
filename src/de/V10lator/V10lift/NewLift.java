package de.V10lator.V10lift;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TreeSet;


class NewLift implements Serializable {
    private static final long serialVersionUID = 1L;

    final HashSet < String > owners = null;
    HashSet < String > whitelist = null;
    final TreeSet < LiftBlock > blocks = null;
    int y;
    String world = null;
    final LinkedHashMap < String, Floor > floors = null;
    final HashSet < LiftSign > signs = null;
    final HashSet < LiftBlock > inputs = null;
    LinkedHashMap < String, Floor > queue = null;
    Floor doorOpen = null;
    final ArrayList < V10Entity > toMove = null;
    int counter;
    boolean defective;
    int speed;
    boolean realistic;
}