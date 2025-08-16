package me.libraryaddictfan.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import me.libraryaddictfan.ClansLegendaries;
import me.libraryaddictfan.Utilities.ConfigSections;
import me.libraryaddictfan.Utilities.Utils;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class LegendariesCommandTabComplete implements TabCompleter {
  public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
    if (cmd.getName().equalsIgnoreCase("legendaries")) {
      if (sender instanceof Player) {
        Player p = (Player) sender;
        if (!p.hasPermission(ClansLegendaries.legendariesCommandUse))
          return null;
      }
      if (args.length == 1)
        return oneArg(sender, args);
      if (args.length == 2)
        return twoArgs(sender, args);
      if (args.length == 3)
        return threeArgs(sender, args);
    }
    return null;
  }

  private List<String> oneArg(CommandSender sender, String[] args) {
    Set<String> arguments = LegendariesCommand.getArguments().keySet();
    ArrayList<String> argTypes = new ArrayList<>();
    if (!args[0].equals("")) {
      for (String nName : arguments) {
        if (nName.toLowerCase().startsWith(args[0].toLowerCase()))
          argTypes.add(nName);
      }
    } else {
      for (String nName : arguments)
        argTypes.add(nName);
    }
    Collections.sort(argTypes);
    return argTypes;
  }

  private List<String> twoArgs(CommandSender sender, String[] args) {
    if (args[0].equalsIgnoreCase("help")) {
      Set<String> arguments = LegendariesCommand.getArguments().keySet();
      ArrayList<String> argTypes = new ArrayList<>();
      if (!args[1].equals("")) {
        for (String nName : arguments) {
          if (nName.toLowerCase().startsWith(args[1].toLowerCase()))
            argTypes.add(nName);
        }
      } else {
        for (String nName : arguments)
          argTypes.add(nName);
      }
      Collections.sort(argTypes);
      return argTypes;
    }
    if (args[0].equalsIgnoreCase("setSection")) {
      ConfigSections[] argumentss = ConfigSections.values();
      ArrayList<String> arguments = new ArrayList<>();
      byte b;
      int i;
      ConfigSections[] arrayOfConfigSections1;
      for (i = (arrayOfConfigSections1 = argumentss).length, b = 0; b < i;) {
        ConfigSections sec = arrayOfConfigSections1[b];
        arguments.add(sec.toString().toLowerCase());
        b++;
      }
      ArrayList<String> argTypes = new ArrayList<>();
      if (!args[1].equals("")) {
        for (String nName : arguments) {
          if (nName.toLowerCase().startsWith(args[1].toLowerCase()))
            argTypes.add(nName);
        }
      } else {
        for (String nName : arguments)
          argTypes.add(nName);
      }
      Collections.sort(argTypes);
      return argTypes;
    }
    return null;
  }

  private List<String> threeArgs(CommandSender sender, String[] args) {
    if (args[0].equalsIgnoreCase("setSection")) {
      ConfigSections[] argumentss = ConfigSections.values();
      ArrayList<String> arguments = new ArrayList<>();
      byte b;
      int i;
      ConfigSections[] arrayOfConfigSections1;
      for (i = (arrayOfConfigSections1 = argumentss).length, b = 0; b < i;) {
        ConfigSections sec = arrayOfConfigSections1[b];
        arguments.add(sec.getDefaultValue().toString());
        b++;
      }
      ArrayList<String> argTypes = new ArrayList<>();
      if (!args[2].equals("")) {
        for (String nName : arguments) {
          try {
            if (ConfigSections.valueOf(args[1].toUpperCase()) != null) {
              String combine = "<insert_" +
                  ConfigSections.valueOf(args[1].toUpperCase()).getDefaultValue().getClass()
                      .getSimpleName()
                  + "_here>";
              argTypes.add(combine);
              continue;
            }
            sender.sendMessage(String.valueOf(Utils.header()) + "Your Config Section (argument 2)" +
                " could not be found.");
            playWarnSound(sender);
            return null;
          } catch (IllegalArgumentException e) {
            sender.sendMessage(String.valueOf(Utils.header()) + "Your Config Section (argument 2)" +
                " could not be found.");
            playWarnSound(sender);
            return null;
          }
        }
      } else {
        for (String nName : arguments) {
          try {
            if (ConfigSections.valueOf(args[1].toUpperCase()) != null) {
              String combine = "<insert_" +
                  ConfigSections.valueOf(args[1].toUpperCase()).getDefaultValue().getClass()
                      .getSimpleName()
                  + "_here>";
              argTypes.add(combine);
              continue;
            }
            sender.sendMessage(String.valueOf(Utils.header()) + "Your Config Section (argument 2)" +
                " could not be found.");
            playWarnSound(sender);
            return null;
          } catch (IllegalArgumentException e) {
            sender.sendMessage(String.valueOf(Utils.header()) + "Your Config Section (argument 2)" +
                " could not be found.");
            playWarnSound(sender);
            return null;
          }
        }
      }
      Collections.sort(argTypes);
      return argTypes;
    }
    return null;
  }

  private void playWarnSound(CommandSender sender) {
    if (sender instanceof Player) {
      Player p = (Player) sender;
      if (Utils.is1_8()) {
        p.playSound(p.getLocation(), Sound.valueOf("NOTE_PLING"), 10.0F, 1.0F);
      } else {
        p.playSound(p.getLocation(), Sound.valueOf("BLOCK_NOTE_PLING"), 10.0F, 1.0F);
      }
    }
  }
}
