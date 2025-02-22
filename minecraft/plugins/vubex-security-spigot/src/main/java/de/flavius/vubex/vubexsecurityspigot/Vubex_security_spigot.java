package de.flavius.vubex.vubexsecurityspigot;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import de.flavius.vubex.vubexsecurityspigot.listener.ChunkBanListener;
import de.flavius.vubex.vubexsecurityspigot.listener.PacketEventsPacketListener;
import de.flavius.vubex.vubexsecurityspigot.listener.*;
import de.flavius.vubex.vubexsecurityspigot.utils.ChatUtils;
import de.flavius.vubex.vubexsecurityspigot.utils.ServerTPS;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * @author : flavius
 * project : VubexProject
 **/
public final class Vubex_security_spigot extends JavaPlugin implements Listener{
    private static Vubex_security_spigot instance;
    private static final Logger logger = Bukkit.getLogger();

    private static final Set<UUID> alertEnabledPlayers = new HashSet<>();
    public static final String serverPrefix = ChatColor.translateAlternateColorCodes('&', "§e§lVubex.DE§r §8» §7");
    public static final String consolePrefix = ChatColor.translateAlternateColorCodes('&', "§e§lVubex-Security - Spigot§r §8» §7");

    @Override
    public void onEnable() {
        instance = this;
        new ServerTPS();

        getLogger().info("");
        getLogger().info(consolePrefix + "|");
        getLogger().info(consolePrefix + "|→ Vubex-Security (Spigot) by " + ChatColor.YELLOW + "Flavius " + ChatColor.GRAY + "/ Plugin-Version: " + ChatColor.YELLOW + getDescription().getVersion() + ChatColor.GRAY + ".");
        getLogger().info(consolePrefix + "|");

        Objects.requireNonNull(getCommand("vubexsecurity")).setExecutor(this);
        PacketEvents.getAPI().getEventManager().registerListener(new PacketEventsPacketListener(), PacketListenerPriority.NORMAL);
        PacketEvents.getAPI().init();

        getServer().getPluginManager().registerEvents(new CommandBlockerListener(), this);
        getServer().getPluginManager().registerEvents(new FaweTabCompletionListener(), this);
        getServer().getPluginManager().registerEvents(new ServerStopOrRestartCountdownListener(), this);
        getServer().getPluginManager().registerEvents(new SignChangeListener(), this);
        getServer().getPluginManager().registerEvents(new RedstoneLagListener(), this);
        getServer().getPluginManager().registerEvents(new ChunkBanListener(), this);
        getServer().getPluginManager().registerEvents(new LagPatchesListener(), this);
        getServer().getPluginManager().registerEvents(new OnTeleportListener(), this);
        getServer().getPluginManager().registerEvents(new PortalBreakerListener(), this);

        getLogger().info(consolePrefix + "| ");
        getLogger().info("");
        getLogger().info(serverPrefix + "Vubex-Security (Spigot) ist bereit.");
        printBanner();
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    public static Vubex_security_spigot getPlugin() {
        return getPlugin(Vubex_security_spigot.class);
    }

    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().checkForUpdates(false).bStats(false);
        PacketEvents.getAPI().load();
    }

    public static Vubex_security_spigot getInstance() {
        return instance;
    }

    private void printBanner() {
        getLogger().info(ChatColor.YELLOW + " _____ _____ _____ _____ __ __     _____ _____ _____ _____ _____ _____ _____ __ __ ");
        getLogger().info(ChatColor.YELLOW + "|  |  |  |  | __  |   __|  |  |___|   __|   __|     |  |  | __  |     |_   _|  |  |");
        getLogger().info(ChatColor.YELLOW + "|  |  |  |  | __ -|   __|-   -|___|__   |   __|   --|  |  |    -|-   -| | | |_   _|");
        getLogger().info(ChatColor.YELLOW + " \\___/|_____|_____|_____|__|__|   |_____|_____|_____|_____|__|__|_____| |_|   |_| ");
        getLogger().info(ChatColor.YELLOW + "                                  (Spigot-Plugin)                                  ");
    }

    public static void sendAlert(String message, String hoverText, String playername, String flag) {
        for (UUID uuid : alertEnabledPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                BaseComponent[] hoverMessage = (new ComponentBuilder(hoverText)).create();
                String formattedString = serverPrefix + ChatUtils.getColoredText("&e%player% &7&dfailed &7e%method% &d[&5%flag%&d]").replaceAll("%player%", playername).replaceAll("%flag%", flag).replaceAll("%method%", message);
                BaseComponent[] components = TextComponent.fromLegacyText(formattedString);
                for (BaseComponent component : components) {
                    if (component instanceof TextComponent)
                        (component).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMessage));
                }
                player.spigot().sendMessage(components);
                sendDiscordWebhook(playername + " failed **" + message + "**");
            }
        }
    }

    public static void sendAlert2(String message, String hoverText, String playername) {
        for (UUID uuid : alertEnabledPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                BaseComponent[] hoverMessage = (new ComponentBuilder(hoverText)).create();
                String formattedString = serverPrefix + ChatUtils.getColoredText("&e%player% &7&dfailed &7e%method% &d[&5%flag%&d] &7and has been kicked").replaceAll("%player%", playername).replaceAll("%method%", message);
                BaseComponent[] components = TextComponent.fromLegacyText(formattedString);
                for (BaseComponent component : components) {
                    if (component instanceof TextComponent)
                        (component).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMessage));
                }
                player.spigot().sendMessage(components);
                sendDiscordWebhook(playername + " kicked **" + message + "**");
            }
        }
    }

    public static void sendAlert3(String message, String hoverText, String blockname, String coordinates) {
        for (UUID uuid : alertEnabledPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                BaseComponent[] hoverMessage = (new ComponentBuilder(hoverText)).create();
                String formattedString = serverPrefix + ChatUtils.getColoredText("&e%player% &7&dfailed &7e%method% &d[&5%flag%&d]").replaceAll("%block%", blockname).replaceAll("%coordinates%", coordinates).replaceAll("%method%", message);
                BaseComponent[] components = TextComponent.fromLegacyText(formattedString);
                for (BaseComponent component : components) {
                    if (component instanceof TextComponent)
                        (component).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMessage));
                }
                player.spigot().sendMessage(components);
                sendDiscordWebhook(blockname + " failed **" + message + "**");
            }
        }
    }

    private static void sendDiscordWebhook(String message) {
        try {
            String jsonMessage = "{\"content\":\"" + message + "\"}";
            URL url = new URL("DISCORD WEB HOOK");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            try (OutputStream stream = connection.getOutputStream()) {
                stream.write(jsonMessage.getBytes());
                stream.flush();
            }
            int responseCode = connection.getResponseCode();
            logger.info("Discord Webhook Response Code: " + responseCode);
            connection.disconnect();
        } catch (Exception e) {
            logger.severe("Error sending Discord webhook: " + e.getMessage());
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission("vubex.vubexsecurity.alerts")) {
            alertEnabledPlayers.remove(event.getPlayer().getUniqueId());
        } else {
            alertEnabledPlayers.add(event.getPlayer().getUniqueId());
        }
    }

    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        sender.sendMessage(serverPrefix + ChatUtils.getColoredText("&eVubexSecurity System by Flavius"));
        return true;
    }
}



