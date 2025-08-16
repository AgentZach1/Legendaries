package me.libraryaddictfan.commands;

import me.libraryaddictfan.ClansLegendaries;
import me.libraryaddictfan.Legendaries.Legendary;
import me.libraryaddictfan.Utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LegendaryCommand implements CommandExecutor {
  private ClansLegendaries main;

  public LegendaryCommand(ClansLegendaries mainn) {
    this.main = mainn;
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (cmd.getName().equalsIgnoreCase("legendary")) {
      if (sender instanceof Player) {
        Player p = (Player) sender;
        if (!p.hasPermission(ClansLegendaries.legendaryCommandUse)) {
          p.sendMessage(String.valueOf(Utils.header()) + "Insufficient Permissions.");
          return true;
        }
        if (args.length != 1) {
          if (args.length == 2) {
            twoArgs(p, args);
            return true;
          }
          p.sendMessage(String.valueOf(Utils.header()) + "Too many or too few arguments! Use <tab>" +
              " to search for arguments!");
          return true;
        }
        boolean found = false;
        for (Legendary l : this.main.getLegendaries()) {
          String s = l.getName().replace(" ", "");
          if (args[0].equalsIgnoreCase(s)) {
            ItemStack it = l.getFullItem();
            p.getInventory().addItem(new ItemStack[] { it });
            p.sendMessage(String.valueOf(Utils.header()) + "You have recieved a(n) " +
                ChatColor.GREEN + l.getName());
            if (args[0].equalsIgnoreCase("MeridianScepter") &&
                Utils.is1_8())
              p.sendMessage(String.valueOf(Utils.header()) + "This legendary is best used" +
                  " on a server of version " + ChatColor.GREEN + "1.11" + ChatColor.GRAY + "!");
            found = true;
            continue;
          }
          if (args[0].equalsIgnoreCase("all")) {
            for (Legendary ll : this.main.getLegendaries()) {
              ItemStack it = ll.getFullItem();
              p.getInventory().addItem(new ItemStack[] { it });
            }
            p.sendMessage(String.valueOf(Utils.header()) + "You have recieved every legendary!");
            return true;
          }
        }
        if (!found) {
          p.sendMessage(String.valueOf(Utils.header()) +
              "This argument could not be found! Use <tab>" +
              " to search for arguments!");
          return true;
        }
      } else {
        sender.sendMessage(ChatColor.RED + "This command is only for players!");
      }
      return true;
    }
    return true;
  }

  private boolean twoArgs(Player p, String[] args) {
    if (!p.hasPermission(ClansLegendaries.legendaryCommandGive)) {
      p.sendMessage(String.valueOf(Utils.header()) + "Insufficient Permissions.");
      return true;
    }
    Player target = null;
    for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
      if (pl.getName().equalsIgnoreCase(args[1]))
        target = pl;
    }
    if (target == null) {
      p.sendMessage(String.valueOf(Utils.header()) + "The player, " + ChatColor.GREEN + args[1] + ChatColor.GRAY +
          ", could not be found!");
      return true;
    }
    boolean found = false;
    for (Legendary l : this.main.getLegendaries()) {
      String s = l.getName().replace(" ", "");
      if (args[0].equalsIgnoreCase(s)) {
        ItemStack it = l.getFullItem();
        target.getInventory().addItem(new ItemStack[] { it });
        p.sendMessage(
            String.valueOf(Utils.header()) + "You have given a(n) " + ChatColor.GREEN + l.getName() + ChatColor.GRAY +
                " to " + ChatColor.GREEN + target.getName());
        target.sendMessage(String.valueOf(Utils.header()) + "You have recieved a(n) " +
            ChatColor.GREEN + l.getName());
        if (args[0].equalsIgnoreCase("MeridianScepter") &&
            Utils.is1_8())
          p.sendMessage(String.valueOf(Utils.header()) + "This legendary is best used" +
              " on a server of version " + ChatColor.GREEN + "1.11" + ChatColor.GRAY + "!");
        found = true;
        continue;
      }
      if (args[0].equalsIgnoreCase("all")) {
        for (Legendary ll : this.main.getLegendaries()) {
          ItemStack it = ll.getFullItem();
          target.getInventory().addItem(new ItemStack[] { it });
        }
        p.sendMessage(
            String.valueOf(Utils.header()) + "You have given every legendary to " + ChatColor.GREEN + target.getName());
        target.sendMessage(String.valueOf(Utils.header()) + "You have recieved every legendary!");
        return true;
      }
    }
    if (!found) {
      p.sendMessage(String.valueOf(Utils.header()) +
          "This argument could not be found! Use <tab>" +
          " to search for arguments!");
      return true;
    }
    return true;
  }
}
