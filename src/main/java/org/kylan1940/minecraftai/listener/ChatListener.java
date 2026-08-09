package org.kylan1940.minecraftai.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.kylan1940.minecraftai.ai.AIService;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatListener implements Listener {

    private final AIService aiService;

    private final Set<UUID> enabledPlayers =
            new HashSet<>();

    public ChatListener(AIService aiService) {
        this.aiService = aiService;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {

        Player player = event.getPlayer();

        if (!enabledPlayers.contains(
                player.getUniqueId()
        )) {
            return;
        }

        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        aiService.ask(player, message);
    }

    public void enable(Player player) {

        enabledPlayers.add(player.getUniqueId());

    }

    public void disable(Player player) {

        enabledPlayers.remove(player.getUniqueId());

    }

    public boolean isEnabled(Player player) {

        return enabledPlayers.contains(player.getUniqueId());

    }
}