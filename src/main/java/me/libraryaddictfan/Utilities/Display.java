package me.libraryaddictfan.Utilities;

import java.lang.reflect.Constructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatMessageType;

public class Display {
  private static final int BARS = 24;

  public static void display(String text, Player player) {
    sendJsonMessage(player, text, ChatAction.ACTION_BAR);
  }

  public static void sendJsonMessage(Player player, String text, ChatAction chatAction) {
    Class<?> chatBaseComponentClass = Utils.getNmsClass("IChatBaseComponent");
    if (chatBaseComponentClass == null) {
      Bukkit.getLogger().warning("[ClansLegendaries] Failed to send JSON message. NMS class not found.");
      return;
    }

    Object chat = null;
    try {
      Class<?> chatSerializerClass = Utils.getNmsClass("IChatBaseComponent$ChatSerializer");
      if (chatSerializerClass == null) {
        chatSerializerClass = Utils.getNmsClass("ChatSerializer");
      }
      if (chatSerializerClass != null) {
        chat = Utils.getAndInvokeMethod(chatSerializerClass, "a", new Class[] { String.class }, null,
            "{\"text\":\" " + text + " \"}");
      }

      Constructor<?> chatConstructor = Utils.getConstructor(
          Utils.getNmsClass("PacketPlayOutChat"),
          new Class[] { chatBaseComponentClass, byte.class });
      if (chatConstructor != null && chat != null) {
        Object packet = chatConstructor.newInstance(chat, chatAction.getValue());
        Utils.sendPacket(player, packet);
      } else {
        Bukkit.getLogger().warning("[ClansLegendaries] Failed to construct chat packet.");
      }
    } catch (Exception e) {
      Bukkit.getLogger().severe("[ClansLegendaries] Error sending JSON message: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public static void displaySubTitle(Player player, String text, int fadeIn, int stay, int fadeOut) {
    if (player != null && player.isOnline()) {
      player.sendTitle("", text, fadeIn, stay, fadeOut);
    } else {
      Bukkit.getLogger().warning("[Legends] Cannot send subtitle. Player is null or offline.");
    }
    // Object subtitle = null;
    // Class[] arry = Utils.getNmsClass("IChatBaseComponent").getDeclaredClasses();
    // if (arry.length == 0) {
    // subtitle = Utils.getAndInvokeMethod(Utils.getNmsClass("ChatSerializer"), "a",
    // new Class[] { String.class }, null, new Object[] { "{\"text\":\" " + text + "
    // " + "\"}" });
    // } else {
    // subtitle =
    // Utils.getAndInvokeMethod(Utils.getNmsClass("IChatBaseComponent").getDeclaredClasses()[0],
    // "a",
    // new Class[] { String.class }, null, new Object[] { "{\"text\":\" " + text + "
    // " + "\"}" });
    // }
    // Constructor<?> subTitleConstructor =
    // Utils.getConstructor(Utils.getNmsClass("PacketPlayOutTitle"),
    // new Class[] {
    // Utils.getNmsClass("PacketPlayOutTitle").getDeclaredClasses()[0],
    // Utils.getNmsClass("IChatBaseComponent"), int.class, int.class, int.class });
    // Object packetSubTitle = Utils.callConstructor(subTitleConstructor, new
    // Object[] { Utils.getEnumConstant(
    // Utils.getNmsClass("PacketPlayOutTitle").getDeclaredClasses()[0], "SUBTITLE"),
    // subtitle,
    // Integer.valueOf(fadeIn), Integer.valueOf(stay), Integer.valueOf(fadeOut) });
    // Utils.sendPacket(player, packetSubTitle);
  }

  public static void displayTitle(Player player, String text, int fadeIn, int stay, int fadeOut) {
    // Object title = null;
    // Class[] arry = Utils.getNmsClass("IChatBaseComponent").getDeclaredClasses();
    // if (arry.length == 0) {
    // title = Utils.getAndInvokeMethod(Utils.getNmsClass("ChatSerializer"), "a",
    // new Class[] { String.class }, null, new Object[] { "{\"text\":\" " + text + "
    // " + "\"}" });
    // } else {
    // title =
    // Utils.getAndInvokeMethod(Utils.getNmsClass("IChatBaseComponent").getDeclaredClasses()[0],
    // "a",
    // new Class[] { String.class }, null, new Object[] { "{\"text\":\" " + text + "
    // " + "\"}" });
    // }
    // Constructor<?> titleConstructor =
    // Utils.getConstructor(Utils.getNmsClass("PacketPlayOutTitle"),
    // new Class[] {
    // Utils.getNmsClass("PacketPlayOutTitle").getDeclaredClasses()[0],
    // Utils.getNmsClass("IChatBaseComponent"), int.class, int.class, int.class });
    // Object packetTitle = Utils.callConstructor(titleConstructor, new Object[] {
    // Utils.getEnumConstant(
    // Utils.getNmsClass("PacketPlayOutTitle").getDeclaredClasses()[0], "TITLE"),
    // title,
    // Integer.valueOf(fadeIn), Integer.valueOf(stay), Integer.valueOf(fadeOut) });
    // Utils.sendPacket(player, packetTitle);
    if (player != null && player.isOnline()) {
      player.sendTitle("", text, fadeIn, stay, fadeOut);
    } else {
      Bukkit.getLogger().warning("[Legends] Cannot send subtitle. Player is null or offline.");
    }
  }

  public static void displayTitleAndSubtitle(Player player, String titleText, String subTitleText, int fadeIn, int stay,
      int fadeOut) {
    displayTitle(player, titleText, fadeIn, stay, fadeOut);
    displaySubTitle(player, subTitleText, fadeIn, stay, fadeOut);
  }

  public static void customError(Exception e, boolean printStack) {
    if (printStack)
      Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "------------------------------");
    Bukkit.getServer().getLogger().info(ChatColor.RED + "ClansLegendariesRelease> " + e.getClass().toString() + "\n");
    e.printStackTrace();
    if (printStack)
      Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "------------------------------");
  }

  public static void displayProgress(String prefix, double amount, double onGround, String suffix,
      boolean progressDirectionSwap, Player... players) {

    StringBuilder progressBar = new StringBuilder();
    if (progressDirectionSwap)
      amount = 1.0D - amount;

    ChatColor currentColor = ChatColor.GREEN;
    for (int i = 0; i < BARS; i++) {
      double progressFraction = i / (double) BARS;
      if (progressFraction >= amount) {
        currentColor = ChatColor.RED; // Switch to red if past the charge threshold
      }
      if (onGround > 0 && progressFraction >= (amount - onGround) && progressFraction < amount) {
        currentColor = ChatColor.YELLOW; // Overlay yellow if within on-ground charge range
      }
      progressBar.append(currentColor).append("|");
    }

    String displayText = (prefix == null ? "" : prefix + ChatColor.RESET + " ") + progressBar +
        (suffix == null ? "" : suffix);

    for (Player player : players) {
      if (player != null && player.isOnline()) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(displayText));
      }
    }
  }
}
