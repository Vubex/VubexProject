package de.flavius.vubex.vubexsecurityspigot.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class SignChangeListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent paramSignChangeEvent) {
        for (String str : paramSignChangeEvent.getLines()) {
            if (str.length() >= 46) {
                System.out.println("SignChangeListener");
                paramSignChangeEvent.setCancelled(true);
                return;
            }
        }
    }
}
