package club.aurorapvp.moneyprinter.data;

import club.aurorapvp.moneyprinter.MoneyPrinter;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CosmeticDataHandler {
    private final MongoCollection<Document> collection;
    private final String playerId;
    private Document playerData;

    public CosmeticDataHandler(Player player) {
        this.collection = MoneyPrinter.getInstance().getDatabase().getCollection("moneyprinter_cosmetics");
        this.playerId = player.getUniqueId().toString();
        this.playerData = collection.find(Filters.eq("_id", playerId)).first();
        if (playerData == null) {
            playerData = new Document("_id", playerId);
        }
    }

    public String getArmorTrimPattern(String piece) {
        Document armorTrims = (Document) playerData.get("armorTrims");
        if (armorTrims != null) {
            Document trim = (Document) armorTrims.get(piece);
            if (trim != null) {
                return trim.getString("pattern");
            }
        }
        return null;
    }

    public String getArmorTrimMaterial(String piece) {
        Document armorTrims = (Document) playerData.get("armorTrims");
        if (armorTrims != null) {
            Document trim = (Document) armorTrims.get(piece);
            if (trim != null) {
                return trim.getString("material");
            }
        }
        return null;
    }

    public void setArmorTrim(String piece, String pattern, String material) {
        Document armorTrims = (Document) playerData.get("armorTrims");
        if (armorTrims == null) {
            armorTrims = new Document();
            playerData.put("armorTrims", armorTrims);
        }
        if (pattern != null && material != null) {
            Document trim = new Document("pattern", pattern).append("material", material);
            armorTrims.put(piece, trim);
        } else {
            armorTrims.remove(piece);
        }
    }

    public List<String> getShieldBannerPatterns() {
        return playerData.getList("shieldBannerPatterns", String.class, new ArrayList<>());
    }

    public void setShieldBannerPatterns(List<String> patterns) {
        playerData.put("shieldBannerPatterns", patterns);
    }

    public String getShieldBaseColor() {
        String color = playerData.getString("shieldBaseColor");
        return color != null ? color : "WHITE";
    }

    public void setShieldBaseColor(String color) {
        playerData.put("shieldBaseColor", color);
    }

    public void save() {
        collection.replaceOne(Filters.eq("_id", playerId), playerData, new com.mongodb.client.model.ReplaceOptions().upsert(true));
    }
}