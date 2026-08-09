package org.kylan1940.minecraftai.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.kylan1940.minecraftai.listener.ChatListener;
import org.kylan1940.minecraftai.message.MessageUtil;

public class AICommand implements CommandExecutor {

    private final ChatListener chatListener;

    public AICommand(ChatListener chatListener) {
        this.chatListener = chatListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length == 0) {
            MessageUtil.send(player, "message.usage");
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "start" -> {

                if (chatListener.isEnabled(player)) {
                    return true;
                }

                chatListener.enable(player);

                MessageUtil.send(player, "message.ai-enabled");

            }

            case "end", "stop" -> {

                if (!chatListener.isEnabled(player)) {
                    return true;
                }

                chatListener.disable(player);

                MessageUtil.send(player, "message.ai-disabled");

            }

            case "status" -> {

                boolean enabled =
                        chatListener.isEnabled(player);

                MessageUtil.send(
                        player, "message.ai-status", "%status%",
                        enabled ? "&aEnabled" : "&cDisabled"
                );

            }

            default -> {
                MessageUtil.send(player, "message.usage");
            }
        }

        return true;
    }
}