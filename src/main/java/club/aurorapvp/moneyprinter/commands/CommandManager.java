package club.aurorapvp.moneyprinter.commands;

import club.aurorapvp.aurorachat.AuroraChat;
import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;

public class CommandManager {

  public static PaperCommandManager MANAGER = new PaperCommandManager(AuroraChat.getInstance());

  public static void init() {
    MANAGER.registerCommand(new CustomizationCommand());
    MANAGER.registerCommand(new MirrorCommand());

    CommandCompletions<BukkitCommandCompletionContext> commandCompletions =
        MANAGER.getCommandCompletions();
  }
}
