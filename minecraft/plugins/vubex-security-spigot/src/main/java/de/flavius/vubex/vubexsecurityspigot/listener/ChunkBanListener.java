package de.flavius.vubex.vubexsecurityspigot.listener;

import de.flavius.vubex.vubexsecurityspigot.Vubex_security_spigot;
import de.flavius.vubex.vubexsecurityspigot.utils.ChatUtils;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class ChunkBanListener implements Listener {
    @EventHandler
    public void hangingPlace(HangingPlaceEvent event) {
        if (Vubex_security_spigot.getPlugin().getConfig().getBoolean("patchchunkban") && (event.getBlock().getChunk().getEntities()).length > 350) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        int status = 0;
        String s = block.getType().toString();
        if (s.equals("SKULL") || s
                .equals("PLAYER_HEAD") || s
                .equals("CREEPER_HEAD") || s
                .equals("ZOMBIE_HEAD") || s
                .equals("WITHER_SKELETON_SKULL") || s
                .equals("SKELETON_SKULL") || s
                .equals("SKULL_ITEM"))
            status = 1;
        if (status == 1 && (event.getBlock().getChunk().getTileEntities()).length > 400) {
            event.setCancelled(true);
            event.getPlayer();
            event.getPlayer().sendMessage(ChatUtils.getColoredText(Vubex_security_spigot.serverPrefix + "&7Chunk bans are prohibited!"));

        }
        if (isTile(block.getType().toString()) && (block.getChunk().getTileEntities()).length > 90) {
            event.setCancelled(true);
            event.getPlayer();
            event.getPlayer().sendMessage(ChatUtils.getColoredText(Vubex_security_spigot.serverPrefix + "&7Tiles are limited per chunk, sorry."));
        }
    }

    private boolean isTile(String s) {
        return switch (s) {
            case "REDSTONE_COMPARATOR", "BEE_HIVE", "ENDER_CHEST", "LIGHT_GRAY_SHULKER_BOX", "SMOKER", "DROPPER", "END_GATEWAY", "DAYLIGHT_DETECTOR", "REPEATING_COMMAND_BLOCK", "MOB_SPAWNER", "PINK_SHULKER_BOX", "LIME_SHULKER_BOX", "SPAWNER", "DISPENSER", "WHITE_SHULKER_BOX", "PURPLE_SHULKER_BOX", "MAGENTA_SHULKER_BOX", "JUKEBOX", "RED_SHULKER_BOX", "TRAPPED_CHEST", "SIGN_POST", "ORANGE_SHULKER_BOX", "BLUE_SHULKER_BOX", "WALL_SIGN", "BELL", "CHEST", "DIODE", "BLAST_FURNACE", "LIGHT_BLUE_SHULKER_BOX", "SILVER_SHULKER_BOX", "FURNACE", "COMMAND_BLOCK", "BROWN_SHULKER_BOX", "BEENEST", "CAMPFIRE", "CHAIN_COMMAND_BLOCK", "CYAN_SHULKER_BOX", "NOTE_BLOCK", "GREEN_SHULKER_BOX", "BREWING_STAND", "YELLOW_SHULKER_BOX", "ENCHANTMENT_TABLE", "BLACK_SHULKER_BOX", "BARREL", "BEACON", "GRAY_SHULKER_BOX", "HOPPER" -> true;
            default -> false;
        };
    }
}
