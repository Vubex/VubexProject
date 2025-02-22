package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author : flavius
 * project : VubexProject
 * created : 19.09.2023, Dienstag
 **/
public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", null, "?");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        TextComponent lobbyText = new TextComponent(ChatUtils.getColoredText("&e/lobby"));
        lobbyText.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/lobby"));
        TextComponent freundText = new TextComponent(ChatUtils.getColoredText("&e/freund"));
        freundText.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/freund"));
        TextComponent clanText = new TextComponent(ChatUtils.getColoredText("&e/clan"));
        clanText.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/clan"));
        TextComponent reportText = new TextComponent(ChatUtils.getColoredText("&e/report"));
        reportText.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/report"));
        TextComponent bugReportText = new TextComponent(ChatUtils.getColoredText("&e/bugreport"));
        bugReportText.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/bugreport"));
        TextComponent supportText = new TextComponent(ChatUtils.getColoredText("&e/support"));
        supportText.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/support"));

        ComponentBuilder builder = new ComponentBuilder().append("\n")
                .append(ChatUtils.getColoredText("&8&m---------------[-&r &e&lVubex&r &8&m-]---------------&r")).append("\n")
                .append(lobbyText).append(ChatUtils.getColoredText(" &7Kehre zur Hauptlobby zurück")).append("\n")
                .append(freundText).append(ChatUtils.getColoredText(" &7Verwalte deine Freunde")).append("\n")
                .append(clanText).append(ChatUtils.getColoredText(" &7Verwalte einen Clan")).append("\n")
                .append(reportText).append(ChatUtils.getColoredText(" &7Melde einen Spieler")).append("\n")
                .append(bugReportText).append(ChatUtils.getColoredText(" &7Melde einen Bug")).append("\n")
                .append("\n")
                .append(supportText).append(ChatUtils.getColoredText(" &7Öffne &7einen &7Chat &7mit &7unseren &7Helfern")).append("\n")
                .append(ChatUtils.getColoredText("&8&m----------------------------------------&r")).append("\n");

        sender.sendMessage(new TextComponent(builder.create()));
    }
}