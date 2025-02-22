package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.api.VubexVanishAPI;
import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
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
public class OnlineCommand extends Command {

    private final MySQLManager mySQLManager;

    public OnlineCommand(MySQLManager mySQLManager) {
        super("online", null);
        this.mySQLManager = mySQLManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int onlinePlayers = 0;
        int teamMembers = 0;

        for (ProxiedPlayer onlinePlayer : Vubex_proxy.getInstance().getProxy().getPlayers()) {
            VubexVanishAPI vanishAPI = new VubexVanishAPI(mySQLManager);
            boolean isVanished = vanishAPI.getVanishStatus(onlinePlayer.getUniqueId());

            if(sender.hasPermission(VubexVanishAPI.VANISH_SEE_PERMISSION)){
                onlinePlayers++;
                if (onlinePlayer.hasPermission("vubex.team")) {
                    teamMembers++;
                }
            }else{
                if(!isVanished){
                    onlinePlayers++;
                    if (onlinePlayer.hasPermission("vubex.team")) {
                        teamMembers++;
                    }
                }
            }
        }

        sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Es sind &e" + onlinePlayers + " &7Spieler Online.")));
        if(teamMembers != 0 && sender.hasPermission("vubex.online.seeteam")){
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Davon sind &e" + teamMembers + " &7Teammitglieder.")));
        }
    }
}