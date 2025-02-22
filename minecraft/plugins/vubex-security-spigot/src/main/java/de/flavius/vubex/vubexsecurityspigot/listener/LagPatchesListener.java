package de.flavius.vubex.vubexsecurityspigot.listener;

import de.flavius.vubex.vubexsecurityspigot.Vubex_security_spigot;
import de.flavius.vubex.vubexsecurityspigot.utils.ServerTPS;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class LagPatchesListener implements Listener {

    private static final HashMap<Player, Integer> vl = new HashMap<>();

    public static void clear() {
        vl.clear();
    }

    public void tell(Player p) {
        if (vl.containsKey(p)) {
            if (vl.get(p) == 0) {
                vl.put(p, 1);
                p.sendMessage(ChatColor.DARK_RED + "Elytras are disabled in low tps.");
                Bukkit.getScheduler().scheduleSyncDelayedTask(Vubex_security_spigot.getPlugin(), () -> vl.put(p, 0), 60L);
            }
        } else {
            vl.put(p, 0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onElytra(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.isGliding() && ServerTPS.getCurrentTps() <= 15.0) {
            Location l2 = e.getTo();
            if (l2 != null && l2.getY() < 5000.0D) {
                Location l = e.getFrom();
                if (Math.abs(l.getZ() - l2.getZ()) +
                        Math.abs(l.getX() - l2.getX()) +
                        Math.abs(l.getY() - l2.getY()) > 15.0) {
                    e.setCancelled(true);
                    tell(p);
                }
            }
        }
    }

    @EventHandler
    public void onNote(NotePlayEvent event) {
        if (ServerTPS.getCurrentTps() < 17.5) {
            event.setCancelled(true);
            Material t = event.getBlock().getType();
            if (ServerTPS.getCurrentTps() < 13.5 && t != Material.BEDROCK && t != Material.END_PORTAL_FRAME)
                event.getBlock().setType(Material.AIR);
        }
    }

    @EventHandler
    public void onTnt(ExplosionPrimeEvent event) {
        if (ServerTPS.getCurrentTps() <= 12.0)
            event.setCancelled(true);
    }

    @EventHandler
    public void onWitherSpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.Wither) {
            if (ServerTPS.getCurrentTps() <= 14.0)
                event.setCancelled(true);
            int count = 0;
            for (Entity e : event.getEntity().getWorld().getNearbyEntities(event.getLocation(), 64.0D, 64.0D, 64.0D)) {
                if (e.getType() == EntityType.WITHER)
                    count++;
            }
            if (count > 96) {
                event.setCancelled(true);
            }
        }
    }
}
