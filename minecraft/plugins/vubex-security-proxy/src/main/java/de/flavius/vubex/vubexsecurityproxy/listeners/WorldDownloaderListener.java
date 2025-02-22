package de.flavius.vubex.vubexsecurityproxy.listeners;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * @author : flavius
 * project : VubexProject
 * created : 05.09.2023, Dienstag
 **/
public class WorldDownloaderListener implements Listener {

    private final List<String> bannedChannels = Arrays.asList("wdl|control", "wdl|init", "wdl|request", "wdl:control", "wdl:init", "wdl:request", "wdl");

    @EventHandler
    public void onPacket(PluginMessageEvent paramPluginMessageEvent) {
        if (!(paramPluginMessageEvent.getSender() instanceof ProxiedPlayer proxiedPlayer))
            return;
        String str1 = paramPluginMessageEvent.getTag().toLowerCase();
        String str2 = (new String(paramPluginMessageEvent.getData(), StandardCharsets.UTF_8)).toLowerCase();
        if (this.bannedChannels.contains(str1) || this.bannedChannels.contains(str2) || str1.contains("wdl") || str1.startsWith("worlddownloader") || str2.contains("wdl") || str2.startsWith("worlddownloader")) {
            paramPluginMessageEvent.setCancelled(true);
            proxiedPlayer.disconnect(new TextComponent("kick nachricht hier"));
        }
    }
}