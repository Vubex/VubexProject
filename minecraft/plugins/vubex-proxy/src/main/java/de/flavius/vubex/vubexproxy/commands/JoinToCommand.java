package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class JoinToCommand extends Command implements TabExecutor {

    public JoinToCommand() {
        super("jointo",null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("vubex.jointo")){
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Diesen &7Befehl &7darfst &7du &7nicht &7ausführen.")));
            return;
        }

        if (!(sender instanceof ProxiedPlayer player)) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + Vubex_proxy.onlyPlayerCommand));
            return;
        }

        if (args.length == 1) {
            String targetName = args[0];
            ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(targetName);

            if (targetPlayer != null) {
                player.connect(targetPlayer.getServer().getInfo());
                player.sendMessage(new TextComponent(ChatUtils.getColoredText(Vubex_proxy.serverPrefix + "&7Du bist zu §e" + targetPlayer.getName() + "&7 auf &e" + targetPlayer.getServer().getInfo().getName() +"&7 gesprungen.")));
            } else {
                player.sendMessage(new TextComponent(ChatUtils.getColoredText(Vubex_proxy.serverPrefix + "&7Der Spieler &e" + targetName + "&7 ist nicht online.")));
            }
        } else {
            player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Verwendung: &e/jointo [Spieler]")));
        }
    }
    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1 && sender.hasPermission("vubex.jointo")) {
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
