package de.flavius.vubex.vubexsecurityspigot.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class FaweTabCompletionListener implements Listener {
    @EventHandler
    public void onPlayerChatTabComplete(PlayerChatTabCompleteEvent paramPlayerChatTabCompleteEvent) {
        if (paramPlayerChatTabCompleteEvent.getChatMessage() == null)
            return;
        String str1 = paramPlayerChatTabCompleteEvent.getChatMessage().toLowerCase();
        String str2 = str1.split(" ")[0];
        if (str2.startsWith("/to") || str2.startsWith("/fastasyncworldedit:to"))
            paramPlayerChatTabCompleteEvent.getTabCompletions().clear();
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase(); // Den Befehl in Kleinbuchstaben konvertieren

        if (command.startsWith("/to") || command.startsWith("/fastasyncworldedit:to")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Du darfst diesen Befehl nicht verwenden.");
        }
    }
}
