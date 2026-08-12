package org.kylan1940.minecraftai.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.kylan1940.minecraftai.MinecraftAI;
import org.kylan1940.minecraftai.ai.AIManager;
import org.kylan1940.minecraftai.message.MessageUtil;

public class AICommand implements CommandExecutor {

    private final MinecraftAI plugin;
    private final AIManager aiManager;

    public AICommand(
            MinecraftAI plugin,
            AIManager aiManager
    ) {
        this.plugin = plugin;
        this.aiManager = aiManager;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, "message.player-only");
            return true;
        }

        if (args.length == 0) {
            MessageUtil.send(player, "message.usage");
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "start" -> {
                if (aiManager.isEnabled()) {
                    MessageUtil.send(player, "message.already-enabled");
                    return true;
                }
                aiManager.enable();
                MessageUtil.send(player, "message.ai-enabled");
            }

            case "stop", "end" -> {
                if (!aiManager.isEnabled()) {
                    MessageUtil.send(player, "message.already-disabled");
                    return true;
                }
                aiManager.disable();
                MessageUtil.send(player, "message.ai-disabled");
            }

            case "status" -> {
                String mode = aiManager.getMode();
                String status = aiManager.isEnabled()
                                ? "Enabled"
                                : "Disabled";
                MessageUtil.send(player, "message.status", "%status%", status, "%mode%", mode);
            }

            case "mode" -> {
                if (args.length < 2) {
                    MessageUtil.send(player, "message.mode-usage");
                    return true;
                }
                String mode = args[1].toLowerCase();
                if (!mode.equals("public")
                        && !mode.equals("mention")
                        && !mode.equals("private")) {
                    MessageUtil.send(player, "message.invalid-mode");
                    return true;
                }
                if (mode.equals("private")) {
                    aiManager.addPrivatePlayer(player);
                } else {
                    aiManager.removePrivatePlayer(player);
                }
                aiManager.setMode(mode);
                MessageUtil.send(player, "message.mode-changed", "%mode%", mode);
            }

            case "clear" -> {
                plugin.getConversationManager()
                        .clear(player.getUniqueId());
                MessageUtil.send(player, "message.conversation-cleared");
            }

            default -> {
                MessageUtil.send(player, "message.unknown-command");
            }
        }

        return true;
    }
}