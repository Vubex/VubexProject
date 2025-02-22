package de.flavius.vubex.vubexproxy.api;

import de.flavius.vubex.vubexproxy.commands.AdminchatCommand;
import de.flavius.vubex.vubexproxy.commands.TeamchatCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import java.awt.*;
import java.time.LocalDateTime;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class VubexDiscordAPI {

    private final JDABuilder jdaBuilder;
    private net.dv8tion.jda.api.JDA jda;

    public VubexDiscordAPI(String token) {
        this.jdaBuilder = JDABuilder.createDefault(token);
        this.jdaBuilder.setActivity(Activity.listening("Vubex.de Proxy"));
        this.jdaBuilder.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGE_REACTIONS);

    }

    public void start() {
        jda = jdaBuilder.build();
        jda.addEventListener(new AdminchatCommand.DiscordMessageListener(this));
        jda.addEventListener(new TeamchatCommand.DiscordMessageListener(this));
    }

    public void stop() {
        if (jda != null) {
            jda.shutdown();
        }
    }

    public void sendMessageToChannel(String channelId, String message) {
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel != null) {
            channel.sendMessage(message).queue();
        }
    }

    public void sendEmbedMessage(String channelId, String title, String[] lines, String iconUrl, Color color) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title);

        for (String line : lines) {
            embedBuilder.appendDescription(line + "\n");
        }
        if(iconUrl.startsWith("http")){
            embedBuilder.setThumbnail(iconUrl);
        }
        embedBuilder.setColor(color);

        LocalDateTime timestamp = LocalDateTime.now();
        embedBuilder.setTimestamp(timestamp);

        MessageEmbed embed = embedBuilder.build();
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel != null) {
            channel.sendMessageEmbeds(embed).queue();
        }
    }
}