package me.libraryaddictfan.Legendaries;

import java.util.Arrays;
import me.libraryaddictfan.ClansLegendaries;
import me.libraryaddictfan.Utilities.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class ScytheOfTheFallenLord extends Legendary {
  public ScytheOfTheFallenLord(ClansLegendaries clanslegendariess, ItemStack item, String name, int damage) {
    super(clanslegendariess, item, name,
        Arrays.asList(new String[] { Utils.white("An old blade fashioned of nothing more"),
            Utils.white("than bones and cloth which served no"), Utils.white("purpose. Brave adventurers however have"),
            Utils.white("imbued it with the remnant powers of a"), Utils.white("dark and powerful foe."), "",
            String.valueOf(Utils.yellow("Attack", true)) + " to use " + Utils.green("Leach", false) }),
        damage);
  }

  @EventHandler
  public void onDmg(EntityDamageByEntityEvent e) {
    if (e.getDamager() instanceof Player) {
      Player p = (Player) e.getDamager();
      ItemStack item = Utils.getItemInHand(p);
      if (isCorrectItem(item)) {
        e.setDamage(getDamage());
        p.setHealth(Math.min(20.0D, p.getHealth() + 2.0D));
      }
    }
  }

  public void rel() {
  }

  public void loop() {
  }

  public void quit(Player player) {
  }

  public void clearMem() {
  }
}
