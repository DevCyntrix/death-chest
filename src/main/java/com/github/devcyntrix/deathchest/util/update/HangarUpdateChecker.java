package com.github.devcyntrix.deathchest.util.update;

import com.google.common.base.Preconditions;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;

public class HangarUpdateChecker implements UpdateChecker {

    private final JavaPlugin plugin;

    public HangarUpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Nullable
    public String getLatestRelease() {
        PluginDescriptionFile description = plugin.getDescription();
        try (InputStream inputStream = new URL("https://hangar.papermc.io/api/v1/projects/%s/latestrelease".formatted(description.getName())).openStream(); Scanner scanner = new Scanner(inputStream)) {
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
    public @Nullable InputStream download(@NotNull String version) {
        Preconditions.checkNotNull(version, "version");
        PluginDescriptionFile description = plugin.getDescription();
        try {
            return new URL("https://hangar.papermc.io/api/v1/projects/%s/versions/%s/PAPER/download".formatted(description.getName(), version)).openStream();
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to download the newest version", e);
        }
        return null;
    }
}
