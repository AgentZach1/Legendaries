package me.libraryaddictfan.Legendaries;

import java.util.ArrayList;
import java.util.Arrays;
import me.libraryaddictfan.ClansLegendaries;
import me.libraryaddictfan.Utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GiantsBroadsword extends Legendary {
  private final Object enumParticleConst = Utils.getEnumConstant(Utils.getNmsClass("EnumParticle"), "ENCHANTED_HIT");

  private final Object enumParticleConstHeart = Utils.getEnumConstant(Utils.getNmsClass("EnumParticle"), "HEART");

  private ArrayList<String> hitStop;

  public GiantsBroadsword(ClansLegendaries main, ItemStack itemm, String namee, int damage) {
    super(main, itemm, namee,
        Arrays.asList(new String[] { Utils.white("Forged in the godly mined of Plagieus"),
            Utils.white("this sword has endured thousands of"), Utils.white("wars. It is sure to grant certain"),
            Utils.white("is granted the underwater agility"), Utils.white("victory in battle."), "",
            Utils.white("Deals " + Utils.yellow("10 Damage", true) + " with attack"),
            String.valueOf(Utils.yellow("Right-Click", true)) + " to use " + Utils.green("Shield", false) }),
        damage);
    this.hitStop = new ArrayList<>();
  }

  @EventHandler
  public void onDmg(EntityDamageByEntityEvent e) {
    if (e.getDamager() instanceof Player) {
      Player p = (Player) e.getDamager();
      ItemStack item = Utils.getItemInHand(p);
      if (this.hitStop.contains(p.getName()))
        e.setCancelled(true);
      if (isCorrectItem(item))
        e.setDamage(getDamage());
    }
  }

  @EventHandler
  public void onClick(PlayerInteractEvent e) {
    Player p = e.getPlayer();
    ItemStack item = Utils.getItemInHand(p);
    if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) &&
        isCorrectItem(item)) {
      if (!Utils.is1_8())
        if (!isHandCorrect(e.getPlayer(), e))
          return;
      this.hitStop.add(p.getName());
      p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 15, 3));
      p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 15, 255));
      if (p != null && p.isOnline())
        if (Utils.is1_8()) {
          p.playSound(p.getLocation(), Sound.valueOf("LAVA_POP"), 13.0F, 2.0F);
        } else {
          p.playSound(p.getLocation(), Sound.valueOf("BLOCK_LAVA_POP"), 13.0F, 2.0F);
        }
      Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) getMain(), () -> {
        if (p != null && p.isOnline())
          if (Utils.is1_8()) {
            p.playSound(p.getLocation(), Sound.valueOf("LAVA_POP"), 13.0F, 2.0F);
          } else {
            p.playSound(p.getLocation(), Sound.valueOf("BLOCK_LAVA_POP"), 13.0F, 2.0F);
          }
      }, 3L);
      Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) getMain(), () -> {
        if (p != null && p.isOnline())
          if (Utils.is1_8()) {
            p.playSound(p.getLocation(), Sound.valueOf("LAVA_POP"), 13.0F, 2.0F);
          } else {
            p.playSound(p.getLocation(), Sound.valueOf("BLOCK_LAVA_POP"), 13.0F, 2.0F);
          }
        this.hitStop.remove(p.getName());
      }, 5L);
      Location block = p.getLocation().clone().add(0.0D, 2.0D, 0.0D);
      double x = block.getX();
      double y = block.getY();
      double z = block.getZ();
      for (Player pla : Bukkit.getServer().getOnlinePlayers()) {
        if (pla.getLocation().distance(new Location(pla.getWorld(), x, y, z)) > 64.0D)
          return;
        Utils.sendParticles(pla,
            this.enumParticleConstHeart,
            false,
            (float) x,
            (float) y,
            (float) z,
            0.1F,
            0.04F,
            0.1F,
            0.05F,
            2,
            new int[0]);
      }
    }
  }

  public void loop() {
    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
      ItemStack item = Utils.getItemInHand(p);
      if (p.isDead())
        return;
      if (isCorrectItem(item)) {
        Location block = p.getLocation().clone().add(0.0D, 1.0D, 0.0D);
        double x = block.getX();
        double y = block.getY();
        double z = block.getZ();
        for (Player pla : Bukkit.getServer().getOnlinePlayers())
          Utils.sendParticles(pla,
              this.enumParticleConst,
              false,
              (float) x,
              (float) y,
              (float) z,
              0.2F,
              0.2F,
              0.2F,
              0.005F,
              1,
              new int[0]);
      }
    }
  }

  public void rel() {
  }

  public void quit(Player player) {
    this.hitStop.remove(player.getName());
  }

  public void clearMem() {
    this.hitStop.clear();
  }
}
