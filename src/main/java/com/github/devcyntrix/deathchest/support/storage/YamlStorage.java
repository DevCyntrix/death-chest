package com.github.devcyntrix.deathchest.support.storage;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.storage.DeathChestStorage;
import com.github.devcyntrix.deathchest.controller.PlaceholderController;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Currently not used but will be implemented soon.
 */
public class YamlStorage implements DeathChestStorage {

    private final PlaceholderController placeHolderController;
    private final Multimap<World, DeathChestModel> deathChestsCache = HashMultimap.create();

    public YamlStorage(PlaceholderController placeHolderController) {
        this.placeHolderController = placeHolderController;
    }

    @Override
    public ConfigurationSection getDefaultOptions() {
        return new MemoryConfiguration();
    }

    private File getFile(World world, boolean create) throws IOException {
        File worldFolder = world.getWorldFolder();
        File file = new File(worldFolder, "death-chests.yml");
        if (create && !file.isFile() && !file.createNewFile())
            throw new IOException("Failed to create file \"%s\"".formatted(file));

        return file;
    }

    /**
     * Migrates the chests out of the saved-chests.yml file to the chests folder which separates the chests by world
     *
     * @param fromFile the saved-chests file
     */
    private void migrateChests(DeathChestPlugin plugin, File[] files, Logger logger) {
        logger.info("Starting death chest migration...");

        for (File file : files) {
            String name = file.getName();
            String realName = name.substring(0, name.lastIndexOf('.'));

            World world = Bukkit.getWorld(realName);
            if (world == null) {
                continue;
            }

            try {
                File newLocation = getFile(world, false);
                if (!file.renameTo(newLocation))
                    logger.severe("Failed to move the file \"" + file + "\" to \"" + newLocation + "\".");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void init(@NotNull DeathChestPlugin plugin, @NotNull ConfigurationSection section) throws IOException {
        Logger logger = plugin.getLogger();

        String filename = section.getString("folder", "chests");
        File chestsFolder = new File(plugin.getDataFolder(), filename);
        File[] files = chestsFolder.listFiles();
        if (files != null) {
            migrateChests(plugin, files, logger);
            files = chestsFolder.listFiles();
            if (files != null && files.length == 0) {
                if (!chestsFolder.delete()) {
                    logger.info("Failed to delete old \"" + chestsFolder + "\" folder");
                }
            }
        }

        // Load all chests of loaded worlds
        for (World world : Bukkit.getWorlds()) {
            File worldFile = getFile(world, false);
            if (!worldFile.isFile())
                continue;
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(worldFile);
            List<Map<String, Object>> chests = (List<Map<String, Object>>) configuration.getList("chests", Collections.emptyList());
            Set<DeathChestModel> list = chests.stream()
                    .map(map -> DeathChestModel.deserialize(map, plugin.getDeathChestConfig().inventoryOptions(), this.placeHolderController))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            this.deathChestsCache.putAll(world, list);
        }

    }

    @Override
    public void put(DeathChestModel chest) {
        this.deathChestsCache.put(chest.getWorld(), chest);
    }

    @Override
    public void update(Collection<DeathChestModel> chests) {
        for (DeathChestModel chest : chests) {
            this.deathChestsCache.put(chest.getWorld(), chest);
        }
    }

    @Override
    public Set<DeathChestModel> getChests() {
        return new HashSet<>(this.deathChestsCache.values());
    }

    @Override
    public Set<DeathChestModel> getChests(@NotNull World world) {
        return new HashSet<>(this.deathChestsCache.get(world));
    }

    @Override
    public void remove(@NotNull DeathChestModel chest) {
        this.deathChestsCache.remove(chest.getWorld(), chest);
    }

    @Override
    public void close() throws IOException {
        // It is important to iterate through all bukkit worlds to avoid duplication bugs because the last death chest in the worlds cannot be overwritten
        for (World world : Bukkit.getWorlds()) {
            File worldFile = getFile(world, true);

            List<Map<String, Object>> collect = deathChestsCache.get(world).stream()
                    .map(DeathChestModel::serialize)
                    .collect((Supplier<List<Map<String, Object>>>) Lists::newArrayList, List::add, List::addAll);
            YamlConfiguration configuration = new YamlConfiguration();
            configuration.set("chests", collect);
            configuration.save(worldFile);
        }
    }
}
