package club.aurorapvp.moneyprinter.events;

import club.aurorapvp.moneyprinter.MoneyPrinter;
import club.aurorapvp.moneyprinter.events.listeners.ClickListener;
import club.aurorapvp.moneyprinter.events.listeners.PlayerEventListener;
import org.bukkit.Bukkit;

public class EventManager {
  public static void init() {
    Bukkit.getPluginManager().registerEvents(new PlayerEventListener(), MoneyPrinter.getInstance());
    Bukkit.getPluginManager().registerEvents(new ClickListener(), MoneyPrinter.getInstance());
  }
}
