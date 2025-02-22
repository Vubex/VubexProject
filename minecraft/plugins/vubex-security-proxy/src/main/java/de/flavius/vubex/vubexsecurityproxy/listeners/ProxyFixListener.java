package de.flavius.vubex.vubexsecurityproxy.listeners;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * @author : flavius
 * project : VubexProject
 * created : 05.09.2023, Dienstag
 **/
public class ProxyFixListener implements Listener {

    @EventHandler
    public void onPacket(PluginMessageEvent paramPluginMessageEvent) {
        String str = paramPluginMessageEvent.getTag();
        if (!(paramPluginMessageEvent.getSender() instanceof ProxiedPlayer proxiedPlayer))
            return;
        if (str.toLowerCase().contains("proxy")) {
            paramPluginMessageEvent.setCancelled(true);
            proxiedPlayer.disconnect(new TextComponent("kick nachricht hier"));
        }
    }
}
