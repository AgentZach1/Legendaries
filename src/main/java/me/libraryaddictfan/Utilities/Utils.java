package me.libraryaddictfan.Utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Utils {
  private static HashMap<String, Method> methodCache = new HashMap<>();

  private static HashMap<String, Class<?>> classCache = new HashMap<>();

  private static HashMap<String, Object> enumCache = new HashMap<>();

  private static long savedTime = 0L;

  public static boolean is1_8() {
    String[] s = Bukkit.getServer().getVersion().split(":");
    String q = s[1].replace(" ", "");
    q = q.replace(")", "");
    return q.contains("1.8");
  }

  public static ItemStack getItemInHand(Player p) {
    if (p == null)
      return new ItemStack(Material.WHITE_CARPET);
    try {
      return p.getInventory().getItemInHand();
    } catch (NullPointerException e) {
      return new ItemStack(Material.WHITE_CARPET);
    }
  }

  public static void clearCaches() {
    classCache.clear();
    methodCache.clear();
    enumCache.clear();
    savedTime = 0L;
  }

  static {
    String[] packageParts = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
    // String version =
    // Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    if (packageParts.length > 3) {
      String version = packageParts[3];
      try {
        classCache.put("net.minecraft.server." + version + "." + "ChatSerializer",
            Class.forName("net.minecraft.server." + version + "." + "ChatSerializer"));
        Bukkit.getServer().getConsoleSender().sendMessage("ClansLegendariesRelease> " + ChatColor.GREEN +
            "Using Located Classes");
      } catch (ClassNotFoundException e) {
        Bukkit.getServer().getConsoleSender().sendMessage("ClansLegendariesRelease> " + ChatColor.GREEN +
            "Using Declared Classes");
      }
    }

  }

  @SuppressWarnings("unchecked")
  public static <T> T getAndInvokeMethod(Class<?> clazz, String methodName, Class[] parametersAsClass, Object instance,
      Object... parametersReal) {
    Method m = getMethod(clazz, methodName, parametersAsClass);
    return (T) invokeMethod(m, instance, parametersReal);
  }

  public static Method getMethod(Class<?> clazz, String methodName, Class... paramaters) {
    if (methodCache.containsKey(methodName))
      return getFromCache(methodName, methodCache);
    try {
      return clazz.getDeclaredMethod(methodName, paramaters);
    } catch (NoSuchMethodException ex) {
      Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + ex.getMessage());
      return null;
    }
  }

  public static <T> Constructor<T> getConstructor(Class<?> clazz, Class... paramaterTypes) {
    try {
      return (Constructor) clazz.getConstructor(paramaterTypes);
    } catch (NoSuchMethodException ex) {
      return null;
    } catch (ClassCastException e1) {
      Bukkit.getServer().getConsoleSender()
          .sendMessage(ChatColor.RED + "--------------Type Parameter Error--------------");
      throw new ClassCastException("Type Parameter was not correct");
    }
  }

  public static <T> T callConstructor(Constructor<T> constructor, Object... paramaters) {
    if (constructor == null)
      throw new NullPointerException("Constructor was null.");
    constructor.setAccessible(true);
    try {
      return constructor.newInstance(paramaters);
    } catch (InvocationTargetException ex) {
      throw new RuntimeException(ex.getCause());
    } catch (ClassCastException e1) {
      Bukkit.getServer().getConsoleSender()
          .sendMessage(ChatColor.RED + "--------------Type Parameter Error--------------");
      throw new ClassCastException("Type Parameter was not correct");
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static void getAndSetField(Class<?> clazz, String name, Object instance, Object value) {
    Field field = getField(clazz, name);
    setFieldValue(field, instance, value);
  }

  public static Field getField(Class<?> clazz, String name) {
    try {
      return clazz.getDeclaredField(name);
    } catch (NoSuchFieldException ex) {
      return null;
    }
  }

  public static <T> T getFieldAndValue(Class<?> clazz, String fieldName, Object instance) {
    Field field = getField(clazz, fieldName);
    return getFieldValue(field, instance);
  }

  public static <T> T getFieldValue(Field field, Object instance) {
    if (field == null)
      throw new NullPointerException("Field was null");
    field.setAccessible(true);
    try {
      return (T) field.get(instance);
    } catch (ClassCastException | IllegalArgumentException | IllegalAccessException e1) {
      Bukkit.getServer().getConsoleSender()
          .sendMessage(ChatColor.RED + "--------------Type Parameter Error--------------");
      throw new ClassCastException("Type Parameter was not correct");
    }
  }

  public static void setFieldValue(Field field, Object instance, Object value) {
    if (field == null)
      throw new NullPointerException("Field was null");
    field.setAccessible(true);
    try {
      field.set(instance, value);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace();
    }
    field.setAccessible(!field.isAccessible());
  }

  public static <T> T invokeMethod(Method method, Object instance, Object... paramaters) {
    if (method == null)
      throw new NullPointerException("Method was null");
    try {
      T output = (T) method.invoke(instance, paramaters);
      addToCache(method.getName(), method, methodCache);
      return output;
    } catch (ClassCastException e1) {
      Bukkit.getServer().getConsoleSender()
          .sendMessage(ChatColor.RED + "--------------Type Parameter Error--------------");
      throw new ClassCastException("Type Parameter was not correct");
    } catch (IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static boolean onGround(Player p) {
    Material blockType = p.getLocation().clone().subtract(0.0D, 1.0D, 0.0D).getBlock().getType();
    if (blockType != Material.AIR && blockType != Material.WATER && blockType != Material.LAVA) {
      if (!p.getLocation().getBlock().isLiquid()) {
        return true;
      }
    }
    return false;
  }

  private static Object getConnection(Player player) {
    Object con = null;
    try {
      Method getHandle = player.getClass().getMethod("getHandle", new Class[0]);
      Object nmsPlayer = getHandle.invoke(player, new Object[0]);
      Field conField = nmsPlayer.getClass().getField("playerConnection");
      con = conField.get(nmsPlayer);
    } catch (NoSuchMethodException | SecurityException | NoSuchFieldException | IllegalArgumentException
        | IllegalAccessException | InvocationTargetException e) {
      Display.customError(e, true);
    }
    return con;
  }

  public static void sendParticles(Player player, Object particle, boolean distEx, float x, float y, float z,
      float xOffset, float yOffset, float zOffset, float speed, int amount, int[] moreData) {
    try {
      Class<?> packetClass = getNmsClass("PacketPlayOutWorldParticles");
      Constructor<?> packetConstructor = packetClass.getConstructor(new Class[] {
          particle.getClass(), boolean.class, float.class, float.class, float.class, float.class, float.class,
          float.class, float.class, int.class,
          int[].class });
      Object packet = packetConstructor.newInstance(new Object[] {
          particle, Boolean.valueOf(distEx), Float.valueOf(x), Float.valueOf(y), Float.valueOf(z),
          Float.valueOf(xOffset), Float.valueOf(yOffset), Float.valueOf(zOffset), Float.valueOf(speed),
          Integer.valueOf(amount),
          moreData });
      Method sendPacket = getNmsClass("PlayerConnection").getMethod("sendPacket",
          new Class[] { getNmsClass("Packet") });
      sendPacket.invoke(getConnection(player), new Object[] { packet });
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException
        | NoSuchMethodException | InstantiationException e) {
      Bukkit.getServer().getLogger().severe("ClansLegendariesRelease> Reflection Error.");
      Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED +
          "ClansLegendariesRelease> Reflection Error (Scepter get)");
    }
  }

  public static void sendPacket(Player player, Object packet) {
    try {
      Object handle = player.getClass().getMethod("getHandle", new Class[0]).invoke(player, new Object[0]);
      Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
      playerConnection.getClass().getMethod("sendPacket", new Class[] { getNmsClass("Packet") })
          .invoke(playerConnection, new Object[] { packet });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static PotionEffect getPotionEffect(Player p, PotionEffectType type) {
    Object nmsPlayer = getHandle(p);
    Class<?> clazz = getNmsClass("MobEffectList");
    Class<?> clazz2 = getNmsClass("MobEffect");
    int typeId = type.getId();
    Method getEffect = getMethod(getNmsClass("EntityLiving"), "getEffect", new Class[] { clazz });
    Method fromId = getMethod(clazz, "fromId", new Class[] { int.class });
    Method getId = getMethod(clazz, "getId", new Class[] { clazz });
    Object mobEffectListFromId = invokeMethod(fromId, null, new Object[] { Integer.valueOf(typeId) });
    Object outPutOfGetEffect = invokeMethod(getEffect, nmsPlayer, new Object[] { mobEffectListFromId });
    Method isShowParticles = getMethod(clazz2, "isShowParticles", new Class[0]);
    Method isAmbient = getMethod(clazz2, "isAmbient", new Class[0]);
    Method getAmplifier = getMethod(clazz2, "getAmplifier", new Class[0]);
    Method getDuration = getMethod(clazz2, "getDuration", new Class[0]);
    Method getMobEffect = getMethod(clazz2, "getMobEffect", new Class[0]);
    Object mobEffect = invokeMethod(getMobEffect, outPutOfGetEffect, new Object[0]);
    int id = ((Integer) invokeMethod(getId, null, new Object[] { mobEffect })).intValue();
    if (outPutOfGetEffect == null)
      return null;
    boolean showParticles = ((Boolean) invokeMethod(isShowParticles, outPutOfGetEffect, new Object[0])).booleanValue();
    boolean ambient = ((Boolean) invokeMethod(isAmbient, outPutOfGetEffect, new Object[0])).booleanValue();
    int amplifier = ((Integer) invokeMethod(getAmplifier, outPutOfGetEffect, new Object[0])).intValue();
    int duration = ((Integer) invokeMethod(getDuration, outPutOfGetEffect, new Object[0])).intValue();
    PotionEffectType potType = PotionEffectType.getById(id);
    PotionEffect newPotionEffect = new PotionEffect(potType, duration, amplifier, ambient, showParticles);
    return newPotionEffect;
  }

  public static <T> T getEnumConstant(Class<T> clazz, String constant) {
    if (clazz == null) {
      return null;
    }
    if (enumCache.containsKey(String.valueOf(clazz.getName()) + "." + constant))
      return (T) getFromCache(String.valueOf(clazz.getName()) + "." + constant, (HashMap) enumCache);
    byte b;
    int i;
    T[] arrayOfT;
    for (i = (arrayOfT = clazz.getEnumConstants()).length, b = 0; b < i;) {
      Object constt = arrayOfT[b];
      if (constt.toString().equalsIgnoreCase(constant)) {
        addToCache(String.valueOf(clazz.getName()) + "." + constant, constt, enumCache);
        return (T) constt;
      }
      b++;
    }
    return null;
  }

  public static String getVersion() {
    String packageName = Bukkit.getServer().getClass().getPackage().getName();
    return packageName.substring(packageName.lastIndexOf('.') + 1);
  }

  public static Object getHandle(Object craftClass) {
    Method getHandle = getMethod(craftClass.getClass(), "getHandle", new Class[0]);
    return invokeMethod(getHandle, craftClass, new Object[0]);
  }

  private static <T> T getFromCache(String key, HashMap<String, T> hashMap) {
    if (key == null)
      throw new NullPointerException("Key was null");
    T output = null;
    if (hashMap.containsKey(key)) {
      output = hashMap.get(key);
    } else {
      return null;
    }
    if (savedTime == 0L) {
      savedTime = System.currentTimeMillis();
    } else if ((System.currentTimeMillis() - savedTime) / 100000L >= 5L) {
      savedTime = System.currentTimeMillis();
      hashMap.clear();
    }
    return output;
  }

  private static <T> void addToCache(String key, T value, HashMap<String, T> hashMap) {
    if (key == null)
      throw new NullPointerException("Key was null");
    if (value == null)
      throw new NullPointerException("Method was null");
    hashMap.put(key, value);
    if (savedTime == 0L) {
      savedTime = System.currentTimeMillis();
    } else if ((System.currentTimeMillis() - savedTime) / 100000L >= 5L) {
      savedTime = System.currentTimeMillis();
      hashMap.clear();
    }
  }

  public static Class<?> getCraftBukkitClass(String name) {
    String className = "org.bukkit.craftbukkit." + getVersion() + "." + name;
    if (classCache.containsKey(className))
      return getFromCache(className, classCache);
    try {
      Class<?> clazz = Class.forName(className);
      addToCache(className, clazz, classCache);
      return clazz;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static Class<?> getUtilClass(String name) {
    String className = "net.minecraft.util." + name;
    if (classCache.containsKey(className))
      return getFromCache(className, classCache);
    try {
      Class<?> clazz = Class.forName(className);
      addToCache(className, clazz, classCache);
      return clazz;
    } catch (ClassNotFoundException ex2) {
      return null;
    }
  }

  public static Class<?> getNmsClass(String name) {
    String packageName = Bukkit.getServer().getClass().getPackage().getName();
    String[] packageParts = packageName.split("\\.");
    
    // Modern Paper/Spigot versions may lack a clear version string in the package name
    if (packageParts.length > 3) {
        String version = packageParts[3];
        String className = "net.minecraft.server." + version + "." + name;

        if (classCache.containsKey(className)) {
            return getFromCache(className, classCache);
        }

        try {
            Class<?> clazz = Class.forName(className);
            addToCache(className, clazz, classCache);
            return clazz;
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().warning("(OLD)[ClansLegendaries] Could not find NMS class: " + className);
        }
    } else {
        // For modern versions, fallback to net.minecraft.server classpaths without a version string
        String modernClassName = "net.minecraft.server." + name;
        try {
            Class<?> clazz = Class.forName(modernClassName);
            addToCache(modernClassName, clazz, classCache);
            return clazz;
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().severe("(OLD)[ClansLegendaries] Modern NMS class not found: " + modernClassName);
        }
    }

    Bukkit.getLogger().severe("(OLD)[ClansLegendaries] Could not determine NMS version from package: " + packageName);
    return null;
}

  public static String splitForCache(String string, boolean interfacee) {
    if (interfacee) {
      String str = string.replace("interface", "");
      str.replace(" ", "");
      str = str.substring(1, str.length());
      return str;
    }
    String split = string.replace("class", "").replace("enum", "").replace("PacketPlayOutTitle.", "");
    split.replace(" ", "");
    split = split.substring(1, split.length());
    return split;
  }

  public static String header() {
    return ChatColor.BLUE + "Clans> " + ChatColor.GRAY;
  }

  public static String green(String str, boolean whiteEnd) {
    return ChatColor.GREEN.toString() + str
        + (whiteEnd ? (String) ChatColor.WHITE.toString() : (String) ChatColor.RESET.toString());
  }

  public static String yellow(String str, boolean whiteEnd) {
    return ChatColor.YELLOW.toString() + str
        + (whiteEnd ? (String) ChatColor.WHITE.toString() : (String) ChatColor.RESET.toString());
  }

  public static String gray(String str) {
    return ChatColor.GRAY.toString() + str;
  }

  public static String white(String str) {
    return ChatColor.WHITE.toString() + str;
  }
}
