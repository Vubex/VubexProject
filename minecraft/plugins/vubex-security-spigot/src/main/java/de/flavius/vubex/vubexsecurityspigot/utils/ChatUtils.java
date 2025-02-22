package de.flavius.vubex.vubexsecurityspigot.utils;

import org.bukkit.ChatColor;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class ChatUtils {
    public static String getColoredText(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
