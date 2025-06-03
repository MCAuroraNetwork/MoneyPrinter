package club.aurorapvp.moneyprinter.modules.customization.gui;

import club.aurorapvp.aurorachat.modules.NameTag;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collections;

public class CustomizationGUI implements InventoryHolder, Listener {
    private final Inventory inventory;
    private final Player player;
    private final NameTag nameTag;

    public CustomizationGUI(Player player) {
        this.player = player;
        this.nameTag = NameTag.getNameTags().get(player.getUniqueId());
        if (this.nameTag == null) {
            throw new IllegalStateException("NameTag not initialized for player: " + player.getName());
        }
        this.inventory = Bukkit.createInventory(this, 9, Component.text("Customize Cosmetics"));
        populateMainGUI();
        Bukkit.getPluginManager().registerEvents(this, MoneyPrinter.getInstance());
    }

    private void populateMainGUI() {
        ItemStack nameItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta nameMeta = (SkullMeta) nameItem.getItemMeta();
        nameMeta.setOwningPlayer(player);
        nameMeta.displayName(Component.text("Customize Display Name").color(NamedTextColor.GOLD));
        nameMeta.lore(Collections.singletonList(Component.text("Click to customize displayname").color(NamedTextColor.GRAY)));
        nameItem.setItemMeta(nameMeta);

        ItemStack armorItem = new ItemStack(Material.IRON_CHESTPLATE);
        armorItem.editMeta(meta -> {
            meta.displayName(Component.text("Customize Armor Trim").color(NamedTextColor.GOLD));
            meta.lore(Collections.singletonList(Component.text("Click to customize armor trim").color(NamedTextColor.GRAY)));
        });

        ItemStack shieldItem = new ItemStack(Material.SHIELD);
        shieldItem.editMeta(meta -> {
            meta.displayName(Component.text("Customize Shield Banner").color(NamedTextColor.GOLD));
            meta.lore(Collections.singletonList(Component.text("Click to customize shield banner").color(NamedTextColor.GRAY)));
        });

        inventory.setItem(2, nameItem);
        inventory.setItem(4, armorItem);
        inventory.setItem(6, shieldItem);
    }

    public void open() {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) return;
        event.setCancelled(true);
        int slot = event.getSlot();
        switch (slot) {
            case 2:
                new NameCustomizationGUI(player, nameTag).open();
                HandlerList.unregisterAll(this);
                break;
            case 4:
                new ArmorSelectionGUI(player).open();
                HandlerList.unregisterAll(this);
                break;
            case 6:
                new ShieldCustomizationGUI(player).open();
                HandlerList.unregisterAll(this);
                break;
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