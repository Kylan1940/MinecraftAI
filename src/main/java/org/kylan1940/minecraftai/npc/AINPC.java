package org.kylan1940.minecraftai.npc;

import org.bukkit.Location;

import java.util.UUID;

public interface AINPC {

    UUID getId();

    String getName();

    Location getLocation();

    void spawn();

    void remove();

    boolean isSpawned();
}