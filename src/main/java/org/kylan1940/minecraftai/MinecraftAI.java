package org.kylan1940.minecraftai;

import org.bukkit.plugin.java.JavaPlugin;
import org.kylan1940.minecraftai.ai.AIService;
import org.kylan1940.minecraftai.ai.CooldownManager;
import org.kylan1940.minecraftai.command.AICommand;
import org.kylan1940.minecraftai.command.AITabCompleter;
import org.kylan1940.minecraftai.listener.ChatListener;
import org.kylan1940.minecraftai.utils.ConfigUpdater;
import org.kylan1940.minecraftai.ai.ConversationManager;
import org.kylan1940.minecraftai.ai.AIManager;
import org.kylan1940.minecraftai.npc.NPCManager;

public final class MinecraftAI extends JavaPlugin {

    private static MinecraftAI instance;
    private AIService aiService;
    private ChatListener chatListener;
    private ConversationManager conversationManager;
    private AIManager aiManager;
    private CooldownManager cooldownManager;
    private NPCManager npcManager;

    @Override
    public void onEnable() {

        instance = this;

        saveDefaultConfig();
        ConfigUpdater.update(this);

        aiManager = new AIManager();
        conversationManager = new ConversationManager();
        aiService = new AIService(this);
        cooldownManager = new CooldownManager(this);
        npcManager = new NPCManager();

        chatListener = new ChatListener(this, aiManager, aiService);

        getServer().getPluginManager().registerEvents(chatListener, this);

        getCommand("ai").setExecutor(new AICommand(this, aiManager, npcManager));
        getCommand("ai").setTabCompleter(new AITabCompleter());

        getLogger().info("MinecraftAI enabled.");

    }

    @Override
    public void onDisable() {
        getLogger().info("MinecraftAI disabled.");
    }

    public static MinecraftAI getInstance() {
        return instance;
    }

    public ConversationManager getConversationManager() {
        return conversationManager;
    }

    public AIManager getAIManager() {
        return aiManager;
    }

    public AIService getAIService() {
        return aiService;
    }

    public ChatListener getChatListener() {
        return chatListener;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public NPCManager getNpcManager() {
        return npcManager;
    }

}
