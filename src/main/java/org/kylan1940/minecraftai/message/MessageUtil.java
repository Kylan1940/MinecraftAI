package org.kylan1940.minecraftai.message;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.kylan1940.minecraftai.MinecraftAI;

import java.util.List;

public final class MessageUtil {

    private MessageUtil() {
    }

    public static void send(CommandSender sender, String path, String... placeholders) {

        List<String> messages = MinecraftAI.getInstance().getConfig().getStringList(path);

        if (messages.isEmpty()) {
            String message = MinecraftAI.getInstance().getConfig().getString(path);

            if (message == null || message.isEmpty()) {
                return;
            }

            sender.sendMessage(color(replace(message, placeholders)));
            return;
        }

        for (String message : messages) {
            sender.sendMessage(color(replace(message, placeholders)));
        }
    }

    private static String replace(String message, String... placeholders) {

        String prefix = MinecraftAI.getInstance().getConfig().getString("prefix", "");
        String name = MinecraftAI.getInstance().getConfig().getString("name", "");

        message = message.replace("%prefix%", prefix);
        message = message.replace("%name%", name);

        for (int i = 0; i < placeholders.length; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }

        return message;
    }

    private static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void sendActionBar(
            Player player,
            String path,
            String... placeholders
    ) {
        String message = MinecraftAI.getInstance()
                .getConfig()
                .getString(path);

        if (message == null || message.isEmpty()) {
            return;
        }

        player.sendActionBar(
                color(
                        replace(
                                message,
                                placeholders
                        )
                )
        );
    }

    public static void broadcast(
            String path,
            String... placeholders
    ) {

        List<String> messages =
                MinecraftAI.getInstance()
                        .getConfig()
                        .getStringList(path);

        if (messages.isEmpty()) {

            String message =
                    MinecraftAI.getInstance()
                            .getConfig()
                            .getString(path);

            if (message == null || message.isEmpty()) {
                return;
            }

            Bukkit.broadcastMessage(
                    color(
                            replace(
                                    message,
                                    placeholders
                            )
                    )
            );

            return;
        }

        for (String message : messages) {

            Bukkit.broadcastMessage(
                    color(
                            replace(
                                    message,
                                    placeholders
                            )
                    )
            );
        }
    }

}