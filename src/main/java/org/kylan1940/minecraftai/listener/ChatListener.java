package org.kylan1940.minecraftai.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.kylan1940.minecraftai.MinecraftAI;
import org.kylan1940.minecraftai.ai.AIManager;
import org.kylan1940.minecraftai.ai.AIService;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ChatListener implements Listener {

    private final MinecraftAI plugin;
    private final AIManager aiManager;
    private final AIService aiService;

    public ChatListener(
            MinecraftAI plugin,
            AIManager aiManager,
            AIService aiService
    ) {
        this.plugin = plugin;
        this.aiManager = aiManager;
        this.aiService = aiService;
    }

    @EventHandler()
    public void onChat(AsyncChatEvent event) {

        if (!aiManager.isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        String mode = aiManager.getMode();

        switch (mode) {

            case "public" -> {
                aiService.ask(player, message);
            }

            case "mention" -> {
                String aiName = plugin.getConfig().getString("name", "James");

                if (!message.toLowerCase().contains(aiName.toLowerCase())) {
                    return;
                }
                aiService.ask(player, message);

            }

            case "private" -> {

                if (!aiManager.isPrivatePlayer(player)) {
                    return;
                }

                aiService.ask(player, message);
            }
        }
    }
}