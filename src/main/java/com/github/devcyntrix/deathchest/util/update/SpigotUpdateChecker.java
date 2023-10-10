package com.github.devcyntrix.deathchest.util.update;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public record SpigotUpdateChecker(JavaPlugin plugin, int resourceId) implements UpdateChecker {

    @Nullable
    public String getLatestRelease() {
        try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream(); Scanner scanner = new Scanner(inputStream)) {
            if (!scanner.hasNext())
                return null;
            return scanner.next();
        } catch (IOException exception) {
            plugin.getLogger().info("Unable to check for updates: " + exception.getMessage());
            plugin.getLogger().info("To disable this message set the update checker to false in the config.yml");
        }
        return null;
    }

    @Override
    public InputStream download(@NotNull String version) {
        return null;
    }

}