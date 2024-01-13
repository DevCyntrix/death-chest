package com.github.devcyntrix.deathchest.controller;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import com.github.devcyntrix.deathchest.util.update.HangarUpdateChecker;
import com.github.devcyntrix.deathchest.util.update.UpdateChecker;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Checks for the newest version by using the SpigotMC API
 */
@Getter
@Singleton
public class UpdateController implements Closeable {

    private String newestVersion;
    private final BukkitTask updateScheduler;

    private final List<Consumer<String>> subscriberList = new ArrayList<>();

    @Inject
    public UpdateController(DeathChestPlugin plugin) {
        UpdateChecker checker = new HangarUpdateChecker(plugin);
        updateScheduler = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            String version = checker.getLatestRelease();
            if (version == null)
                return;
            if (plugin.getDescription().getVersion().equals(version))
                return;
            if (version.equals(this.newestVersion))
                return;

            this.newestVersion = version;
            subscriberList.forEach(subscriber -> subscriber.accept(newestVersion));

            DeathChestConfig config = plugin.getDeathChestConfig();
            if (config.autoUpdate()) {
                try (InputStream download = checker.download(this.newestVersion)) {
                    if (download != null) {
                        File updateFolder = Bukkit.getUpdateFolderFile();
                        if (!updateFolder.isDirectory() && !updateFolder.mkdirs())
                            return;

                        File updateFile = new File(updateFolder, plugin.getFile().getName());
                        if (!updateFile.isFile() && !updateFile.createNewFile())
                            return;

                        try (FileOutputStream stream = new FileOutputStream(updateFile)) {
                            download.transferTo(stream);
                        }
                    }
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to write the download on the disk.", e);
                }
            }
        }, 0, 20 * 60 * 30); // Every 30 minutes

    }

    public void subscribe(@NotNull Consumer<String> subscriber) {
        subscriberList.add(subscriber);
    }

    @Override
    public void close() {
        if (updateScheduler != null) {
            updateScheduler.cancel();
        }
    }
}
