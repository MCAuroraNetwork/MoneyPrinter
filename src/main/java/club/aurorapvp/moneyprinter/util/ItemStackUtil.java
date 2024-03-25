package club.aurorapvp.moneyprinter.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemStackUtil {

  public static boolean hasBeenRenamed(ItemStack itemStack) {

    String defaultName =
    // Replace underscores with spaces and capitalize the first letter
    defaultName = defaultName.replace("_", " ");
    defaultName = defaultName.substring(0, 1).toUpperCase() + defaultName.substring(1).toLowerCase();
    // Compare the display name and the default name
    return !displayName.equals(defaultName);
  }
}
