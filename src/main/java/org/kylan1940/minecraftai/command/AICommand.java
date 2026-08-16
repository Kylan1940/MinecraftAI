package org.kylan1940.minecraftai.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.kylan1940.minecraftai.MinecraftAI;
import org.kylan1940.minecraftai.ai.AIManager;
import org.kylan1940.minecraftai.message.MessageUtil;
import org.kylan1940.minecraftai.npc.AINPC;
import org.kylan1940.minecraftai.npc.NPCManager;
import org.kylan1940.minecraftai.npc.BukkitNPC;

public class AICommand implements CommandExecutor {

    private final MinecraftAI plugin;
    private final AIManager aiManager;
    private final NPCManager npcManager;

    public AICommand(
            MinecraftAI plugin,
            AIManager aiManager,
            NPCManager npcManager
    ) {
        this.plugin = plugin;
        this.aiManager = aiManager;
        this.npcManager = npcManager;
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

        if (!player.hasPermission("minecraftai.command")) {
            player.sendMessage(ChatColor.RED + "You do not have permission.");
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

            case "npc" -> {
                if (!player.hasPermission("minecraftai.npc")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage /ai npc <create|remove|list|info>");
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "create" -> {
                        if (args.length < 3) {
                            player.sendMessage(ChatColor.RED + "Usage: /ai create <name>");
                            return true;
                        }
                        String name = args[2];
                        if(npcManager.exists(name)) {
                            player.sendMessage(ChatColor.RED + "A npc with that name already exists.");
                            return true;
                        }
                        BukkitNPC npc = new BukkitNPC(name, player.getLocation());
                        npc.spawn();
                        npcManager.register(npc);
                        player.sendMessage(ChatColor.GOLD + "Created NPC " + npc.getName());
                    }
                    case "remove" -> {
                        if (args.length < 3) {
                            player.sendMessage(ChatColor.RED + "Usage: /ai npc remove <name>");
                            return true;
                        }
                        String name = args[2];
                        AINPC npc = npcManager.get(name);
                        if (npc == null) {
                            player.sendMessage(ChatColor.RED + "NPC " + name + " was not found.");
                            return true;
                        }
                        npc.remove();
                        npcManager.remove(name);
                        player.sendMessage(ChatColor.GREEN + "NPC " + name + " has been removed.");
                    }
                    case "list" -> {
                        if (npcManager.getNPCs().isEmpty()) {
                            player.sendMessage(ChatColor.YELLOW + "There are no NPCs.");
                            return true;
                        }
                        player.sendMessage(ChatColor.GREEN + "NPCs:");
                        for (AINPC npc : npcManager.getNPCs()) {
                            player.sendMessage(ChatColor.GRAY + "- " + npc.getName());
                        }
                    }
                    case "info" -> {
                        if (args.length < 3) {
                            player.sendMessage(ChatColor.RED + "Usage: /ai info <name>");
                            return true;
                        }
                        AINPC npc = npcManager.get(args[2]);
                        if (npc == null) {
                            player.sendMessage(ChatColor.RED + "NPC " + args[2] + " was not found.");
                        }
                        player.sendMessage(ChatColor.GREEN + "NPC: " + npc.getName());
                        player.sendMessage(ChatColor.GRAY + "ID: " + npc.getId());
                        player.sendMessage(ChatColor.GRAY + "World: " + npc.getLocation().getWorld().getName());
                        player.sendMessage(ChatColor.GRAY + "Location: " + npc.getLocation().getX() + ", " + npc.getLocation().getY() + ", " + npc.getLocation().getZ());
                        player.sendMessage(ChatColor.GRAY + "Spawned: " + npc.isSpawned());
                    }
                    default ->  player.sendMessage(ChatColor.RED + "Usage: /ai npc <create|remove|list|info>");
                }
            }

            default -> {
                MessageUtil.send(player, "message.unknown-command");
            }
        }

        return true;
    }
}