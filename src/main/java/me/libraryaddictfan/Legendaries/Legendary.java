package me.libraryaddictfan.Legendaries;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import me.libraryaddictfan.ClansLegendaries;
import me.libraryaddictfan.Utilities.Utils;

public abstract class Legendary implements Listener {
  private ItemStack item;

  private String name;

  private List<String> lore;

  private int damage;

  private ClansLegendaries clanslegendaries;

  public Legendary() {
  }

  public Legendary(ClansLegendaries clanslegendariess, ItemStack itemm, String namee, List<String> loree, int damagee) {
    Bukkit.getServer().getPluginManager().registerEvents(this, (Plugin) clanslegendariess);
    this.clanslegendaries = clanslegendariess;
    this.item = itemm;
    this.name = namee;
    this.lore = loree;
    this.damage = damagee;
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    if (e.getPlayer().getInventory().contains(getFullItem()))
      quit(e.getPlayer());
  }

  public abstract void quit(Player paramPlayer);

  public ItemStack getItem() {
    return this.item;
  }

  public abstract void rel();

  public String specialHeader() {
    return ChatColor.BLUE + this.name + "> " + ChatColor.GRAY;
  }

  public boolean isCorrectItem(ItemStack item) {
    String meta;
    if (item == null)
      return false;
    try {
      meta = item.getItemMeta().getDisplayName();
    } catch (NullPointerException e) {
      return false;
    }
    if (meta == null)
      return false;
    if (item.getType() == getItem().getType() && meta.equals(ChatColor.GOLD + getName()))
      return true;
    return false;
  }

  public String getName() {
    return this.name;
  }

  public List<String> getLore() {
    return (this.lore != null) ? this.lore : Arrays.<String>asList(new String[] { "" });
  }

  public void setLore(List<String> loree) {
    this.lore = loree;
  }

  public ItemStack getFullItem() {
    ItemStack item = new ItemStack(getItem().getType());
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(ChatColor.GOLD + getName());
    meta.setLore(getLore());
    List<String> list = meta.getLore();
    meta.setLore(list);
    if (getName().contains("Breeze Blade")) {
      meta.setCustomModelData(1);
    }
    if (getName().contains("Magnetic Maul")) {
      meta.setCustomModelData(2);
    }
    if (getName().contains("Alligators Tooth")) {
      meta.setCustomModelData(3);
    }
    item.setItemMeta(meta);
    return item;
  }

  public ClansLegendaries getMain() {
    return this.clanslegendaries;
  }

  public int getDamage() {
    return this.damage;
  }

  public void setDamage(int damagee) {
    this.damage = damagee;
  }

  public abstract void loop();

  public boolean isHandCorrect(Player p, PlayerInteractEvent e) {
    if (Utils.is1_8())
      return true;
    return !(Utils.getAndInvokeMethod(PlayerInteractEvent.class, "getHand", new Class[0], e,
        new Object[0]) != EquipmentSlot.HAND);
  }

  public abstract void clearMem();
}
