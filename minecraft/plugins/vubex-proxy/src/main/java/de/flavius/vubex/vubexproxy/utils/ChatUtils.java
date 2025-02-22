package de.flavius.vubex.vubexproxy.utils;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import net.md_5.bungee.api.ChatColor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Level;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class ChatUtils {

    private ChatUtils() {
        throw new IllegalStateException("Chat-Utility class");
    }

    public static String getColoredText(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String removeFormattingCharacters(String input) {
        StringBuilder result = new StringBuilder();
        if(!input.contains("§")){
            input = getColoredText(input);
        }

        char[] chars = input.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '§' && i + 1 < chars.length) {
                // Skip the character after the § symbol
                i++;
            } else {
                result.append(chars[i]);
            }
        }

        return result.toString();
    }


    private static final ConcurrentHashMap<UUID, String> playerNameCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Long> playerNameCacheTime = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, UUID> playerUUIDCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> playerUUIDCacheTime = new ConcurrentHashMap<>();
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static String getOfflinePlayerName(UUID uuid) {
        String cachedName = playerNameCache.get(uuid);

        if (cachedName != null) {
            Long cachedTime = playerNameCacheTime.get(uuid);
            long currentTime = System.currentTimeMillis();
            if (cachedTime != null && currentTime - cachedTime >= 5000) {
                playerNameCache.remove(uuid);
                playerNameCacheTime.remove(uuid);
            } else {
                return cachedName;
            }
        }

        Future<String> futureName = executorService.submit(() -> fetchPlayerNameFromServer(uuid));

        try {
            String name = futureName.get(5, TimeUnit.SECONDS);
            if (name != null && !name.isEmpty()) {
                playerNameCache.put(uuid, name);
                playerNameCacheTime.put(uuid, System.currentTimeMillis());
                return name;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist beim Abrufen des Spielernamens für die UUID " + uuid + " aufgetreten:", e);
        }

        return "Unbekannter Spielername";
    }

    public static UUID getUUIDFromPlayerName(String playerName) {
        UUID cachedUUID = playerUUIDCache.get(playerName);

        if (cachedUUID != null) {
            long cachedTime = playerUUIDCacheTime.get(playerName);
            long currentTime = System.currentTimeMillis();
            if (currentTime - cachedTime >= 5000) {
                playerUUIDCache.remove(playerName);
            } else {
                return cachedUUID;
            }
        }

        CompletableFuture<UUID> futureUUID = CompletableFuture.supplyAsync(() -> fetchUUIDFromServer(playerName), executorService);

        try {
            UUID uuid = futureUUID.get(5, TimeUnit.SECONDS);
            if (uuid != null) {
                playerUUIDCache.put(playerName, uuid);
                playerUUIDCacheTime.put(playerName, System.currentTimeMillis());
                return uuid;
            } else {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist beim Abrufen der UUID für den Spieler " + playerName + " aufgetreten.");
            }
        } catch (TimeoutException e) {
            futureUUID.cancel(true);
            playerUUIDCache.remove(playerName);
        } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        } catch (Exception e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist beim Abrufen der UUID für den Spieler " + playerName + " aufgetreten:", e);
        }

        return null;
    }

    private static String fetchPlayerNameFromServer(UUID uuid) {
        try {
            String apiEndpoint = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", "");
            URL url = new URL(apiEndpoint);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                String responseString = response.toString();
                return extractNameFromResponseString(responseString);
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist beim Abrufen des Spielernamens für UUID " + uuid + " aufgetreten:", e);
        }
        return null;
    }

    private static UUID fetchUUIDFromServer(String playerName) {
        try {
            String apiUrl = "https://api.mojang.com/users/profiles/minecraft/" + playerName;
            URL url = new URL(apiUrl);
            String responseString = fetchResponseStringFromURL(url);

            if (!responseString.isEmpty()) {
                String[] responseParts = responseString.split("\"");
                String uuidString = responseParts[3];

                if (!uuidString.isEmpty()) {
                    // Convert the UUID without dashes to a valid UUID format
                    uuidString = uuidString.replaceAll("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3-$4-$5");
                    return UUID.fromString(uuidString);
                }
            }

            return null;
        } catch (IOException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist beim Abrufen der UUID für den Spieler " + playerName + " aufgetreten:", e);
            return null;
        }
    }

    private static String extractNameFromResponseString(String responseString) {
        int nameStartIndex = responseString.indexOf("\"name\" : \"") + 10;
        int nameEndIndex = responseString.indexOf("\"", nameStartIndex);
        if (nameStartIndex >= 0 && nameEndIndex >= 0) {
            return responseString.substring(nameStartIndex, nameEndIndex);
        }
        return null;
    }

    private static String fetchResponseStringFromURL(URL url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } catch (IOException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist beim Abrufen der Antwort von der URL " + url + " aufgetreten:", e);
            return "";
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}