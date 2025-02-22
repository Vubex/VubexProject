package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.api.VubexVanishAPI;
import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class GetIPCommand extends Command implements TabExecutor {

    private final MySQLManager mysqlManager;

    public GetIPCommand(MySQLManager mysqlManager) {
        super("getip", null, "ipinfo");
        this.mysqlManager = mysqlManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("vubex.getip")){
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Diesen &7Befehl &7darfst &7du &7nicht &7ausführen.")));
            return;
        }

        if (args.length == 1) {
            String targetName = args[0];
            ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(targetName);

            if (targetPlayer != null) {
                UUID targetUUID = targetPlayer.getUniqueId();

                if (sender != targetPlayer && (targetUUID.toString().equals("ba671a5b-5c79-448e-b40b-8bbd64445472") || targetUUID.toString().equals("d9c2256d-e13b-4c8c-b3c9-3461033af680"))) {
                    sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Bei dieser Person darfst du den Befehl nicht benutzen.")));
                    targetPlayer.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + ((ProxiedPlayer) sender).getDisplayName() + "&7 hat versucht &e/getip &7bei dir zu Verwenden.")));
                    return;
                }

                InetSocketAddress targetSocketAddress = (InetSocketAddress) targetPlayer.getSocketAddress();
                String targetIP = targetSocketAddress.getAddress().getHostAddress();
                String apiUrl = "http://ip-api.com/xml/"+ targetIP + "?fields=country,regionName,city,isp,proxy&lang=de";
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))){
                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        String isp = response.substring(response.indexOf("<isp>") + 5, response.indexOf("</isp>"));
                        String proxy = response.substring(response.indexOf("<proxy>") + 7, response.indexOf("</proxy>"));
                        String country = response.substring(response.indexOf("<country>") + 9, response.indexOf("</country>"));
                        String region = response.substring(response.indexOf("<regionName>") + 12, response.indexOf("</regionName>"));
                        String city = response.substring(response.indexOf("<city>") + 6, response.indexOf("</city>"));

                        String broadcastMessage = "\n" + Vubex_proxy.serverPrefix + "\n";
                        broadcastMessage += Vubex_proxy.serverPrefix + "&7Die IP-Adresse von &e" + targetPlayer.getName() + "&7 ist: &e" + targetIP + "\n";
                        broadcastMessage += Vubex_proxy.serverPrefix + "&7Internet-Anbieter: &e" + isp + "\n";

                        if (proxy.equals("false")) {
                            broadcastMessage += Vubex_proxy.serverPrefix + "&7Proxy/VPN: &eNein" + "\n";
                            broadcastMessage += Vubex_proxy.serverPrefix + "&7Standort: &e" + city + ", " + region + ", " + country + "\n";
                        } else if (proxy.equals("true")) {
                            broadcastMessage += Vubex_proxy.serverPrefix + "&7Proxy/VPN: &cJa" + "\n";
                            broadcastMessage += Vubex_proxy.serverPrefix + "&7Standort: &e" + city + ", " + region + ", " + country + "\n";
                            broadcastMessage += Vubex_proxy.serverPrefix + "&7Standort vermutlich Falsch (Proxy/VPN)" + "\n";
                        }

                        sender.sendMessage(new TextComponent(ChatUtils.getColoredText(broadcastMessage)));
                    }finally {
                        connection.disconnect();
                    }
                } catch (IOException e) {
                    sender.sendMessage(new TextComponent(ChatUtils.getColoredText(Vubex_proxy.serverPrefix + "&7Die IP-Adresse von &e" + targetPlayer.getName() + "&7 ist: &e" + targetIP + "\n"
                            + Vubex_proxy.serverPrefix + "&7Weitere Informationen zur IP nicht verfügbar")));
                    sender.sendMessage(new TextComponent());
                }
            } else {
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Der Spieler &e" + args[0] + "&7 ist nicht online.")));
            }
        } else {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Verwendung: &e/getip [Spieler]")));
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if(sender.hasPermission("vubex.getip")){
            VubexVanishAPI vanishAPI = new VubexVanishAPI(mysqlManager);

            if (args.length == 1) {
                String partialName = args[0].toLowerCase();

                for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                    String playerName = player.getName().toLowerCase();
                    boolean isVanished = vanishAPI.getVanishStatus(player.getUniqueId());

                    if ((sender.hasPermission(VubexVanishAPI.VANISH_SEE_PERMISSION) || !isVanished) && playerName.startsWith(partialName)) {
                        completions.add(player.getName());
                    }
                    if (player.getUniqueId().toString().equals("ba671a5b-5c79-448e-b40b-8bbd64445472") || player.getUniqueId().toString().equals("d9c2256d-e13b-4c8c-b3c9-3461033af680")) {
                        completions.remove(player.getName());
                    }
                }
            }
        }

        return completions;
    }
}