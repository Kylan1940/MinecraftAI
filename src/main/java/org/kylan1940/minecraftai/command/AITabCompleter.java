package org.kylan1940.minecraftai.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class AITabCompleter implements TabCompleter {

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
                            "clear"
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