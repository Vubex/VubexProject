package de.flavius.vubex.vubexsecurityproxy;

import de.flavius.vubex.vubexsecurityproxy.listeners.RandomExploitListener;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * @author : flavius
 * project : VubexProject
 **/
public final class Vubex_security_proxy extends Plugin {

    public static String serverPrefix = ChatColor.translateAlternateColorCodes('§', "§e§lVubex.DE§r §8» §7");
    public static String consolePrefix = ChatColor.translateAlternateColorCodes('§', "§e§lVubex-Security - Proxy§r §8» §7");

    @Override
    public void onEnable() {
        getProxy().getConsole().sendMessage(new TextComponent(""));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|"));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|→ Vubex-Security (Proxy) by " + ChatColor.YELLOW + "Flavius " + ChatColor.GRAY + "/ Plugin-Version: " + ChatColor.YELLOW + getDescription().getVersion() + ChatColor.GRAY + "."));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|"));

        getProxy().getPluginManager().registerListener(this, new RandomExploitListener());


        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "| "));
        getProxy().getConsole().sendMessage(new TextComponent(""));
        getProxy().getConsole().sendMessage(new TextComponent(serverPrefix + "Vubex-Security (Proxy) ist bereit."));
        printBanner();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    private void printBanner() {
        getProxy().getConsole().sendMessage(new TextComponent(ChatColor.YELLOW + " _____ _____ _____ _____ __ __     _____ _____ _____ _____ _____ _____ _____ __ __ "));
        getProxy().getConsole().sendMessage(new TextComponent(ChatColor.YELLOW + "|  |  |  |  | __  |   __|  |  |___|   __|   __|     |  |  | __  |     |_   _|  |  |"));
        getProxy().getConsole().sendMessage(new TextComponent(ChatColor.YELLOW + "|  |  |  |  | __ -|   __|-   -|___|__   |   __|   --|  |  |    -|-   -| | | |_   _|"));
        getProxy().getConsole().sendMessage(new TextComponent(ChatColor.YELLOW + " \\___/|_____|_____|_____|__|__|   |_____|_____|_____|_____|__|__|_____| |_|   |_| "));
        getProxy().getConsole().sendMessage(new TextComponent(ChatColor.YELLOW + "                                  (Proxy-Plugin)                                  "));
    }
}
