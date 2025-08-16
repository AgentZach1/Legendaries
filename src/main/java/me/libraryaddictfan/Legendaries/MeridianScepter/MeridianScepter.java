package me.libraryaddictfan.Legendaries.MeridianScepter;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import me.libraryaddictfan.ClansLegendaries;
import me.libraryaddictfan.Legendaries.Legendary;
import me.libraryaddictfan.Utilities.ConfigSections;
import me.libraryaddictfan.Utilities.ConfigUtils;
import me.libraryaddictfan.Utilities.Display;
import me.libraryaddictfan.Utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MeridianScepter extends Legendary {
  private static boolean targetEnt;

  private static ArrayList<ScepterShot> shots;

  private HashMap<String, Long> cooldown;

  private int damage;

  public MeridianScepter(ClansLegendaries mainn, ItemStack itemm, String namee, int damagee) {
    super(mainn, itemm, namee,
        Arrays.asList(
            new String[] { Utils.white("Legend says that this scepter"), Utils.white("was found, and retrieved from"),
                Utils.white("the deepest trench in all of"), Utils.white("Minecraftia. It is said that he"),
                Utils.white("wields this scepter holds"), Utils.white("the power of Poseidon himself."), "",
                String.valueOf(Utils.yellow("Right-Click", true)) + " to use " + Utils.green("Scepter", false) }),
        damagee);
    shots = new ArrayList<>();
    this.cooldown = new HashMap<>();
    this.damage = ConfigUtils.getIntegerSection(ConfigSections.SCEPTER_DAMAGE);
  }

  public static boolean isTargetEnt() {
    return targetEnt;
  }

  public void loop() {
    if (!shots.isEmpty()) {
      ArrayList<ScepterShot> copy = (ArrayList<ScepterShot>) shots.clone();
      for (ScepterShot shot : copy) {
        if (!shot.getArrow().isDead() && !shot.isGone())
          shot.update();
      }
    }
    for (String s : this.cooldown.keySet()) {
      Player p = Bukkit.getServer().getPlayer(s);
      if ((System.currentTimeMillis() - ((Long) this.cooldown.get(s)).longValue()) / 1000L > 1L) {
        this.cooldown.remove(s);
        p.sendMessage(ChatColor.BLUE + getName() + ChatColor.BLUE + "> " + ChatColor.GRAY +
            "You can use " + ChatColor.GREEN + getName());
        if (isCorrectItem(Utils.getItemInHand(p)))
          Display.display(ChatColor.GREEN + getName() + " Recharged", p);
        continue;
      }
      if (isCorrectItem(Utils.getItemInHand(p))) {
        Double x = Double.valueOf(2.0D - Math.pow(10.0D, -1.0D)
            * ((System.currentTimeMillis() - ((Long) this.cooldown.get(p.getName())).longValue()) / 100L));
        double divide = (System.currentTimeMillis() - ((Long) this.cooldown.get(s)).longValue()) / 2000.0D;
        String[] zz = x.toString().replace('.', '-').split("-");
        String concat = String.valueOf(zz[0]) + "." + zz[1].substring(0, 1);
        Display.displayProgress(getName(), divide, 0, ChatColor.WHITE + "  Seconds", false, new Player[] { p });
      }
    }
  }

  public static void removeShot(ScepterShot shot) {
    if (!shots.contains(shot))
      return;
    shots.remove(shot);
  }

  @EventHandler
  public void onRightClick(PlayerInteractEvent e) {
    Player p = e.getPlayer();
    if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) &&
        isCorrectItem(Utils.getItemInHand(p))) {
      if (!Utils.is1_8() &&
          !isHandCorrect(e.getPlayer(), e))
        return;
      if (p.getLocation().getBlock().isLiquid()) {
        p.sendMessage(ChatColor.BLUE + getName() + ChatColor.BLUE +
            "> " + ChatColor.GRAY + "You cannot use " +
            ChatColor.GREEN + getName() + ChatColor.GRAY +
            " in water.");
        return;
      }
      if (this.cooldown.containsKey(p.getName()))
        return;
      if (Utils.is1_8()) {
        p.playSound(p.getLocation(), Sound.valueOf("BLAZE_BREATH"), 1.0F, 0.0F);
      } else {
        p.playSound(p.getLocation(), Sound.valueOf("ENTITY_BLAZE_AMBIENT"), 1.0F, 0.0F);
      }
      ScepterShot shot = new ScepterShot(getMain(), p);
      this.cooldown.put(p.getName(), Long.valueOf(System.currentTimeMillis()));
      shots.add(shot);
      shot.launch();
    }
  }

  @EventHandler
  public void onArrowHit(ProjectileHitEvent e) {
    if (e.getEntity() instanceof Arrow) {
      Arrow arrow = (Arrow) e.getEntity();
      for (ScepterShot shot : shots) {
        if (shot.getArrow() == arrow) {
          if (Utils.is1_8()) {
            Block block = null;
            Block hitBlock = null;
            block = shot.getArrow().getLocation().getBlock();
            Material type = null;
            type = block.getType();
            hitBlock = null;
            Class<?> movingObjectPosition = Utils.getNmsClass("MovingObjectPosition");
            Class<?> blockPositionn = Utils.getNmsClass("BlockPosition");
            Constructor<?> construct = Utils.getConstructor(movingObjectPosition, new Class[] { Entity.class });
            Object position = Utils.callConstructor(construct, new Object[] { arrow });
            Object posType = Utils.getFieldAndValue(movingObjectPosition, "type", position);
            if (posType == Utils.getEnumConstant(movingObjectPosition, "BLOCK")) {
              Object blockposition = Utils.getAndInvokeMethod(movingObjectPosition, "a",
                  new Class[0], position, new Object[0]);
              hitBlock = e.getEntity().getWorld()
                  .getBlockAt(new Location(e.getEntity().getWorld(),
                      ((Double) Utils.getAndInvokeMethod(blockPositionn, "getX",
                          new Class[0], blockposition, new Object[0])).doubleValue(),
                      ((Double) Utils.getAndInvokeMethod(blockPositionn, "getY",
                          new Class[0], blockposition, new Object[0])).doubleValue(),
                      ((Double) Utils.getAndInvokeMethod(blockPositionn, "getZ",
                          new Class[0], blockposition, new Object[0])).doubleValue()));
            }
            if (hitBlock != null &&
                hitBlock.getType() != Material.AIR) {
              if (type == Material.WATER ||
                  type == Material.LAVA)
                return;
              arrow.teleport(new Location(arrow.getWorld(), 0.0D, -10.0D, 0.0D));
              if (shot.isToRemove())
                continue;
              shot.delete();
            }
            continue;
          }
          Block output = (Block) Utils.getAndInvokeMethod(ProjectileHitEvent.class, "getHitBlock",
              new Class[0], e, new Object[0]);
          if (output != null && output.getType() != Material.AIR) {
            if (output.getType() == Material.WATER ||
                output.getType() == Material.LAVA)
              return;
            arrow.teleport(new Location(arrow.getWorld(), 0.0D, -10.0D, 0.0D));
            if (shot.isToRemove())
              continue;
            shot.delete();
          }
        }
      }
    }
  }

  @EventHandler
  public void onDeath(EntityDeathEvent e) {
    if (e.getEntity() instanceof Arrow) {
      Arrow arrow = (Arrow) e.getEntity();
      for (ScepterShot shot : shots) {
        if (shot.getArrow() == arrow) {
          if (shot.isToRemove())
            return;
          shot.delete();
        }
      }
    }
  }

  @EventHandler
  public void onDmg(EntityDamageByEntityEvent e) {
    if (e.isCancelled()) {
      if (e.getDamager() instanceof Arrow) {
        Arrow arrow = (Arrow) e.getDamager();
        for (ScepterShot shot : shots) {
          if (shot.getArrow() == arrow) {
            arrow.teleport(new Location(arrow.getWorld(), 0.0D, -10.0D, 0.0D));
            shot.delete();
          }
        }
      }
      return;
    }
    if (e.getDamager() instanceof Arrow &&
        e.getEntity() instanceof LivingEntity) {
      Arrow arrow = (Arrow) e.getDamager();
      LivingEntity struckEnt = (LivingEntity) e.getEntity();
      if (struckEnt.isDead())
        return;
      for (ScepterShot shot : shots) {
        if (shot.getArrow() == arrow) {
          if (shot.getShooter() == struckEnt) {
            e.setCancelled(true);
            continue;
          }
          arrow.teleport(new Location(arrow.getWorld(), 0.0D, -10.0D, 0.0D));
          if (struckEnt instanceof Player) {
            Player struck = (Player) struckEnt;
            shot.getShooter().sendMessage(ChatColor.BLUE + "Clans> " +
                ChatColor.GRAY + "You struck " + ChatColor.YELLOW + struck.getName() +
                ChatColor.GRAY + " with your " + ChatColor.YELLOW + getName() + ChatColor.GRAY +
                ".");
            struck.sendMessage(ChatColor.BLUE + "Clans> " +
                ChatColor.YELLOW + shot.getShooter().getName() + ChatColor.GRAY +
                " hit you with a " + ChatColor.YELLOW + getName() + ChatColor.GRAY +
                ".");
          } else {
            String string = struckEnt.getType().toString().toLowerCase().replace("_", " ");
            shot.getShooter().sendMessage(ChatColor.BLUE + "Clans> " +
                ChatColor.GRAY + "You struck " + ChatColor.YELLOW + string +
                ChatColor.GRAY + " with your " + ChatColor.YELLOW + getName() + ChatColor.GRAY +
                ".");
          }
          e.setCancelled(true);
          Player p = shot.getShooter();

          Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) getMain(), () -> {
            if (struckEnt.isDead())
              return;
            struckEnt.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 0));
            struckEnt.getWorld().strikeLightningEffect(struckEnt.getLocation());
            struckEnt.damage(this.damage, p);
          }, 60L);

          shot.delete();
        }
      }
    }
  }

  public void rel() {
    targetEnt = ConfigUtils.getBooleanSection(ConfigSections.SCEPTER_TARGET_ENT);
    this.damage = ConfigUtils.getIntegerSection(ConfigSections.SCEPTER_DAMAGE);
  }

  public void quit(Player player) {
  }

  public void clearMem() {
    this.cooldown.clear();
    shots.clear();
  }
}
