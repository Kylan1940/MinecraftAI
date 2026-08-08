package org.kylan1940.minecraftai;

import org.bukkit.plugin.java.JavaPlugin;
import org.kylan1940.minecraftai.ai.AIService;
import org.kylan1940.minecraftai.command.AICommand;
import org.kylan1940.minecraftai.listener.ChatListener;
import org.kylan1940.minecraftai.utils.ConfigUpdater;

public final class MinecraftAI extends JavaPlugin {

    private static MinecraftAI instance;
    private AIService aiService;
    private ChatListener chatListener;

    @Override
    public void onEnable() {

        instance = this;

        saveDefaultConfig();
        ConfigUpdater.update(this);

        aiService = new AIService(this);

        chatListener = new ChatListener(aiService);

        getServer().getPluginManager().registerEvents(chatListener, this);

        getCommand("ai").setExecutor(new AICommand(chatListener));

        getLogger().info("MinecraftAI enabled.");

    }

    @Override
    public void onDisable() {

    }

    public static MinecraftAI getInstance() {
        return instance;
    }
}
