package de.flavius.vubex.vubexproxy.listeners;

import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import de.flavius.vubex.vubexproxy.utils.MuteManager;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import java.util.UUID;

/**
 * @author : flavius
 * project : VubexProject
 * created : 11.09.2023, Montag
 **/
public class ChatListener implements Listener {

    private final MySQLManager mysqlManager;

    public ChatListener(MySQLManager mysqlManager) {
        this.mysqlManager = mysqlManager;
    }

    @EventHandler
    public void onChatEvent(ChatEvent event){
        MuteManager muteManager = new MuteManager(mysqlManager);
        String trimmedMessage = event.getMessage().trim();

        muteManager.checkAndUpdateMuteStatus();
        if (event.getSender() instanceof ProxiedPlayer player) {
            UUID playerUUID = player.getUniqueId();

            if(muteManager.isMuted(playerUUID) && !trimmedMessage.startsWith("/")){
                event.setCancelled(true);
                event.setMessage(null);
                player.sendMessage(new TextComponent(ChatUtils.getColoredText(muteManager.getMuteMessage(playerUUID))));
            }
        }
    }
}