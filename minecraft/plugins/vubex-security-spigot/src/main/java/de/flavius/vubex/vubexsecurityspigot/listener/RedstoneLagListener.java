package de.flavius.vubex.vubexsecurityspigot.listener;

import de.flavius.vubex.vubexsecurityspigot.Vubex_security_spigot;
import de.flavius.vubex.vubexsecurityspigot.utils.ServerTPS;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class RedstoneLagListener implements Listener {
    private int rCount = 0;

    private boolean isRedstoneUI(Material m) {
        return switch (m) {
            case LEGACY_WOOD_BUTTON, STONE_BUTTON, TRAPPED_CHEST -> true;
            default -> false;
        };
    }

    @EventHandler
    public void onRedstoneTick(BlockRedstoneEvent event) {
        if (!isRedstoneUI(event.getBlock().getType()) && ServerTPS.getCurrentTps() <= 15.0) {
            event.setNewCurrent(0);
            int blockX = event.getBlock().getLocation().getBlockX();
            int blockY = event.getBlock().getLocation().getBlockY();
            int blockZ = event.getBlock().getLocation().getBlockZ();
            if (ServerTPS.getCurrentTps() <= 10.0 && !Vubex_security_spigot.getInstance().getServer().getMotd().toLowerCase().contains("lobby"))
                event.getBlock().breakNaturally();
            Vubex_security_spigot.sendAlert3("Redstone", "§dItem: §5", event.getBlock().getType().name(), "Likely lag Machine(Broke) at X: " + blockX + ", Y: " + blockY + ", Z:" + blockZ);
            if (this.rCount < 2) {
                this.rCount++;
                Vubex_security_spigot.sendAlert3("Redstone", "§dItem: §5", event.getBlock().getType().name(), "Likely lag Machine(Paused) at X: " + blockX + ", Y: " + blockY + ", Z:" + blockZ);
                Bukkit.getScheduler().scheduleSyncDelayedTask(Vubex_security_spigot.getPlugin(), () -> this.rCount = 0, 80L);
            }
        }
    }
}