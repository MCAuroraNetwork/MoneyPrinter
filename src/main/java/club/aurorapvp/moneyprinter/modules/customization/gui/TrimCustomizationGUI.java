package club.aurorapvp.moneyprinter.modules.customization.gui;

import club.aurorapvp.moneyprinter.MoneyPrinter;
import club.aurorapvp.moneyprinter.modules.customization.CosmeticSettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrimCustomizationGUI implements InventoryHolder, Listener {
  private final Inventory inventory;
  private final Player player;
  private final String armorType;
  private final CosmeticSettingsManager settingsManager;
  private String selectedPattern;
  private String selectedMaterial;
  private int currentMaterialIndex;
  private final List<Material> materials;

  private static final List<Map.Entry<Material, String>> TRIM_PATTERNS = List.of(
          Map.entry(Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, "sentry"),
          Map.entry(Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, "dune"),
          Map.entry(Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, "coast"),
          Map.entry(Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, "wild"),
          Map.entry(Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, "ward"),
          Map.entry(Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, "eye"),
          Map.entry(Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, "vex"),
          Map.entry(Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, "tide"),
          Map.entry(Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, "snout"),
          Map.entry(Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, "rib"),
          Map.entry(Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, "spire"),
          Map.entry(Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, "wayfinder"),
          Map.entry(Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, "shaper"),
          Map.entry(Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, "silence"),
          Map.entry(Material.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, "raiser"),
          Map.entry(Material.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, "host"),
          Map.entry(Material.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, "flow"),
          Map.entry(Material.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, "bolt")
  );

  private static final List<Integer> TRIM_SLOTS = List.of(
          0, 9, 18, 27, 36, 45,  // Column 0
          1, 10, 19, 28, 37, 46, // Column 1
          2, 11, 20, 29, 38, 47  // Column 2
  );

  private static final List<Integer> MATERIAL_SLOTS = List.of(
          3, 12, 21, 30, 39,  // Column 3, rows 0-4
          4, 13, 22, 31, 40   // Column 4, rows 0-4
  );

  public TrimCustomizationGUI(Player player, String armorType) {
    this.player = player;
    this.armorType = armorType.toLowerCase();
    this.settingsManager = CosmeticSettingsManager.getInstance(player);
    this.selectedPattern = settingsManager.getArmorTrimPattern(this.armorType);
    this.selectedMaterial = settingsManager.getArmorTrimMaterial(this.armorType);
    this.materials = getMaterialsForType(this.armorType);
    ItemStack worn = getWornArmor();
    if (worn != null && materials.contains(worn.getType())) {
      currentMaterialIndex = materials.indexOf(worn.getType());
    } else {
      currentMaterialIndex = 0;
    }
    String armorPiece = capitalizeFirstLetter(this.armorType);
    this.inventory = Bukkit.createInventory(this, 54, MoneyPrinter.getInstance().getLang().formatComponent("customize-armor-trim", armorPiece));
    populateGUI();
    Bukkit.getPluginManager().registerEvents(this, MoneyPrinter.getInstance());
  }

  private List<Material> getMaterialsForType(String armorType) {
    return switch (armorType) {
      case "helmet" -> List.of(Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.GOLDEN_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET);
      case "chestplate" -> List.of(Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE);
      case "leggings" -> List.of(Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS);
      case "boots" -> List.of(Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS);
      default -> List.of();
    };
  }

  private ItemStack getWornArmor() {
    return switch (armorType) {
      case "helmet" -> player.getInventory().getHelmet();
      case "chestplate" -> player.getInventory().getChestplate();
      case "leggings" -> player.getInventory().getLeggings();
      case "boots" -> player.getInventory().getBoots();
      default -> null;
    };
  }

  private Material getDisplayMaterial(TrimMaterial trimMaterial) {
    String key = trimMaterial.getKey().getKey();
      return switch (key) {
          case "iron" -> Material.IRON_INGOT;
          case "gold" -> Material.GOLD_INGOT;
          case "diamond" -> Material.DIAMOND;
          case "emerald" -> Material.EMERALD;
          case "lapis" -> Material.LAPIS_LAZULI;
          case "amethyst" -> Material.AMETHYST_SHARD;
          case "copper" -> Material.COPPER_INGOT;
          case "netherite" -> Material.NETHERITE_INGOT;
          case "quartz" -> Material.QUARTZ;
          case "redstone" -> Material.REDSTONE;
          case "resin" -> Material.RESIN_BLOCK;
          default -> Material.STONE;
      };
  }

  private void populateGUI() {
    inventory.clear();

    for (int i = 0; i < TRIM_PATTERNS.size() && i < TRIM_SLOTS.size(); i++) {
      int slot = TRIM_SLOTS.get(i);
      Material templateMaterial = TRIM_PATTERNS.get(i).getKey();
      String patternKey = TRIM_PATTERNS.get(i).getValue();
      String patternName = capitalizeFirstLetter(patternKey);
      ItemStack item = new ItemStack(templateMaterial);
      item.editMeta(meta -> meta.displayName(MoneyPrinter.getInstance().getLang().formatComponent("trim-pattern", patternName)));
      inventory.setItem(slot, item);
    }

    List<TrimMaterial> materialsList = Registry.TRIM_MATERIAL.stream().collect(Collectors.toList());
    for (int i = 0; i < materialsList.size() && i < MATERIAL_SLOTS.size(); i++) {
      int slot = MATERIAL_SLOTS.get(i);
      TrimMaterial material = materialsList.get(i);
      String materialKey = material.getKey().getKey();
      String materialName = capitalizeFirstLetter(materialKey);
      Material displayMaterial = getDisplayMaterial(material);
      ItemStack item = new ItemStack(displayMaterial);
      item.editMeta(meta -> meta.displayName(MoneyPrinter.getInstance().getLang().formatComponent("trim-material", materialName)));
      inventory.setItem(slot, item);
    }

    Material currentMaterial = materials.get(currentMaterialIndex);
    ItemStack armorItem = new ItemStack(currentMaterial);
    if (selectedPattern != null && selectedMaterial != null) {
      TrimPattern pattern = Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(selectedPattern));
      TrimMaterial material = Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(selectedMaterial));
      if (pattern != null && material != null && armorItem.getItemMeta() instanceof ArmorMeta armorMeta) {
        ArmorTrim trim = new ArmorTrim(material, pattern);
        armorMeta.setTrim(trim);
        armorItem.setItemMeta(armorMeta);
      }
    }
    inventory.setItem(8, armorItem);

    if (selectedPattern != null) {
      String patternName = capitalizeFirstLetter(selectedPattern);
      Material templateMaterial = TRIM_PATTERNS.stream()
              .filter(entry -> entry.getValue().equals(selectedPattern))
              .map(Map.Entry::getKey)
              .findFirst()
              .orElse(Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE);
      ItemStack selectedPatternItem = new ItemStack(templateMaterial);
      selectedPatternItem.editMeta(meta -> meta.displayName(MoneyPrinter.getInstance().getLang().formatComponent("selected-trim-pattern", patternName)));
      inventory.setItem(17, selectedPatternItem);
    } else {
      inventory.setItem(17, null);
    }

    if (selectedMaterial != null) {
      TrimMaterial trimMaterial = Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(selectedMaterial));
      if (trimMaterial != null) {
        String materialName = capitalizeFirstLetter(selectedMaterial);
        Material displayMaterial = getDisplayMaterial(trimMaterial);
        ItemStack selectedMaterialItem = new ItemStack(displayMaterial);
        selectedMaterialItem.editMeta(meta -> meta.displayName(MoneyPrinter.getInstance().getLang().formatComponent("selected-trim-material", materialName)));
        inventory.setItem(26, selectedMaterialItem);
      }
    } else {
      inventory.setItem(26, null);
    }

    ItemStack clear = new ItemStack(Material.TNT);
    clear.editMeta(meta -> meta.displayName(MoneyPrinter.getInstance().getLang().getComponent("clear-trim")));
    inventory.setItem(44, clear);

    ItemStack back = new ItemStack(Material.BARRIER);
    back.editMeta(meta -> meta.displayName(MoneyPrinter.getInstance().getLang().getComponent("back")));
    inventory.setItem(53, back);
  }

  private String capitalizeFirstLetter(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
  }

  public void open() {
    player.openInventory(inventory);
  }

  @Override
  public Inventory getInventory() {
    return inventory;
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getInventory().getHolder() != this) return;
    event.setCancelled(true);
    int slot = event.getSlot();
    if (TRIM_SLOTS.contains(slot)) {
      int index = TRIM_SLOTS.indexOf(slot);
      if (index < TRIM_PATTERNS.size()) {
        selectedPattern = TRIM_PATTERNS.get(index).getValue();
        populateGUI();
        applyTrimToWornArmor();
      }
    } else if (MATERIAL_SLOTS.contains(slot)) {
      int index = MATERIAL_SLOTS.indexOf(slot);
      List<TrimMaterial> materialsList = Registry.TRIM_MATERIAL.stream().collect(Collectors.toList());
      if (index < materialsList.size()) {
        selectedMaterial = materialsList.get(index).getKey().getKey();
        populateGUI();
        applyTrimToWornArmor();
      }
    } else if (slot == 44) {
      selectedPattern = null;
      selectedMaterial = null;
      populateGUI();
      applyTrimToWornArmor();
    } else if (slot == 53) {
      new ArmorSelectionGUI(player).open();
      HandlerList.unregisterAll(this);
    }
  }

  private void applyTrimToWornArmor() {
    ItemStack worn = getWornArmor();
    if (worn != null && worn.getItemMeta() instanceof ArmorMeta armorMeta) {
      if (selectedPattern != null && selectedMaterial != null) {
        TrimPattern pattern = Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(selectedPattern));
        TrimMaterial material = Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(selectedMaterial));
        if (pattern != null && material != null) {
          ArmorTrim trim = new ArmorTrim(material, pattern);
          armorMeta.setTrim(trim);
        } else {
          armorMeta.setTrim(null);
        }
      } else {
        armorMeta.setTrim(null);
      }
      worn.setItemMeta(armorMeta);
    }
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    if (event.getInventory().getHolder() == this) {
      settingsManager.setArmorTrim(armorType, selectedPattern, selectedMaterial);
      HandlerList.unregisterAll(this);
    }
  }
}