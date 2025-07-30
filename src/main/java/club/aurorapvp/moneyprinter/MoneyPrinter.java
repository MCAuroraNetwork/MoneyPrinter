package club.aurorapvp.moneyprinter;

import club.aurorapvp.moneyprinter.commands.CommandManager;
import club.aurorapvp.moneyprinter.configs.Config;
import club.aurorapvp.moneyprinter.configs.Lang;
import club.aurorapvp.moneyprinter.events.EventManager;
import club.aurorapvp.moneyprinter.modules.MirrorManager;
import com.github.retrooper.packetevents.PacketEvents;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class MoneyPrinter extends JavaPlugin {

  private static final Map<Player, Team> PLAYER_TEAMS = new HashMap<>();
  private static MoneyPrinter INSTANCE;
  private Lang lang;
  private Config config;
  private MongoClient mongoClient;
  private MongoDatabase mongoDatabase;

  public static MoneyPrinter getInstance() {
    return INSTANCE;
  }

  public Lang getLang() {
    return lang;
  }

  public @NotNull YamlConfiguration getConfig() {
    return config.getYaml();
  }

  public MongoDatabase getDatabase() {
    return mongoDatabase;
  }

  @Override
  public void onEnable() {
    long startTime = System.currentTimeMillis();

    INSTANCE = this;

    // Setup configs
    lang = new Lang();
    config = new Config();

    String connectionString = this.getConfig().getString("mongodb.address", "mongodb://localhost:27017");
    String databaseName = this.getConfig().getString("mongodb.database-name", "money_printer");

    mongoClient = MongoClients.create(connectionString);
    mongoDatabase = mongoClient.getDatabase(databaseName);

    getLogger().info("Connected to MongoDB database: " + databaseName);

    // Initialize classes
    EventManager.init();
    CommandManager.init();
    PacketEvents.setAPI(SpigotPacketEventsBuilder.build(MoneyPrinter.getInstance()));
    PacketEvents.getAPI().getSettings().bStats(true).checkForUpdates(false).debug(false);
    PacketEvents.getAPI().load();
    MirrorManager.init();

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