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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BroadcastCommand extends Command implements TabExecutor {
    private final VubexDiscordAPI vubexDiscordAPI;

    public BroadcastCommand(VubexDiscordAPI vubexDiscordAPI) {
        super("broadcast", null, "bc");
        this.vubexDiscordAPI = vubexDiscordAPI;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("vubex.broadcast")){
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Diesen &7Befehl &7darfst &7du &7nicht &7ausführen.")));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Verwendung: &e/broadcast [Nachricht]")));
            return;
        }

        String message = ChatUtils.getColoredText(String.join(" ", args));
        String formattedMessage = formatMessage(args);

        StringBuilder broadcastMessageBuilder = new StringBuilder(Vubex_proxy.serverPrefix + "\n");
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (player.hasPermission("vubex.broadcast.info")) {
                broadcastMessageBuilder.append(Vubex_proxy.serverPrefix).append(ChatUtils.getColoredText("&e" + sender.getName() + " &7hat &7eine &7Broadcast-Nachricht &7gesendet:")).append("\n");
            }
        }
        String broadcastMessage = broadcastMessageBuilder.toString();
        broadcastMessage += formattedMessage + "\n";
        broadcastMessage += Vubex_proxy.serverPrefix;
        ProxyServer.getInstance().broadcast(new TextComponent(ChatUtils.getColoredText(broadcastMessage)));

        String title = "Team-Information";
        String iconUrl;
        Color color = Color.ORANGE;
        String[] lines;
        if (sender instanceof ProxiedPlayer) {
            iconUrl = "https://mc-heads.net/head/" + ((ProxiedPlayer) sender).getUniqueId() + "/600";
            lines = new String[]{"Der Spieler **" + sender.getName() + "** hat eine Broadcast-Nachricht gesendet:\n" + ChatUtils.removeFormattingCharacters(message)};
        } else {
            iconUrl = "";
            lines = new String[]{"Die **Console** hat eine Broadcast-Nachricht gesendet:\n" + ChatUtils.removeFormattingCharacters(message)};
        }
        vubexDiscordAPI.sendEmbedMessage("1222293871464153198", title, lines, iconUrl, color);
        ProxyServer.getInstance().getConsole().sendMessage(new TextComponent(ChatUtils.getColoredText(Vubex_proxy.serverPrefix + "&e" + sender.getName() + "&7 hat eine Broadcast-Nachricht gesendet: &e" + message)));
    }

    private String formatMessage(String[] args) {
        StringBuilder formattedMessage = new StringBuilder();
        String currentColorCode = "&7";

        for (String arg : args) {
            Pattern colorPattern = Pattern.compile("(&[0-9A-Fa-fKkLlMmNnOoRr])");
            Matcher matcher = colorPattern.matcher(arg);
            int currentIndex = 0;

            while (matcher.find()) {
                formattedMessage.append(currentColorCode).append(arg, currentIndex, matcher.start());
                currentColorCode = matcher.group();
                currentIndex = matcher.end();
            }

            formattedMessage.append(currentColorCode).append(arg.substring(currentIndex));
            formattedMessage.append(" ");
        }

        return ChatUtils.getColoredText(Vubex_proxy.serverPrefix + ChatUtils.getColoredText(formattedMessage.toString().trim()));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>(); // Keine Tab-Vervollständigung für diesen Befehl
    }
}