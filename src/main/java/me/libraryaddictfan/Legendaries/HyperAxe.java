package me.libraryaddictfan.Legendaries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import me.libraryaddictfan.ClansLegendaries;
import me.libraryaddictfan.Utilities.ConfigSections;
import me.libraryaddictfan.Utilities.ConfigUtils;
import me.libraryaddictfan.Utilities.Display;
import me.libraryaddictfan.Utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class HyperAxe extends Legendary {
  private HashMap<String, Long> cooldown;

  private HashMap<LivingEntity, Integer> toRemove;

  public static int DAMAGE_TICKS;

  public HyperAxe(ClansLegendaries clanslegendariess, ItemStack item, String name, int damage) {
    super(clanslegendariess, item, name,
        Arrays.asList(new String[] { Utils.white("Of all the weapons known to man,"),
            Utils.white("none is more prevalant than the"), Utils.white("Hyper Axe. Infused with rabbit's"),
            Utils.white("speed and pigman's ferocity, this"), Utils.white("blade can rip through any opponent."), "",
            Utils.white("Hit delay is reduced by " + Utils.yellow("50%", false)),
            Utils.white("Deals " + Utils.yellow("3 Damage", true) + " with attack"),
            String.valueOf(Utils.yellow("Right-Click", true)) + " to use " + Utils.green("Dash", false) }),
        damage);
    this.cooldown = new HashMap<>();
    this.toRemove = new HashMap<>();
  }

  public void rel() {
    DAMAGE_TICKS = ConfigUtils.getIntegerSection(ConfigSections.HYPERAXE_DAMAGE_DELAY);
    setDamage(ConfigUtils.getIntegerSection(ConfigSections.HYPERAXE_DAMAGE));
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onNormalDmg(EntityDamageEvent e) {
    if (e.isCancelled())
      return;
    if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
        e.getEntity() instanceof LivingEntity) {
      LivingEntity ent = (LivingEntity) e.getEntity();
      if (this.toRemove.containsKey(ent)) {
        ent.setMaximumNoDamageTicks(20);
        this.toRemove.remove(ent);
      }
    }
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onDmg(EntityDamageByEntityEvent e) {
    if (e.isCancelled())
      return;
    if (e.getDamager() instanceof Player) {
      Player p = (Player) e.getDamager();
      ItemStack item = Utils.getItemInHand(p);
      if (isCorrectItem(item)) {
        if (e.getEntity() instanceof LivingEntity) {
          if (e.getEntity() instanceof Player && (((Player) e.getEntity()).getGameMode() == GameMode.CREATIVE
              || ((Player) e.getEntity()).getGameMode() == GameMode.SPECTATOR))
            return;
          LivingEntity entLiv = (LivingEntity) e.getEntity();
          entLiv.setMaximumNoDamageTicks(DAMAGE_TICKS);
          entLiv.setVelocity(new Vector(0.0D, 0.12D, 0.0D));
          this.toRemove.put(entLiv, Integer.valueOf(7));
        }
        e.setDamage(getDamage());
      } else if (e.getEntity() instanceof LivingEntity) {
        LivingEntity entLiv = (LivingEntity) e.getEntity();
        if (this.toRemove.containsKey(e.getEntity())) {
          entLiv.setMaximumNoDamageTicks(20);
          this.toRemove.remove(e.getEntity());
        }
      }
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
      if (p.getLocation().getBlock().isLiquid()) {
        p.sendMessage(ChatColor.BLUE + getName() + ChatColor.BLUE +
            "> " + ChatColor.GRAY + "You cannot use " +
            ChatColor.GREEN + getName() + ChatColor.GRAY +
            " in water.");
        return;
      }
      if (this.cooldown.containsKey(p.getName())) {
        Double x = Double.valueOf(16.0D - Math.pow(10.0D, -1.0D)
            * ((System.currentTimeMillis() - ((Long) this.cooldown.get(p.getName())).longValue()) / 100L));
        String[] zz = x.toString().replace('.', '-').split("-");
        String concat = String.valueOf(zz[0]) + "." + zz[1].substring(0, 1);
        try {
          p.sendMessage(ChatColor.BLUE + getName() + "> " + ChatColor.GRAY +
              "Your cannot use " + ChatColor.GREEN + "Hyper Rush" + ChatColor.GRAY +
              " for " + ChatColor.GREEN +
              concat + " Seconds");
        } catch (IndexOutOfBoundsException exc) {
          Bukkit.getServer().getLogger().warning("Index out of bounds in Hyper Axe msg. Should have been canceled");
        }
        return;
      }
      this.cooldown.put(p.getName(), Long.valueOf(System.currentTimeMillis()));
      p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2));
      p.sendMessage(ChatColor.BLUE + getName() + "> " + ChatColor.GRAY + "You used " + ChatColor.GREEN +
          "Hyper Rush");
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    if (this.toRemove.containsKey(e.getPlayer())) {
      this.toRemove.remove(e.getPlayer());
      e.getPlayer().setMaximumNoDamageTicks(20);
    } else {
      e.getPlayer().setMaximumNoDamageTicks(20);
    }
  }

  @EventHandler
  public void onDeath(EntityDeathEvent e) {
    LivingEntity livingEntity = e.getEntity();
    if (this.toRemove.containsKey(livingEntity)) {
      if (livingEntity instanceof LivingEntity)
        livingEntity.setMaximumNoDamageTicks(20);
      this.toRemove.remove(livingEntity);
    }
  }

  public void loop() {
    ArrayList<LivingEntity> remoeee = new ArrayList<>();
    for (LivingEntity ent : this.toRemove.keySet()) {
      if (ent.isDead())
        continue;
      if (((Integer) this.toRemove.get(ent)).intValue() > 0) {
        if (this.toRemove.containsKey(ent))
          this.toRemove.put(ent, Integer.valueOf(((Integer) this.toRemove.get(ent)).intValue() - 1));
        continue;
      }
      ent.setMaximumNoDamageTicks(20);
      if (this.toRemove.containsKey(ent))
        remoeee.add(ent);
    }
    for (LivingEntity entt : remoeee)
      this.toRemove.remove(entt);
    for (String s : this.cooldown.keySet()) {
      Player p = Bukkit.getServer().getPlayer(s);
      if (p == null || !p.isOnline()) {
        this.cooldown.remove(s);
        continue;
      }
      if ((System.currentTimeMillis() - ((Long) this.cooldown.get(s)).longValue()) / 1000L > 15L) {
        this.cooldown.remove(s);
        p.sendMessage(ChatColor.BLUE + getName() + ChatColor.BLUE + "> " + ChatColor.GRAY +
            "You can use " + ChatColor.GREEN + "Hyper Rush");
        if (isCorrectItem(Utils.getItemInHand(p)))
          Display.display(ChatColor.GREEN + "Hyper Rush Recharged", p);
        if (Utils.is1_8()) {
          p.playSound(p.getLocation(), Sound.valueOf("NOTE_PLING"), 5.0F, 1.0F);
          continue;
        }
        p.playSound(p.getLocation(), Sound.valueOf("BLOCK_NOTE_PLING"), 5.0F, 1.0F);
        continue;
      }
      if (isCorrectItem(Utils.getItemInHand(p))) {
        Double x = Double.valueOf(16.0D - Math.pow(10.0D, -1.0D)
            * ((System.currentTimeMillis() - ((Long) this.cooldown.get(p.getName())).longValue()) / 100L));
        double divide = (System.currentTimeMillis() - ((Long) this.cooldown.get(s)).longValue()) / 16000.0D;
        String[] zz = x.toString().replace('.', '-').split("-");
        String concat = String.valueOf(zz[0]) + "." + zz[1].substring(0, 1);
        Display.displayProgress("Rush", divide, 0,
            ChatColor.WHITE + " " + concat + " Seconds", false, new Player[] { p });
      }
    }
  }

  public void quit(Player player) {
  }

  public void clearMem() {
    this.toRemove.clear();
    this.cooldown.clear();
  }
}
