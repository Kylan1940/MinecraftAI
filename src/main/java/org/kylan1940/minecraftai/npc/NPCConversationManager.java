package org.kylan1940.minecraftai.npc;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NPCConversationManager {

    private final Map<UUID, AINPC> conversations = new HashMap<>();

    public void start(Player player, AINPC npc) {
        conversations.put(player.getUniqueId(), npc);
    }

    public void end(Player player) {
        conversations.remove(player.getUniqueId());
    }

    public AINPC get(Player player) {
        return conversations.get(player.getUniqueId());
    }

    public boolean isConversing(Player player) {
        return conversations.containsKey(player.getUniqueId());
    }

    public void clear(Player player) {
        end(player);
    }
}