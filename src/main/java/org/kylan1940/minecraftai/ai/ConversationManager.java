package org.kylan1940.minecraftai.ai;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConversationManager {

    private final Map<UUID, Conversation> conversations =
            new ConcurrentHashMap<>();

    public Conversation get(UUID playerId) {

        return conversations.computeIfAbsent(
                playerId,
                id -> new Conversation()
        );
    }

    public void clear(UUID playerId) {
        conversations.remove(playerId);
    }

    public void clearAll() {
        conversations.clear();
    }
}