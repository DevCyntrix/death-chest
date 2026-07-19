package com.github.devcyntrix.deathchest.feature.update;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import com.github.devcyntrix.deathchest.util.update.HangarUpdateChecker;
import com.github.devcyntrix.deathchest.util.update.UpdateChecker;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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
@Singleton
public class UpdateService implements Closeable {

    private final DeathChestPlugin plugin;
    private final UpdateChecker updateChecker;
    private final BukkitTask updateScheduler;
    private final List<Consumer<NewUpdate>> subscriberList = new ArrayList<>();

    private NewUpdate newestVersion;

    @Inject
    public UpdateService(DeathChestPlugin plugin) {
        this.plugin = plugin;
        this.updateChecker = new HangarUpdateChecker(plugin);

        this.updateScheduler = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            String version = updateChecker.getLatestRelease();
            if (version == null)
                return;
            if (plugin.getDescription().getVersion().equals(version))
                return;
            if (this.newestVersion != null && version.equals(this.newestVersion.version()))
                return;

            DeathChestConfig config = plugin.getDeathChestConfig();
            boolean installed = config.autoUpdate() && downloadAndInstall(version);

            this.newestVersion = new NewUpdate(version, installed);
            this.subscriberList.forEach(subscriber -> subscriber.accept(newestVersion));

        }, 0, 20 * 60 * 30); // Every 30 minutes

    }

    private boolean downloadAndInstall(String version) {
        try (InputStream download = updateChecker.download(version)) {
            if (download == null)
                return false;

            File updateFolder = Bukkit.getUpdateFolderFile();
            if (!updateFolder.isDirectory() && !updateFolder.mkdirs())
                return false;

            File updateFile = new File(updateFolder, plugin.getFile().getName());
            if (!updateFile.isFile() && !updateFile.createNewFile())
                return false;

            try (FileOutputStream stream = new FileOutputStream(updateFile)) {
                download.transferTo(stream);
            }

            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to write the download on the disk.", e);
        }
        return false;
    }

    public void subscribe(@NotNull Consumer<NewUpdate> subscriber) {
        subscriberList.add(subscriber);
    }

    public DeathChestPlugin getPlugin() {
        return plugin;
    }

    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    public BukkitTask getUpdateScheduler() {
        return updateScheduler;
    }

    public List<Consumer<NewUpdate>> getSubscriberList() {
        return subscriberList;
    }

    public NewUpdate getNewestVersion() {
        return newestVersion;
    }

    @Override
    public void close() {
        if (updateScheduler != null) {
            updateScheduler.cancel();
        }
    }
}
