package club.aurorapvp.moneyprinter.configs;

import club.aurorapvp.moneyprinter.MoneyPrinter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;

public class Lang {
  private final HashMap<String, String> PLACEHOLDERS = new HashMap<>();
  private final File FILE = new File(MoneyPrinter.getInstance().getDataFolder(), "lang.yml");
  private YamlConfiguration lang;

  public Lang() {
    this.reload();
    this.generateDefaults();
  }

  public void generateDefaults() {
    final HashMap<String, String> DEFAULTS = new HashMap<>();

    for (var path : getYaml().getKeys(false).toArray()) {
      if (Objects.requireNonNull(getYaml().getString((String) path)).startsWith("~")
              && Objects.requireNonNull(getYaml().getString((String) path)).endsWith("~")) {
        PLACEHOLDERS.put(
                (String) path,
                Objects.requireNonNull(getYaml().getString((String) path)).replace("~", ""));
      }
    }

    DEFAULTS.put("prefix", "~<gradient:#FFAA00:#FF55FF><bold>AuroraCosmetics ><reset>~");
    DEFAULTS.put("store", "~https://store.link~");
    DEFAULTS.put("cannot-use-personalized", "<red>You cannot use personalized items! ✗ Visit <bold>store<reset><red> to become a supporter!");
    DEFAULTS.put("customize-display-name", "<yellow><bold>Customize Your Name");
    DEFAULTS.put("toggle-prefix-suffix", "<yellow>Toggle Prefix/Suffix 🔄");
    DEFAULTS.put("reset", "<yellow>Reset to Default ⚪");
    DEFAULTS.put("next-frame", "<yellow>Next Frame ➡");
    DEFAULTS.put("back", "<yellow>Back ⬅");
    DEFAULTS.put("decrease-refresh-rate", "<yellow>Slower Refresh ⏬");
    DEFAULTS.put("increase-refresh-rate", "<yellow>Faster Refresh ⏫");
    DEFAULTS.put("refresh-rate", "<yellow>Refresh Rate: <bold>%d<reset><yellow> ticks");
    DEFAULTS.put("add-color", "<yellow>Add Color: <bold>%s<reset><yellow> 🎨");
    DEFAULTS.put("previous-frame", "<yellow>Previous Frame ⬅");
    DEFAULTS.put("preview-animation", "<yellow>Preview Animation");
    DEFAULTS.put("max-frames-reached", "<red>Max frames (10) reached! ✗");
    DEFAULTS.put("delete-frame", "<red>Delete Frame 🗑");
    DEFAULTS.put("back-to-customization", "<yellow>Back to Customization Menu ⬅");
    DEFAULTS.put("select-armor-piece", "<yellow><bold>Choose Armor Piece 🛡");
    DEFAULTS.put("customize-armor-trim", "<yellow><bold>Customize %s Trim");
    DEFAULTS.put("trim-pattern", "<yellow>%s");
    DEFAULTS.put("trim-material", "<yellow>%s");
    DEFAULTS.put("selected-trim-pattern", "<yellow>Selected Pattern: <bold>%s<reset><yellow> ✓");
    DEFAULTS.put("selected-trim-material", "<yellow>Selected Material: <bold>%s<reset><yellow> ✓");
    DEFAULTS.put("clear-trim", "<red>Clear Trim ✗");
    DEFAULTS.put("customize-shield-banner", "<yellow><bold>Customize Shield Banner 🛡");
    DEFAULTS.put("base-color", "<yellow>Base Color: <bold>%s");
    DEFAULTS.put("pattern", "<yellow>Pattern: <bold>%s");
    DEFAULTS.put("color", "<yellow>Color: <bold>%s");
    DEFAULTS.put("add-pattern", "<yellow>Add Pattern ➕");
    DEFAULTS.put("clear-all-patterns", "<red>Clear All Patterns ✗");
    DEFAULTS.put("mirror-created", "prefix <green>A new mirror has been created at your location.");
    DEFAULTS.put("mirror-removed", "prefix <green>The nearest mirror to your location has been removed.");
    DEFAULTS.put("mirror-not-found", "prefix <red>No mirror found within 10 blocks to remove.");

    for (String path : DEFAULTS.keySet()) {
      if (!getYaml().contains(path) || getYaml().getString(path) == null) {
        getYaml().set(path, DEFAULTS.get(path));
      }
    }

    try {
      getYaml().save(FILE);
    } catch (IOException e) {
      MoneyPrinter.getInstance().getLogger().log(Level.SEVERE, "Failed to save lang file", e);
    }

    for (var path : getYaml().getKeys(false).toArray()) {
      if (Objects.requireNonNull(getYaml().getString((String) path)).startsWith("~")
              && Objects.requireNonNull(getYaml().getString((String) path)).endsWith("~")) {
        PLACEHOLDERS.put(
                (String) path,
                Objects.requireNonNull(getYaml().getString((String) path)).replace("~", ""));
      }
    }
  }

  public String getString(String message) {
    String pathString = getYaml().getString(message);
    for (String placeholder : PLACEHOLDERS.keySet()) {
      assert pathString != null;
      if (pathString.contains(placeholder)) {
        pathString = pathString.replace(placeholder, PLACEHOLDERS.get(placeholder));
      }
    }
    return pathString;
  }

  public Component formatComponent(String message, Object... args) {
    String pathString = getYaml().getString(message);
    assert pathString != null;
    for (String placeholder : PLACEHOLDERS.keySet()) {
      if (pathString.contains(placeholder)) {
        pathString = pathString.replace(placeholder, PLACEHOLDERS.get(placeholder));
      }
    }
    pathString = String.format(pathString, args);
    return MiniMessage.miniMessage().deserialize(pathString);
  }

  public Component getComponent(String message) {
    String pathString = getYaml().getString(message);
    assert pathString != null;
    for (String placeholder : PLACEHOLDERS.keySet()) {
      if (pathString.contains(placeholder)) {
        pathString = pathString.replace(placeholder, PLACEHOLDERS.get(placeholder));
      }
    }
    return MiniMessage.miniMessage().deserialize(pathString);
  }

  public Component getformattedComponentCommand(String message, String hover, String command, Object... args) {
    String pathString = getYaml().getString(message);
    assert pathString != null;
    for (String placeholder : PLACEHOLDERS.keySet()) {
      if (pathString.contains(placeholder)) {
        pathString = pathString.replace(placeholder, PLACEHOLDERS.get(placeholder));
      }
    }
    pathString = String.format(pathString, args);
    Component component = MiniMessage.miniMessage().deserialize(pathString);
    return component
            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command))
            .hoverEvent(HoverEvent.showText(Component.text(hover)));
  }

  public Component getComponentCommand(String message, String hover, String command) {
    String pathString = getYaml().getString(message);
    assert pathString != null;
    for (String placeholder : PLACEHOLDERS.keySet()) {
      if (pathString.contains(placeholder)) {
        pathString = pathString.replace(placeholder, PLACEHOLDERS.get(placeholder));
      }
    }
    Component component = MiniMessage.miniMessage().deserialize(pathString);
    return component
            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command))
            .hoverEvent(HoverEvent.showText(Component.text(hover)));
  }

  public YamlConfiguration getYaml() {
    return lang;
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void reload() {
    if (!FILE.exists()) {
      try {
        FILE.getParentFile().mkdirs();
        FILE.createNewFile();
        lang = YamlConfiguration.loadConfiguration(FILE);
        this.generateDefaults();
      } catch (IOException e) {
        MoneyPrinter.getInstance().getLogger().log(Level.SEVERE, "Failed to generate lang file", e);
      }
    }
    lang = YamlConfiguration.loadConfiguration(FILE);
    MoneyPrinter.getInstance().getLogger().info("Lang reloaded!");
  }
}