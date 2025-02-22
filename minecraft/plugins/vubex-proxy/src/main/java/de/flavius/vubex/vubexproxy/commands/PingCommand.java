package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author : flavius
 * project : VubexProject
 * created : 10.09.2023, Sonntag
 **/
public class PingCommand extends Command {

    public PingCommand() {
        super("ping", null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Dein aktueller &ePing &7beträgt: &e" + player.getPing() + "&ems&7.")));
        } else {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + Vubex_proxy.onlyPlayerCommand));
        }
    }
}
