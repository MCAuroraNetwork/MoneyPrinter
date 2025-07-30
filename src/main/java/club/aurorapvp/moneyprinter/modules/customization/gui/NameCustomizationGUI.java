package club.aurorapvp.moneyprinter.modules.customization.gui;

import club.aurorapvp.aurorachat.modules.DisplayName;
import club.aurorapvp.aurorachat.modules.NameTag;
import club.aurorapvp.aurorachat.util.ComponentUtil;
import club.aurorapvp.aurorachat.util.ExtendedTextColor;
import club.aurorapvp.moneyprinter.MoneyPrinter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class NameCustomizationGUI implements InventoryHolder, Listener {
  private final Player player;
  private final NameTag nameTag;
  private final Inventory inventory;
  private int currentFrameIndex = 0;

  private static final List<Material> SELECTION_DYES = List.of(
          Material.WHITE_DYE, Material.ORANGE_DYE, Material.MAGENTA_DYE, Material.LIGHT_BLUE_DYE,
          Material.YELLOW_DYE, Material.LIME_DYE, Material.PINK_DYE, Material.GRAY_DYE,
          Material.LIGHT_GRAY_DYE, Material.CYAN_DYE, Material.PURPLE_DYE, Material.BLUE_DYE,
          Material.BROWN_DYE, Material.GREEN_DYE, Material.RED_DYE, Material.BLACK_DYE
  );

  private static final Map<Material, TextColor> DYE_COLORS =
          Map.ofEntries(
                  entry(Material.WHITE_DYE, ExtendedTextColor.WHITE),
                  entry(Material.ORANGE_DYE, ExtendedTextColor.ORANGE),
                  entry(Material.MAGENTA_DYE, ExtendedTextColor.MAGENTA),
                  entry(Material.LIGHT_BLUE_DYE, ExtendedTextColor.LIGHT_BLUE),
                  entry(Material.YELLOW_DYE, ExtendedTextColor.YELLOW),
                  entry(Material.LIME_DYE, ExtendedTextColor.LIME),
                  entry(Material.PINK_DYE, ExtendedTextColor.PINK),
                  entry(Material.GRAY_DYE, ExtendedTextColor.GRAY),
                  entry(Material.LIGHT_GRAY_DYE, ExtendedTextColor.LIGHT_GRAY),
                  entry(Material.CYAN_DYE, ExtendedTextColor.CYAN),
                  entry(Material.PURPLE_DYE, ExtendedTextColor.PURPLE),
                  entry(Material.BLUE_DYE, ExtendedTextColor.BLUE),
                  entry(Material.BROWN_DYE, ExtendedTextColor.BROWN),
                  entry(Material.GREEN_DYE, ExtendedTextColor.GREEN),
                  entry(Material.RED_DYE, ExtendedTextColor.RED),
                  entry(Material.BLACK_DYE, ExtendedTextColor.BLACK)
          );

  private static final Map<Material, String> DYE_NAMES =
          Map.ofEntries(
                  entry(Material.WHITE_DYE, "White"),
                  entry(Material.ORANGE_DYE, "Orange"),
                  entry(Material.MAGENTA_DYE, "Magenta"),
                  entry(Material.LIGHT_BLUE_DYE, "Light Blue"),
                  entry(Material.YELLOW_DYE, "Yellow"),
                  entry(Material.LIME_DYE, "Lime"),
                  entry(Material.PINK_DYE, "Pink"),
                  entry(Material.GRAY_DYE, "Gray"),
                  entry(Material.LIGHT_GRAY_DYE, "Light Gray"),
                  entry(Material.CYAN_DYE, "Cyan"),
                  entry(Material.PURPLE_DYE, "Purple"),
                  entry(Material.BLUE_DYE, "Blue"),
                  entry(Material.BROWN_DYE, "Brown"),
                  entry(Material.GREEN_DYE, "Green"),
                  entry(Material.RED_DYE, "Red"),
                  entry(Material.BLACK_DYE, "Black")
          );

  private static final List<Integer> DYE_SLOTS = List.of(0, 9, 18, 27, 1, 10, 19, 28, 2, 11, 20, 29, 3, 12, 21, 30);
  private static final List<Integer> SELECTED_SLOTS = List.of(17, 26, 35, 44);

  public NameCustomizationGUI(Player player, NameTag nameTag) {
    this.player = player;
    this.nameTag = nameTag;
    this.inventory = Bukkit.createInventory(
            this, 54, MoneyPrinter.getInstance().getLang().getComponent("customize-display-name"));
    DisplayName displayName = nameTag.getDisplayName();
    List<List<TextColor>> frameColors = displayName.getFrameColors();
    if (frameColors == null) {
      frameColors = new ArrayList<>();
      frameColors.add(new ArrayList<>());
      displayName.setFrameColors(frameColors);
    }
    if (!frameColors.isEmpty()) {
      for (int i = 0; i < frameColors.size(); i++) {
        if (frameColors.get(i) != null && !frameColors.get(i).isEmpty()) {
          currentFrameIndex = i;
          break;
        }
      }
    }
    populateGUI();
    Bukkit.getPluginManager().registerEvents(this, MoneyPrinter.getInstance());
  }

  private void populateGUI() {
    DisplayName displayName = nameTag.getDisplayName();
    List<List<TextColor>> frameColors = displayName.getFrameColors();
    if (frameColors == null) {
      frameColors = new ArrayList<>();
      frameColors.add(new ArrayList<>());
      displayName.setFrameColors(frameColors);
    }
    if (frameColors.isEmpty()) {
      frameColors.add(new ArrayList<>());
    }
    if (currentFrameIndex >= frameColors.size() || frameColors.get(currentFrameIndex) == null) {
      currentFrameIndex = 0;
    }
    List<TextColor> colors = frameColors.get(currentFrameIndex);

    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
    head.editMeta(
            SkullMeta.class,
            meta -> {
              meta.setOwningPlayer(player);
              Component gradient = ComponentUtil.createGradient(
                      player.getName(), colors.isEmpty() ? List.of(ExtendedTextColor.WHITE) : colors);
              meta.displayName(gradient);
            });
    inventory.setItem(8, head);

    for (int i = 0; i < 4; i++) {
      int slot = SELECTED_SLOTS.get(i);
      if (colors != null && i < colors.size()) {
        Material dye = getDyeMaterial(colors.get(i));
        if (dye != null) {
          inventory.setItem(slot, new ItemStack(dye));
        } else {
          inventory.setItem(slot, null);
        }
      } else {
        inventory.setItem(slot, null);
      }
    }

    for (int i = 0; i < SELECTION_DYES.size(); i++) {
      Material dyeType = SELECTION_DYES.get(i);
      String colorName = DYE_NAMES.get(dyeType);
      ItemStack dye = new ItemStack(dyeType);
      dye.editMeta(m -> m.displayName(
              MoneyPrinter.getInstance().getLang().formatComponent("add-color", colorName)));
      int slot = DYE_SLOTS.get(i);
      inventory.setItem(slot, dye);
    }

    ItemStack previousFrame = new ItemStack(Material.ARROW);
    previousFrame.editMeta(
            m -> m.displayName(MoneyPrinter.getInstance().getLang().getComponent("previous-frame")));
    inventory.setItem(45, previousFrame);

    ItemStack nextFrame = new ItemStack(Material.ARROW);
    nextFrame.editMeta(
            m -> m.displayName(MoneyPrinter.getInstance().getLang().getComponent("next-frame")));
    inventory.setItem(46, nextFrame);

    ItemStack preview = new ItemStack(Material.ENDER_EYE);
    preview.editMeta(
            m -> m.displayName(MoneyPrinter.getInstance().getLang().getComponent("preview-animation")));
    inventory.setItem(47, preview);

    ItemStack deleteFrame = new ItemStack(Material.TNT);
    deleteFrame.editMeta(
            m -> m.displayName(MoneyPrinter.getInstance().getLang().getComponent("delete-frame")));
    inventory.setItem(48, frameColors.size() > 1 ? deleteFrame : null);

    boolean isEnabled = displayName.isPrefixEnabled();
    Material toggleMaterial = isEnabled ? Material.GREEN_WOOL : Material.RED_WOOL;
    ItemStack toggle = new ItemStack(toggleMaterial);
    toggle.editMeta(
            m -> m.displayName(
                    MoneyPrinter.getInstance().getLang().getComponent("toggle-prefix-suffix")));
    inventory.setItem(49, toggle);

    ItemStack reset = new ItemStack(Material.STRUCTURE_VOID);
    reset.editMeta(m -> m.displayName(MoneyPrinter.getInstance().getLang().getComponent("reset")));
    inventory.setItem(50, reset);

    int refreshRate = displayName.getRefreshRate();
    ItemStack decreaseRate = new ItemStack(Material.REDSTONE);
    decreaseRate.editMeta(
            m -> m.displayName(
                    MoneyPrinter.getInstance().getLang().formatComponent("decrease-refresh-rate", refreshRate)));
    inventory.setItem(51, decreaseRate);

    ItemStack increaseRate = new ItemStack(Material.EMERALD);
    increaseRate.editMeta(
            m -> m.displayName(
                    MoneyPrinter.getInstance().getLang().formatComponent("increase-refresh-rate", refreshRate)));
    inventory.setItem(52, increaseRate);

    ItemStack backButton = new ItemStack(Material.BARRIER);
    backButton.editMeta(m -> m.displayName(MoneyPrinter.getInstance().getLang().getComponent("back-to-customization")));
    inventory.setItem(53, backButton);
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getInventory().getHolder() != this) return;
    event.setCancelled(true);
    int slot = event.getSlot();
    DisplayName displayName = nameTag.getDisplayName();
    List<List<TextColor>> frameColors = new ArrayList<>(displayName.getFrameColors());
    if (frameColors.isEmpty()) {
      frameColors.add(new ArrayList<>());
    }
    if (currentFrameIndex >= frameColors.size()) {
      currentFrameIndex = 0;
    }
    List<TextColor> colors = frameColors.get(currentFrameIndex);

    if (DYE_SLOTS.contains(slot)) {
      int index = DYE_SLOTS.indexOf(slot);
      Material dyeType = SELECTION_DYES.get(index);
      TextColor color = DYE_COLORS.get(dyeType);
      if (color != null && colors.size() < 4) {
        colors.add(color);
        frameColors.set(currentFrameIndex, colors);
        displayName.setFrameColors(frameColors);
        populateGUI();
        player.updateInventory();
      }
    } else if (SELECTED_SLOTS.contains(slot)) {
      int index = SELECTED_SLOTS.indexOf(slot);
      if (index < colors.size()) {
        colors.remove(index);
        frameColors.set(currentFrameIndex, colors);
        displayName.setFrameColors(frameColors);
        populateGUI();
        player.updateInventory();
      }
    } else if (slot == 45) {
      int totalFrames = frameColors.size();
      currentFrameIndex = (currentFrameIndex - 1 + totalFrames) % totalFrames;
      populateGUI();
      player.updateInventory();
    } else if (slot == 46) {
      int totalFrames = frameColors.size();
      if (currentFrameIndex == totalFrames - 1 && !frameColors.get(totalFrames - 1).isEmpty() && totalFrames < 10) {
        frameColors.add(new ArrayList<>());
        currentFrameIndex++;
      } else {
        currentFrameIndex = (currentFrameIndex + 1) % totalFrames;
      }
      populateGUI();
      player.updateInventory();
    } else if (slot == 47) {
      new PreviewAnimationGUI(player, nameTag).open();
    } else if (slot == 48 && frameColors.size() > 1) {
      frameColors.remove(currentFrameIndex);
      if (currentFrameIndex >= frameColors.size()) {
        currentFrameIndex = frameColors.size() - 1;
      }
      displayName.setFrameColors(frameColors);
      populateGUI();
      player.updateInventory();
    } else if (slot == 49) {
      boolean isEnabled = !displayName.isPrefixEnabled();
      displayName.setPrefixEnabled(isEnabled);
      displayName.setSuffixEnabled(isEnabled);
      populateGUI();
      player.updateInventory();
    } else if (slot == 50) {
      List<List<TextColor>> newFrameColors = new ArrayList<>();
      displayName.setFrameColors(newFrameColors);
      nameTag.setFrameDisplayNameColors(newFrameColors);
      currentFrameIndex = 0;
      populateGUI();
      player.updateInventory();
    } else if (slot == 51) {
      int currentRate = displayName.getRefreshRate();
      if (currentRate > 1) {
        displayName.setRefreshRate(currentRate - 1);
        populateGUI();
        player.updateInventory();
      }
    } else if (slot == 52) {
      int currentRate = displayName.getRefreshRate();
      if (currentRate < 10) {
        displayName.setRefreshRate(currentRate + 1);
        populateGUI();
        player.updateInventory();
      }
    } else if (slot == 53) {
      new CustomizationGUI(player).open();
      HandlerList.unregisterAll(this);
    }
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    if (event.getInventory().getHolder() == this) {
      DisplayName displayName = nameTag.getDisplayName();
      List<List<TextColor>> frameColors = new ArrayList<>(displayName.getFrameColors());
      frameColors.removeIf(List::isEmpty);
      displayName.setFrameColors(frameColors);
      displayName.save();
      HandlerList.unregisterAll(this);
    }
  }

  private Material getDyeMaterial(TextColor color) {
    for (Map.Entry<Material, TextColor> entry : DYE_COLORS.entrySet()) {
      if (entry.getValue().equals(color)) {
        return entry.getKey();
      }
    }
    Material closestDye = null;
    double minDistance = Double.MAX_VALUE;
    for (Map.Entry<Material, TextColor> entry : DYE_COLORS.entrySet()) {
      double distance = colorDistance(color, entry.getValue());
      if (distance < minDistance) {
        minDistance = distance;
        closestDye = entry.getKey();
      }
    }
    return closestDye;
  }

  private double colorDistance(TextColor color1, TextColor color2) {
    int r1 = color1.red();
    int g1 = color1.green();
    int b1 = color1.blue();
    int r2 = color2.red();
    int g2 = color2.green();
    int b2 = color2.blue();
    return Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));
  }

  public void open() {
    player.openInventory(inventory);
  }

  @Override
  public Inventory getInventory() {
    return inventory;
  }
}

class PreviewAnimationGUI implements InventoryHolder, Listener {
  private final Player player;
  private final NameTag nameTag;
  private final Inventory inventory;
  private BukkitRunnable animationTask;

  public PreviewAnimationGUI(Player player, NameTag nameTag) {
    this.player = player;
    this.nameTag = nameTag;
    this.inventory =
            Bukkit.createInventory(
                    this, 9, MoneyPrinter.getInstance().getLang().getComponent("preview-animation"));
    populateGUI();
    Bukkit.getPluginManager().registerEvents(this, MoneyPrinter.getInstance());
  }

  private void populateGUI() {
    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
    head.editMeta(
            SkullMeta.class,
            meta -> {
              meta.setOwningPlayer(player);
              meta.displayName(Component.text("Preview").color(NamedTextColor.GOLD));
            });
    inventory.setItem(4, head);

    ItemStack back = new ItemStack(Material.BARRIER);
    back.editMeta(m -> m.displayName(MoneyPrinter.getInstance().getLang().getComponent("back")));
    inventory.setItem(8, back);
  }

  public void open() {
    player.openInventory(inventory);
    startAnimation();
  }

  private void startAnimation() {
    List<List<TextColor>> frameColors = nameTag.getDisplayName().getFrameColors();
    int refreshRate = nameTag.getDisplayName().getRefreshRate();
    long ticksPerFrame = Math.max(2, 20 / refreshRate);

    animationTask =
            new BukkitRunnable() {
              int current = 0;

              @Override
              public void run() {
                if (frameColors.isEmpty()) {
                  cancel();
                  return;
                }
                List<TextColor> frame = frameColors.get(current % frameColors.size());
                Component displayName =
                        ComponentUtil.createGradient(
                                player.getName(), frame.isEmpty() ? List.of(NamedTextColor.WHITE) : frame);
                ItemStack head = inventory.getItem(4);
                if (head != null) {
                  head.editMeta(
                          SkullMeta.class,
                          meta -> {
                            meta.setOwningPlayer(player);
                            meta.displayName(displayName);
                          });
                }
                current++;
              }
            };
    animationTask.runTaskTimer(MoneyPrinter.getInstance(), 0, ticksPerFrame);
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getInventory().getHolder() != this) return;
    event.setCancelled(true);
    if (event.getSlot() == 8) {
      if (animationTask != null) {
        animationTask.cancel();
      }
      new NameCustomizationGUI(player, nameTag).open();
      HandlerList.unregisterAll(this);
    }
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    if (event.getInventory().getHolder() == this) {
      if (animationTask != null) {
        animationTask.cancel();
      }
      HandlerList.unregisterAll(this);
    }
  }

  @Override
  public Inventory getInventory() {
    return inventory;
  }
}