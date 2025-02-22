package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class LobbyCommand extends Command {

    public LobbyCommand() {
        super("lobby", null, "l", "hub");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {

            if (args.length > 0) {
                player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Dieser Befehl erfordert keine Argumente."));
                return;
            }

            if (player.getServer().getInfo().getName().toLowerCase().startsWith("lobby")) {
                player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Du befindest dich bereits auf einer Lobby."));
            } else {
                ServerInfo lobbyServer = getFreeLobbyServer();
                if (lobbyServer != null) {
                    player.connect(lobbyServer);
                    player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Du wurdest auf den Lobby-Server &e" + lobbyServer.getName() + "&7 teleportiert.")));

                } else {
                    player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Keinen freien Lobby-Server gefunden."));
                }
            }
        } else {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + Vubex_proxy.onlyPlayerCommand));
        }
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
