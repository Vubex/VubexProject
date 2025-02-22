package de.flavius.vubex.vubexproxy.modules;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class AutoBroadcastModule {
    private final Plugin plugin;
    private final List<String> broadcastMessages;
    private static ScheduledTask broadcastTask;
    private int currentMessageIndex = 0;

    public AutoBroadcastModule(Plugin plugin) {
        this.plugin = plugin;
        this.broadcastMessages = new ArrayList<>();
        loadBroadcastMessages();
        startBroadcastTask();
    }

    private void loadBroadcastMessages() {
        broadcastMessages.add("&7Besuche unsere Website unter &ewww.vubex.de");
    }

    private void startBroadcastTask() {
        if (broadcastMessages.isEmpty()) {
            return;
        }

        int interval = 300; // Intervall in Sekunden (300 = 5 Minuten)
        broadcastTask = ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            String message = broadcastMessages.get(currentMessageIndex);

            ProxyServer.getInstance().broadcast(new TextComponent(Vubex_proxy.serverPrefix));
            ProxyServer.getInstance().broadcast(new TextComponent(ChatColor.translateAlternateColorCodes('&', Vubex_proxy.serverPrefix + message)));
            ProxyServer.getInstance().broadcast(new TextComponent(Vubex_proxy.serverPrefix));
            currentMessageIndex = (currentMessageIndex + 1) % broadcastMessages.size();
        }, interval, interval, TimeUnit.SECONDS);
    }

    public static void stopBroadcastTask() {
        if (broadcastTask != null) {
            broadcastTask.cancel();
        }
    }

    public List<String> getBroadcastMessages() {
        return new ArrayList<>(broadcastMessages);
    }
}