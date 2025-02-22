package de.flavius.vubex.vubexproxy.listeners;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Arrays;
import java.util.List;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class CommandBlockerListener implements Listener {

    private final List<String> playerBlockedCommands = Arrays.asList("/gamemode"); // Geblockte Spieler-Befehle
    private final List<String> adminBlockedCommands = Arrays.asList("/bukkit:plugins", "/plugins", "/bukkit:pl", "/pl", "/bukkit:version", "/version", "/bukkit:ver", "/ver", "/bukkit:?", "/bukkit:help", "/minecraft:seed", "/seed"); // Geblockte Admin-Befehle
    private final List<String> flaviusBlockedCommands = Arrays.asList("/minecraft:op", "/op", "/minecraft:deop", "/deop", "/minecraft:save-on", "/save-on", "/minecraft:save-off", "/save-off", "/minecraft:ban", "/minecraft:kick", "/minecraft:ban-ip", "/ban-ip", "/minecraft:pardon", "/minecraft:banlist", "/minecraft:defaultgamemode", "/defaultgamemode", "/minecraft:help", "/minecraft:me", "/me", "/minecraft:say", "/say", "/minecraft:setidletimeout", "/setidletimeout", "/minecraft:stop", "/minecraft:tell", "/minecraft:tellraw", "/tellraw", "/minecraft:whitelist", "/whitelist");
    public CommandBlockerListener() {
    }

    @EventHandler(priority = 35)
    public void onPlayerChatEvent(ChatEvent event) {
        if (event.getSender() instanceof ProxiedPlayer player) {
            String message = event.getMessage();

            if (message != null && message.startsWith("/")) {
                String command = message.split(" ")[0].toLowerCase();

                String playerBypassPermission = "vubex.proxy.commandblocker.bypass";
                String adminBypassPermission = "vubex.proxy.commandblocker.adminbypass";

                if (playerBlockedCommands.contains(command) && !player.hasPermission(playerBypassPermission)) {
                    event.setCancelled(true);
                    player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Diesen &7Befehl &7darfst &7du &7nicht &7ausführen.")));
                } else if ((adminBlockedCommands.contains(command) && !player.hasPermission(adminBypassPermission)) || (flaviusBlockedCommands.contains(command) && !player.getUniqueId().toString().equals("ba671a5b-5c79-448e-b40b-8bbd64445472"))) {
                    event.setCancelled(true);
                    player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Diesen &cAdmin-Befehl &7darfst &7du &7nicht &7ausführen.")));
                }
            }
        }
    }
}