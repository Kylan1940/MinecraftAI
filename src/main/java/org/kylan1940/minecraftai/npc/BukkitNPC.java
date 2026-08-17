package org.kylan1940.minecraftai.npc;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class BukkitNPC implements AINPC {

    private static final NamespacedKey NPC_KEY = NamespacedKey.fromString("minecraftai:npc");

    private static final NamespacedKey NPC_ID_KEY = NamespacedKey.fromString("minecraftai:npc_id");

    private final UUID id;
    private final String name;
    private final Location location;

    private Villager villager;

    public BukkitNPC(String name, Location location) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.location = location.clone();
    }

    private BukkitNPC(
            UUID id,
            String name,
            Villager villager
    ) {
        this.id = id;
        this.name = name;
        this.villager = villager;
        this.location = villager.getLocation().clone();
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

        villager.getPersistentDataContainer().set(
                NPC_KEY,
                PersistentDataType.BYTE,
                (byte) 1
        );

        villager.getPersistentDataContainer().set(
                NPC_ID_KEY,
                PersistentDataType.STRING,
                id.toString()
        );
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

    public static boolean isNPC(Villager villager) {
        Byte value = villager.getPersistentDataContainer().get(NPC_KEY, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    public static BukkitNPC fromVillager(Villager villager) {

        String idString = villager.getPersistentDataContainer().get(NPC_ID_KEY, PersistentDataType.STRING);

        if (idString == null) {
            return null;
        }

        UUID id;

        try {
            id = UUID.fromString(idString);
        } catch (IllegalArgumentException exception) {
            return null;
        }

        String name = villager.getCustomName();

        if (name == null || name.isBlank()) {
            return null;
        }

        return new BukkitNPC(id, name, villager);
    }
}