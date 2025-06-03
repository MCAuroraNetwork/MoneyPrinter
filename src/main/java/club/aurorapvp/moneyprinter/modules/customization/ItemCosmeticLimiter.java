package club.aurorapvp.moneyprinter.modules.customization;

import club.aurorapvp.moneyprinter.MoneyPrinter;
import club.aurorapvp.moneyprinter.util.ItemStackUtil;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ItemCosmeticLimiter {
  private static final Map<UUID, Map<Integer, Long>> lastUpdateTimes = new HashMap<>();
  private static final long DEBOUNCE_TICKS = 10; // 0.5 seconds

  public static void onObtainItem(PlayerInventorySlotChangeEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    int slot = event.getSlot();
    long currentTime = Bukkit.getCurrentTick();

    lastUpdateTimes.computeIfAbsent(playerId, k -> new HashMap<>());
    Long lastUpdate = lastUpdateTimes.get(playerId).get(slot);
    if (lastUpdate != null && currentTime - lastUpdate < DEBOUNCE_TICKS) {
      return;
    }
    lastUpdateTimes.get(playerId).put(slot, currentTime);

    ItemStack itemStack = event.getNewItemStack();
    if (itemStack.getItemMeta() == null) {
      return;
    }

    // Handle item renaming permissions
    if (!player.hasPermission("moneyprinter.renameitems") && ItemStackUtil.hasBeenRenamed(itemStack)) {
      ItemStackUtil.renameToPlainText(itemStack);
      player.sendMessage(MoneyPrinter.getInstance().getLang().getComponent("cannot-use-personalized"));
      player.getInventory().setItem(slot, itemStack);
    }

    CosmeticSettingsManager settingsManager = CosmeticSettingsManager.getInstance(player);

    // Handle armor customization
    if (player.hasPermission("moneyprinter.customize.armor") && itemStack.getItemMeta() instanceof ArmorMeta armorMeta) {
      String piece = getArmorPieceType(itemStack.getType());
      if (piece != null) {
        String patternKey = settingsManager.getArmorTrimPattern(piece);
        String materialKey = settingsManager.getArmorTrimMaterial(piece);
        if (patternKey != null && materialKey != null) {
          new BukkitRunnable() {
            @Override
            public void run() {
              TrimPattern trimPattern = Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(patternKey));
              TrimMaterial trimMaterial = Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(materialKey));
              if (trimPattern != null && trimMaterial != null) {
                ArmorTrim armorTrim = new ArmorTrim(trimMaterial, trimPattern);
                armorMeta.setTrim(armorTrim);
                itemStack.setItemMeta(armorMeta);
                player.getInventory().setItem(slot, itemStack);
              }
            }
          }.runTaskLater(MoneyPrinter.getInstance(), 2L);
        }
      }
    }

    // Handle shield customization
    if (player.hasPermission("moneyprinter.customize.shield") && itemStack.getType() == Material.SHIELD) {
      List<String> patternStrings = settingsManager.getShieldBannerPatterns();
      String baseColorStr = settingsManager.getShieldBaseColor();
      ItemStack oldItem = event.getOldItemStack();
      boolean isNewShield = oldItem == null || oldItem.getType() == Material.AIR;

      if (isNewShield || !isShieldCustomizedCorrectly(itemStack, baseColorStr, patternStrings)) {
        new BukkitRunnable() {
          @Override
          public void run() {
            BlockStateMeta shieldMeta = (BlockStateMeta) itemStack.getItemMeta();
            Banner banner = (Banner) shieldMeta.getBlockState();

            DyeColor baseColor;
            try {
              baseColor = DyeColor.valueOf(baseColorStr);
            } catch (IllegalArgumentException e) {
              baseColor = DyeColor.WHITE;
              MoneyPrinter.getInstance().getLogger().warning("Invalid base color for shield: " + baseColorStr);
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
            itemStack.setItemMeta(shieldMeta);
            player.getInventory().setItem(slot, itemStack);
          }
        }.runTaskLater(MoneyPrinter.getInstance(), 2L);
      }
    }

    settingsManager.save();
  }

  private static boolean isShieldCustomizedCorrectly(ItemStack shield, String baseColorStr, List<String> patternStrings) {
    if (!(shield.getItemMeta() instanceof BlockStateMeta shieldMeta)) {
      return false;
    }
    if (!(shieldMeta.getBlockState() instanceof Banner banner)) {
      return false;
    }
    DyeColor currentBaseColor = banner.getBaseColor();
    List<Pattern> currentPatterns = banner.getPatterns();
    List<String> currentPatternStrings = currentPatterns.stream()
            .map(p -> p.getPattern().name() + ":" + p.getColor().name())
            .collect(Collectors.toList());
    return currentBaseColor.name().equals(baseColorStr) && currentPatternStrings.equals(patternStrings);
  }

  private static String getArmorPieceType(Material material) {
    if (material.name().endsWith("_HELMET")) return "helmet";
    if (material.name().endsWith("_CHESTPLATE")) return "chestplate";
    if (material.name().endsWith("_LEGGINGS")) return "leggings";
    if (material.name().endsWith("_BOOTS")) return "boots";
    return null;
  }

  private static String capitalizeFirstLetter(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
  }
}