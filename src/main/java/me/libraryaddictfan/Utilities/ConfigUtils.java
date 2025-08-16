package me.libraryaddictfan.Utilities;

import me.libraryaddictfan.ClansLegendaries;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigUtils {
  private static FileConfiguration config;

  private static ClansLegendaries main;

  public static void register(ClansLegendaries mainn) {
    config = mainn.getConfig();
    main = mainn;
  }

  public static void reload() {
    config = main.getConfig();
  }

  public static void setBooleanSection(ConfigSections section, boolean obj) {
    config.set(section.getConfigSection(), Boolean.valueOf(obj));
  }

  public static void setIntegerSection(ConfigSections section, int obj) {
    config.set(section.getConfigSection(), Integer.valueOf(obj));
  }

  public static void setDoubleSection(ConfigSections section, double obj) {
    config.set(section.getConfigSection(), Double.valueOf(obj));
  }

  public static boolean getBooleanSection(ConfigSections section) {
    return config.getBoolean(section.getConfigSection(), ((Boolean) section.getDefaultValue()).booleanValue());
  }

  public static double getDoubleSection(ConfigSections section) {
    return config.getDouble(section.getConfigSection(), ((Double) section.getDefaultValue()).doubleValue());
  }

  public static int getIntegerSection(ConfigSections section) {
    return config.getInt(section.getConfigSection(), ((Integer) section.getDefaultValue()).intValue());
  }
}
