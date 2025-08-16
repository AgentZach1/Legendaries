package me.libraryaddictfan.Legendaries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import me.libraryaddictfan.ClansLegendaries;
import me.libraryaddictfan.Utilities.Display;
import me.libraryaddictfan.Utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class MagneticMaul extends Legendary {
  private HashMap<String, Float> charges;

  private HashMap<String, Float> smoother;

  public MagneticMaul(ClansLegendaries mainn, ItemStack itemStack, String name, int damagee) {
    super(mainn, itemStack, name, Arrays.asList(new String[] {
        Utils.white("A dwarven civilization once harnessed "),
        Utils.white("the power of magnetism. Lost to time "),
        Utils.white("this hammer has been unearthed. Those who wield "),
        Utils.white("the Magnetic Maul attract many friends."), "",
        String.valueOf(Utils.yellow("Right-Click", true)) + " to use " + Utils.green("Magnetic Pull", false) }),
        damagee);
    this.charges = new HashMap<>();
    this.smoother = new HashMap<>();
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onEntityPunchEntity(EntityDamageByEntityEvent e) {
    if (e.isCancelled())
      return;
    if (e.getEntity().getLastDamageCause() != null && e.getEntity().getLastDamageCause().getCause() != null &&
        e.getEntity().getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.SUICIDE) {
      e.getEntity().setLastDamageCause(
          new EntityDamageEvent(e.getDamager(), EntityDamageEvent.DamageCause.ENTITY_ATTACK, getDamage()));
      return;
    }
    if (e.getDamager() instanceof Player) {
      Player p = (Player) e.getDamager();
      // ItemStack item = Utils.getItemInHand(p);
      ItemStack mainHandItem = p.getInventory().getItemInMainHand();
      if (isCorrectItem(mainHandItem)) {
        if (e.getEntity() instanceof LivingEntity) {
          if (e.getEntity() instanceof Player && (((Player) e.getEntity()).getGameMode() == GameMode.CREATIVE
              || ((Player) e.getEntity()).getGameMode() == GameMode.SPECTATOR)) {
            return;
          }
          double baseItemDamage = e.getDamage(); // The damage Minecraft calculates (includes strength, enchants, etc.)
          double legendaryBonusDamage = getDamage(); // Magnetic Maul's additional damage
          double totalDamage = baseItemDamage + legendaryBonusDamage;
          e.setCancelled(true);
          generateParticles(p, false);
          LivingEntity l = (LivingEntity) e.getEntity();
          l.setLastDamageCause(new EntityDamageEvent((Entity) p, EntityDamageEvent.DamageCause.SUICIDE, getDamage()));
          l.damage(totalDamage, (Entity) p);
          Vector vec = p.getLocation().toVector()
              .subtract(e.getEntity().getLocation().toVector()).normalize().add(new Vector(0.0D, 0.4D, 0.0D))
              .multiply(0.4D);
          e.getEntity().setVelocity(vec);
        }
      }
    }
  }

  public void onUpdate(Player p) {
    ItemStack mainHandItem = p.getInventory().getItemInMainHand();
    ItemStack offHandItem = p.getInventory().getItemInOffHand();
    boolean isCorrect = isCorrectItem(mainHandItem) || isCorrectItem(offHandItem);

    if (!this.charges.containsKey(p.getName()) && isCorrect) {
      this.charges.putIfAbsent(p.getName(), Float.valueOf(0.0F));
    }
    if (!this.smoother.containsKey(p.getName()) && isCorrect) {
      this.smoother.putIfAbsent(p.getName(), Float.valueOf(0.0F));
    }
    // If the item is correct, update charge and smoother values
    if (isCorrect) {
      float smootherValue = this.smoother.get(p.getName());
      float chargeValue = this.charges.get(p.getName());

      // Increase charge if smoother is low or charge is below the threshold
      if (smootherValue == 0.0F || chargeValue <= 0.13D) {
        Charge(p);
      } else if (smootherValue != 0.0F) {
        this.charges.put(p.getName(), Math.max(0.0F, chargeValue - 0.038F));
      }

      // Decrease smoother over time
      this.smoother.put(p.getName(), Math.max(0.0F, smootherValue - 0.5F));
      Display.displayProgress(null, getCharge(p), 0, null, false, new Player[] { p });
    } else {
      memoryRemove(p);
    }
  }

  public void loop() {
    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
      // ItemStack item = Utils.getItemInHand(p);
      ItemStack mainHandItem = p.getInventory().getItemInMainHand();
      ItemStack offHandItem = p.getInventory().getItemInOffHand();
      boolean isCorrect = isCorrectItem(mainHandItem) || isCorrectItem(offHandItem);
      if (!p.getInventory().contains(getFullItem()) && !isCorrect)
        this.charges.remove(p.getName());
      if (isCorrect) {
        onUpdate(p);

        // Apply attraction logic to nearby entities
        try {
          if (this.smoother.get(p.getName()) > 0.6F) {
            for (Entity e : p.getNearbyEntities(8.0D, 8.0D, 8.0D)) {
              if (e == p || e.isDead()) {
                continue;
              }
              if (e instanceof Player && !p.canSee((Player) e)) {
                continue;
              }
              if ((e instanceof LivingEntity || e instanceof org.bukkit.entity.Item)
                  && getLookingAt(p, e)) {
                Vector vec = p.getLocation().toVector().subtract(e.getLocation().toVector()).normalize();
                Vector newVec = vec.multiply(0.7D);
                Vector useCharge = newVec.multiply(1.2D).multiply(Math.min(0.34D, Math.max(getCharge(p), 0.23D)));
                e.setVelocity(useCharge);
                generateParticles(p, true);
              }
            }
          }
        } catch (NullPointerException ignored) {
        }
      } else {
        memoryRemove(p);
      }
    }
  }

  @EventHandler
  public void onClick(PlayerInteractEvent e) {
    ItemStack mainHandItem = e.getPlayer().getInventory().getItemInMainHand();
    ItemStack offHandItem = e.getPlayer().getInventory().getItemInOffHand();

    if (e.getAction() == Action.RIGHT_CLICK_AIR ||
        e.getAction() == Action.RIGHT_CLICK_BLOCK)
      if (isCorrectItem(mainHandItem) || isCorrectItem(offHandItem)) {
        generateParticles(e.getPlayer(), false);
        List<Entity> tempList = new ArrayList<>();
        for (Entity ent : e.getPlayer().getNearbyEntities(8.0D, 8.0D, 8.0D)) {

          // if (!(ent instanceof LivingEntity))
          // continue;
          if (ent.isDead())
            continue;
          if ((ent instanceof LivingEntity || ent instanceof org.bukkit.entity.Item)
              && getLookingAt(e.getPlayer(), ent)) {
            Vector vec = e.getPlayer().getLocation().toVector().subtract(ent.getLocation().toVector())
                .normalize();
            Vector newVec = vec.multiply(0.7D);
            Vector useCharge = newVec.multiply(1.2D).multiply(Math.min(0.24D, getCharge(e.getPlayer())));
            ent.setVelocity(useCharge);
            tempList.add(ent);
            generateParticles(e.getPlayer(), true);
          }
        }
      }
    this.smoother.putIfAbsent(e.getPlayer().getName(), 0.0F); // Ensure smoother exists
    float smootherValue = this.smoother.get(e.getPlayer().getName()); // Safe retrieval
    this.smoother.put(e.getPlayer().getName(), Math.min(5.0F, smootherValue + 2.0F));
  }

  public boolean Charge(Player player) {
    if (!this.charges.containsKey(player.getName()))
      this.charges.put(player.getName(), Float.valueOf(0.0F));
    float charge = ((Float) this.charges.get(player.getName())).floatValue();
    charge = (float) Math.min(1.0D, charge + 0.022D);
    this.charges.put(player.getName(), Float.valueOf(charge));
    Display.displayProgress(null, charge, 0, null, false, new Player[] { player });
    return (charge >= 1.0F);
  }

  public float getCharge(Player player) {
    if (!this.charges.containsKey(player.getName()))
      return 0.0F;
    return ((Float) this.charges.get(player.getName())).floatValue();
  }

  private boolean getLookingAt(Player player, Entity ent) {
    Location eye = player.getLocation();
    Vector toEntity = ent.getLocation().toVector().subtract(eye.toVector());
    double dot = toEntity.normalize().dot(eye.getDirection());
    return (dot > 0.7D);
  }

  private void memoryRemove(Player p) {
    if (this.smoother.containsKey(p.getName()))
      this.smoother.remove(p.getName());
  }

  private void generateParticles(Player player, boolean ent) {
    // player.sendMessage("Particles generated!");
    Location playerLocation = player.getLocation().add(0, 1.0, 0);
    Vector direction = playerLocation.getDirection().normalize(); // Direction the player is looking
    double maxRange = 8.0; // Maximum range of the cone
    double coneAngle = Math.toRadians(45); // Cone spread angle in radians (30 degrees)
    player.playSound(player.getLocation(), Sound.valueOf("BLOCK_CHORUS_FLOWER_GROW"), 1.0F, 2.0F);
    int particleCount = (int) (25 * (getCharge(player))); // Total particles to spawn

    if (ent) {
      double randomDistance = Math.random() * maxRange; // Random distance along the cone
      double randomAngle = Math.random() * coneAngle - (coneAngle / 2); // Random angle within the cone

      // Rotate the direction vector by the random angle
      Vector rotatedDirection = direction.clone();
      rotatedDirection.rotateAroundY(randomAngle);

      // Scale the rotated vector by the random distance
      Vector particlePosition = rotatedDirection.multiply(randomDistance);

      // Calculate the final particle location
      Location particleLocation = playerLocation.clone().add(particlePosition);

      // Spawn the particle
      player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, particleLocation, 1, 0, 0, 0, 0);

    } else {
      for (int i = 0; i < particleCount; i++) {

        double randomDistance = Math.random() * maxRange; // Random distance along the cone
        double randomAngle = Math.random() * coneAngle - (coneAngle / 2); // Random angle within the cone

        // Rotate the direction vector by the random angle
        Vector rotatedDirection = direction.clone();
        rotatedDirection.rotateAroundY(randomAngle);

        // Scale the rotated vector by the random distance
        Vector particlePosition = rotatedDirection.multiply(randomDistance);

        // Calculate the final particle location
        Location particleLocation = playerLocation.clone().add(particlePosition);

        // Spawn the particle
        player.getWorld().spawnParticle(
            Particle.ENCHANTED_HIT, // Particle type
            particleLocation, // Location
            1, // Count
            0, 0, 0, // Offset
            0.2 // Speed
        );
      }
    }
  }

  public void rel() {
  }

  public void quit(Player player) {
    this.charges.remove(player.getName());
    this.smoother.remove(player.getName());
  }

  public void clearMem() {
    this.charges.clear();
    this.smoother.clear();
  }
}
