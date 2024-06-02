package club.aurorapvp.moneyprinter;

import club.aurorapvp.moneyprinter.configs.Config;
import club.aurorapvp.moneyprinter.configs.Lang;
import club.aurorapvp.moneyprinter.events.EventManager;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

public final class MoneyPrinter extends JavaPlugin {

  private static Map<Player, Team> PLAYER_TEAMS = new HashMap<>();
  private static MoneyPrinter INSTANCE;
  private Lang lang;
  private Config config;

  public static MoneyPrinter getInstance() {
    return INSTANCE;
  }

  public Lang getLang() {
    return lang;
  }

  public @NotNull YamlConfiguration getConfig() {
    return config.getYaml();
  }

  @Override
  public void onEnable() {
    long startTime = System.currentTimeMillis();

    INSTANCE = this;

    // Setup configs
    lang = new Lang();
    config = new Config();

    // Initialize classes
    EventManager.init();

    getLogger().info("MoneyPrinter enabled in " + (System.currentTimeMillis() - startTime) + "ms");
  }

  @Override
  public void onDisable() {
    long startTime = System.currentTimeMillis();

    getLogger().info("MoneyPrinter disabled in " + (System.currentTimeMillis() - startTime) + "ms");
  }

  public void onPlayerJoin(Player player) {
    Team team = Bukkit.getScoreboardManager().getMainScoreboard()
        .getTeam("MoneyPrinter " + player.getName());

    if (team == null) {
      team = Bukkit.getScoreboardManager().getMainScoreboard()
          .registerNewTeam("MoneyPrinter " + player.getName());
    }

    PLAYER_TEAMS.put(player, team);
  }

  public Team getTeam(Player player) {
    return PLAYER_TEAMS.get(player);
  }

  public void removeTeam(Player player) {
    PLAYER_TEAMS.remove(player);
  }

  public void reloadConfig() {
    config.reload();
  }
}
