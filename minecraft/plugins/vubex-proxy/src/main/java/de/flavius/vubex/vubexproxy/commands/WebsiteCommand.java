package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class WebsiteCommand extends Command {

    public WebsiteCommand() {
        super("website", null, "web");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "§7Dieser Befehl erfordert keine Argumente."));
            return;
        }

        if (sender instanceof ProxiedPlayer player) {

            String clickMessage = "Klicke hier, um zu unserer Website zu gelangen.";
            TextComponent clickableMessage = new TextComponent(clickMessage);
            clickableMessage.setColor(ChatColor.GRAY);

            // ClickEvent
            String websiteURL = "https://www.vubex.de/";
            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, websiteURL);
            clickableMessage.setClickEvent(clickEvent);

            // HoverEvent hinzufügen
            String hoverText = ChatColor.YELLOW + "Klicke hier";
            BaseComponent[] hoverComponents = TextComponent.fromLegacyText(hoverText);
            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverComponents));
            clickableMessage.setHoverEvent(hoverEvent);

            // Prefix hinzufügen
            String prefix = Vubex_proxy.serverPrefix;
            TextComponent prefixComponent = new TextComponent(prefix);
            prefixComponent.setColor(ChatColor.YELLOW);

            // Verknüpfe Prefix mit anklickbarer Nachricht
            prefixComponent.addExtra(clickableMessage);

            player.sendMessage(prefixComponent);
        } else {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + Vubex_proxy.onlyPlayerCommand));
        }
    }
}