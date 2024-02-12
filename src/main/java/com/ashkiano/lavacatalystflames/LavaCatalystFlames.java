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
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LavaCatalystFlames extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        Metrics metrics = new Metrics(this, 19540);

        this.getLogger().info("Thank you for using the LavaCatalystFlames plugin! If you enjoy using this plugin, please consider making a donation to support the development. You can donate at: https://donate.ashkiano.com");

        checkForUpdates();
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

    private void checkForUpdates() {
        try {
            String pluginName = this.getDescription().getName();
            URL url = new URL("https://www.ashkiano.com/version_check.php?plugin=" + pluginName);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String jsonResponse = response.toString();
                JSONObject jsonObject = new JSONObject(jsonResponse);
                if (jsonObject.has("error")) {
                    this.getLogger().warning("Error when checking for updates: " + jsonObject.getString("error"));
                } else {
                    String latestVersion = jsonObject.getString("latest_version");

                    String currentVersion = this.getDescription().getVersion();
                    if (currentVersion.equals(latestVersion)) {
                        this.getLogger().info("This plugin is up to date!");
                    } else {
                        this.getLogger().warning("There is a newer version (" + latestVersion + ") available! Please update!");
                    }
                }
            } else {
                this.getLogger().warning("Failed to check for updates. Response code: " + responseCode);
            }
        } catch (Exception e) {
            this.getLogger().warning("Failed to check for updates. Error: " + e.getMessage());
        }
    }

}