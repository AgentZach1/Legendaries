package me.libraryaddictfan.Legendaries;

import java.util.Arrays;
import me.libraryaddictfan.ClansLegendaries;
import me.libraryaddictfan.Utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AlligatorsTooth extends Legendary {
  public AlligatorsTooth(ClansLegendaries clanslegendariess, ItemStack item, String name, int damage) {
    super(clanslegendariess, item, name,
        Arrays.asList(new String[] { Utils.white("This deadly tooth was stolen from"),
            Utils.white("a best of reptillian beasts long"), Utils.white("ago. Legends say that the holder"),
            Utils.white("is granted the underwater agility"), Utils.white("of an Alligator"), "",
            String.valueOf(Utils.yellow("Right-Click", true)) + " to use " + Utils.green("Swim", false) }),
        damage);
  }

  @EventHandler
  public void onDmg(EntityDamageByEntityEvent e) {
    if (e.getDamager() instanceof Player) {
      Player p = (Player) e.getDamager();
      ItemStack item = Utils.getItemInHand(p);
      if (isCorrectItem(item)) {
        double base = e.getDamage();
        double tooth = getDamage();
        if (p.getLocation().getBlock().isLiquid() || isWaterlogged(p.getLocation().getBlock())) {
          e.setDamage(base + tooth + 5.0D);
        } else {
          e.setDamage(base + tooth);
        }
      }
    }
  }

  @EventHandler
  public void onClick(PlayerInteractEvent e) {
    Player p = e.getPlayer();
    ItemStack item = Utils.getItemInHand(p);
    ItemStack mainHandItem = p.getInventory().getItemInMainHand();
    ItemStack offHandItem = p.getInventory().getItemInOffHand();

    if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) &&
        (isCorrectItem(mainHandItem) || isCorrectItem(offHandItem))) {
      if (!Utils.is1_8())
        if (!isHandCorrect(e.getPlayer(), e))
          return;
      if (p.getLocation().getBlock().getType() == Material.WATER || isWaterlogged(p.getLocation().getBlock())) {
        p.setVelocity(p.getLocation().getDirection().multiply(1.6D));
        Block block = p.getLocation().getBlock();
        block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, 8);
        if (Utils.is1_8()) {
          block.getWorld().playSound(block.getLocation(), Sound.valueOf("SPLASH"), 0.4F, 1.0F);
        } else {
          block.getWorld().playSound(block.getLocation(), Sound.valueOf("ENTITY_PLAYER_SPLASH"), 0.4F, 1.0F);
          p.getWorld().spawnParticle(Particle.CLOUD, block.getLocation(), 5, 0.012F, 0.2F, 0.012F, 0.1);
          p.getWorld().spawnParticle(Particle.CURRENT_DOWN, block.getLocation(), 30, 0.1F, 0.2F, 0.1F, 0.1);
          p.getWorld().spawnParticle(Particle.BUBBLE, block.getLocation(), 15, 0.3F, 0.3F, 0.3F, 0.1);
        }
      }
    }
  }

  public void loop() {
    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
      // ItemStack item = Utils.getItemInHand(p);
      ItemStack mainHandItem = p.getInventory().getItemInMainHand();
      ItemStack offHandItem = p.getInventory().getItemInOffHand();

      if (isCorrectItem(mainHandItem) || isCorrectItem(offHandItem)) {
        if (p.hasPotionEffect(PotionEffectType.CONDUIT_POWER))
          return;
        p.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 25 * 20, 1));
      }
    }
  }

  private boolean isWaterlogged(Block block) {
    BlockData data = block.getBlockData(); // Get the block's data
    if (data instanceof Waterlogged) {
      return ((Waterlogged) data).isWaterlogged();
    }
    switch (block.getType()) {
      case SEAGRASS:
      case TALL_SEAGRASS:
      case KELP:
      case KELP_PLANT:
      case BUBBLE_COLUMN:
        return true;
      default:
        return false;
    }
  }

  public void rel() {
  }

  public void quit(Player player) {
  }

  public void clearMem() {
  }
}
