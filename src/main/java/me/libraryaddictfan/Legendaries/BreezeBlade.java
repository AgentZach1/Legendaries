package me.libraryaddictfan.Legendaries;

import java.util.Arrays;
import java.util.HashMap;
import me.libraryaddictfan.ClansLegendaries;
import me.libraryaddictfan.Utilities.ConfigSections;
import me.libraryaddictfan.Utilities.ConfigUtils;
import me.libraryaddictfan.Utilities.Display;
import me.libraryaddictfan.Utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class BreezeBlade extends Legendary {
  public static final double MAX_COUNTS = 5.0D;

  public static double VEL_MULT;

  public static final double CHARGE_COST = 0.02D;

  public static boolean isInfinite = false;

  public static final Object enumConst = Utils.getEnumConstant(Utils.getNmsClass("EnumParticle"), "CLOUD");

  private HashMap<String, Float> charges;

  private HashMap<String, Float> launchCharge;

  private HashMap<String, Float> groundCounts;

  private HashMap<String, Long> cooldown;

  private HashMap<String, Float> smoother;

  private HashMap<String, Vector> veccs;

  public BreezeBlade(ClansLegendaries main, ItemStack itemm, String namee, int damage) {
    super(main, itemm, namee,
        Arrays.asList(new String[] { Utils.white("Long ago, a race of cloud dwellers"),
            Utils.white("terrorized the skies. This airy blade is"),
            Utils.white(" evidence of their tyranny. It is said that "),
            Utils.white("a million Breeze were slaughtered "),
            Utils.white("to construct the Breeze Blade."), "",
            String.valueOf(Utils.yellow("Right-Click", true)) + " to use " + Utils.green("Fly", false) }),
        damage);
    this.charges = new HashMap<>();
    this.launchCharge = new HashMap<>();
    this.groundCounts = new HashMap<>();
    this.cooldown = new HashMap<>();
    this.smoother = new HashMap<>();
    this.veccs = new HashMap<>();
    VEL_MULT = ConfigUtils.getDoubleSection(ConfigSections.BREEZEBLADE_VELOCITY);
    isInfinite = ConfigUtils.getBooleanSection(ConfigSections.BREEZEBLADE_INFINITE);
  }

  public void rel() {
    isInfinite = ConfigUtils.getBooleanSection(ConfigSections.BREEZEBLADE_INFINITE);
    VEL_MULT = ConfigUtils.getDoubleSection(ConfigSections.BREEZEBLADE_VELOCITY);
  }

  @EventHandler
  public void onDmg(EntityDamageByEntityEvent e) {
    if (e.getDamager() instanceof Player) {
      Player p = (Player) e.getDamager();
      ItemStack item = Utils.getItemInHand(p);
      if (isCorrectItem(item)) {
        double baseDamage = getDamage(); // Breeze Blade's base damage
        double originalDamage = e.getDamage(); // The damage calculated by Minecraft

        // Add the base damage to the original damage
        e.setDamage(originalDamage + baseDamage);
      }
    }
  }

  @EventHandler
  public void dmg(EntityDamageEvent e) {
    if (e.getEntity() instanceof Player) {
      Player p = (Player) e.getEntity();
      ItemStack mainHandItem = p.getInventory().getItemInMainHand();
      ItemStack offHandItem = p.getInventory().getItemInOffHand();
      if (isCorrectItem(mainHandItem) || isCorrectItem(offHandItem))
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
          p.spawnParticle(Particle.CLOUD, p.getLocation(), 10, 0.2, 0.4, 0.2, 0.02);
          e.setCancelled(true);
        }

    }
  }

  public void onUpdate(Player p) {
    if (!this.charges.containsKey(p.getName()))
      this.charges.put(p.getName(), Float.valueOf(0.0F));
    if (!this.launchCharge.containsKey(p.getName()))
      this.launchCharge.put(p.getName(), 0.0F);
    boolean isWaterlogged = p.getLocation().getBlock().isLiquid() || isWaterlogged(p.getLocation().getBlock());
    if (Utils.onGround(p) && !isWaterlogged)
      Charge(p);
    if (!this.veccs.containsKey(p.getName()))
      this.veccs.put(p.getName(), null);
    if (!this.smoother.containsKey(p.getName())) {
      this.smoother.put(p.getName(), Float.valueOf(0.0F));
    } else if (this.smoother.get(p.getName()) != null && ((Float) this.smoother.get(p.getName())).floatValue() > 0.0F) {
      if (this.veccs.get(p.getName()) != null) {
        if (!isInfinite)
          this.charges.put(p.getName(),
              Float.valueOf((float) Math.max(0.0D, GetCharge(p) - 0.02D)));
        p.setVelocity(this.veccs.get(p.getName()));
      }
    } else if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) {
      p.setAllowFlight(true);
    } else {
      p.setAllowFlight(false);
    }
    if (!this.groundCounts.containsKey(p.getName()))
      this.groundCounts.put(p.getName(), Float.valueOf(0.0F));
    this.groundCounts.put(p.getName(),
        Float.valueOf(Math.max(0.0F, ((Float) this.groundCounts.get(p.getName())).floatValue() - 0.15F)));
    this.smoother.put(p.getName(),
        Float.valueOf((float) Math.max(0.0D, ((Float) this.smoother.get(p.getName())).floatValue() - 0.5D)));

    ItemStack mainHandItem = p.getInventory().getItemInMainHand();
    ItemStack offHandItem = p.getInventory().getItemInOffHand();
    float charge = GetCharge(p);
    // SIT: Maintain Y value while crouching
    if (p.isSneaking() && (isCorrectItem(mainHandItem) || isCorrectItem(offHandItem)) && !Utils.onGround(p)
        && charge > 0.01F) {
      p.setVelocity(new Vector(p.getVelocity().getX(), 0, p.getVelocity().getZ())); // Stop downward movement
      p.spawnParticle(Particle.CLOUD, p.getLocation(), 5, 0.2, 0.2, 0.2, 0.02); // Hover effect
      if (!isInfinite && !isWaterlogged)
        this.charges.put(p.getName(),
            Math.max(0.0F, this.charges.get(p.getName()) - 0.005F)); // Slow depletion
    }
    // LAUNCH
    float lC = this.launchCharge.getOrDefault(p.getName(), 0.0F);
    if (!p.isSneaking() && lC > 0.0F) {
      // apply velocity
      // Apply cumulative velocity based on charge
      Vector launchVector = p.getLocation().getDirection().normalize().multiply(lC * 5);
      p.setVelocity(launchVector);

      // Reset launch charge
      this.launchCharge.put(p.getName(), 0.0F);

      // Play effects
      p.getWorld().playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1, 1);
      p.spawnParticle(Particle.EXPLOSION, p.getLocation(), 1);

      this.cooldown.put(p.getName(), System.currentTimeMillis());
    }
    float onGroundCharge = Math.min(charge, this.groundCounts.get(p.getName()) / (float) MAX_COUNTS);

    if (this.cooldown.containsKey(p.getName())) {
      Display.displayProgress(ChatColor.YELLOW + "Cooling Down", 0, charge, null, true, p);
      p.spawnParticle(Particle.DUST_PLUME, p.getLocation(), 4, 0.2, 0.2, 0.2, 0.02);
      p.spawnParticle(Particle.SMOKE, p.getLocation(), 1, 0.2, 0.2, 0.2, 0);
    } else if (this.launchCharge.getOrDefault(p.getName(), 0.0F) >= 0.85F) {
      Display.displayProgress(ChatColor.AQUA + "Ready To Launch!", charge, this.launchCharge.get(p.getName()),
          null, false, p);
    } else if (this.launchCharge.getOrDefault(p.getName(), 0.0F) > 0) {
      Display.displayProgress(ChatColor.AQUA + "Launch Charging", charge, this.launchCharge.get(p.getName()),
          null, false, p);

    } else {
      Display.displayProgress(null, charge, onGroundCharge, null, false, p);
    }

    // if (GetCharge(p) == 0.0F) {
    // Display.displayProgress(null, 0.0D, null, false, new Player[] { p });
    // } else {
    // Display.displayProgress(null, GetCharge(p), null, false, new Player[] { p });
    // }
  }

  public boolean Charge(Player player) {
    if (player.getLocation().getBlock().isLiquid() || isWaterlogged(player.getLocation().getBlock()))
      return false; // Prevent charging in waterlogged blocks
    if (!this.charges.containsKey(player.getName()))
      this.charges.put(player.getName(), Float.valueOf(0.0F));
    float charge = ((Float) this.charges.get(player.getName())).floatValue();
    charge = (float) Math.min(1.0D, charge + 0.01D);
    this.charges.put(player.getName(), Float.valueOf(charge));
    float onGroundCharge = this.groundCounts.getOrDefault(player.getName(), 0.0F) / (float) MAX_COUNTS;
    // Display.displayProgress(null, charge, onGroundCharge, null, false, player);
    // Display.displayProgress(null, charge, null, false, new Player[] { player });
    return (charge >= 1.0F);
  }

  public float GetCharge(Player player) {
    if (!this.charges.containsKey(player.getName()))
      this.charges.put(player.getName(), Float.valueOf(0.0F));
    return ((Float) this.charges.get(player.getName())).floatValue();
  }

  @EventHandler
  public void onClick(PlayerInteractEvent e) {
    Player p = e.getPlayer();
    ItemStack item = Utils.getItemInHand(p);
    ItemStack mainHandItem = p.getInventory().getItemInMainHand();
    ItemStack offHandItem = p.getInventory().getItemInOffHand();
    if ((e.getAction() == Action.RIGHT_CLICK_AIR ||
        e.getAction() == Action.RIGHT_CLICK_BLOCK) &&
        (isCorrectItem(mainHandItem) || isCorrectItem(offHandItem))) {
      // if (!Utils.is1_8())
      // if (!isHandCorrect(e.getPlayer(), e))
      // return;
      if (p.getLocation().getBlock().isLiquid() || isWaterlogged(p.getLocation().getBlock())) {
        p.sendMessage(ChatColor.BLUE + getName() + ChatColor.BLUE +
            "> " + ChatColor.GRAY + "You cannot use " +
            ChatColor.GREEN + "Wind Rider" + ChatColor.GRAY +
            " in water.");
        return;
      }
      if (GetCharge(p) <= 0.0F)
        return;
      if (this.cooldown.containsKey(p.getName())) {
        Double x = Double.valueOf(3.0D - Math.pow(10.0D, -1.0D)
            * ((System.currentTimeMillis() - ((Long) this.cooldown.get(p.getName())).longValue()) / 100L));
        String[] zz = x.toString().replace('.', '-').split("-");
        String concat = String.valueOf(zz[0]) + "." + zz[1].substring(0, 1);
        try {
          p.sendMessage(ChatColor.BLUE + getName() + "> " + ChatColor.GRAY +
              "Your flight powers will recharge in " + ChatColor.GREEN +
              concat + " Seconds");
        } catch (IndexOutOfBoundsException exc) {
          Bukkit.getServer().getLogger().warning("Index out of bounds in Breeze Blade msg. Should have been canceled");
        }
        return;
      }
      if (p.isSneaking()) {
        // Start charging launch
        float currentCharge = this.charges.getOrDefault(p.getName(), 0.0F);
        float launchCharge = this.launchCharge.getOrDefault(p.getName(), 0.0F);

        if (currentCharge > 0) {
          float chargeAmount = Math.min(currentCharge, 0.08F); // Transfer charge
          this.launchCharge.put(p.getName(), Math.min(1.0F, launchCharge + chargeAmount));
          this.charges.put(p.getName(), Math.max(0.0F, currentCharge - chargeAmount));
          p.playSound(p.getLocation(), Sound.valueOf("BLOCK_NOTE_BLOCK_PLING"), 0.1F,
              this.launchCharge.getOrDefault(p.getName(), 0.0F) * 2.0F);
          p.spawnParticle(Particle.END_ROD, p.getLocation(), 5, 0.2, 0.2, 0.2, 0.01); // Charging effect

        }
      } else {
        // Normal Fly
        windLaunch(p);
      }
    }
  }

  @EventHandler
  public void onToggleFlight(PlayerToggleFlightEvent e) {
    ItemStack mainHandItem = e.getPlayer().getInventory().getItemInMainHand();
    ItemStack offHandItem = e.getPlayer().getInventory().getItemInOffHand();
    if ((e.getPlayer().getGameMode() == GameMode.SURVIVAL || e.getPlayer().getGameMode() == GameMode.ADVENTURE) &&
        (isCorrectItem(mainHandItem) || isCorrectItem(offHandItem))) {
      if (this.smoother.get(e.getPlayer().getName()) == null)
        return;
      if (((Float) this.smoother.get(e.getPlayer().getName())).floatValue() == 0.0F)
        return;
      // float launchCharge = this.launchCharge.getOrDefault(e.getPlayer().getName(),
      // 0.0F);

      // if (launchCharge > 0) {

      // }
      e.setCancelled(true); // Prevent normal flight toggle
    }
  }

  private void windLaunch(Player p) {
    Vector vec = p.getLocation().getDirection();
    boolean onGround = Utils.onGround(p);
    if (Double.isNaN(vec.getX()) || Double.isNaN(vec.getY()) ||
        Double.isNaN(vec.getZ()) || vec.length() == 0.0D)
      return;
    vec.normalize();
    vec.multiply(VEL_MULT);
    p.setVelocity(vec);
    // for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
    // if (pl.getLocation().distance(p.getLocation()) < 64.0D)

    // }
    if (!this.groundCounts.containsKey(p.getName())) {
      this.groundCounts.put(p.getName(), Float.valueOf(1.0F));
    } else {
      if (onGround) {
        this.groundCounts.put(p.getName(),
            Float.valueOf(((Float) this.groundCounts.get(p.getName())).floatValue() + 1.0F));
        p.playSound(p.getLocation(), Sound.valueOf("BLOCK_NOTE_BLOCK_SNARE"), 13.0F, 2.0F);
        doGroundParticles(p, (float) p.getLocation().getX(),
            (float) p.getLocation().getY() + 0.2F,
            (float) p.getLocation().getZ());
      } else {
        doParticles(p, (float) p.getLocation().getX(),
            (float) p.getLocation().getY() + 1.0F,
            (float) p.getLocation().getZ());
      }
      // if (Utils.is1_8()) {
      // p.playSound(p.getLocation(), Sound.valueOf("BLOCK_LAVA_POP"), 13.0F, 2.0F);
      // } else {
      // }
    }

    // if (!Utils.onGround(p)) {
    // for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
    // if (pl.getLocation().distance(p.getLocation()) < 64.0D)
    // doParticles(pl, (float) p.getLocation().getX(),
    // (float) p.getLocation().getY() + 1.0F,
    // (float) p.getLocation().getZ());
    // }
    // }
    Player paramPlayer = p;
    Vector paramVector = vec;
    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) getMain(), () -> {
      paramPlayer.setVelocity(paramVector);
      if (onGround) {
        paramPlayer.playSound(paramPlayer.getLocation(), Sound.valueOf("BLOCK_NOTE_BLOCK_SNARE"), 13.0F, 2.0F);
        doGroundParticles(paramPlayer, (float) p.getLocation().getX(),
            (float) p.getLocation().getY() + 0.2F,
            (float) p.getLocation().getZ());

        // for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
        // doGroundParticles(p, (float) p.getLocation().getX(),
        // (float) p.getLocation().getY() + 1.0F,
        // (float) p.getLocation().getZ());
        // }
        // if (Utils.is1_8()) {
        // paramPlayer.playSound(paramPlayer.getLocation(),
        // Sound.valueOf("BLOCK_LAVA_POP"), 13.0F, 2.0F);
        // } else {
        // paramPlayer.playSound(paramPlayer.getLocation(),
        // Sound.valueOf("BLOCK_NOTE_BLOCK_SNARE"), 13.0F, 2.0F);
        // }
      } else {
        doParticles(paramPlayer, (float) paramPlayer.getLocation().getX(),
            (float) paramPlayer.getLocation().getY() + 1.0F,
            (float) paramPlayer.getLocation().getZ());
        // for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
        // if (pl.getLocation().distance(paramPlayer.getLocation()) < 64.0D)
        // doParticles(pl, (float) paramPlayer.getLocation().getX(), (float)
        // paramPlayer.getLocation().getY() + 1.0F,
        // (float) paramPlayer.getLocation().getZ());
        // }
      }

    }, 2L);
    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) getMain(), () -> {
      paramPlayer.setVelocity(paramVector);
      if (onGround) {
        paramPlayer.playSound(paramPlayer.getLocation(), Sound.valueOf("BLOCK_NOTE_BLOCK_SNARE"), 13.0F, 2.0F);
        doGroundParticles(paramPlayer, (float) paramPlayer.getLocation().getX(),
            (float) paramPlayer.getLocation().getY() + 0.2F,
            (float) paramPlayer.getLocation().getZ());

        // for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
        // if (pl.getLocation().distance(paramPlayer.getLocation()) < 64.0D)
        // }
        // if (Utils.is1_8()) {
        // paramPlayer.playSound(paramPlayer.getLocation(),
        // Sound.valueOf("BLOCK_LAVA_POP"), 13.0F, 2.0F);
        // } else {
        // }
      } else {
        doParticles(paramPlayer, (float) paramPlayer.getLocation().getX(),
            (float) paramPlayer.getLocation().getY() + 1.0F,
            (float) paramPlayer.getLocation().getZ());
        // for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
        // if (pl.getLocation().distance(paramPlayer.getLocation()) < 64.0D)
        // doParticles(pl, (float) paramPlayer.getLocation().getX(), (float)
        // paramPlayer.getLocation().getY() + 1.0F,
        // (float) paramPlayer.getLocation().getZ());
        // }
      }

    }, 3L);
    if (((Float) this.groundCounts.get(p.getName())).floatValue() > 5.0D) {
      this.groundCounts.put(p.getName(), Float.valueOf(0.0F));
      p.sendMessage(ChatColor.BLUE + getName() + ChatColor.BLUE + "> " + ChatColor.GRAY +
          "Flight powers diminished whilst scraping the ground. Recharging in " + ChatColor.GREEN +
          "3.0 Seconds");
      p.playSound(p.getLocation(), Sound.valueOf("BLOCK_ANVIL_USE"), 3.0F, 2.0F);
      // if (Utils.is1_8()) {
      // p.playSound(p.getLocation(), Sound.valueOf("ANVIL_USE"), 3.0F, 2.0F);
      // } else {
      // p.playSound(p.getLocation(), Sound.valueOf("BLOCK_ANVIL_USE"), 3.0F, 2.0F);
      // }
      this.cooldown.put(p.getName(), Long.valueOf(System.currentTimeMillis()));
      return;
    }
    this.veccs.put(p.getName(), vec);
    if (!this.smoother.containsKey(p.getName()))
      this.smoother.put(p.getName(), Float.valueOf(0.0F));
    this.smoother.put(p.getName(),
        Float.valueOf(Math.min(5.0F, ((Float) this.smoother.get(p.getName())).floatValue() + 1.0F)));
    if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE)
      p.setAllowFlight(true);
    // if (Utils.is1_8()) {
    // p.getWorld().playSound(p.getLocation(),
    // Sound.valueOf("ENTITY_BREEZE_IDLE_GROUND"), 1.2F, 1.5F);
    // } else {
    // p.getWorld().playSound(p.getLocation(),
    // Sound.valueOf("ENTITY_BREEZE_IDLE_GROUND"), 1.2F, 1.5F);
    // }
    p.getWorld().playSound(p.getLocation(),
        Sound.valueOf("ENTITY_BREEZE_IDLE_GROUND"), 1.2F, 1.5F);
  }

  public void loop() {
    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
      doCoolDowns();

      ItemStack mainHandItem = p.getInventory().getItemInMainHand();
      ItemStack offHandItem = p.getInventory().getItemInOffHand();
      // ItemStack item = Utils.getItemInHand(p);
      if (isCorrectItem(mainHandItem) || isCorrectItem(offHandItem)) {
        onUpdate(p);
        continue;
      }
      memoryRemove(p, false);
    }
  }

  private void memoryRemove(Player p, boolean logged) {
    if (logged) {
      if (this.cooldown.containsKey(p.getName()))
        this.cooldown.remove(p.getName());
      if (this.charges.containsKey(p.getName()))
        this.charges.remove(p.getName());
    }
    if (this.veccs.containsKey(p.getName()))
      this.veccs.remove(p.getName());
    if (this.smoother.containsKey(p.getName())) {
      if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) {
        p.setAllowFlight(true);
        this.smoother.remove(p.getName());
      } else {
        p.setAllowFlight(false);
      }
      this.smoother.remove(p.getName());
    }
    if (this.groundCounts.containsKey(p.getName())) {
      if (logged) {
        if (this.groundCounts.containsKey(p.getName()))
          this.groundCounts.remove(p.getName());
        return;
      }
      if (!p.getInventory().contains(getFullItem()) &&
          this.groundCounts.containsKey(p.getName()))
        this.groundCounts.remove(p.getName());
    }
  }

  @EventHandler
  public void onDie(PlayerDeathEvent e) {
    Player p = e.getEntity();
    memoryRemove(p, true);
  }

  private void doCoolDowns() {
    for (String s : this.cooldown.keySet()) {
      if (System.currentTimeMillis() - ((Long) this.cooldown.get(s)).longValue() >= 3000L) {
        Player p = Bukkit.getServer().getPlayer(s);
        this.cooldown.remove(s);
        Player pl = Bukkit.getServer().getPlayer(s);
        pl.sendMessage(ChatColor.BLUE + getName() + ChatColor.BLUE + "> " +
            ChatColor.GRAY + "Your flight powers have replenished!");
        if (Utils.is1_8()) {
          p.playSound(p.getLocation(), Sound.valueOf("BLOCK_NOTE_BLOCK_PLING"), 5.0F, 2.0F);
          continue;
        }
        p.playSound(p.getLocation(), Sound.valueOf("BLOCK_NOTE_BLOCK_PLING"), 5.0F, 2.0F);
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

  // public void doParticles(Player pl, float x, float y, float z) {
  // Object object = enumConst;
  // Utils.sendParticles(pl,
  // object,
  // false,
  // x,
  // y,
  // z,
  // 0.012F,
  // 0.2F,
  // 0.012F,
  // 0.1F,
  // 4,
  // new int[0]);
  // }
  public void doParticles(Player player, float x, float y, float z) {
    player.getWorld().spawnParticle(Particle.CLOUD, x, y, z, 4, 0.012F, 0.2F, 0.012F, 0.1);
    player.getWorld().spawnParticle(Particle.SCULK_SOUL, x, y, z, 1, 0.012F, 0.2F, 0.012F, 0.1);
  }

  public void doGroundParticles(Player player, float x, float y, float z) {
    // player.spawnParticle(Particle.GUST, x, y, z, 1, 0.012F, 0.2F, 0.012F, 0);
    player.getWorld().spawnParticle(Particle.DUST_PLUME, x, y, z, 14, 0.012F, 0.2F, 0.012F, 0.1);
    player.getWorld().spawnParticle(Particle.WHITE_ASH, x, y, z, 14, 0.012F, 0.2F, 0.012F, 0.1);
  }

  public void quit(Player player) {
    memoryRemove(player, true);
  }

  public void clearMem() {
    this.charges.clear();
    this.groundCounts.clear();
    this.cooldown.clear();
    this.smoother.clear();
    this.veccs.clear();
  }
}
