package org.kylan1940.minecraftai.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.kylan1940.minecraftai.MinecraftAI;
import org.kylan1940.minecraftai.npc.AINPC;

import java.util.ArrayList;
import java.util.List;

public class AITabCompleter implements TabCompleter {

    private final MinecraftAI plugin;

    public AITabCompleter(MinecraftAI plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {

        if (args.length == 1) {
            return filter(
                    List.of(
                            "start",
                            "end",
                            "mode",
                            "status",
                            "clear",
                            "npc"
                    ),
                    args[0]
            );
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("mode")) {
            return filter(
                    List.of(
                            "private",
                            "mention",
                            "public"
                    ),
                    args[1]
            );
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("npc")) {
            return filter(
                    List.of(
                            "create",
                            "remove",
                            "list",
                            "info"
                    ),
                    args[1]
            );
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("npc")) {
            if (args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("info")) {
                List<String> npcNames = new ArrayList<>();
                for (AINPC npc : plugin.getNpcManager().getNPCs()) {
                    npcNames.add(npc.getName());
                }
                return filter(npcNames, args[2]);
            }
        }

        return List.of();
    }

    private List<String> filter(
            List<String> options,
            String input
    ) {
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(input.toLowerCase())) {
                result.add(option);
            }
        }
        return result;
    }
}