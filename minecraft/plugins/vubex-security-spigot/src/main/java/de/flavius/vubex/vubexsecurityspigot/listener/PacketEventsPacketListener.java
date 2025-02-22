package de.flavius.vubex.vubexsecurityspigot.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import de.flavius.vubex.vubexsecurityspigot.Vubex_security_spigot;
import de.flavius.vubex.vubexsecurityspigot.utils.ChatUtils;
import de.flavius.vubex.vubexsecurityspigot.utils.UserMethodPair;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class PacketEventsPacketListener implements PacketListener {
    private final Map<String, Integer> packetCountMap = new ConcurrentHashMap<>();

    private final Map<Player, Integer> fireworkCounts = new HashMap<>();

    private final Map<Player, Long> lastFireworkTimes = new HashMap<>();

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final Map<UserMethodPair, Integer> methodFlagCounts = new HashMap<>();

    private Logger logger;

    private final String kickmessage = Vubex_security_spigot.serverPrefix + ChatUtils.getColoredText("&c&lAntiExploit System");

    public Boolean isLoggingEnabled = true;

    public PacketEventsPacketListener() {
        scheduler.scheduleAtFixedRate(this::resetCounts, 1L, 1L, TimeUnit.SECONDS);
        if (isLoggingEnabled)
            try {
                String date = LocalDate.now().toString();
                this.logger = Logger.getLogger("PacketEventsLogger");
                Path path = Paths.get("./plugins/VubexSecurityAntiExploit/logs");
                this.logger.setUseParentHandlers(false);
                if (!Files.exists(path))
                    Files.createDirectories(path);
                FileHandler fileHandler = new FileHandler(path + "/" + date + ".log", true);
                fileHandler.setFormatter(new SimpleFormatter());
                this.logger.addHandler(fileHandler);
            } catch (IOException e) {
                logger.severe("Error on PacketEventsPacketListener " + e.getMessage());
            }
    }

    private void resetCounts() {
        this.packetCountMap.clear();
    }

    public void onPacketReceive(PacketReceiveEvent event) {
        List<PacketType.Play.Client> ignoredPackets = Collections.singletonList(PacketType.Play.Client.CREATIVE_INVENTORY_ACTION);
        Set<PacketType.Play.Client> ignoredPacketsSet = new HashSet<>(ignoredPackets);
        if (!ignoredPacketsSet.contains(event.getPacketType()))
            try {
                String key = event.getUser().getUUID().toString() + event.getPacketType().toString();
                int packetCount = this.packetCountMap.getOrDefault(key, 0) + 1;
                if (packetCount > 150) {
                    event.setCancelled(true);
                    PacketReceiveEvent copy = event.clone();
                    copy.getUser().sendPacket(new WrapperPlayServerDisconnect(Component.text(kickmessage)));
                    copy.getUser().closeConnection();
                    copy.cleanUp();
                    Vubex_security_spigot.sendAlert("Flood A", "§dPackage: §5" + event.getPacketType().getName(), event.getUser().getName(), "0");
                    this.packetCountMap.put(key, packetCount);
                } else {
                    this.packetCountMap.put(key, packetCount);
                }
            } catch (Exception ignored) {}
        int kickLimit = 5;
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            WrapperPlayClientClickWindow evento = new WrapperPlayClientClickWindow(event);
            if (evento.getCarriedItemStack() != null && (
                    evento.getCarriedItemStack().getType() == ItemTypes.WRITTEN_BOOK || evento.getCarriedItemStack().getType() == ItemTypes.WRITABLE_BOOK || evento.getCarriedItemStack().getType() == ItemTypes.FIREWORK_ROCKET || evento.getCarriedItemStack().getType() == ItemTypes.BOOK || evento.getCarriedItemStack().getType() == ItemTypes.OAK_SIGN)) {
                String num = String.valueOf(0);
                assert evento.getBuffer() != null;
                String msg = evento.getBuffer().toString();
                String patternString = "widx:\\s*(\\d+)";
                Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
                Matcher risultato = pattern.matcher(msg);
                if (risultato.find())
                    num = risultato.group(1);
                if (21 < Integer.parseInt(num)) {
                    event.setCancelled(true);
                    UserMethodPair key = new UserMethodPair(event.getUser().getUUID(), event.getPacketType().getName());
                    int flagCount = this.methodFlagCounts.getOrDefault(key, 0);
                    Vubex_security_spigot.sendAlert("click_window", "§dItem: §5" + evento.getCarriedItemStack(), event.getUser().getName(), String.valueOf(flagCount));
                    if (flagCount >= kickLimit) {
                        PacketReceiveEvent copy = event.clone();
                        copy.getUser().sendPacket(new WrapperPlayServerDisconnect(Component.text(kickmessage)));
                        copy.getUser().closeConnection();
                        copy.cleanUp();
                        Vubex_security_spigot.sendAlert2("click_Window", "§dItem: §5" + evento.getCarriedItemStack(), event.getUser().getName());
                        this.methodFlagCounts.remove(key);
                    } else {
                        this.methodFlagCounts.put(key, flagCount + 1);
                    }
                }
            }
        }
        if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
            WrapperPlayClientTabComplete evento = new WrapperPlayClientTabComplete(event);
            String channelName = evento.getText();
            List<String> containsList = Arrays.asList(
                    "while",
                    "targetoffset",
                    "/to",
                    "//to",
                    "/calc",
                    "//calc",
                    "for(",
                    "^(.",
                    "*."
            );
            for (String item : containsList) {
                if (channelName.contains(item)) {
                    event.setCancelled(true);
                    UserMethodPair key = new UserMethodPair(event.getUser().getUUID(), event.getPacketType().getName());
                    int flagCount = this.methodFlagCounts.getOrDefault(key, 0);
                    Vubex_security_spigot.sendAlert("Tab-A", "§dCommands: §5" + evento.getText(), event.getUser().getName(), String.valueOf(flagCount));
                    if (flagCount >= kickLimit) {
                        PacketReceiveEvent copy = event.clone();
                        copy.getUser().sendPacket(new WrapperPlayServerDisconnect(Component.text(kickmessage)));
                        copy.getUser().closeConnection();
                        copy.cleanUp();
                        Vubex_security_spigot.sendAlert2("Tab-A", "§dCommands: §5" + evento.getText(), event.getUser().getName());
                        this.methodFlagCounts.remove(key);
                    } else {
                        this.methodFlagCounts.put(key, flagCount + 1);
                    }
                    return;
                }
            }
        }
        if (event.getPacketType() == PacketType.Play.Client.CHAT_MESSAGE) {
            WrapperPlayClientChatMessage evento = new WrapperPlayClientChatMessage(event);
            String channelName = evento.getMessage();
            List<String> containsList = Arrays.asList(
                    // WorldEdit exploit
                    "//calc",
                    "//calculate",
                    "//eval",
                    "//evaluate",
                    "//solve",
                    // HolographicDisplays exploit
                    "/hd readtext",
                    "/holo readtext",
                    "/hologram readtext",
                    "/holograms readtext",
                    "/holographicdisplays readtext",
                    // Multiverse exploit
                    "/mv ^",
                    "/mv help ^",
                    "/mvhelp ^",
                    "/$"
            );
            for (String item : containsList) {
                if (channelName.startsWith(item)) {
                    event.setCancelled(true);
                    UserMethodPair key = new UserMethodPair(event.getUser().getUUID(), event.getPacketType().getName());
                    int flagCount = this.methodFlagCounts.getOrDefault(key, 0);
                    Vubex_security_spigot.sendAlert("Command-A", "§dCommands: §5" + evento.getMessage(), event.getUser().getName(), String.valueOf(flagCount));
                    if (flagCount >= kickLimit) {
                        PacketReceiveEvent copy = event.clone();
                        copy.getUser().sendPacket(new WrapperPlayServerDisconnect(Component.text(kickmessage)));
                        copy.getUser().closeConnection();
                        copy.cleanUp();
                        Vubex_security_spigot.sendAlert2("Command-A", "§dCommands: §5" + evento.getMessage(), event.getUser().getName());
                        this.methodFlagCounts.remove(key);
                    } else {
                        this.methodFlagCounts.put(key, flagCount + 1);
                    }
                    return;
                }
            }
        }
        if (event.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
            WrapperPlayClientPluginMessage evento = new WrapperPlayClientPluginMessage(event);
            if (evento.getChannelName().contains("MC|BEdit") || evento.getChannelName().contains("MC|BSign") || evento.getChannelName().contains("MC|BOpen")) {
                Player player = (Player)event.getPlayer();
                if (player.getInventory().getItemInMainHand().getType().equals(Material.WRITTEN_BOOK) || player.getInventory().getItemInMainHand().getType().equals(Material.LEGACY_BOOK_AND_QUILL)) {
                    String num = String.valueOf(0);
                    assert evento.getBuffer() != null;
                    String msg = evento.getBuffer().toString();
                    String patternString = "widx:\\s*(\\d+)";
                    Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
                    Matcher risultato = pattern.matcher(msg);
                    if (risultato.find())
                        num = risultato.group(1);
                    if (4564 < Integer.parseInt(num)) {
                        event.setCancelled(true);
                        UserMethodPair userMethodPair = new UserMethodPair(event.getUser().getUUID(), event.getPacketType().getName());
                        int j = this.methodFlagCounts.getOrDefault(userMethodPair, 0);
                        Vubex_security_spigot.sendAlert("PayLoad-B", "§dPackage: §5" + evento.getChannelName(), event.getUser().getName(), String.valueOf(j));
                        if (j >= kickLimit) {
                            PacketReceiveEvent copy = event.clone();
                            copy.getUser().sendPacket(new WrapperPlayServerDisconnect(Component.text(kickmessage)));
                            copy.getUser().closeConnection();
                            copy.cleanUp();
                            Vubex_security_spigot.sendAlert2("PayLoad-B", "§dPackage: §5" + evento.getChannelName(), event.getUser().getName());
                            this.methodFlagCounts.remove(userMethodPair);
                        } else {
                            this.methodFlagCounts.put(userMethodPair, j + 1);
                        }
                    }
                    return;
                }
                event.setCancelled(true);
                UserMethodPair key = new UserMethodPair(event.getUser().getUUID(), event.getPacketType().getName());
                int flagCount = this.methodFlagCounts.getOrDefault(key, 0);
                Vubex_security_spigot.sendAlert("PayLoad", "§dPackage: §5" + evento.getChannelName(), event.getUser().getName(), String.valueOf(flagCount));
                if (flagCount >= kickLimit) {
                    PacketReceiveEvent copy = event.clone();
                    copy.getUser().sendPacket(new WrapperPlayServerDisconnect(Component.text(kickmessage)));
                    copy.getUser().closeConnection();
                    copy.cleanUp();
                    Vubex_security_spigot.sendAlert2("PayLoad", "§dPackage: §5" + evento.getChannelName(), event.getUser().getName());
                    this.methodFlagCounts.remove(key);
                } else {
                    this.methodFlagCounts.put(key, flagCount + 1);
                }
            }
        }
        if (event.getPacketType() == PacketType.Play.Client.CREATIVE_INVENTORY_ACTION) {
            Player player = (Player)event.getPlayer();
            if (player != null && player.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
                new WrapperPlayClientCreativeInventoryAction(event);
                PacketReceiveEvent copy = event.clone();
                copy.getUser().sendPacket(new WrapperPlayServerDisconnect(Component.text(kickmessage)));
                copy.getUser().closeConnection();
                copy.cleanUp();
                Vubex_security_spigot.sendAlert("Creative", "§dSomeone tried to switch to creative mode.", event.getUser().getName(), "0");
                return;
            }
            assert player != null;
            if (player.getGameMode() == GameMode.CREATIVE) {
                WrapperPlayClientCreativeInventoryAction evento = new WrapperPlayClientCreativeInventoryAction(event);
                String num = String.valueOf(0);
                assert evento.getBuffer() != null;
                String msg = evento.getBuffer().toString();
                String patternString = "widx:\\s*(\\d+)";
                Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
                Matcher risultato = pattern.matcher(msg);
                if (risultato.find())
                    num = risultato.group(1);
                if (90 < Integer.parseInt(num)) {
                    event.setCancelled(true);
                    UserMethodPair key = new UserMethodPair(event.getUser().getUUID(), event.getPacketType().getName());
                    int flagCount = this.methodFlagCounts.getOrDefault(key, 0);
                    Vubex_security_spigot.sendAlert("Creative-B", "§dMethod: §5§dtried to create a crasher in creative mode.", event.getUser().getName(), String.valueOf(flagCount));
                    if (flagCount >= kickLimit) {
                        PacketReceiveEvent copy = event.clone();
                        copy.getUser().sendPacket(new WrapperPlayServerDisconnect(Component.text(kickmessage)));
                        copy.getUser().closeConnection();
                        copy.cleanUp();
                        Vubex_security_spigot.sendAlert2("Creative-B", "§dMethod: §5§dtried to create a crasher in creative mode.", event.getUser().getName());
                        this.methodFlagCounts.remove(key);
                    } else {
                        this.methodFlagCounts.put(key, flagCount + 1);
                    }
                }
                return;
            }
        }
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            WrapperPlayClientPlayerBlockPlacement evento = new WrapperPlayClientPlayerBlockPlacement(event);
            if (evento.getItemStack().isPresent()) {
                ItemStack wrappedItemStack = evento.getItemStack().get();
                Player player = (Player)event.getPlayer();
                if (player.getInventory().getItemInMainHand().getType().equals(Material.FIREWORK_ROCKET)) {
                    Long lastFireworkTime = this.lastFireworkTimes.getOrDefault(player, 0L);
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastFireworkTime > 1000L)
                        this.fireworkCounts.put(player, 0);
                    this.lastFireworkTimes.put(player, currentTime);
                    Integer count = this.fireworkCounts.getOrDefault(player, 0);
                    count = count + 1;
                    this.fireworkCounts.put(player, count);
                    if (count > 10) {
                        event.setCancelled(true);
                        UserMethodPair key = new UserMethodPair(event.getUser().getUUID(), event.getPacketType().getName());
                        int flagCount = this.methodFlagCounts.getOrDefault(key, 0);
                        Vubex_security_spigot.sendAlert("Netty A", "§dItem: §5FireWork", event.getUser().getName(), String.valueOf(flagCount));
                        if (flagCount >= kickLimit) {
                            PacketReceiveEvent copy = event.clone();
                            copy.getUser().sendPacket(new WrapperPlayServerDisconnect(Component.text(kickmessage)));
                            copy.getUser().closeConnection();
                            copy.cleanUp();
                            Vubex_security_spigot.sendAlert2("Netty A", "§dItem: §5FireWork", event.getUser().getName());
                            this.methodFlagCounts.remove(key);
                        } else {
                            this.methodFlagCounts.put(key, flagCount + 1);
                        }
                        this.fireworkCounts.remove(player);
                        this.lastFireworkTimes.remove(player);
                    }
                } else if (wrappedItemStack.getType() == ItemTypes.FIREWORK_ROCKET || wrappedItemStack.getType() == ItemTypes.WRITTEN_BOOK || wrappedItemStack.getType() == ItemTypes.WRITABLE_BOOK) {
                    if (player.getInventory().getItemInMainHand().getType().equals(Material.WRITTEN_BOOK) || player.getInventory().getItemInMainHand().getType().equals(Material.LEGACY_BOOK_AND_QUILL)) {
                        assert evento.getBuffer() != null;
                        System.out.println(evento.getBuffer().toString());
                        return;
                    }
                    event.setCancelled(true);
                    this.fireworkCounts.remove(player);
                    UserMethodPair key = new UserMethodPair(event.getUser().getUUID(), event.getPacketType().getName());
                    int flagCount = this.methodFlagCounts.getOrDefault(key, 0);
                    Vubex_security_spigot.sendAlert("Netty B", "§dItem: §5" + wrappedItemStack.getType().getName(), event.getUser().getName(), String.valueOf(flagCount));
                    if (flagCount >= kickLimit) {
                        PacketReceiveEvent copy = event.clone();
                        copy.getUser().sendPacket(new WrapperPlayServerDisconnect(Component.text(kickmessage)));
                        copy.getUser().closeConnection();
                        copy.cleanUp();
                        Vubex_security_spigot.sendAlert2("Netty B", "§dItem: §5" + wrappedItemStack.getType().getName(), event.getUser().getName());
                        this.methodFlagCounts.remove(key);
                    } else {
                        this.methodFlagCounts.put(key, flagCount + 1);
                    }
                    this.lastFireworkTimes.remove(player);
                } else {
                    this.fireworkCounts.remove(player);
                    this.lastFireworkTimes.remove(player);
                }
            }
        }
        List<PacketType.Play.Client> ignoredPackets2 = Arrays.asList(PacketType.Play.Client.PLAYER_FLYING, PacketType.Play.Client.HELD_ITEM_CHANGE, PacketType.Play.Client.CLIENT_STATUS, PacketType.Play.Client.CLIENT_SETTINGS, PacketType.Play.Client.PLAYER_POSITION, PacketType.Play.Client.KEEP_ALIVE, PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION, PacketType.Play.Client.ANIMATION, PacketType.Play.Client.PLAYER_ROTATION, PacketType.Play.Client.PLAYER_DIGGING);
        if (event.getPacketType() instanceof PacketType.Play.Client packetType) {
            if (!ignoredPackets2.contains(packetType) && isLoggingEnabled) {
                this.logger.info(event.getPacketType().getName() + " " + event.getUser().getName());
            }
        }
    }

    public void onPacketSend(PacketSendEvent event) {
        PacketListener.super.onPacketSend(event);
    }
}
