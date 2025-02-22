package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.api.VubexDiscordAPI;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class TeamchatCommand extends Command {
    private final VubexDiscordAPI vubexDiscordAPI;

    public TeamchatCommand(VubexDiscordAPI vubexDiscordAPI) {
        super("teamchat", null, "tc");
        this.vubexDiscordAPI = vubexDiscordAPI;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("vubex.teamchat")){
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Diesen &7Befehl &7darfst &7du &7nicht &7ausführen.")));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Verwendung: &e/teamchat [Nachricht]")));
            return;
        }

        String message = String.join(" ", args);

        String formattedMessage = formatMessage(sender.getName(), args);

        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (player.hasPermission("vubex.teamchat")) {
                player.sendMessage(new TextComponent(formattedMessage));
            }
        }
        vubexDiscordAPI.sendMessageToChannel("1222293220248129536", ChatUtils.removeFormattingCharacters(ChatUtils.getColoredText("**" + sender.getName() + "**" + ": " + message)));
        ProxyServer.getInstance().getConsole().sendMessage(new TextComponent((ChatUtils.getColoredText("&a&lTEAMCHAT&r &8» &7" + sender.getName() + "&7: &7" + message))));
    }

    private String formatMessage(String senderName, String[] args) {
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

        return ChatUtils.getColoredText("&a&lTEAMCHAT&r &8» &7" + senderName + ChatUtils.getColoredText("&7: &7") + formattedMessage.toString().trim());
    }

    public static class DiscordMessageListener extends ListenerAdapter {

        public DiscordMessageListener(VubexDiscordAPI ignoredVubexDiscordAPI) {
        }

        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            TextChannel textChannel = event.getChannel().asTextChannel();
            if (!event.getAuthor().isBot() && textChannel.getId().equals("1222293220248129536")) {
                String senderName = event.getAuthor().getName();
                String[] message = new String[]{event.getMessage().getContentDisplay()};

                String formattedMessage = formatMessage(senderName, message);
                ProxyServer proxyServer = ProxyServer.getInstance();
                for (ProxiedPlayer player : proxyServer.getPlayers()) {
                    if (player.hasPermission("vubex.teamchat")) {
                        player.sendMessage(new TextComponent(formattedMessage));
                    }
                }
                proxyServer.getConsole().sendMessage(new TextComponent(ChatUtils.getColoredText("&a&lTEAMCHAT&r &8» &9[DISCORD] &7" + senderName + "&7: &7" + Arrays.toString(message))));
            }
        }

        private String formatMessage(String senderName, String[] message) {
            StringBuilder formattedMessage = new StringBuilder();
            String currentColorCode = "&7";

            for (String msg : message) {
                Pattern colorPattern = Pattern.compile("(&[0-9A-Fa-fKkLlMmNnOoRr])");
                Matcher matcher = colorPattern.matcher(msg);
                int currentIndex = 0;

                while (matcher.find()) {
                    formattedMessage.append(currentColorCode).append(msg, currentIndex, matcher.start());
                    currentColorCode = matcher.group();
                    currentIndex = matcher.end();
                }
                formattedMessage.append(currentColorCode).append(msg.substring(currentIndex));
                formattedMessage.append(" ");
            }

            return ChatUtils.getColoredText("&a&lTEAMCHAT&r &8» &9[DISCORD] &7" + senderName + ChatUtils.getColoredText("&7: &7") + formattedMessage.toString().trim());
        }
    }
}