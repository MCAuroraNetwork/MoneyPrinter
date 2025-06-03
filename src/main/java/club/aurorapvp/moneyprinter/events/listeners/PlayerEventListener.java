package club.aurorapvp.moneyprinter.events.listeners;

import club.aurorapvp.moneyprinter.MoneyPrinter;
import club.aurorapvp.moneyprinter.modules.customization.ItemCosmeticLimiter;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEventListener implements Listener {

  @EventHandler
  public void onObtainItem(PlayerInventorySlotChangeEvent event) {
    ItemCosmeticLimiter.onObtainItem(event);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    MoneyPrinter.getInstance().removeTeam(event.getPlayer());
  }
}
