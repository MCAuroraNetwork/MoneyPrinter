package club.aurorapvp.moneyprinter.modules;

import club.aurorapvp.aurorachat.modules.NameTag;
import club.aurorapvp.moneyprinter.MoneyPrinter;
import club.aurorapvp.moneyprinter.modules.customization.CosmeticSettingsManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.SkinTrait;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MirrorTrait extends Trait {

  /* ─── static helpers ─────────────────────────────────────────── */
  private static final ProtocolManager PM = ProtocolLibrary.getProtocolManager();
  private static final Set<MirrorTrait> ACTIVE = new HashSet<>();
  private static Location MIRROR_LOC;

  static {
    Bukkit.getPluginManager().registerEvents(new JoinSync(), MoneyPrinter.getInstance());
  }

  private UUID owner;

  private TextDisplay tagDisp;
  private int gearTicker;

  public MirrorTrait() {
    super("mirror");
  }

  @Override
  public void load(DataKey k) {
    owner = UUID.fromString(k.getString("owner"));
  }

  @Override
  public void save(DataKey k) {
    k.setString("owner", owner.toString());
  }

  public void setOwner(Player p) {
    owner = p.getUniqueId();
  }

  @Override
  public void onSpawn() {
    ACTIVE.add(this);

    npc.setName("");
    npc.data().setPersistent("nameplate-visible", false);

    runLoop();
  }

  @Override
  public void onRemove() {
    ACTIVE.remove(this);
    if (tagDisp != null && !tagDisp.isDead()) {
      tagDisp.remove();
      tagDisp = null;
    }
  }

  private void runLoop() {
    new BukkitRunnable() {
      @Override
      public void run() {
        if (!npc.isSpawned()) {
          cancel();
          return;
        }

        Player me = Bukkit.getPlayer(owner);
        boolean online = me != null && me.isOnline();

        updateVisibility(online);
        if (!online) return;

        if (++gearTicker >= 10) {
          pushEquipment(me);
          gearTicker = 0;
        }
        syncNametag(me);
      }
    }.runTaskTimer(MoneyPrinter.getInstance(), 1, 1);
  }

  private void updateVisibility(boolean ownerOnline) {
    Bukkit.getOnlinePlayers()
        .forEach(
            pl -> {
              boolean shouldSee = ownerOnline && pl.getUniqueId().equals(owner);
              if (shouldSee) {
                pl.showEntity(MoneyPrinter.getInstance(), npc.getEntity());
                if (tagDisp != null) pl.showEntity(MoneyPrinter.getInstance(), tagDisp);
              } else {
                pl.hideEntity(MoneyPrinter.getInstance(), npc.getEntity());
                if (tagDisp != null) pl.hideEntity(MoneyPrinter.getInstance(), tagDisp);
              }
            });
  }

  /* ─── nametag replication ───────────────────────────────────── */
  private void syncNametag(Player me) {
    NameTag src = NameTag.getNameTags().get(owner);
    if (src == null || src.getEntity() == null) return;

    Component want = src.getEntity().text();
    Location pos = npc.getEntity().getLocation().add(0, 2.05, 0); // +Y higher

    if (tagDisp == null || tagDisp.isDead()) {
      tagDisp =
          (TextDisplay)
              me.getWorld()
                  .spawnEntity(
                      pos,
                      EntityType.TEXT_DISPLAY,
                      CreatureSpawnEvent.SpawnReason.CUSTOM,
                      e -> {
                        TextDisplay td = (TextDisplay) e;
                        td.setBillboard(Display.Billboard.CENTER);
                        td.setPersistent(false);
                        td.setInvulnerable(true);
                        td.text(want);
                      });
      updateVisibility(true);
    } else {
      tagDisp.teleportAsync(pos);
      tagDisp.text(want);
    }
  }

  /* ─── cosmetic handling ─────────────────────────────────────── */
  private void pushEquipment(Player me) {
    List<Pair<EnumWrappers.ItemSlot, ItemStack>> list =
        List.of(
            new Pair<>(EnumWrappers.ItemSlot.HEAD, trim(Material.NETHERITE_HELMET, me, "helmet")),
            new Pair<>(
                EnumWrappers.ItemSlot.CHEST, trim(Material.NETHERITE_CHESTPLATE, me, "chestplate")),
            new Pair<>(
                EnumWrappers.ItemSlot.LEGS, trim(Material.NETHERITE_LEGGINGS, me, "leggings")),
            new Pair<>(EnumWrappers.ItemSlot.FEET, trim(Material.NETHERITE_BOOTS, me, "boots")),
            new Pair<>(EnumWrappers.ItemSlot.MAINHAND, shield(me)));
    PacketContainer pkt =
        PM.createPacket(com.comphenix.protocol.PacketType.Play.Server.ENTITY_EQUIPMENT);
    pkt.getIntegers().write(0, npc.getEntity().getEntityId());
    pkt.getSlotStackPairLists().write(0, list);
    PM.sendServerPacket(me, pkt);
  }

  private ItemStack trim(Material base, Player p, String slot) {
    ItemStack is = new ItemStack(base);
    CosmeticSettingsManager c = CosmeticSettingsManager.getInstance(p);
    String pat = c.getArmorTrimPattern(slot), mat = c.getArmorTrimMaterial(slot);
    if (pat != null && mat != null && is.getItemMeta() instanceof ArmorMeta m) {
      TrimPattern tp = Bukkit.getRegistry(TrimPattern.class).get(NamespacedKey.minecraft(pat));
      TrimMaterial tm = Bukkit.getRegistry(TrimMaterial.class).get(NamespacedKey.minecraft(mat));
      if (tp != null && tm != null) m.setTrim(new ArmorTrim(tm, tp));
      is.setItemMeta(m);
    }
    return is;
  }

  private ItemStack shield(Player p) {
    ItemStack sh = new ItemStack(Material.SHIELD);
    CosmeticSettingsManager c = CosmeticSettingsManager.getInstance(p);
    BlockStateMeta meta = (BlockStateMeta) sh.getItemMeta();
    Banner b = (Banner) meta.getBlockState();
    try {
      b.setBaseColor(DyeColor.valueOf(c.getShieldBaseColor()));
    } catch (Exception ignore) {
      b.setBaseColor(DyeColor.WHITE);
    }
    b.getPatterns().clear();
    for (String s : c.getShieldBannerPatterns()) {
      String[] sp = s.split(":");
      try {
        b.addPattern(new Pattern(DyeColor.valueOf(sp[1]), PatternType.valueOf(sp[0])));
      } catch (Exception ignore) {
      }
    }
    b.update();
    meta.setBlockState(b);
    sh.setItemMeta(meta);
    return sh;
  }

  public static void setMirrorLocation(Location l) {
    MIRROR_LOC = l.clone();
  }

  private static final class JoinSync implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
      if (MIRROR_LOC == null) return;
      Player p = e.getPlayer();

      for (NPC n : CitizensAPI.getNPCRegistry())
        if (n.getName().equalsIgnoreCase(p.getName() + "_mirror")) return;

      NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, p.getName() + "_mirror");

      npc.getOrAddTrait(MirrorTrait.class).setOwner(p);
      npc.getOrAddTrait(SkinTrait.class).setSkinName(p.getName());
      npc.setProtected(true);
      npc.spawn(MIRROR_LOC);
    }
  }
}
