package club.aurorapvp.moneyprinter.commands;

import club.aurorapvp.moneyprinter.modules.MirrorTrait;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@CommandAlias("mirror")
@CommandPermission("moneyprinter.command.mirror")
public class MirrorCommand extends BaseCommand {

  @Subcommand("create")
  public void create(Player sender, @Optional String who) {

    Location spot = sender.getLocation();
    MirrorTrait.setMirrorLocation(spot);

    if (who == null) { // everyone online
      Bukkit.getOnlinePlayers().forEach(p -> spawnMirror(p, spot));
      sender.sendMessage("§aMirrors created for all players online.");
      return;
    }

    Player target = who.equalsIgnoreCase("here") ? sender : Bukkit.getPlayerExact(who);
    if (target == null) {
      sender.sendMessage("§cPlayer not found.");
      return;
    }

    spawnMirror(target, spot);
    sender.sendMessage("§aMirror created for §e" + target.getName());
  }

  private void spawnMirror(Player p, Location loc) {
    NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, p.getName() + "_mirror");

    npc.getOrAddTrait(SkinTrait.class).setSkinName(p.getName());
    npc.setProtected(true);

    npc.getOrAddTrait(MirrorTrait.class).setOwner(p);
    npc.spawn(loc); // then spawn
  }

  @Subcommand("remove")
  @Syntax("<npc-id>")
  public void remove(Player sender, int id) {

    NPC ref = CitizensAPI.getNPCRegistry().getById(id);
    if (ref == null) {
      sender.sendMessage("§cNo NPC with id " + id);
      return;
    }

    if (!ref.isSpawned()) {
      ref.destroy();
      sender.sendMessage("§aRemoved NPC " + id + " (only one, was despawned).");
      return;
    }

    World w = ref.getEntity().getWorld();
    int bx = ref.getEntity().getLocation().getBlockX();
    int by = ref.getEntity().getLocation().getBlockY();
    int bz = ref.getEntity().getLocation().getBlockZ();

    List<NPC> toKill = new ArrayList<>();
    for (NPC n : CitizensAPI.getNPCRegistry()) {
      if (!n.isSpawned()) continue;
      Location l = n.getEntity().getLocation();
      if (l.getWorld() == w && l.getBlockX() == bx && l.getBlockY() == by && l.getBlockZ() == bz) {
        toKill.add(n);
      }
    }

    toKill.forEach(NPC::destroy);
    sender.sendMessage("§aRemoved §e" + toKill.size() + " §amirrors at this spot.");
  }
}
