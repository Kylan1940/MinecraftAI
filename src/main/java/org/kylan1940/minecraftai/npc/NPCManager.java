package org.kylan1940.minecraftai.npc;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Villager;

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

    public void loadNPCs() {

        for (World world : Bukkit.getWorlds()) {

            for (Villager villager : world.getEntitiesByClass(Villager.class)) {

                if (!BukkitNPC.isNPC(villager)) {
                    continue;
                }

                BukkitNPC npc = BukkitNPC.fromVillager(villager);

                if (npc != null) {
                    register(npc);
                }
            }
        }
    }
}