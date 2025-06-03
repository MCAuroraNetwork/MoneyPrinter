package club.aurorapvp.moneyprinter.events.listeners;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClickListener implements Listener {

  @EventHandler
  public void onLeftClick(NPCLeftClickEvent e) {
    e.getClicker().performCommand("customize");
  }

  @EventHandler
  public void onRightClick(NPCRightClickEvent e) {
    e.getClicker().performCommand("customize");
  }
}
