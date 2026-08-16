package org.kylan1940.minecraftai.npc;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NPCManager {

    private final Map<String, AINPC> npcs = new HashMap<>();

    public void register(AINPC npc) {
        npcs.put(npc.getName().toLowerCase(), npc);
    }

    public AINPC get(String name) {
        return npcs.get(name.toLowerCase());
    }

    public boolean exists(String name) {
        return npcs.containsKey(name.toLowerCase());
    }

    public void remove(String name) {
        npcs.remove(name.toLowerCase());
    }

    public Collection<AINPC> getNPCs() {
        return Collections.unmodifiableCollection(npcs.values());
    }
}