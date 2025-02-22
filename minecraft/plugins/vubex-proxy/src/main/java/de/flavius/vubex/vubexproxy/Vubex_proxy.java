package de.flavius.vubex.vubexproxy;

import de.flavius.vubex.vubexproxy.api.VubexDiscordAPI;
import de.flavius.vubex.vubexproxy.commands.*;
import de.flavius.vubex.vubexproxy.listeners.*;
import de.flavius.vubex.vubexproxy.modules.*;
import de.flavius.vubex.vubexproxy.mysql.*;
import de.flavius.vubex.vubexproxy.utils.BanManager;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.sql.SQLException;
import java.util.logging.Level;

/**
 * @author : flavius
 * project : VubexProject
 **/
public final class Vubex_proxy extends Plugin {

    public static final int hasAvailableSlotMaxPlayersLobby = 200;

    public static final String serverPrefix = ChatUtils.getColoredText("&e&lVubex.DE&r &8» &7");
    public static final String consolePrefix = ChatUtils.getColoredText("&e&lVubex-Proxy&r &8» &7");
    public static final String onlyPlayerCommand = ChatUtils.getColoredText("&7Dieser Befehl kann nur von einem Spieler ausgeführt werden.");

    public MySQLManager mysqlManager;
    private static Vubex_proxy instance;
    private VubexDiscordAPI vubexDiscordAPI;
    public static Vubex_proxy getInstance() {
        return instance;
    }

    public MySQLManager getMysqlManager() {
        return mysqlManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        vubexDiscordAPI = new VubexDiscordAPI("DISCORDTOKENHIER");
        vubexDiscordAPI.start();

        try {
            // Initialize MySQL
            String host = "localhost";
            String database = "Datenbank";
            String username = "root";
            String password = "Pw";

            mysqlManager = new MySQLManager(host, database, username, password);
            mysqlManager.connect();
            getLogger().info("Connected to MySQL database.");
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to connect to MySQL database.", e);
            getProxy().stop(); // Stop the proxy since the plugin can't function properly
            return;
        }
        mysqlManager.createTablesIfNotExists();

        getProxy().getConsole().sendMessage(new TextComponent(""));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|"));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|→ Vubex-Proxy by " + ChatColor.YELLOW + "Flavius " + ChatColor.GRAY + "/ Plugin-Version: " + ChatColor.YELLOW + getDescription().getVersion() + ChatColor.GRAY + "."));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|"));


        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|→ Commands werden geladen."));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ VubexCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new Vubex_proxy.VubexCommand());
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ KickAllCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new KickAllCommand(vubexDiscordAPI));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ AdminchatCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new AdminchatCommand(vubexDiscordAPI));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ TeamchatCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new TeamchatCommand(vubexDiscordAPI));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ WebsiteCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new WebsiteCommand());
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ TiktokCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new TiktokCommand());
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ InstagramCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new InstagramCommand());
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ LobbyCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new LobbyCommand());
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ GetIPCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new GetIPCommand(mysqlManager));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ GetUUIDCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new GetUUIDCommand(mysqlManager));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ JoinToCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new JoinToCommand());
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ KickCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new KickCommand(vubexDiscordAPI));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ DiscordCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new DiscordCommand());
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ BroadcastCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new BroadcastCommand(vubexDiscordAPI));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ BanCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new BanCommand(mysqlManager, vubexDiscordAPI));
        getProxy().getPluginManager().registerListener(this, new BanManager(mysqlManager));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ BanInfoCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new BanInfoCommand(mysqlManager));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ BanDurationCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new BanDurationCommand(mysqlManager, vubexDiscordAPI));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ UnBanCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new UnBanCommand(mysqlManager, vubexDiscordAPI));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ CoinsCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new CoinsCommand(mysqlManager));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ AdminCoinsCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new AdminCoinsCommand(mysqlManager));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ OnlineCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new OnlineCommand(mysqlManager));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ PingCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new PingCommand());
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ WhereIsCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new WhereIsCommand(mysqlManager));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ MuteCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new MuteCommand(mysqlManager, vubexDiscordAPI));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ UnMuteCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new UnMuteCommand(mysqlManager, vubexDiscordAPI));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ MuteInfoCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new MuteInfoCommand(mysqlManager));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ MuteDurationCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new MuteDurationCommand(mysqlManager, vubexDiscordAPI));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ AdvertCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new AdvertCommand(mysqlManager));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ OnlineTimeCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new OnlineTimeCommand(mysqlManager));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ HelpCommand wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new HelpCommand());



        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|"));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|→ Listeners werden geladen."));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ CommandBlockerListener wird geladen."));
        getProxy().getPluginManager().registerListener(this, new CommandBlockerListener());
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ JoinListener wird geladen."));
        getProxy().getPluginManager().registerListener(this, new JoinListener(mysqlManager));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ ChatListener wird geladen."));
        getProxy().getPluginManager().registerListener(this, new ChatListener(mysqlManager));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ OnlineTimeListener wird geladen."));
        getProxy().getPluginManager().registerListener(this, new OnlineTimeListener(mysqlManager));



        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|"));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|→ Module werden geladen."));
        AutoBroadcastModule broadcastModule = new AutoBroadcastModule(this);
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ AutoBroadcastModule wird geladen. ["+ broadcastModule.getBroadcastMessages().size() + " Nachrichten]"));
        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "|   ↳ MaintenanceModule wird geladen."));
        getProxy().getPluginManager().registerCommand(this, new MaintenanceModule(mysqlManager));
        getProxy().getPluginManager().registerListener(this, new MaintenanceModule(mysqlManager));


        getProxy().getConsole().sendMessage(new TextComponent(consolePrefix + "| "));
        getProxy().getConsole().sendMessage(new TextComponent(""));
        getProxy().getConsole().sendMessage(new TextComponent(serverPrefix + "Vubex-Proxy ist bereit."));
        printBanner();
    }

    @Override
    public void onDisable() {
        if (mysqlManager != null) {
            try {
                mysqlManager.disconnect();
                getLogger().info("Disconnected from MySQL database.");
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to disconnect from MySQL database.", e);
            }
        }
        if (vubexDiscordAPI != null) {
            vubexDiscordAPI.stop();
        }
        AutoBroadcastModule.stopBroadcastTask();
    }

    private void printBanner() {
        getProxy().getConsole().sendMessage(new TextComponent(ChatColor.YELLOW + " _____ _____ _____ _____ __ __     _____ _____ _____ __ __ __ __ "));
        getProxy().getConsole().sendMessage(new TextComponent(ChatColor.YELLOW + "|  |  |  |  | __  |   __|  |  |___|  _  | __  |     |  |  |  |  |"));
        getProxy().getConsole().sendMessage(new TextComponent(ChatColor.YELLOW + "|  |  |  |  | __ -|   __|-   -|___|   __|    -|  |  |-   -|_   _|"));
        getProxy().getConsole().sendMessage(new TextComponent(ChatColor.YELLOW + " \\___/|_____|_____|_____|__|__|   |__|  |__|__|_____|__|__| |_| "));
        getProxy().getConsole().sendMessage(new TextComponent(ChatColor.YELLOW + "                                                                 "));
    }

    public static class VubexCommand extends Command {

        public VubexCommand() {
            super("vubex", null, "vubex-proxy", "developer", "system", "bungee");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatColor.translateAlternateColorCodes('&', "&eWillkommen auf Vubex! &7Programmiert von Flavius")));
        }
    }
}