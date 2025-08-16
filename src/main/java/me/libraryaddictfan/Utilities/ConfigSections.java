package me.libraryaddictfan.Utilities;

public enum ConfigSections {
  BREEZEBLADE_INFINITE("breezeblade-infinite-charge", Boolean.valueOf(false)),
  BREEZEBLADE_VELOCITY("breezeblade-velocity", Double.valueOf(0.66D)),
  SCEPTER_TARGET_ENT("scepter-nonplayer-target", Boolean.valueOf(true)),
  HYPERAXE_DAMAGE_DELAY("hyper-damage-delay", Integer.valueOf(7)),
  HYPERAXE_DAMAGE("hyper-damage", Integer.valueOf(4)),
  SCEPTER_DAMAGE("scepter-damage", Integer.valueOf(11));

  private String configSection;

  private Object defaultValue;

  ConfigSections(String configSectionn, Object defaultValuee) {
    this.configSection = configSectionn;
    this.defaultValue = defaultValuee;
  }

  public String getConfigSection() {
    return this.configSection;
  }

  public Object getDefaultValue() {
    return this.defaultValue;
  }
}
