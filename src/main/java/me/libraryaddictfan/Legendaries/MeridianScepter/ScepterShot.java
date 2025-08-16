package me.libraryaddictfan.Legendaries.MeridianScepter;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import me.libraryaddictfan.ClansLegendaries;
import me.libraryaddictfan.Utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ScepterShot {
  private boolean toRemove = false;

  private long timeAliveStart;

  private long totalTimeAlive;

  private final Object enumParticleConst = Utils.getEnumConstant(Utils.getNmsClass("EnumParticle"), "REDSTONE");

  private Player shooter;

  private Location start;

  private Arrow fakeEnt;

  private boolean gone = false;

  private ClansLegendaries main;

  private LivingEntity target;

  private Vector vec;

  public ScepterShot(ClansLegendaries mainn, Player player) {
    this.shooter = player;
    this.main = mainn;
    this.timeAliveStart = System.currentTimeMillis();
    this.totalTimeAlive = System.currentTimeMillis() - this.timeAliveStart;
  }

  public void update() {
    this.totalTimeAlive = System.currentTimeMillis() - this.timeAliveStart;
    if (this.totalTimeAlive / 1000L > 12L || this.fakeEnt.getTicksLived() > 220 ||
        this.fakeEnt.getLocation().distance(this.start) > 84.0D || this.fakeEnt.getVelocity() == null
        || this.fakeEnt.getVelocity()
            .equals(new Vector(0, 0, 0)))
      delete();
    for (Player pla : Bukkit.getServer().getOnlinePlayers()) {
      if (!this.toRemove) {
        int[] data = new int[0];
        Utils.sendParticles(
            pla,
            this.enumParticleConst,
            false,
            (float) this.fakeEnt.getLocation().getX(),
            (float) this.fakeEnt.getLocation().getY(),
            (float) this.fakeEnt.getLocation().getZ(),
            0.77F,
            0.0F,
            1.0F,
            1.0F,
            0,
            data);
        for (int i = 0; i < 10; i++) {
          if (!pla.isOnline())
            return;
          if (this.toRemove)
            return;
          if (this.gone)
            return;
          Utils.sendParticles(
              pla,
              this.enumParticleConst,
              false,
              (float) this.fakeEnt.getLocation().getX(),
              (float) this.fakeEnt.getLocation().getY(),
              (float) this.fakeEnt.getLocation().getZ(),
              0.77F,
              0.0F,
              1.0F,
              1.0F,
              0,
              data);
        }
      }
    }
    if (this.target == null) {
      if (!this.toRemove) {
        if (this.vec == null) {
          this.fakeEnt.setVelocity(this.fakeEnt.getVelocity());
        } else {
          this.fakeEnt.setVelocity(this.vec);
        }
        searchForTarget();
      }
    } else if (!this.target.isDead() && this.target.hasLineOfSight((Entity) this.fakeEnt) && this.target.getLocation()
        .distance(this.fakeEnt.getLocation()) < 64.0D) {
      if (!this.toRemove) {
        Vector toTarget = this.target.getEyeLocation().clone()
            .subtract(this.fakeEnt.getLocation()).toVector();
        Vector dirVelocity = this.fakeEnt.getVelocity().clone().normalize();
        Vector dirToTarget = toTarget.clone().normalize();
        Vector newDir = dirVelocity.clone().add(dirToTarget.clone());
        newDir.normalize();
        Vector newVelocity = newDir.clone().multiply(0.4D);
        this.fakeEnt.setVelocity(newVelocity.add(new Vector(0.0D, 0.01D, 0.0D)));
      }
    } else if (!this.toRemove) {
      this.target = null;
    }
    if (this.toRemove) {
      MeridianScepter.removeShot(this);
      this.fakeEnt.remove();
      this.gone = true;
      this.toRemove = false;
      try {
        finalize();
      } catch (Throwable e) {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "ClansLegendariesRelease> Error when trying" +
            " to de-lag scepter.");
      }
    }
  }

  public boolean isGone() {
    return this.gone;
  }

  public void delete() {
    this.toRemove = true;
  }

  public void launch() {
    Arrow arrow = (Arrow) this.shooter.launchProjectile(Arrow.class,
        this.shooter.getLocation().getDirection().multiply(0.5D));
    this.fakeEnt = arrow;
    hideArrow(arrow);
    this.start = this.fakeEnt.getLocation();
    this.fakeEnt.setVelocity(this.shooter.getLocation().getDirection().multiply(0.67D));
    this.vec = this.fakeEnt.getVelocity().clone().multiply(0.67D);
    if (Utils.is1_8())
      return;
    Class[] args = { boolean.class };
    Utils.getAndInvokeMethod(Entity.class, "setGravity", args, this.fakeEnt, new Object[] { Boolean.valueOf(false) });
  }

  public Arrow getArrow() {
    return this.fakeEnt;
  }

  public Player getShooter() {
    return this.shooter;
  }

  public LivingEntity getTarget() {
    return this.target;
  }

  private void searchForTarget() {
    HashMap<LivingEntity, Double> entities = new HashMap<>();
    LivingEntity curLowest = null;
    for (Entity ent : this.fakeEnt.getNearbyEntities(30.0D, 15.0D, 30.0D)) {
      if (ent instanceof LivingEntity) {
        if (ent == this.shooter)
          continue;
        if (ent instanceof Arrow)
          continue;
        if (!MeridianScepter.isTargetEnt() &&
            !(ent instanceof Player))
          continue;
        if (ent instanceof org.bukkit.entity.ArmorStand)
          continue;
        if (ent instanceof Player) {
          Player pl = (Player) ent;
          if (!this.shooter.canSee(pl))
            continue;
          if (pl.getGameMode() == GameMode.CREATIVE || pl.getGameMode() == GameMode.SPECTATOR)
            continue;
        }
        if (ent.isDead())
          continue;
        if (this.fakeEnt.getLocation().distance(ent.getLocation()) > 40.0D)
          continue;
        LivingEntity entLiving = (LivingEntity) ent;
        entities.put(entLiving, Double.valueOf(Math.abs(entLiving.getLocation().distance(this.fakeEnt.getLocation()))));
      }
    }
    for (LivingEntity ent : entities.keySet()) {
      if (curLowest == null
          || ((Double) entities.get(ent)).doubleValue() <= ((Double) entities.get(curLowest)).doubleValue())
        curLowest = ent;
    }
    this.target = curLowest;
  }

  public boolean isToRemove() {
    if (this.toRemove)
      return true;
    return false;
  }

  private void hideArrow(Arrow arrow) {
    Constructor<?> titleConstructor = Utils.getConstructor(Utils.getNmsClass("PacketPlayOutEntityDestroy"),
        new Class[] { int[].class });
    int[] array = { arrow.getEntityId() };
    Object packet = Utils.callConstructor(titleConstructor, new Object[] { array });
    for (Player p : Bukkit.getServer().getOnlinePlayers())
      Utils.sendPacket(p, packet);
  }
}
