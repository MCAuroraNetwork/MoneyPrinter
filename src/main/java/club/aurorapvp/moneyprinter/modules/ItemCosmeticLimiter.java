package club.aurorapvp.moneyprinter.modules;

import club.aurorapvp.moneyprinter.MoneyPrinter;
import club.aurorapvp.moneyprinter.util.ItemStackUtil;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import java.util.logging.Level;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class ItemCosmeticLimiter {

  public static void onObtainItem(PlayerInventorySlotChangeEvent event) {
    if (event.getPlayer().hasPermission("moneyprinter.renameitems")) {
      MoneyPrinter.getInstance().getLogger().log(Level.INFO, "joe");
      return;
    }

    ItemStack itemStack = event.getNewItemStack();

    if (itemStack.getItemMeta() == null) {
      return;
    }

    if (!ItemStackUtil.hasBeenRenamed(itemStack)) {
      return;
    }

    ItemStackUtil.renameToPlainText(itemStack);

    event.getPlayer().sendMessage(
        MoneyPrinter.getInstance().getLang().getComponent("cannot-use-personalized"));
  }
}
