package com.ashkiano.lavacatalystflames;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class LavaCatalystFlames extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!item.isValid()) {
                    cancel();
                    return;
                }

                Location location = item.getLocation();

                // Ověření, zda je předmět v lávě
                if (location.getBlock().getType() == Material.LAVA) {
                    new BukkitRunnable() {
                        double y = 0;
                        @Override
                        public void run() {
                            // Vytvoření čáry částic plamenů směrem vzhůru
                            Location particleLocation = new Location(location.getWorld(), location.getX(), location.getY() + y, location.getZ());
                            location.getWorld().spawnParticle(Particle.LAVA, particleLocation, 5, 0.1, 0, 0.1, 0); // 5 částic v každém kroku, s malým rozptylem

                            y += 0.1; // Rychlejší pohyb vzhůru

                            // Zrušit úlohu, pokud částice dosáhly určité výšky
                            if (y > 3) {
                                cancel();
                            }
                        }
                    }.runTaskTimer(LavaCatalystFlames.this, 0, 2); // Spustí každé 2 ticky

                    // Smazání předmětu
                    item.remove();
                    cancel(); // Ukončení vnější úlohy
                }
            }
        }.runTaskTimer(this, 0, 5); // Kontroluje každých 5 ticků
    }
}