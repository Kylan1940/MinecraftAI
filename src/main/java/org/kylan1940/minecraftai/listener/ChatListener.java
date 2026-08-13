package org.kylan1940.minecraftai.listener;

import java.util.regex.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.kylan1940.minecraftai.MinecraftAI;
import org.kylan1940.minecraftai.ai.AIManager;
import org.kylan1940.minecraftai.ai.AIService;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.kylan1940.minecraftai.ai.CooldownManager;
import org.kylan1940.minecraftai.message.MessageUtil;

public class ChatListener implements Listener {

    private final MinecraftAI plugin;
    private final AIManager aiManager;
    private final AIService aiService;
    private final CooldownManager cooldownManager;

    public ChatListener(
            MinecraftAI plugin,
            AIManager aiManager,
            AIService aiService
    ) {
        this.plugin = plugin;
        this.aiManager = aiManager;
        this.aiService = aiService;
        this.cooldownManager = new CooldownManager(plugin);
    }

    @EventHandler()
    public void onChat(AsyncChatEvent event) {

        if (!aiManager.isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        String mode = aiManager.getMode();

        if (mode.equals("mention")) {
            String aiName = plugin.getConfig().getString("name", "James");
            if (!message.toLowerCase().contains(aiName.toLowerCase())) {
                return;
            }
            message = message.replaceAll(
                    "(?i)\\b"
                            + Pattern.quote(aiName)
                            + "\\b",
                    ""
            ).trim();
            if (message.isEmpty()) {
                return;
            }
        }

        if (mode.equals("private") && !aiManager.isPrivatePlayer(player)) {
            return;
        }

        if (!mode.equals("public") && !mode.equals("mention") && !mode.equals("private")) {
            return;
        }

        if (cooldownManager.isOnCooldown(player)) {
            long seconds = cooldownManager.getRemainingSeconds(player);
            MessageUtil.send(player, "message.cooldown", "%seconds%", String.valueOf(seconds));
            return;
        }
        cooldownManager.setCooldown(player);
        aiService.ask(player, message);
    }
}