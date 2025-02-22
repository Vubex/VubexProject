package de.flavius.vubex.vubexsecurityspigot.listener;

import de.flavius.vubex.vubexsecurityspigot.Vubex_security_spigot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class OnTeleportListener implements Listener {

    @EventHandler
    public void onEntityPortal(EntityPortalEvent event) {
        if (!(event.getEntity() instanceof Player))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPTeleport(final PlayerTeleportEvent e) {
        final Player player = e.getPlayer();
        if (player.isInsideVehicle()) {
            e.setCancelled(true);
        }
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND) {
            e.setCancelled(true);
            final Location to = e.getTo();
            teleportPlayer(player, getRandom(), 18000, getRandom(), player.getWorld());
            (new BukkitRunnable() {
                public void run() {
                    if (to != null) {
                        teleportPlayer(player, to.getBlockX(), to.getBlockY(), to.getBlockZ(), to.getWorld());
                    }
                }
            }).runTaskLater(Vubex_security_spigot.getPlugin(), 2L);
        }
    }
    private void teleportPlayer(Player player, int x, int y, int z, World g) {
        player.teleport(new Location(g, x, y, z));
    }
    private int getRandom() {
        return (int)(Math.random() * 7500);
    }
}
