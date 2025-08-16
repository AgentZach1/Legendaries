package me.libraryaddictfan;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hoglin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Particle;

import me.libraryaddictfan.Legendaries.Legendary;

import java.util.Random;

public class MobDeathListener implements Listener {

    private ClansLegendaries plugin;
    private Random random;

    public MobDeathListener(ClansLegendaries plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        // Specify the conditions for the drop
        EntityType entityType = event.getEntityType();
        double dropChance = 0.09; // 9% chance to drop a legendary
        double mushDropChance = 0.3; // 30% chance to drop a mushroom stew

        if (isLordOfTheTower(event.getEntity())
                || entityType == EntityType.EVOKER
                || entityType == EntityType.WITHER
                || entityType == EntityType.WARDEN) {
            if (random.nextDouble() < dropChance) {
                // Select a random legendary item
                Legendary legendary = getRandomLegendary();
                if (legendary != null) {
                    // Drop the item at the mob's death location
                    ItemStack legendaryItem = legendary.getFullItem();
                    event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), legendaryItem);

                    // Optional: Notify nearby players
                    event.getEntity().getWorld().playSound(event.getEntity().getLocation(),
                            Sound.valueOf("ENTITY_ZOMBIE_VILLAGER_CONVERTED"), 1.0F, 0.0F);
                    event.getEntity().getWorld().spawnParticle(
                            Particle.PORTAL,
                            event.getEntity().getLocation(),
                            50,
                            0.5, 0.5, 0.5,
                            0.1);
                    event.getEntity().getWorld().spawnParticle(
                            Particle.REVERSE_PORTAL,
                            event.getEntity().getLocation(),
                            100,
                            0.5, 1, 0.5,
                            0.1);
                    String mobName = event.getEntity().getName(); // Get the name of the mob
                    String legendaryName = legendary.getName(); // Get the name of the legendary item
                    Bukkit.broadcastMessage(
                            ChatColor.GOLD + "[Legendary Drop] " + ChatColor.RED + mobName +
                                    ChatColor.WHITE + " has dropped " + ChatColor.GOLD + legendaryName
                                    + ChatColor.WHITE + "!");
                }
            }
        } else {
            if (random.nextDouble() < mushDropChance) {
                ItemStack stew = new ItemStack(Material.MUSHROOM_STEW);
                event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), stew);
            }
        }
    }

    private Legendary getRandomLegendary() {
        if (plugin.getLegendaries().isEmpty()) {
            return null;
        }
        int index = random.nextInt(plugin.getLegendaries().size());
        Legendary leg = plugin.getLegendaries().get(index);
        if (leg.getName() == "Breeze Blade" || leg.getName() == "Magnetic Maul" || leg.getName() == "Alligators Tooth") {
            return leg;
        } else {
            return getRandomLegendary();
        }
    }

    private boolean isLordOfTheTower(org.bukkit.entity.Entity entity) {
        // Ensure the entity is a Hoglin
        if (entity.getType() != EntityType.HOGLIN) {
            return false;
        }

        // Check if the custom name matches "Lord of the Tower"
        if (!"Lord of the Tower".equals(entity.getCustomName())) {
            return false;
        }

        // Check for zombification immunity (NBT tag or default attribute)
        if (entity instanceof org.bukkit.entity.Hoglin) {
            org.bukkit.entity.Hoglin hoglin = (org.bukkit.entity.Hoglin) entity;
            return hoglin.isImmuneToZombification();
        }
        return false;
    }
}
