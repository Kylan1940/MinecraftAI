package org.kylan1940.minecraftai.ai;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.function.Consumer;

public class AICommandExecutor {

    private final JavaPlugin plugin;

    public AICommandExecutor(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(Player player, String command, Consumer<CommandResult> callback) {

        if (command == null || command.isBlank()) {
            callback.accept(CommandResult.INVALID);
            return;
        }

        boolean enabled = plugin.getConfig().getBoolean("ai.commands.enabled", false);

        if (!enabled) {
            callback.accept(CommandResult.DISABLED);
            return;
        }

        if (!player.hasPermission("minecraftai.execute")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
            callback.accept(CommandResult.NO_PERMISSION);
            return;
        }

        command = command.trim();

        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        if (command.contains(";") || command.contains("&&") || command.contains("||") || command.contains("\n")) {
            plugin.getLogger().warning("Blocked unsafe AI command from " + command);
            callback.accept(CommandResult.INVALID);
            return;
        }

        String[] parts = command.split("\\s+");

        if (parts.length == 0) {
            callback.accept(CommandResult.INVALID);
            return;
        }

        String commandName = parts[0].toLowerCase();

        List<String> allowedCommands = plugin.getConfig().getStringList("ai.commands.allowed");

        boolean allowed = allowedCommands.stream().anyMatch(allowedCommand -> allowedCommand.equalsIgnoreCase(commandName));

        if (!allowed) {
            plugin.getLogger().warning("Blocked non-whitelisted AI command: " + command);
            callback.accept(CommandResult.NOT_ALLOWED);
            return;
        }

        command = command.replace("%player%", player.getName());

        String finalCommand = command;

        Bukkit.getScheduler().runTask(
                plugin,
                () -> {
                    boolean success = Bukkit.dispatchCommand(player, finalCommand);
                    plugin.getLogger().info("AI command /" + finalCommand + " for " + player.getName() + ": " + success );
                    callback.accept(success ? CommandResult.SUCCESS : CommandResult.FAILED);
                }
        );
    }
}