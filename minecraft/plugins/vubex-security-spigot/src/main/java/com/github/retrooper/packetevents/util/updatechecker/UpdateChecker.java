/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.retrooper.packetevents.util.updatechecker;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.ColorUtil;
import com.github.retrooper.packetevents.util.PEVersion;
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * PacketEvents update checker.
 *
 * @author retrooper
 * @since 1.6.9
 */
public class UpdateChecker {
    public String checkLatestReleasedVersion() {
        try {
            URLConnection connection = new URL("https://api.github.com/repos/retrooper/packetevents/releases/latest").openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/4.0");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String jsonResponse = reader.readLine();
            reader.close();
            JsonObject jsonObject = AdventureSerializer.getGsonSerializer().serializer().fromJson(jsonResponse, JsonObject.class);
            return jsonObject.get("name").getAsString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse packetevents version!", e);
        }
    }

    /**
     * Check for an update and log in the console (ALL DONE ON THE CURRENT THREAD).
     */
    public UpdateCheckerStatus checkForUpdate() {
        PEVersion localVersion = PacketEvents.getAPI().getVersion();
        PEVersion newVersion;
        try {
            newVersion = new PEVersion(checkLatestReleasedVersion());
        } catch (Exception ex) {
            ex.printStackTrace();
            newVersion = null;
        }
        if (newVersion != null && localVersion.isOlderThan(newVersion)) {
            PacketEvents.getAPI().getLogManager().warn("There is an update available for packetevents! Your build: ("
                    + ColorUtil.toString(NamedTextColor.YELLOW) + localVersion
                    + ColorUtil.toString(NamedTextColor.WHITE) + ") | Latest released build: ("
                    + ColorUtil.toString(NamedTextColor.GREEN) + newVersion
                    + ColorUtil.toString(NamedTextColor.RED) + ")");
            return UpdateCheckerStatus.OUTDATED;
        } else if (newVersion != null && localVersion.isNewerThan(newVersion)) {
            PacketEvents.getAPI().getLogManager().info("You are on a dev or pre released build of packetevents. Your build: ("
                    + ColorUtil.toString(NamedTextColor.AQUA) + localVersion
                    + ColorUtil.toString(NamedTextColor.WHITE) + ") | Latest released build: ("
                    + ColorUtil.toString(NamedTextColor.DARK_AQUA) + newVersion
                    + ColorUtil.toString(NamedTextColor.WHITE) + ")");
            return UpdateCheckerStatus.PRE_RELEASE;
        } else if (localVersion.equals(newVersion)) {
            PacketEvents.getAPI().getLogManager().info("You are on the latest released version of packetevents. ("
                    + ColorUtil.toString(NamedTextColor.GREEN) + newVersion + ColorUtil.toString(NamedTextColor.WHITE) + ")");
            return UpdateCheckerStatus.UP_TO_DATE;
        } else {
            PacketEvents.getAPI().getLogManager().warn("Something went wrong while checking for an update. Your build: (" + localVersion + ")");
            return UpdateCheckerStatus.FAILED;
        }
    }

    public void handleUpdateCheck() {
        Thread thread = new Thread(() -> {
            PacketEvents.getAPI().getLogManager().info("Checking for an update, please wait...");
            UpdateCheckerStatus status = checkForUpdate();
            int waitTimeInSeconds = 5;
            int maxRetryCount = 5;
            int retries = 0;
            while (retries < maxRetryCount) {
                if (status != UpdateCheckerStatus.FAILED) {
                    break;
                }
                PacketEvents.getAPI().getLogManager().warn("[Checking for an update again in " + waitTimeInSeconds + " seconds...");
                try {
                    Thread.sleep(waitTimeInSeconds * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                waitTimeInSeconds *= 2;

                status = checkForUpdate();

                if (retries == (maxRetryCount - 1)) {
                    PacketEvents.getAPI().getLogManager().warn("packetevents failed to check for an update. No longer retrying.");
                    break;
                }

                retries++;
            }

        }, "packetevents-update-check-thread");
        thread.start();
    }

    /**
     * Result of an update check.
     *
     * @author retrooper
     * @since 1.8
     */
    public enum UpdateCheckerStatus {
        /**
         * Your build is outdated, an update is available.
         */
        OUTDATED,
        /**
         * You are on a dev or pre-released build. Not on the latest stable release(not necessarily bad).
         */
        PRE_RELEASE,
        /**
         * Your build is up-to-date. Latest stable release.
         */
        UP_TO_DATE,
        /**
         * Failed to check for an update. There might be an issue with your connection.
         */
        FAILED
    }
}