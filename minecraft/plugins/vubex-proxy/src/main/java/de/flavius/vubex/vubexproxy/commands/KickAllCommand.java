package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.api.VubexDiscordAPI;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.awt.*;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class KickAllCommand extends Command implements TabExecutor {

    private final VubexDiscordAPI vubexDiscordAPI;

    public KickAllCommand(VubexDiscordAPI vubexDiscordAPI) {
        super("kickall", null);
        this.vubexDiscordAPI = vubexDiscordAPI;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("vubex.kickall")){
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Diesen &7Befehl &7darfst &7du &7nicht &7ausführen.")));
            return;
        }

        if (args.length < 1 ) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Verwendung &e/kickall [Server]")));
            return;
        } else if (args.length > 2) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Zu viele Argumente. Verwende &e/kickall [Server]")));
            return;
        }

        ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(args[0]);
        if (serverInfo == null) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Kein Server mit dem Namen: &e" + args[0] + "&7 gefunden.")));
            return;
        }

        if (serverInfo.getName().toLowerCase().contains("lobby")) {
            if (args.length == 2 && "confirm".equals(args[1])) {
                BaseComponent[] kickMessage = TextComponent.fromLegacyText(ChatUtils.getColoredText("&8» &e&lVubex.DE &r&8«\n\n &7Alle Spieler aus dem Lobby-Server &e" + args[0] + "&7 wurden rausgeworfen!\n\n&7Du solltest dich gleich wieder verbinden können."));
                int numPlayersKicked = 0;  
                for (ProxiedPlayer player : serverInfo.getPlayers()) {
                    if (!player.hasPermission("vubex.kickall.bypass") && !player.getUniqueId().toString().equals("ba671a5b-5c79-448e-b40b-8bbd64445472") && !player.getUniqueId().toString().equals("d9c2256d-e13b-4c8c-b3c9-3461033af680")) {
                        player.disconnect(kickMessage);
                        numPlayersKicked++; 
                    }
                }

                for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                    if (player.hasPermission("vubex.admin.kickall.broadcast")) {
                        String broadcastMessage = Vubex_proxy.serverPrefix + "\n";
                        broadcastMessage += Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + numPlayersKicked + "&7 Spieler wurden von &e" + sender.getName() + " &7aus &e" + args[0] + " &7gekickt." + "\n");
                        broadcastMessage += Vubex_proxy.serverPrefix + "\n";
                        player.sendMessage(new TextComponent(broadcastMessage));
                    }
                }
                ProxyServer.getInstance().getConsole().sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + numPlayersKicked + "&7 Spieler wurden von &e" + sender.getName() + " &7aus &e" + args[0] + " &7gekickt.")));
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Du hast &e" + numPlayersKicked + " &7Spieler von &e" + args[0] + " &7gekickt.")));
                String title = "Team-Information";
                String iconUrl = "";
                if(sender instanceof ProxiedPlayer){
                    iconUrl = "https://mc-heads.net/head/" + ((ProxiedPlayer) sender).getUniqueId() + "/600";
                }
                Color color = Color.ORANGE;
                String[] lines = new String[]{"**" + sender.getName() + "** hat **/kickall** auf einem Lobby-Server verwendet und **" + numPlayersKicked + " Spieler** vom Server **" + args[0].toLowerCase() + "** gekickt."};
                vubexDiscordAPI.sendEmbedMessage("1222293450284863498", title, lines, iconUrl, color);
            } else {
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Du benutzt &e/kickall &7auf einem Lobby-Server. &7Wenn du sicher bist, verwende: &e/kickall &e" + args[0] + " &econfirm")));
            }
        } else {
            int numPlayersMoved = 0;
            for (ProxiedPlayer player : serverInfo.getPlayers()) {
                if (!player.hasPermission("vubex.admin.kickall.bypass")) {
                    ServerInfo lobbyServer = getFreeLobbyServer();
                    if (lobbyServer != null) {
                        player.connect(lobbyServer);
                        player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Du wurdest auf den Lobby-Server &e" + lobbyServer.getName() + " &7verschoben.")));

                    } else {
                        player.disconnect(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&8» &e&lVubex.DE &r&8«\n\n&7Du wurdest verschoben doch es wurde kein freier Lobby-Server gefunden.\n\n&7Du solltest dich gleich wieder verbinden können.")));
                    }
                    numPlayersMoved++;
                }
            }

            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                if (player.hasPermission("vubex.kickall.broadcast")) {
                    String broadcastMessage = Vubex_proxy.serverPrefix + "\n";
                    broadcastMessage += Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + numPlayersMoved + "&7 Spieler wurden von &e" + sender.getName() + "&7 aus &e" + args[0].toLowerCase() + " &7verschoben." + "\n");
                    broadcastMessage += Vubex_proxy.serverPrefix + "\n";
                    player.sendMessage(new TextComponent(broadcastMessage));
                }
            }
            ProxyServer.getInstance().getConsole().sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + numPlayersMoved + "&7 Spieler wurden von &e" + sender.getName() + "&7 aus &e" + args[0].toLowerCase() + "&7 verschoben.")));
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Du hast &e" + numPlayersMoved + " &7Spieler von &e" + args[0].toLowerCase() + " &7auf den Lobby-Server verschoben.")));
            String title = "Team-Information";
            String iconUrl = "";
            if(sender instanceof ProxiedPlayer){
                iconUrl = "https://mc-heads.net/head/" + ((ProxiedPlayer) sender).getUniqueId() + "/600";
            }
            Color color = Color.ORANGE;
            String[] lines = new String[]{"**" + sender.getName() + "** hat **/kickall** verwendet und **" + numPlayersMoved + " Spieler** vom Server **" + args[0].toLowerCase() + "** verschoben."};
            vubexDiscordAPI.sendEmbedMessage("1222293450284863498", title, lines, iconUrl, color);
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return ProxyServer.getInstance().getServersCopy().values().stream()
                    .map(ServerInfo::getName)
                    .filter(s -> s.startsWith(args[0]))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[1].isEmpty()) {
            return Collections.singletonList("confirm");
        }
        return Collections.emptyList();
    }

    private ServerInfo getFreeLobbyServer() {
        for (ServerInfo server : ProxyServer.getInstance().getServersCopy().values()) {
            String serverName = server.getName().toLowerCase();
            if (serverName.startsWith("lobby") && hasAvailableSlot(server)) {
                return server;
            }
        }
        return null;
    }

    private boolean hasAvailableSlot(ServerInfo server) {
        int currentPlayers = server.getPlayers().size();
        return currentPlayers < Vubex_proxy.hasAvailableSlotMaxPlayersLobby;
    }
}
