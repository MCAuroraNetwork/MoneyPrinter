package club.aurorapvp.moneyprinter.configs;

import club.aurorapvp.moneyprinter.MoneyPrinter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {

  private final File FILE = new File(MoneyPrinter.getInstance().getDataFolder(), "config.yml");
  private YamlConfiguration config;

  public Config() {
    this.reload();
    this.generateDefaults();
  }

  public void generateDefaults() {
    final HashMap<String, Object> DEFAULTS = new HashMap<>();

    DEFAULTS.put("mongodb.database-name", "money_printer");
    DEFAULTS.put("mongodb.address", "mongodb://localhost:27017");

    for (String path : DEFAULTS.keySet()) {
      if (!getYaml().isSet(path) || getYaml().getString(path) == null) {
        getYaml().set(path, DEFAULTS.get(path));
      }
    }

    try {
      getYaml().save(FILE);
    } catch (IOException e) {
      MoneyPrinter.getInstance().getLogger().log(Level.SEVERE, "Failed to save config file", e);
    }
  }

  public YamlConfiguration getYaml() {
    return config;
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void reload() {
    if (!FILE.exists()) {
      try {
        FILE.getParentFile().mkdirs();
        FILE.createNewFile();

        config = YamlConfiguration.loadConfiguration(FILE);

        this.generateDefaults();
      } catch (IOException e) {
        MoneyPrinter.getInstance().getLogger()
            .log(Level.SEVERE, "Failed to generate config file", e);
      }
    }
    config = YamlConfiguration.loadConfiguration(FILE);
    MoneyPrinter.getInstance().getLogger().info("Config reloaded!");
  }
}