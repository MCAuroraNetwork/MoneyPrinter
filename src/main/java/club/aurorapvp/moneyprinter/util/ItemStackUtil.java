package club.aurorapvp.moneyprinter.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackUtil {

  public static boolean hasBeenRenamed(ItemStack itemStack) {
    ItemStack defaultItemState = new ItemStack(itemStack.getType(), itemStack.getAmount());

    defaultItemState.addEnchantments(itemStack.getEnchantments());

    Component defaultName = defaultItemState.displayName();

    Component itemName = itemStack.displayName();

    String originalName = PlainTextComponentSerializer.plainText().serialize(defaultName);

    String customName = PlainTextComponentSerializer.plainText().serialize(itemName);

    return !itemName.decorations().equals(defaultName.decorations()) && !originalName.equals(
        customName);
  }

  public static void renameToPlainText(ItemStack itemStack) {
    Component displayName = itemStack.displayName();

    ItemMeta meta = itemStack.getItemMeta();

    String displayNameStr = PlainTextComponentSerializer.plainText().serialize(displayName);

    displayNameStr = displayNameStr.replace("[", "").replace("]", "");

    Component displayNameWithoutBrackets = Component.text(displayNameStr);

    meta.displayName(displayNameWithoutBrackets);

    itemStack.setItemMeta(meta);
  }
}
