package org.kylan1940.minecraftai.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.papermc.paper.event.player.AsyncChatEvent;

import org.kylan1940.minecraftai.MinecraftAI;
import org.kylan1940.minecraftai.ai.AIManager;
import org.kylan1940.minecraftai.ai.AIService;
import org.kylan1940.minecraftai.ai.CooldownManager;
import org.kylan1940.minecraftai.message.MessageUtil;
import org.kylan1940.minecraftai.npc.AINPC;
import org.kylan1940.minecraftai.npc.NPCConversationManager;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

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

    @EventHandler
    public void onChat(AsyncChatEvent event) {

        if (!aiManager.isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        if (message.isEmpty()) {
            return;
        }

        if (plugin.getNpcConversationManager().isConversing(player)) {

            AINPC npc = plugin.getNpcConversationManager().get(player);
            event.setCancelled(true);

            if (npc == null) {
                plugin.getNpcConversationManager().end(player);
                return;
            }

            if (cooldownManager.isOnCooldown(player)) {
                long seconds = cooldownManager.getRemainingSeconds(player);
                MessageUtil.send(player, "message.cooldown", "%seconds%", String.valueOf(seconds));
                return;
            }

            cooldownManager.setCooldown(player);
            aiService.askNPC(player, npc, message);
            return;
        }

        AINPC mentionedNPC = findMentionedNPC(message);

        if (mentionedNPC != null) {
            event.setCancelled(true);
            plugin.getNpcConversationManager().start(player, mentionedNPC);

            String npcMessage = removeNPCName(message, mentionedNPC.getName());
            if (npcMessage.isBlank()) {
                player.sendMessage("§e" + mentionedNPC.getName() + " §7is now talking with you.");
                return;
            }

            if (cooldownManager.isOnCooldown(player)) {
                long seconds = cooldownManager.getRemainingSeconds(player);
                MessageUtil.send(player, "message.cooldown", "%seconds%", String.valueOf(seconds));
                return;
            }
            cooldownManager.setCooldown(player);
            aiService.askNPC(player, mentionedNPC, npcMessage);
            return;
        }

        String mode = aiManager.getMode();

        if (mode.equals("mention")) {
            String aiName = plugin.getConfig().getString("name", "James");
            if (!containsWord(message, aiName)) {
                return;
            }
            message = message.replaceAll("(?i)\\b" + Pattern.quote(aiName) + "\\b", "").trim();
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

    private AINPC findMentionedNPC(String message) {
        for (AINPC npc : plugin.getNpcManager().getNPCs()) {
            String name = npc.getName();
            if (containsWord(message, name)) {
                return npc;
            }
        }
        return null;
    }

    private String removeNPCName(
            String message,
            String npcName
    ) {
        return message.replaceAll("(?i)\\b" + Pattern.quote(npcName) + "\\b", "").trim();
    }

    private boolean containsWord(String message, String word) {
        return Pattern.compile("(?i)\\b" + Pattern.quote(word) + "\\b").matcher(message).find();
    }
}