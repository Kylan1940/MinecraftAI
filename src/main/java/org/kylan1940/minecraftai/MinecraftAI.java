package org.kylan1940.minecraftai;

import org.bukkit.plugin.java.JavaPlugin;
import org.kylan1940.minecraftai.ai.AIService;
import org.kylan1940.minecraftai.command.AICommand;
import org.kylan1940.minecraftai.listener.ChatListener;
import org.kylan1940.minecraftai.utils.ConfigUpdater;
import org.kylan1940.minecraftai.ai.ConversationManager;
import org.kylan1940.minecraftai.ai.AIManager;

public final class MinecraftAI extends JavaPlugin {

    private static MinecraftAI instance;
    private AIService aiService;
    private ChatListener chatListener;
    private ConversationManager conversationManager;
    private AIManager aiManager;

    @Override
    public void onEnable() {

        instance = this;

        saveDefaultConfig();
        ConfigUpdater.update(this);

        aiManager = new AIManager();
        conversationManager = new ConversationManager();
        aiService = new AIService(this);

        chatListener = new ChatListener(this, aiManager, aiService);

        getServer().getPluginManager().registerEvents(chatListener, this);

        getCommand("ai").setExecutor(new AICommand(this, aiManager));

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

}
