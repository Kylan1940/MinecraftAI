package org.kylan1940.minecraftai.npc;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

import java.util.UUID;

public class BukkitNPC implements AINPC {

    private final UUID id;
    private final String name;
    private final Location location;

    private Villager villager;

    public BukkitNPC(String name, Location location) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.location = location.clone();
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public void spawn() {
        if (isSpawned()) {
            return;
        }

        Entity entity = location.getWorld().spawnEntity(
                location,
                EntityType.VILLAGER
        );

        if (!(entity instanceof Villager spawnedVillager)) {
            entity.remove();
            return;
        }

        villager = spawnedVillager;

        villager.setCustomName(name);
        villager.setCustomNameVisible(true);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);
        villager.setCollidable(false);
    }

    @Override
    public void remove() {
        if (villager == null) {
            return;
        }

        if (!villager.isDead()) {
            villager.remove();
        }

        villager = null;
    }

    @Override
    public boolean isSpawned() {
        return villager != null && !villager.isDead();
    }
}