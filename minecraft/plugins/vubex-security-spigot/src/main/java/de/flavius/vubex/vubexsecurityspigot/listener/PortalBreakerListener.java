package de.flavius.vubex.vubexsecurityspigot.listener;

import org.bukkit.event.Listener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class PortalBreakerListener implements Listener {
    String epo = "ENDER_PORTAL";

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent event) {
        Location blc = event.getBlockClicked().getLocation();
        World world = blc.getWorld();
        int x = blc.getBlockX();
        int z = blc.getBlockZ();
        int y = blc.getBlockY();
        int yNeg = y - 1;
        int yPos = y + 1;
        int xNeg = x - 1;
        int zNeg = z - 1;
        int xPos = x + 1;
        int zPos = z + 1;
        if (world != null && (event.getBlockClicked().getType() == Material.END_PORTAL_FRAME || world
                .getBlockAt(x, yNeg, z).getType().toString().equals(this.epo) || world
                .getBlockAt(x, yPos, z).getType().toString().equals(this.epo) || world
                .getBlockAt(x, y, zNeg).getType().toString().equals(this.epo) || world
                .getBlockAt(xNeg, y, z).getType().toString().equals(this.epo) || world
                .getBlockAt(x, y, zPos).getType().toString().equals(this.epo) || world
                .getBlockAt(xPos, y, z).getType().toString().equals(this.epo))) event.setCancelled(true);
    }

    @EventHandler
    public void onDispense(BlockDispenseEvent event) {
        Location loc = event.getBlock().getLocation();
        World world = loc.getWorld();
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        int y = loc.getBlockY();
        int yNeg = y - 1;
        int yPos = y + 1;
        int xNeg = x - 1;
        int zNeg = z - 1;
        int xPos = x + 1;
        int zPos = z + 1;
        if (world != null && (event.getBlock().getType() == Material.END_PORTAL_FRAME || world
                .getBlockAt(x, yNeg, z).getType().toString().equals(this.epo) || world
                .getBlockAt(x, yPos, z).getType().toString().equals(this.epo) || world
                .getBlockAt(x, y, zNeg).getType().toString().equals(this.epo) || world
                .getBlockAt(xNeg, y, z).getType().toString().equals(this.epo) || world
                .getBlockAt(x, y, zPos).getType().toString().equals(this.epo) || world
                .getBlockAt(xPos, y, z).getType().toString().equals(this.epo))) event.setCancelled(true);
    }
}
