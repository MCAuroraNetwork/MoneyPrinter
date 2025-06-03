package club.aurorapvp.moneyprinter.commands;

import club.aurorapvp.moneyprinter.modules.customization.gui.CustomizationGUI;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import org.bukkit.entity.Player;

@CommandAlias("customize")
@CommandPermission("moneyprinter.command.customize")
public class CustomizationCommand extends BaseCommand {
    @Default
    public void onCustomize(Player player) {
        new CustomizationGUI(player).open();
    }
}
