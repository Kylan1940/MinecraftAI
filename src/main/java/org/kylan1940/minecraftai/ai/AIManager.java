package org.kylan1940.minecraftai.ai;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AIManager {

    private boolean enabled = false;

    private String mode = "mention";

    private final Set<UUID> privatePlayers = new HashSet<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
        privatePlayers.clear();
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode.toLowerCase();
    }

    public void addPrivatePlayer(Player player) {
        privatePlayers.add(player.getUniqueId());
    }

    public void removePrivatePlayer(Player player) {
        privatePlayers.remove(player.getUniqueId());
    }

    public boolean isPrivatePlayer(Player player) {
        return privatePlayers.contains(
                player.getUniqueId()
        );
    }

    public Set<UUID> getPrivatePlayers() {
        return Set.copyOf(privatePlayers);
    }
}