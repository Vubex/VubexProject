package de.flavius.vubex.vubexsecurityspigot.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class CommandBlockerListener implements Listener {

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase(); // Den Befehl in Kleinbuchstaben konvertieren

        if (command.startsWith("/pl") || command.startsWith("/bukkit:pl")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Du darfst diesen Befehl nicht verwenden.");
        }
    }
}
