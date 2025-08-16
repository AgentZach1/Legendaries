package me.libraryaddictfan.Legendaries;

import java.util.Arrays;
import java.util.HashMap;
import me.libraryaddictfan.ClansLegendaries;
import me.libraryaddictfan.Utilities.Display;
import me.libraryaddictfan.Utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RunedPickaxe extends Legendary {
  private HashMap<String, Long> cooldown;

  private HashMap<String, Long> instantMining;

  public RunedPickaxe(ClansLegendaries clanslegendariess, ItemStack item, String name, int damage) {
    super(clanslegendariess, item, name,
        Arrays.asList(
            new String[] { Utils.white("What an interesting design this"), Utils.white("pickaxe seems to have!"), "",
                Utils.white("Deals " + Utils.yellow("3 Damage", true) + " with attack"),
                String.valueOf(Utils.yellow("Right-Click", true)) + " to use " + Utils.green("Instant", false),
                Utils.green("Mine", false) }),
        damage);
    this.cooldown = new HashMap<>();
    this.instantMining = new HashMap<>();
  }

  public void rel() {
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBlockMine(BlockBreakEvent e) {
    if (e.isCancelled())
      return;
    Player p = e.getPlayer();
    Block b = e.getBlock();
    if (isCorrectItem(Utils.getItemInHand(p)))
      b.breakNaturally();
  }

  @EventHandler
  public void onClick(PlayerInteractEvent e) {
    Player p = e.getPlayer();
    ItemStack item = Utils.getItemInHand(p);
    if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
      if (isCorrectItem(item)) {
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
          Double x = Double.valueOf(15.0D - Math.pow(10.0D, -1.0D)
              * ((System.currentTimeMillis() - ((Long) this.cooldown.get(p.getName())).longValue()) / 100L));
          String[] zz = x.toString().replace('.', '-').split("-");
          String concat = String.valueOf(zz[0]) + "." + zz[1].substring(0, 1);
          try {
            p.sendMessage(ChatColor.BLUE + getName() + "> " + ChatColor.GRAY +
                "Your cannot use " + ChatColor.GREEN + "Instant Mine" + ChatColor.GRAY +
                " for " + ChatColor.GREEN +
                concat + " Seconds");
          } catch (IndexOutOfBoundsException exc) {
            Bukkit.getServer().getLogger()
                .warning("Index out of bounds in Runed Pickaxe msg. Should have been canceled");
          }
          return;
        }
        if (this.instantMining.containsKey(p.getName()))
          return;
        this.instantMining.put(p.getName(), Long.valueOf(System.currentTimeMillis()));
        p.removePotionEffect(PotionEffectType.HASTE);
        Display.displayTitleAndSubtitle(p, " ", ChatColor.WHITE + "Instant mine enabled for " + ChatColor.YELLOW +
            "12 Seconds", 5, 30, 5);
        p.sendMessage(ChatColor.BLUE + getName() + "> " + ChatColor.GRAY + "You used " + ChatColor.GREEN +
            "Instant Mine");
      }
    } else if (e.getAction() == Action.LEFT_CLICK_BLOCK &&
        isCorrectItem(item)) {
      if (!Utils.is1_8())
        if (!isHandCorrect(e.getPlayer(), e))
          return;
      if (this.instantMining.containsKey(p.getName())) {
        if (e.isCancelled())
          return;
        BlockBreakEvent newEvent = new BlockBreakEvent(e.getClickedBlock(), e.getPlayer());
        Bukkit.getServer().getPluginManager().callEvent((Event) newEvent);
      }
    }
  }

  public void loop() {
    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
      if (!this.instantMining.containsKey(p.getName()))
        if (isCorrectItem(Utils.getItemInHand(p)))
          p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 80, 103));
    }
    for (String s : this.instantMining.keySet()) {
      Player p = Bukkit.getServer().getPlayer(s);
      if (p == null || !p.isOnline()) {
        this.instantMining.remove(s);
        continue;
      }
      if ((System.currentTimeMillis() - ((Long) this.instantMining.get(s)).longValue()) / 1000L > 11L) {
        this.instantMining.remove(s);
        this.cooldown.put(p.getName(), Long.valueOf(System.currentTimeMillis()));
        continue;
      }
      if (isCorrectItem(Utils.getItemInHand(p))) {
        double divide = (System.currentTimeMillis() - ((Long) this.instantMining.get(s)).longValue()) / 12000.0D;
        Display.displayProgress("Mine", divide, 0,
            null, true, new Player[] { p });
      }
    }
    for (String s : this.cooldown.keySet()) {
      Player p = Bukkit.getServer().getPlayer(s);
      if (p == null || !p.isOnline()) {
        this.cooldown.remove(s);
        continue;
      }
      if ((System.currentTimeMillis() - ((Long) this.cooldown.get(s)).longValue()) / 1000L > 14L) {
        this.cooldown.remove(s);
        p.sendMessage(ChatColor.BLUE + getName() + ChatColor.BLUE + "> " + ChatColor.GRAY +
            "You can use " + ChatColor.GREEN + "Instant Mine");
        if (isCorrectItem(Utils.getItemInHand(p)))
          Display.display(ChatColor.GREEN + "Instant Mine Recharged", p);
        if (Utils.is1_8()) {
          p.playSound(p.getLocation(), Sound.valueOf("NOTE_PLING"), 5.0F, 1.0F);
          continue;
        }
        p.playSound(p.getLocation(), Sound.valueOf("BLOCK_NOTE_PLING"), 5.0F, 1.0F);
        continue;
      }
      if (isCorrectItem(Utils.getItemInHand(p))) {
        Double x = Double.valueOf(15.0D - Math.pow(10.0D, -1.0D)
            * ((System.currentTimeMillis() - ((Long) this.cooldown.get(p.getName())).longValue()) / 100L));
        double divide = (System.currentTimeMillis() - ((Long) this.cooldown.get(s)).longValue()) / 15000.0D;
        String[] zz = x.toString().replace('.', '-').split("-");
        String concat = String.valueOf(zz[0]) + "." + zz[1].substring(0, 1);
        Display.displayProgress("Mine", divide, 0,
            ChatColor.WHITE + " " + concat + " Seconds", false, new Player[] { p });
      }
    }
  }

  public void quit(Player player) {
  }

  public void clearMem() {
    this.cooldown.clear();
    this.instantMining.clear();
  }
}
