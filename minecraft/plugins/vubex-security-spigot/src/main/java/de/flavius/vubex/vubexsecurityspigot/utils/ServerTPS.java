package de.flavius.vubex.vubexsecurityspigot.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.flavius.vubex.vubexsecurityspigot.Vubex_security_spigot;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class ServerTPS {
    private static final HashMap<Long, Integer> tpsCash = Maps.newHashMap();

    private static long startMillis = 0;
    private static int currentTicks = 0;
    private static int currentTps = -1;

    public ServerTPS() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Vubex_security_spigot.getPlugin(), () -> {
            if(startMillis + 1000 <= System.currentTimeMillis()) {
                currentTps = currentTicks;
                tpsCash.put(startMillis, currentTps);
                startMillis = System.currentTimeMillis();
                currentTicks = 0;
                List<Long> valuesToRemove = Lists.newArrayList();
                tpsCash.forEach((aLong, integer) -> {
                    if(aLong <= System.currentTimeMillis() - 60000) {
                        valuesToRemove.add(aLong);
                    }
                });
                if(!valuesToRemove.isEmpty()) {
                    valuesToRemove.forEach(tpsCash::remove);
                }
            }
            currentTicks++;
        }, 0, 1);
    }

    public static double getCurrentTps() {
        return currentTps;
    }

    public static double getAverageTps(int seconds) {
        long time = seconds * 1000L;
        ArrayList<Integer> list = Lists.newArrayList();
        tpsCash.forEach((aLong, integer) -> {
            if(aLong > System.currentTimeMillis() - time) {
                list.add(integer);
            }
        });
        double value = 0;
        for (Integer integer : list) {
            value += integer;
        }
        return value / list.size();
    }
}
