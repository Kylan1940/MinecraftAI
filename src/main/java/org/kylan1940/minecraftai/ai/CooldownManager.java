package org.kylan1940.minecraftai.ai;

import org.bukkit.entity.Player;
import org.kylan1940.minecraftai.MinecraftAI;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final MinecraftAI plugin;

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public CooldownManager(MinecraftAI plugin) {
        this.plugin = plugin;
    }

    public boolean isOnCooldown(Player player) {
        return getRemaining(player) > 0;
    }

    public long getRemaining(Player player) {

        UUID uuid = player.getUniqueId();
        Long lastUsed = cooldowns.get(uuid);

        if (lastUsed == null) {
            return 0;
        }

        long cooldownSeconds = plugin.getConfig().getLong("ai.cooldown", 3);

        long cooldownMillis = cooldownSeconds * 1000L;

        long remaining = (lastUsed + cooldownMillis) - System.currentTimeMillis();

        if (remaining <= 0) {
            cooldowns.remove(uuid);
            return 0;
        }

        return remaining;
    }

    public long getRemainingSeconds(Player player) {
        long remaining = getRemaining(player);

        if (remaining <= 0) {
            return 0;
        }

        return (remaining + 999) / 1000;
    }

    public void setCooldown(Player player) {
        cooldowns.put(
                player.getUniqueId(),
                System.currentTimeMillis()
        );
    }

    public void clear(Player player) {
        cooldowns.remove(
                player.getUniqueId()
        );
    }

    public void clearAll() {
        cooldowns.clear();
    }
}