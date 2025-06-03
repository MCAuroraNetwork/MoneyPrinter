package club.aurorapvp.moneyprinter.modules.customization;

import club.aurorapvp.moneyprinter.data.CosmeticDataHandler;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CosmeticSettingsManager {
  private static final Map<UUID, CosmeticSettingsManager> instances = new HashMap<>();
  private final CosmeticDataHandler dataHandler;

  private CosmeticSettingsManager(Player player) {
    this.dataHandler = new CosmeticDataHandler(player);
  }

  public static CosmeticSettingsManager getInstance(Player player) {
    UUID playerId = player.getUniqueId();
    if (!instances.containsKey(playerId)) {
      instances.put(playerId, new CosmeticSettingsManager(player));
    }
    return instances.get(playerId);
  }

  public String getArmorTrimPattern(String piece) {
    return dataHandler.getArmorTrimPattern(piece.toLowerCase());
  }

  public String getArmorTrimMaterial(String piece) {
    return dataHandler.getArmorTrimMaterial(piece.toLowerCase());
  }

  public void setArmorTrim(String piece, String pattern, String material) {
    dataHandler.setArmorTrim(piece.toLowerCase(), pattern, material);
    dataHandler.save();
  }

  public List<String> getShieldBannerPatterns() {
    return dataHandler.getShieldBannerPatterns();
  }

  public void setShieldBannerPatterns(List<String> patterns) {
    dataHandler.setShieldBannerPatterns(patterns);
    dataHandler.save();
  }

  public String getShieldBaseColor() {
    return dataHandler.getShieldBaseColor();
  }

  public void setShieldBaseColor(String color) {
    dataHandler.setShieldBaseColor(color);
    dataHandler.save();
  }

  public void save() {
    dataHandler.save();
  }
}