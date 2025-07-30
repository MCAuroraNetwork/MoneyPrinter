package club.aurorapvp.moneyprinter.modules;

import club.aurorapvp.aurorachat.modules.NameTag;
import club.aurorapvp.moneyprinter.MoneyPrinter;
import club.aurorapvp.moneyprinter.modules.customization.CosmeticSettingsManager;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.logging.Level;

public final class MirrorManager {

  private static final class MirrorReflection {
    private final int entityId;
    private final UUID npcUuid;
    private final ServerPlayer npcPlayer;
    private final org.bukkit.entity.TextDisplay bukkitTextDisplay;
    private final UUID reflectedPlayerUuid; // Track whose reflection this is

    private MirrorReflection(
        int entityId,
        UUID npcUuid,
        ServerPlayer npcPlayer,
        org.bukkit.entity.TextDisplay bukkitTextDisplay,
        UUID reflectedPlayerUuid) {
      this.entityId = entityId;
      this.npcUuid = npcUuid;
      this.npcPlayer = npcPlayer;
      this.bukkitTextDisplay = bukkitTextDisplay;
      this.reflectedPlayerUuid = reflectedPlayerUuid;
    }
  }

  private static final Random RANDOM = new Random();

  private static final Set<Location> MIRROR_LOCATIONS = new HashSet<>();
  private static final Map<UUID, Map<Location, MirrorReflection>> ACTIVE_REFLECTIONS =
      new HashMap<>();
  private static final Map<Integer, UUID> REFLECTION_ENTITY_TO_PLAYER = new HashMap<>();
  private static final double VIEW_DISTANCE_SQUARED = 20 * 20;

  public static void init() {
    Bukkit.getScheduler().runTaskTimer(MoneyPrinter.getInstance(), MirrorManager::tick, 0L, 1L);
    PacketEvents.getAPI().getEventManager().registerListener(new InteractionListener());
  }

  public static void createMirror(Location location) {
    Location blockLocation = location.toBlockLocation();
    MIRROR_LOCATIONS.add(blockLocation);
  }

  public static void removeMirror(Location location) {
    Location blockLocation = location.toBlockLocation();
    if (MIRROR_LOCATIONS.remove(blockLocation)) {
      ACTIVE_REFLECTIONS.forEach(
          (playerUUID, reflections) -> {
            if (reflections.containsKey(blockLocation)) {
              Player player = Bukkit.getPlayer(playerUUID);
              if (player != null) {
                destroyReflection(player, blockLocation);
              }
            }
          });
    }
  }

  public static void onPlayerQuit(Player player) {
    Map<Location, MirrorReflection> playerReflections =
        ACTIVE_REFLECTIONS.remove(player.getUniqueId());
    if (playerReflections != null) {
      playerReflections
          .values()
          .forEach(reflection -> REFLECTION_ENTITY_TO_PLAYER.remove(reflection.entityId));
    }
  }

  public static Set<Location> getMirrorLocations() {
    return Collections.unmodifiableSet(MIRROR_LOCATIONS);
  }

  private static void tick() {
    if (MIRROR_LOCATIONS.isEmpty()) return;

    for (Player player : Bukkit.getOnlinePlayers()) {
      for (Location mirrorLocation : MIRROR_LOCATIONS) {
        if (!player.getWorld().equals(mirrorLocation.getWorld())) {
          destroyReflection(player, mirrorLocation);
          continue;
        }

        if (player.getLocation().distanceSquared(mirrorLocation) <= VIEW_DISTANCE_SQUARED) {
          updateOrCreateReflection(player, mirrorLocation);
        } else {
          destroyReflection(player, mirrorLocation);
        }
      }
    }
  }

  private static void updateOrCreateReflection(Player player, Location mirrorLocation) {
    MirrorReflection reflection =
        ACTIVE_REFLECTIONS
            .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
            .get(mirrorLocation);

    if (reflection == null) {
      spawnReflection(player, mirrorLocation);
    } else {
      updateReflection(player, reflection, mirrorLocation);
    }
  }

  private static void spawnReflection(Player player, Location mirrorLocation) {
    ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
    ServerLevel world = ((CraftWorld) mirrorLocation.getWorld()).getHandle();
    MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();

    UUID npcUuid = UUID.randomUUID();
    String npcName = getInvisibleName(RANDOM.nextInt(999999));
    GameProfile npcProfile = new GameProfile(npcUuid, npcName);

    GameProfile playerProfile = ((CraftPlayer) player).getProfile();
    playerProfile
        .getProperties()
        .get("textures")
        .forEach(prop -> npcProfile.getProperties().put("textures", prop));

    ClientInformation clientInfo = nmsPlayer.clientInformation();

    ServerPlayer npcPlayer = new ServerPlayer(server, world, npcProfile, clientInfo);
    npcPlayer.setPos(mirrorLocation.getX(), mirrorLocation.getY(), mirrorLocation.getZ());

    int entityId = npcPlayer.getId();

    ClientboundPlayerInfoUpdatePacket.Entry playerInfoEntry =
        new ClientboundPlayerInfoUpdatePacket.Entry(
            npcUuid,
            npcProfile,
            true,
            0,
            nmsPlayer.gameMode.getGameModeForPlayer(),
            net.minecraft.network.chat.Component.literal(npcName),
            true,
            0,
            null);

    ClientboundPlayerInfoUpdatePacket infoPacket =
        new ClientboundPlayerInfoUpdatePacket(
            EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER),
            List.of(playerInfoEntry));
    nmsPlayer.connection.send(infoPacket);

    ClientboundAddEntityPacket spawnPacket =
        new ClientboundAddEntityPacket(
            entityId,
            npcUuid,
            mirrorLocation.getX(),
            mirrorLocation.getY(),
            mirrorLocation.getZ(),
            0f,
            0f,
            EntityType.PLAYER,
            0,
            Vec3.ZERO,
            0.0);
    nmsPlayer.connection.send(spawnPacket);

    SynchedEntityData.DataItem<Byte> skinPartsData =
        new SynchedEntityData.DataItem<>(
            new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 0x7F);
    ClientboundSetEntityDataPacket playerDataPacket =
        new ClientboundSetEntityDataPacket(entityId, List.of(skinPartsData.value()));
    nmsPlayer.connection.send(playerDataPacket);

    Scoreboard scoreboard = new Scoreboard();
    PlayerTeam team = new PlayerTeam(scoreboard, npcName);
    team.setNameTagVisibility(Team.Visibility.NEVER);
    team.getPlayers().add(npcName);
    ClientboundSetPlayerTeamPacket teamPacket =
        ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
    nmsPlayer.connection.send(teamPacket);

    sendEquipment(player, entityId);

    org.bukkit.entity.TextDisplay textDisplay = null;

    NameTag nameTag = NameTag.getNameTags().get(player.getUniqueId());
    if (nameTag != null && nameTag.getEntity() != null) {
      Location textLocation = mirrorLocation.clone().add(0.5, 1.8, 0.5);

      try {
        textDisplay =
            (org.bukkit.entity.TextDisplay)
                mirrorLocation
                    .getWorld()
                    .spawnEntity(
                        textLocation,
                        org.bukkit.entity.EntityType.TEXT_DISPLAY,
                        CreatureSpawnEvent.SpawnReason.CUSTOM,
                        entity -> {
                          org.bukkit.entity.TextDisplay td = (org.bukkit.entity.TextDisplay) entity;

                          org.bukkit.entity.TextDisplay originalDisplay = nameTag.getEntity();
                          if (originalDisplay != null) {
                            td.text(originalDisplay.text());
                            td.setAlignment(originalDisplay.getAlignment());
                            td.setBillboard(originalDisplay.getBillboard());
                            td.setSeeThrough(originalDisplay.isSeeThrough());
                            td.setViewRange(originalDisplay.getViewRange());
                            td.setInterpolationDuration(originalDisplay.getInterpolationDuration());
                            td.setInterpolationDelay(originalDisplay.getInterpolationDelay());
                            td.setShadowRadius(originalDisplay.getShadowRadius());
                            td.setShadowed(originalDisplay.isShadowed());
                            td.setTextOpacity(originalDisplay.getTextOpacity());
                            if (originalDisplay.getBackgroundColor() != null) {
                              td.setBackgroundColor(originalDisplay.getBackgroundColor());
                            } else {
                              td.setDefaultBackground(true);
                            }
                            td.setTransformation(originalDisplay.getTransformation());
                          } else {
                            td.setAlignment(org.bukkit.entity.TextDisplay.TextAlignment.CENTER);
                            td.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
                            td.setSeeThrough(false);
                            td.setViewRange(64.0f);
                            td.setShadowRadius(0);
                            td.setShadowed(false);
                            td.setTextOpacity((byte) 255);
                            td.text(Component.text(player.getName()));
                            td.setDefaultBackground(true);
                          }

                          td.setInvulnerable(true);
                          td.setPersistent(false);
                        });

        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
          if (otherPlayer.equals(player)) {
            otherPlayer.showEntity(MoneyPrinter.getInstance(), textDisplay);
          } else {
            otherPlayer.hideEntity(MoneyPrinter.getInstance(), textDisplay);
          }
        }

      } catch (Exception e) {
        MoneyPrinter.getInstance()
            .getLogger()
            .log(Level.WARNING, "Could not create TextDisplay: " + e.getMessage());
      }
    }

    MirrorReflection reflection =
        new MirrorReflection(entityId, npcUuid, npcPlayer, textDisplay, player.getUniqueId());
    ACTIVE_REFLECTIONS
        .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
        .put(mirrorLocation, reflection);
    REFLECTION_ENTITY_TO_PLAYER.put(entityId, player.getUniqueId());

    Bukkit.getScheduler()
        .scheduleSyncDelayedTask(
            MoneyPrinter.getInstance(),
            () -> {
              ClientboundPlayerInfoRemovePacket removePacket =
                  new ClientboundPlayerInfoRemovePacket(Collections.singletonList(npcUuid));
              nmsPlayer.connection.send(removePacket);
            },
            40L);
  }

  private static void destroyReflection(Player player, Location mirrorLocation) {
    Map<Location, MirrorReflection> playerReflections =
        ACTIVE_REFLECTIONS.get(player.getUniqueId());
    if (playerReflections == null) return;

    MirrorReflection reflection = playerReflections.remove(mirrorLocation);
    if (reflection == null) return;

    ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

    ClientboundPlayerInfoRemovePacket removeInfo =
        new ClientboundPlayerInfoRemovePacket(Collections.singletonList(reflection.npcUuid));
    nmsPlayer.connection.send(removeInfo);

    ClientboundRemoveEntitiesPacket destroyPacket =
        new ClientboundRemoveEntitiesPacket(reflection.entityId);
    nmsPlayer.connection.send(destroyPacket);

    if (reflection.bukkitTextDisplay != null && !reflection.bukkitTextDisplay.isDead()) {
      reflection.bukkitTextDisplay.remove();
    }

    REFLECTION_ENTITY_TO_PLAYER.remove(reflection.entityId);
  }

  private static void updateReflection(
      Player player, MirrorReflection reflection, Location mirrorLocation) {
    ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

    sendEquipment(player, reflection.entityId);

    if (reflection.bukkitTextDisplay != null && !reflection.bukkitTextDisplay.isDead()) {
      NameTag nameTag = NameTag.getNameTags().get(reflection.reflectedPlayerUuid);
      if (nameTag != null && nameTag.getEntity() != null) {
        org.bukkit.entity.TextDisplay originalDisplay = nameTag.getEntity();
        org.bukkit.entity.TextDisplay mirrorDisplay = reflection.bukkitTextDisplay;

        mirrorDisplay.text(originalDisplay.text());

        mirrorDisplay.setAlignment(originalDisplay.getAlignment());
        mirrorDisplay.setBillboard(
            originalDisplay.getBillboard()); // Ensures rotation follows viewers
        mirrorDisplay.setSeeThrough(originalDisplay.isSeeThrough());
        mirrorDisplay.setShadowed(originalDisplay.isShadowed());
        mirrorDisplay.setTextOpacity(originalDisplay.getTextOpacity());
        mirrorDisplay.setTransformation(originalDisplay.getTransformation());

        if (originalDisplay.getBackgroundColor() != null) {
          mirrorDisplay.setBackgroundColor(originalDisplay.getBackgroundColor());
        } else {
          mirrorDisplay.setDefaultBackground(true);
        }

        Location textLocation = mirrorLocation.clone().add(0.5, 1.8, 0.5);
        mirrorDisplay.teleport(textLocation);
      }
    }

    Vector direction =
        player.getEyeLocation().toVector().subtract(mirrorLocation.toVector()).normalize();
    Location tempLoc = new Location(mirrorLocation.getWorld(), 0, 0, 0);
    tempLoc.setDirection(direction);

    float yaw = tempLoc.getYaw();
    float pitch = 0.0f;

    BlockPos blockPos =
        new BlockPos(
            mirrorLocation.getBlockX(), mirrorLocation.getBlockY(), mirrorLocation.getBlockZ());
    PositionMoveRotation posRotation =
        new PositionMoveRotation(Vec3.atBottomCenterOf(blockPos), Vec3.ZERO, yaw, pitch);

    ClientboundTeleportEntityPacket teleportPacket =
        new ClientboundTeleportEntityPacket(reflection.entityId, posRotation, Set.of(), true);
    nmsPlayer.connection.send(teleportPacket);

    ClientboundRotateHeadPacket headPacket =
        new ClientboundRotateHeadPacket(
            reflection.npcPlayer, (byte) Mth.floor(yaw * 256.0F / 360.0F));
    nmsPlayer.connection.send(headPacket);
  }

  private static void sendEquipment(Player player, int entityId) {
    ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
    CosmeticSettingsManager settings = CosmeticSettingsManager.getInstance(player);

    List<Pair<EquipmentSlot, ItemStack>> equipment = new ArrayList<>();
    equipment.add(
        Pair.of(
            EquipmentSlot.HEAD, createTrimmedArmor(Items.NETHERITE_HELMET, settings, "helmet")));
    equipment.add(
        Pair.of(
            EquipmentSlot.CHEST,
            createTrimmedArmor(Items.NETHERITE_CHESTPLATE, settings, "chestplate")));
    equipment.add(
        Pair.of(
            EquipmentSlot.LEGS,
            createTrimmedArmor(Items.NETHERITE_LEGGINGS, settings, "leggings")));
    equipment.add(
        Pair.of(EquipmentSlot.FEET, createTrimmedArmor(Items.NETHERITE_BOOTS, settings, "boots")));
    equipment.add(Pair.of(EquipmentSlot.MAINHAND, createShield(settings)));

    ClientboundSetEquipmentPacket equipPacket =
        new ClientboundSetEquipmentPacket(entityId, equipment);
    nmsPlayer.connection.send(equipPacket);
  }

  private static ItemStack createTrimmedArmor(
      net.minecraft.world.item.Item item, CosmeticSettingsManager settings, String piece) {
    ItemStack itemStack = new ItemStack(item);

    String patternKey = settings.getArmorTrimPattern(piece);
    String materialKey = settings.getArmorTrimMaterial(piece);

    if (patternKey != null && materialKey != null) {
      try {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();

        ResourceLocation patternLocation = ResourceLocation.parse(patternKey);
        ResourceLocation materialLocation = ResourceLocation.parse(materialKey);

        Holder<TrimPattern> trimPattern =
            server
                .registryAccess()
                .lookupOrThrow(Registries.TRIM_PATTERN)
                .get(patternLocation)
                .orElse(null);

        Holder<TrimMaterial> trimMaterial =
            server
                .registryAccess()
                .lookupOrThrow(Registries.TRIM_MATERIAL)
                .get(materialLocation)
                .orElse(null);

        if (trimPattern != null && trimMaterial != null) {
          ArmorTrim armorTrim = new ArmorTrim(trimMaterial, trimPattern);
          itemStack.set(DataComponents.TRIM, armorTrim);
        }
      } catch (Exception e) {
        MoneyPrinter.getInstance()
            .getLogger()
            .log(Level.WARNING, "Could not apply armor trim: " + e.getMessage());
      }
    }

    return itemStack;
  }

  private static ItemStack createShield(CosmeticSettingsManager settings) {
    ItemStack shield = new ItemStack(Items.SHIELD);

    try {
      MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();

      String baseColorStr = settings.getShieldBaseColor();
      DyeColor baseColor;
      try {
        baseColor = DyeColor.valueOf(baseColorStr.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
        baseColor = DyeColor.WHITE;
      }

      shield.set(DataComponents.BASE_COLOR, baseColor);

      List<String> patternStrings = settings.getShieldBannerPatterns();
      if (patternStrings != null && !patternStrings.isEmpty()) {
        List<BannerPatternLayers.Layer> layers = new ArrayList<>();

        for (String patternString : patternStrings) {
          try {
            String[] parts = patternString.split(":");
            if (parts.length == 2) {
              String patternKey = parts[0].toLowerCase(Locale.ROOT);
              ResourceLocation patternLocation = ResourceLocation.parse(patternKey);
              DyeColor patternColor = DyeColor.valueOf(parts[1].toUpperCase(Locale.ROOT));

              Holder<BannerPattern> bannerPattern =
                  server
                      .registryAccess()
                      .lookupOrThrow(Registries.BANNER_PATTERN)
                      .get(patternLocation)
                      .orElse(null);

              if (bannerPattern != null) {
                layers.add(new BannerPatternLayers.Layer(bannerPattern, patternColor));
              } else {
                MoneyPrinter.getInstance()
                    .getLogger()
                    .log(Level.WARNING, "Banner pattern not found in registry: " + patternKey);
              }
            }
          } catch (Exception e) {
            MoneyPrinter.getInstance()
                .getLogger()
                .log(
                    Level.WARNING,
                    "Could not parse banner pattern: " + patternString + " - " + e.getMessage());
          }
        }

        if (!layers.isEmpty()) {
          BannerPatternLayers bannerPatternLayers = new BannerPatternLayers(layers);
          shield.set(DataComponents.BANNER_PATTERNS, bannerPatternLayers);
        }
      }

    } catch (Exception e) {
      MoneyPrinter.getInstance()
          .getLogger()
          .log(Level.WARNING, "Could not apply shield customization: " + e.getMessage());
    }

    return shield;
  }

  private static String getInvisibleName(int id) {
    return Integer.toHexString(id).replaceAll("(.)", "§$1");
  }

  private static class InteractionListener extends PacketListenerAbstract {
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType()
          == com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client
              .INTERACT_ENTITY) {
        WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
        int entityId = wrapper.getEntityId();
        UUID reflectedPlayerUUID = REFLECTION_ENTITY_TO_PLAYER.get(entityId);

        UUID clickerUUID = event.getUser().getUUID();
        Player clicker = Bukkit.getPlayer(clickerUUID);

        if (clicker == null) {
          MoneyPrinter.getInstance()
              .getLogger()
              .log(Level.WARNING, "Could not find player for UUID: " + clickerUUID);
          return;
        }

        if (reflectedPlayerUUID != null && reflectedPlayerUUID.equals(clicker.getUniqueId())) {
          Bukkit.getScheduler()
              .runTask(MoneyPrinter.getInstance(), () -> clicker.performCommand("customize"));
        }
      }
    }
  }
}
