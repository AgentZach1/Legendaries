package me.libraryaddictfan;

import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

import org.bukkit.entity.*;
import org.bukkit.entity.EntityType;
import me.libraryaddictfan.Legendaries.AlligatorsTooth;
import me.libraryaddictfan.Legendaries.GiantsBroadsword;
import me.libraryaddictfan.Legendaries.HyperAxe;
import me.libraryaddictfan.Legendaries.Legendary;
import me.libraryaddictfan.Legendaries.MagneticMaul;
import me.libraryaddictfan.Legendaries.MeridianScepter.MeridianScepter;
import me.libraryaddictfan.Legendaries.RunedPickaxe;
import me.libraryaddictfan.Legendaries.ScytheOfTheFallenLord;
import me.libraryaddictfan.Legendaries.BreezeBlade;
import me.libraryaddictfan.Utilities.ConfigSections;
import me.libraryaddictfan.Utilities.ConfigUtils;
import me.libraryaddictfan.Utilities.Display;
import me.libraryaddictfan.Utilities.Utils;
import me.libraryaddictfan.commands.LegendariesCommand;
import me.libraryaddictfan.commands.LegendariesCommandTabComplete;
import me.libraryaddictfan.commands.LegendaryCommand;
import me.libraryaddictfan.commands.LegendaryCommandTabComplete;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ClansLegendaries extends JavaPlugin implements Listener {
  private ArrayList<Legendary> legendaries;

  public static Permission legendaryCommandUse = new Permission("clanslegendaries.legendary-command.use",
      "Use the legendary command.");

  public static Permission legendaryCommandGive = new Permission("clanslegendaries.legendary-command.giveLegendary",
      "Give a legendary to another player.");

  public static Permission legendariesCommandUse = new Permission("clanslegendaries.legendaries-command.use",
      "Use the legendaries command");

  public static Permission legendariesCommandHelp = new Permission("clanslegendaries.legendaries-command.help",
      "Use the help feature of the legendaries command");

  public static Permission legendariesCommandSetSection = new Permission(
      "clanslegendaries.legendaries-command.setSection", "Use the setSection feature of the legendaries command");

  public static Permission legendariesCommandTestConfig = new Permission(
      "clanslegendaries.legendaries-command.testConfig", "Use the testConfig feature of the legendaries command");

  public static Permission legendariesCommandReloadConfig = new Permission(
      "clanslegendaries.legendaries-command.reloadConfig", "Use the reloadConfig feature of the legendaries command");

  public static Permission legendariesCommandFixHyperDelay = new Permission(
      "clanslegendaries.legendaries-command.fixHyperDelay", "Use the fixHyperDelay feature of the legendaries command");

  public void onEnable() {
    saveDefaultConfig();
    getConfig().options().header("Default settings: false, 0.66, true, 7 (in ticks), 4.");
    getConfig().options().copyHeader(true);
    ConfigUtils.register(this);
    legendaryCommandUse.setDefault(PermissionDefault.OP);
    // registerPermission(legendaryCommandUse, PermissionDefault.OP);
    // registerPermission(legendaryCommandGive, PermissionDefault.OP);
    registerPermission(legendariesCommandUse, PermissionDefault.OP);
    registerPermission(legendariesCommandHelp, PermissionDefault.OP);
    registerPermission(legendariesCommandSetSection, PermissionDefault.OP);
    registerPermission(legendariesCommandTestConfig, PermissionDefault.OP);
    registerPermission(legendariesCommandReloadConfig, PermissionDefault.OP);
    registerPermission(legendariesCommandFixHyperDelay, PermissionDefault.OP);
    this.legendaries = new ArrayList<>();
    // getCommand("legendaries").setExecutor((CommandExecutor) new LegendariesCommand(this));
    // getCommand("legendaries").setTabCompleter((TabCompleter) new LegendariesCommandTabComplete());
    // getCommand("legendary").setExecutor((CommandExecutor) new LegendaryCommand(this));
    // getCommand("legendary").setTabCompleter((TabCompleter) new LegendaryCommandTabComplete(this));
    // getCommand("legendary").setAliases(Arrays.asList(new String[] { "leg" }));
    Bukkit.getServer().getPluginManager().registerEvents(this, (Plugin) this);
    Bukkit.getPluginManager().registerEvents(new MobDeathListener(this), this);
    registerLeggies();
    Bukkit.getServer().getLogger().info(ChatColor.GOLD + "[ClansLegendaries] " +
        "This plugin is best used on version " + ChatColor.GREEN +
        "1.21.3");
    // assignBuffyToMatchingEvokers();
    Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin) this, new Runnable() {
      public void run() {
        for (Legendary leg : ClansLegendaries.this.legendaries)
          leg.loop();
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
          // if (p.hasPotionEffect(PotionEffectType.CONDUIT_POWER) &&
          //     (p.getInventory().getItemInMainHand().getType() != Material.MUSIC_DISC_CAT 
          //     || p.getInventory().getItemInOffHand().getType() != Material.MUSIC_DISC_CAT))
          //   if (Utils.is1_8()) {
          //     try {
          //       if (Utils.getPotionEffect(p, PotionEffectType.CONDUIT_POWER).getAmplifier() == 1)
          //         p.removePotionEffect(PotionEffectType.CONDUIT_POWER);
          //     } catch (NullPointerException nullPointerException) {
          //     }
          //   } else if (((PotionEffect) Utils.getAndInvokeMethod(LivingEntity.class, "getPotionEffect",
          //       new Class[] { PotionEffectType.class }, p, new Object[] { PotionEffectType.CONDUIT_POWER }))
          //       .getAmplifier() == 1) {
          //     p.removePotionEffect(PotionEffectType.CONDUIT_POWER);
          //   }
          if (p.hasPotionEffect(PotionEffectType.HASTE) &&
              Utils.getItemInHand(p).getType() != Material.MUSIC_DISC_CHIRP) {
            if (Utils.is1_8()) {
              try {
                if (Utils.getPotionEffect(p, PotionEffectType.HASTE).getAmplifier() == 103)
                  p.removePotionEffect(PotionEffectType.HASTE);
              } catch (NullPointerException nullPointerException) {
              }
              continue;
            }
            if (((PotionEffect) Utils.getAndInvokeMethod(LivingEntity.class, "getPotionEffect",
                new Class[] { PotionEffectType.class }, p, new Object[] { PotionEffectType.HASTE }))
                .getAmplifier() == 103)
              p.removePotionEffect(PotionEffectType.HASTE);
          }
          ItemStack mainhand = p.getInventory().getItemInMainHand();
          boolean legMain = false;
          ItemStack offhand = p.getInventory().getItemInOffHand();
          boolean legOff = false;
          for (Legendary leg : ClansLegendaries.this.legendaries) {
            if (leg.isCorrectItem(mainhand)) {
              legMain = true;

            }
            if (leg.isCorrectItem(offhand)) {
              legOff = true;
            }
            if (legOff && legMain) {
              break;
            }
          }
          if (mainhand.getType() != Material.AIR && offhand.getType() != Material.AIR
              && mainhand.getType() == Material.MUSIC_DISC_CAT && legMain
              && offhand.getType() == Material.MUSIC_DISC_CAT && legOff) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 1, true, true));
            Display.displaySubTitle(p, ChatColor.RED + "Your greed for power drains you", 1, 2, 1);
          }
        }
      }
    }, 2L, 0L);
    for (Legendary leg : this.legendaries)
      leg.rel();
  }

  /**
   * Registers a permission if it is not already registered.
   */
  private void registerPermission(Permission permission, PermissionDefault defaultValue) {
    if (!Bukkit.getPluginManager().getPermissions().contains(permission)) {
      permission.setDefault(defaultValue);
      Bukkit.getPluginManager().addPermission(permission);
    }
  }

  public void onDisable() {
    saveConfig();
    Utils.clearCaches();
    if (this.legendaries == null)
      return;
    for (Legendary l : this.legendaries)
      l.clearMem();
  }

  public ArrayList<Legendary> getLegendaries() {
    return this.legendaries;
  }

  private void registerLeggies() {
    AlligatorsTooth tooth = new AlligatorsTooth(this, new ItemStack(Material.MUSIC_DISC_CAT), "Alligators Tooth", 7);
    GiantsBroadsword broad = new GiantsBroadsword(this, new ItemStack(Material.MUSIC_DISC_MELLOHI), "Giants Broadsword",
        10);
    BreezeBlade blade = new BreezeBlade(this, new ItemStack(Material.MUSIC_DISC_CAT), "Breeze Blade", 7);
    MeridianScepter scep = new MeridianScepter(this, new ItemStack(Material.MUSIC_DISC_MALL), "Meridian Scepter", 3);
    HyperAxe hyper = new HyperAxe(this, new ItemStack(Material.MUSIC_DISC_BLOCKS), "Hyper Axe",
        ((Integer) ConfigSections.HYPERAXE_DAMAGE.getDefaultValue()).intValue());
    MagneticMaul maul = new MagneticMaul(this, new ItemStack(Material.MUSIC_DISC_CAT), "Magnetic Maul", 8);
    RunedPickaxe pick = new RunedPickaxe(this, new ItemStack(Material.MUSIC_DISC_CHIRP), "Runed Pickaxe", 1);
    ScytheOfTheFallenLord scythe = new ScytheOfTheFallenLord(this, new ItemStack(Material.MUSIC_DISC_STAL),
        "Scythe of the Fallen Lord", 8);
    this.legendaries.add(blade);
    this.legendaries.add(scep);
    this.legendaries.add(hyper);
    this.legendaries.add(maul); // Turn into
    this.legendaries.add(broad);
    this.legendaries.add(scythe);
    this.legendaries.add(tooth);
    this.legendaries.add(pick);
    // Add MagmaRider -> Have to add netherite to tooth and then take 1 lava dmg to
    // activate it... Or found in a nether boss idk
  }

  // /**
  // * Checks all loaded entities and assigns Buffy behavior to matching evokers.
  // */
  // private void assignBuffyToMatchingEvokers() {
  // for (World world : Bukkit.getWorlds()) {
  // for (Entity entity : world.getEntities()) {
  // if (entity.getEntityType() == EntityType.EVOKER) {
  // Bukkit.getLogger().info("Found an evoker!...");
  // if (isBuffyValid(evoker)) {
  // Bukkit.getLogger().info("Found a matching Buffy evoker! Assigning
  // behavior...");
  // new me.libraryaddictfan.boss.Buffy.Buffy(evoker, this); // Assign Buffy
  // behavior
  // }
  // }
  // }
  // }
  // }

  // /**
  // * Checks if an evoker matches Buffy's criteria.
  // */
  // private boolean isBuffyValid(Evoker evoker) {
  // Bukkit.getLogger().info("Checking if evoker is Buffy. Main");
  // if (evoker == null)
  // return false;

  // // Check custom name
  // if (evoker.getCustomName() == null || !evoker.getCustomName().equals("Buffy
  // the Vampire")) {
  // return false;
  // }

  // // Check scale or health attribute
  // // AttributeInstance maxHealth = evoker.getAttribute(Attribute.MAX_HEALTH);
  // return true; // Adjust according to your summon command
  // }
}
