package club.aurorapvp.moneyprinter.modules.customization.gui;

import club.aurorapvp.moneyprinter.MoneyPrinter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

public class ArmorSelectionGUI implements InventoryHolder, Listener {
  private final Inventory inventory;
  private final Player player;

  public ArmorSelectionGUI(Player player) {
    this.player = player;
    this.inventory =
        Bukkit.createInventory(
            this, 9, MoneyPrinter.getInstance().getLang().getComponent("select-armor-piece"));
    populateGUI();
    Bukkit.getPluginManager().registerEvents(this, MoneyPrinter.getInstance());
  }

  private void populateGUI() {
    ItemStack helmet =
        player.getInventory().getHelmet() != null
            ? player.getInventory().getHelmet().clone()
            : new ItemStack(Material.LEATHER_HELMET);
    helmet.editMeta(meta -> meta.displayName(Component.text("Helmet").color(NamedTextColor.GOLD)));
    inventory.setItem(2, helmet);

    ItemStack chestplate =
        player.getInventory().getChestplate() != null
            ? player.getInventory().getChestplate().clone()
            : new ItemStack(Material.LEATHER_CHESTPLATE);
    chestplate.editMeta(
        meta -> meta.displayName(Component.text("Chestplate").color(NamedTextColor.GOLD)));
    inventory.setItem(3, chestplate);

    ItemStack leggings =
        player.getInventory().getLeggings() != null
            ? player.getInventory().getLeggings().clone()
            : new ItemStack(Material.LEATHER_LEGGINGS);
    leggings.editMeta(
        meta -> meta.displayName(Component.text("Leggings").color(NamedTextColor.GOLD)));
    inventory.setItem(5, leggings);

    ItemStack boots =
        player.getInventory().getBoots() != null
            ? player.getInventory().getBoots().clone()
            : new ItemStack(Material.LEATHER_BOOTS);
    boots.editMeta(meta -> meta.displayName(Component.text("Boots").color(NamedTextColor.GOLD)));
    inventory.setItem(6, boots);

    ItemStack back = new ItemStack(Material.BARRIER);
    back.editMeta(
        meta -> meta.displayName(MoneyPrinter.getInstance().getLang().getComponent("back")));
    inventory.setItem(8, back);
  }

  public void open() {
    player.openInventory(inventory);
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getInventory().getHolder() != this) return;
    event.setCancelled(true);
    int slot = event.getSlot();
    String armorType = null;
    if (slot == 2) armorType = "HELMET";
    else if (slot == 3) armorType = "CHESTPLATE";
    else if (slot == 5) armorType = "LEGGINGS";
    else if (slot == 6) armorType = "BOOTS";
    else if (slot == 8) {
      new CustomizationGUI(player).open();
      HandlerList.unregisterAll(this);
      return;
    }
    if (armorType != null) {
      new TrimCustomizationGUI(player, armorType).open();
      HandlerList.unregisterAll(this);
    }
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    if (event.getInventory().getHolder() == this) {
      HandlerList.unregisterAll(this);
    }
  }

  @Override
  public Inventory getInventory() {
    return inventory;
  }
}
