package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.api.VubexDiscordAPI;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class KickCommand extends Command implements TabExecutor {

    private final VubexDiscordAPI vubexDiscordAPI;

    public KickCommand(VubexDiscordAPI vubexDiscordAPI) {
        super("kick", null);
        this.vubexDiscordAPI = vubexDiscordAPI;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("vubex.kick")){
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Diesen &7Befehl &7darfst &7du &7nicht &7ausführen.")));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Verwendung: &e/kick [Spieler] [Grund]")));
            return;
        }

        ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(args[0]);

        if (targetPlayer == null) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Der Spieler &e" + args[0] + "&7 ist nicht online.")));
            return;
        }

        if(targetPlayer.getUniqueId().toString().equals("ba671a5b-5c79-448e-b40b-8bbd64445472") || targetPlayer.getUniqueId().toString().equals("d9c2256d-e13b-4c8c-b3c9-3461033af680")) {
            if (sender != targetPlayer){
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Bei dieser Person darfst du den Befehl nicht benutzen.")));
                if(targetPlayer.isConnected()){
                    targetPlayer.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + ((ProxiedPlayer) sender).getDisplayName() + "&7 hat versucht dich zu kicken.")));
                }
            }
            return;
        }

        String reason = "Kein Grund angegeben";

        if (args.length > 1) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
            reason = reasonBuilder.toString().trim();
        }

        if (!(reason.equals("Kein Grund angegeben"))) {
            targetPlayer.disconnect(new TextComponent(ChatUtils.getColoredText("&8» &e&lVubex.DE &r&8«\n\n &7Du wurdest von &e" + sender.getName() + "&7 vom Server &cgekickt&7!\n\n&7Grund: " + reason)));
        } else {
            targetPlayer.disconnect(new TextComponent(ChatUtils.getColoredText("&8» &e&lVubex.DE &r&8«\n\n &7Du wurdest von &e" + sender.getName() + "&7 vom Server &cgekickt&7!")));
        }

        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (player != sender && player.hasPermission("vubex.kick.broadcast")) {
                player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix));
                player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + targetPlayer.getName() + "&7 wurde von &e" + sender.getName() + " &7gekickt.")));
                if (!(reason.equals("Kein Grund angegeben"))) {
                    player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Grund &e" + reason)));
                }
                player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix));
            }
        }

        ProxyServer.getInstance().getConsole().sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + targetPlayer.getName() + "&7 wurde von &e" + sender.getName() + " &7gekickt.")));
        if (!(reason.equals("Kein Grund angegeben"))) {
            ProxyServer.getInstance().getConsole().sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Grund &e" + reason)));
        }
        sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("Der Spieler &e" + targetPlayer.getName() + "&7 wurde gekickt.")));

        String title = "Team-Information";
        String iconUrl = "";
        String[] lines;
        Color color = Color.ORANGE;
        if(sender instanceof ProxiedPlayer){
            iconUrl = "https://mc-heads.net/head/" + ((ProxiedPlayer) sender).getUniqueId() + "/600";
        }
        if (!(reason.equals("Kein Grund angegeben"))) {
            lines = new String[]{"**" + targetPlayer.getName() + "** wurde von **" + sender.getName() + "** gekickt. \nGrund: " + reason};
        }else{
            lines = new String[]{"**" + targetPlayer.getName() + "** wurde von **" + sender.getName() + "** gekickt. \nGrund: **Keinen**"};
        }
        vubexDiscordAPI.sendEmbedMessage("1222293450284863498", title, lines, iconUrl, color);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1 && sender.hasPermission("vubex.kick")) {
            String partialName = args[0].toLowerCase();

            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                String playerName = player.getName().toLowerCase();
                if (playerName.startsWith(partialName)) {
                    completions.add(player.getName());
                }
            }
        }

        return completions;
    }
}