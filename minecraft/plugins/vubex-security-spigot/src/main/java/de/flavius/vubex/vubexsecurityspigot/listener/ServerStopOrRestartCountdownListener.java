package de.flavius.vubex.vubexsecurityspigot.listener;

import de.flavius.vubex.vubexsecurityspigot.Vubex_security_spigot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class ServerStopOrRestartCountdownListener implements Listener {
    // Außerhalb der BukkitRunnable-Klasse
    private final String STOP_TITLE = ChatColor.RED + "Server wird gestoppt";
    private final String RESTART_TITLE = ChatColor.RED + "Server wird neugestartet";

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();

        if (command.equalsIgnoreCase("/restart") || command.equalsIgnoreCase("/stop")) {
            event.setCancelled(true);

            player.sendTitle(
                    command.equalsIgnoreCase("/restart") ? RESTART_TITLE : STOP_TITLE,
                    ChatColor.GRAY + "In 10 Sekunden", 10, 70, 20
            );

            // Starte den Countdown
            new BukkitRunnable() {
                int countdown = 10;
                String lastTitle = ChatColor.GRAY + "In 10 Sekunden";

                @Override
                public void run() {
                    if (countdown <= 0) {
                        if (command.equalsIgnoreCase("/restart")) {
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                onlinePlayer.kickPlayer(ChatColor.RED + "Der Server wird neugestartet."); // TODO: Sende Spieler auf lobby server
                            }
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "restart");
                        } else if (command.equalsIgnoreCase("/stop")) {
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                onlinePlayer.kickPlayer(ChatColor.RED + "Der Server wurde gestoppt."); // TODO: Sende Spieler auf lobby server
                            }
                            Bukkit.getServer().shutdown();
                        }
                        cancel();
                        return;
                    }

                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);

                    String title = ChatColor.GRAY + "In " + countdown + " Sekunden";
                    if (!title.equals(lastTitle)) {
                        player.sendTitle(
                                command.equalsIgnoreCase("/restart") ? RESTART_TITLE : STOP_TITLE,
                                title, 0, 20, 0
                        );
                        lastTitle = title;
                    }

                    countdown--;
                }
            }.runTaskTimer(Vubex_security_spigot.getInstance(), 0, 20); // 20 Ticks pro Sekunde
        }
    }
}
