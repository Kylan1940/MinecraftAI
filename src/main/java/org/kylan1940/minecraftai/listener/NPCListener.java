package org.kylan1940.minecraftai.listener;

import org.bukkit.Chunk;
import org.bukkit.entity.Villager;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.kylan1940.minecraftai.MinecraftAI;
import org.kylan1940.minecraftai.npc.AINPC;
import org.kylan1940.minecraftai.npc.BukkitNPC;

public class NPCListener implements Listener {

    private final MinecraftAI plugin;

    public NPCListener(MinecraftAI plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {

        Chunk chunk = event.getChunk();

        for (Entity entity : chunk.getEntities()) {

            if (!(entity instanceof Villager villager)) {
                continue;
            }

            if (!BukkitNPC.isNPC(villager)) {
                continue;
            }

            BukkitNPC npc = BukkitNPC.fromVillager(villager);
            if (npc == null) {
                continue;
            }

            if (!plugin.getNpcManager().exists(npc.getName())) {
                plugin.getNpcManager().register(npc);
                plugin.getLogger().info("Loaded NPC: " + npc.getName());
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                Chunk chunk = player.getLocation().getChunk();
                for (Entity entity : chunk.getEntities()) {
                    if (!(entity instanceof Villager villager)) {
                        continue;
                    }
                    if (!BukkitNPC.isNPC(villager)) {
                        continue;
                    }
                    BukkitNPC npc = BukkitNPC.fromVillager(villager);
                    if (npc == null) {
                        continue;
                    }

                    if (!plugin.getNpcManager().exists(npc.getName())) {
                        plugin.getNpcManager().register(npc);
                        plugin.getLogger().info("Loaded NPC on player join: " + npc.getName());
                    }
                }
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onNPCInteract(PlayerInteractEntityEvent event) {

        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        for (AINPC npc : plugin.getNpcManager().getNPCs()) {

            if (!npc.isSpawned()) {
                continue;
            }

            if (npc.getLocation().getWorld() == null) {
                continue;
            }

            if (!npc.getLocation().getWorld().equals(entity.getWorld())) {
                continue;
            }

            if (npc.getLocation().distanceSquared(entity.getLocation()) > 4) {
                continue;
            }

            if (!npc.getLocation().getBlock().equals(entity.getLocation().getBlock())) {
                continue;
            }

            event.setCancelled(true);
            plugin.getNpcConversationManager().start(player, npc);
            player.sendMessage("§e" + npc.getName() + " §7» §fHello, " + player.getName() + "!");
            break;
        }
    }
}