package club.aurorapvp.moneyprinter.commands;

import club.aurorapvp.moneyprinter.MoneyPrinter;
import club.aurorapvp.moneyprinter.modules.MirrorManager;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.Optional;

@CommandAlias("mirror")
@CommandPermission("moneyprinter.command.mirror")
public class MirrorCommand extends BaseCommand {

  @Subcommand("create")
  @Description("Creates a mirror at your current location.")
  @SuppressWarnings("unused")
  public void create(Player sender) {
    MirrorManager.createMirror(sender.getLocation());
    sender.sendMessage(MoneyPrinter.getInstance().getLang().getComponent("mirror-created"));
  }

  @Subcommand("remove")
  @Description("Removes the mirror nearest to you.")
  @SuppressWarnings("unused")
  public void remove(Player sender) {
    Location playerLocation = sender.getLocation();

    Optional<Location> closestMirror = MirrorManager.getMirrorLocations().stream()
            .filter(loc -> loc.getWorld().equals(playerLocation.getWorld()))
            .min(Comparator.comparingDouble(loc -> loc.distanceSquared(playerLocation)));

    if (closestMirror.isPresent() && closestMirror.get().distanceSquared(playerLocation) <= 100) { // 10 blocks squared
      MirrorManager.removeMirror(closestMirror.get());
      sender.sendMessage(MoneyPrinter.getInstance().getLang().getComponent("mirror-removed"));
    } else {
      sender.sendMessage(MoneyPrinter.getInstance().getLang().getComponent("mirror-not-found"));
    }
  }
}
