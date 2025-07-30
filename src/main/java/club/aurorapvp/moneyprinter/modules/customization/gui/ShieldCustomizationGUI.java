package club.aurorapvp.moneyprinter.modules.customization.gui;

import club.aurorapvp.moneyprinter.MoneyPrinter;
import club.aurorapvp.moneyprinter.modules.customization.CosmeticSettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShieldCustomizationGUI implements InventoryHolder, Listener {
  private final Inventory inventory;
  private final Player player;
  private final CosmeticSettingsManager settingsManager;
  private List<Pattern> currentPatterns;
  private int selectedBaseColorIndex;
  private int selectedPatternTypeIndex;
  private int selectedColorIndex;
  private DyeColor baseColor;
  private final DyeColor[] colors = DyeColor.values();
  private final PatternType[] patternTypes = PatternType.values();

  public ShieldCustomizationGUI(Player player) {
    this.player = player;
    this.settingsManager = CosmeticSettingsManager.getInstance(player);
    this.currentPatterns = loadPatterns();
    String baseColorStr = settingsManager.getShieldBaseColor();
    try {
      DyeColor initialBaseColor = DyeColor.valueOf(baseColorStr);
      this.selectedBaseColorIndex = initialBaseColor.ordinal();
      this.baseColor = initialBaseColor;
    } catch (IllegalArgumentException e) {
      this.selectedBaseColorIndex = DyeColor.WHITE.ordinal();
      this.baseColor = DyeColor.WHITE;
    }
    this.selectedPatternTypeIndex = 0;
    this.selectedColorIndex = 0;
    this.inventory =
        Bukkit.createInventory(
            this,
            9,
            MoneyPrinter.getInstance().getLang().formatComponent("customize-shield-banner"));
    populateGUI();
    Bukkit.getPluginManager().registerEvents(this, MoneyPrinter.getInstance());
  }

  private List<Pattern> loadPatterns() {
    List<String> patternStrings = settingsManager.getShieldBannerPatterns();
    List<Pattern> patterns = new ArrayList<>();
    for (String str : patternStrings) {
      String[] parts = str.split(":");
      if (parts.length == 2) {
        try {
          PatternType type = PatternType.valueOf(parts[0]);
          DyeColor color = DyeColor.valueOf(parts[1]);
          patterns.add(new Pattern(color, type));
        } catch (IllegalArgumentException e) {
          MoneyPrinter.getInstance().getLogger().warning("Invalid pattern string: " + str);
        }
      }
    }
    return patterns;
  }

  private void savePatterns() {
    List<String> patternStrings =
        currentPatterns.stream()
            .map(p -> p.getPattern().name() + ":" + p.getColor().name())
            .collect(Collectors.toList());
    settingsManager.setShieldBannerPatterns(patternStrings);
  }

  private void populateGUI() {
    inventory.clear();

    inventory.setItem(0, createBaseColorItem());

    inventory.setItem(1, createPatternTypeItem());

    inventory.setItem(2, createPatternColorItem());

    ItemStack addPattern = new ItemStack(Material.EMERALD);
    addPattern.editMeta(
        meta -> meta.displayName(MoneyPrinter.getInstance().getLang().getComponent("add-pattern")));
    inventory.setItem(3, addPattern);

    ItemStack clearAll = new ItemStack(Material.TNT);
    clearAll.editMeta(
        meta ->
            meta.displayName(
                MoneyPrinter.getInstance().getLang().getComponent("clear-all-patterns")));
    inventory.setItem(4, clearAll);

    inventory.setItem(7, createPreviewShield());

    ItemStack back = new ItemStack(Material.BARRIER);
    back.editMeta(
        meta -> meta.displayName(MoneyPrinter.getInstance().getLang().getComponent("back")));
    inventory.setItem(8, back);
  }

  private ItemStack createBaseColorItem() {
    DyeColor color = colors[selectedBaseColorIndex];
    Material woolMaterial = Material.valueOf(color.name() + "_WOOL");
    ItemStack wool = new ItemStack(woolMaterial);
    wool.editMeta(
        meta ->
            meta.displayName(
                MoneyPrinter.getInstance()
                    .getLang()
                    .formatComponent(
                        "base-color", capitalizeFirstLetter(color.name().toLowerCase()))));
    return wool;
  }

  private ItemStack createPatternTypeItem() {
    PatternType type = patternTypes[selectedPatternTypeIndex];
    DyeColor selectedColor = colors[selectedColorIndex];
    ItemStack banner = new ItemStack(Material.valueOf(baseColor.name() + "_BANNER"));
    banner.editMeta(
        BannerMeta.class,
        meta -> {
          meta.addPattern(new Pattern(selectedColor, type));
          meta.displayName(
              MoneyPrinter.getInstance()
                  .getLang()
                  .formatComponent("pattern", formatEnumName(type.name())));
        });
    return banner;
  }

  private ItemStack createPatternColorItem() {
    DyeColor color = colors[selectedColorIndex];
    Material woolMaterial = Material.valueOf(color.name() + "_WOOL");
    ItemStack wool = new ItemStack(woolMaterial);
    wool.editMeta(
        meta ->
            meta.displayName(
                MoneyPrinter.getInstance()
                    .getLang()
                    .formatComponent("color", capitalizeFirstLetter(color.name().toLowerCase()))));
    return wool;
  }

  private ItemStack createPreviewShield() {
    ItemStack shield = new ItemStack(Material.SHIELD);
    BlockStateMeta shieldMeta = (BlockStateMeta) shield.getItemMeta();
    Banner banner = (Banner) shieldMeta.getBlockState();

    banner.setBaseColor(baseColor);
    banner.getPatterns().clear();
    for (Pattern pattern : currentPatterns) {
      banner.addPattern(pattern);
    }

    banner.update();
    shieldMeta.setBlockState(banner);
    shield.setItemMeta(shieldMeta);
    return shield;
  }

  public void open() {
    player.openInventory(inventory);
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getInventory().getHolder() != this) return;
    event.setCancelled(true);
    int slot = event.getSlot();

    if (slot == 0) {
      selectedBaseColorIndex = (selectedBaseColorIndex + 1) % colors.length;
      baseColor = colors[selectedBaseColorIndex];
      inventory.setItem(0, createBaseColorItem());
      inventory.setItem(1, createPatternTypeItem());
      inventory.setItem(7, createPreviewShield());
    } else if (slot == 1) {
      selectedPatternTypeIndex = (selectedPatternTypeIndex + 1) % patternTypes.length;
      inventory.setItem(1, createPatternTypeItem());
    } else if (slot == 2) {
      selectedColorIndex = (selectedColorIndex + 1) % colors.length;
      inventory.setItem(2, createPatternColorItem());
      inventory.setItem(1, createPatternTypeItem());
    } else if (slot == 3 && currentPatterns.size() < 6) {
      PatternType type = patternTypes[selectedPatternTypeIndex];
      DyeColor color = colors[selectedColorIndex];
      currentPatterns.add(new Pattern(color, type));
      inventory.setItem(7, createPreviewShield());
    } else if (slot == 4) {
      currentPatterns.clear();
      inventory.setItem(7, createPreviewShield());
    } else if (slot == 8) {
      new CustomizationGUI(player).open();
      HandlerList.unregisterAll(this);
    }
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    if (event.getInventory().getHolder() == this) {
      savePatterns();
      settingsManager.setShieldBaseColor(baseColor.name());
      for (ItemStack item : player.getInventory()) {
        if (item != null && item.getType() == Material.SHIELD) {
          applyShieldCustomization(item);
        }
      }
      ItemStack offhand = player.getInventory().getItemInOffHand();
      if (offhand.getType() == Material.SHIELD) {
        applyShieldCustomization(offhand);
      }
      HandlerList.unregisterAll(this);
    }
  }

  private void applyShieldCustomization(ItemStack shield) {
    List<String> patternStrings = settingsManager.getShieldBannerPatterns();
    String baseColorStr = settingsManager.getShieldBaseColor();
    BlockStateMeta shieldMeta = (BlockStateMeta) shield.getItemMeta();
    Banner banner = (Banner) shieldMeta.getBlockState();

    DyeColor baseColor;
    try {
      baseColor = DyeColor.valueOf(baseColorStr);
    } catch (IllegalArgumentException e) {
      baseColor = DyeColor.WHITE;
    }
    banner.setBaseColor(baseColor);

    banner.getPatterns().clear();
    for (String str : patternStrings) {
      String[] parts = str.split(":");
      if (parts.length == 2) {
        try {
          PatternType type = PatternType.valueOf(parts[0]);
          DyeColor color = DyeColor.valueOf(parts[1]);
          banner.addPattern(new Pattern(color, type));
        } catch (IllegalArgumentException e) {
          MoneyPrinter.getInstance().getLogger().warning("Invalid pattern string: " + str);
        }
      }
    }

    banner.update();
    shieldMeta.setBlockState(banner);
    shield.setItemMeta(shieldMeta);
  }

  @Override
  public Inventory getInventory() {
    return inventory;
  }

  private String capitalizeFirstLetter(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
  }

  private String formatEnumName(String enumName) {
    String[] parts = enumName.split("_");
    StringBuilder formatted = new StringBuilder();
    for (String part : parts) {
      if (!formatted.isEmpty()) {
        formatted.append(" ");
      }
      formatted
          .append(Character.toUpperCase(part.charAt(0)))
          .append(part.substring(1).toLowerCase());
    }
    return formatted.toString();
  }
}
