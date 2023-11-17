package com.github.devcyntrix.deathchest.support.storage;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.storage.DeathChestStorage;
import com.github.devcyntrix.deathchest.controller.PlaceholderController;
import com.google.common.base.Preconditions;
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
    private File chestsFolder;
    private final Multimap<World, DeathChestModel> deathChestsCache = HashMultimap.create();

    public YamlStorage(PlaceholderController placeHolderController) {
        this.placeHolderController = placeHolderController;
    }

    @Override
    public ConfigurationSection getDefaultOptions() {
        ConfigurationSection section = new MemoryConfiguration();
        section.addDefault("file", "saved-chests.yml");
        section.addDefault("folder", "chests");
        return section;
    }

    private File getFile(World world, boolean create) throws IOException {
        Preconditions.checkNotNull(this.chestsFolder);
        File file = new File(this.chestsFolder, world.getName() + ".yml");
        if (create && !file.isFile() && !file.createNewFile())
            throw new IOException("Failed to create file \"%s\"".formatted(file));

        return file;
    }

    /**
     * Migrates the chests out of the saved-chests.yml file to the chests folder which separates the chests by world
     *
     * @param fromFile the saved-chests file
     */
    private void migrateChests(DeathChestPlugin plugin, File fromFile, Logger logger) {
        Preconditions.checkArgument(fromFile.isFile());
        logger.info("Starting death chest migration...");

        // Loading saved chests of the file
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(fromFile);
        List<?> chests = configuration.getList("chests", Collections.emptyList());
        logger.info(chests.size() + " chests found");

        Set<DeathChestModel> deathChests = new HashSet<>();
        for (Object chest : chests) {
            if (!(chest instanceof Map<?, ?> map))
                continue;
            DeathChestModel deserialize = DeathChestModel.deserialize((Map<String, Object>) map, plugin.getDeathChestConfig().inventoryOptions(), this.placeHolderController);
            if (deserialize == null) {
                logger.warning("Failed to deserialize a death chest");
                continue;
            }
            deathChests.add(deserialize);
        }

        // Separate the chests by world
        Map<World, YamlConfiguration> map = new HashMap<>();

        Iterator<DeathChestModel> iterator = deathChests.iterator();
        while (iterator.hasNext()) {
            DeathChestModel next = iterator.next();
            if (next.getWorld() == null)
                continue;
            iterator.remove();

            YamlConfiguration yamlConfiguration = map.computeIfAbsent(next.getWorld(), world -> {
                try {
                    File file = getFile(world, false);
                    if (file.isFile())
                        return YamlConfiguration.loadConfiguration(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return new YamlConfiguration();
            });
            List<Map<?, ?>> chests1 = (List<Map<?, ?>>) yamlConfiguration.getList("chests", new ArrayList<>());
            chests1.add(next.serialize());
        }

        // Save chests in separate files
        for (World world : map.keySet()) {
            try {
                File worldFile = getFile(world, true);
                map.get(world).save(worldFile);
            } catch (IOException e) {
                logger.severe("Failed to save chests in world \"" + world.getName() + "\" during migration");
                throw new RuntimeException(e);
            }
        }

        // Save all not migrated chests
        configuration = new YamlConfiguration();
        configuration.set("chests", deathChests.stream()
                .map(DeathChestModel::serialize)
                .toList());
        try {
            configuration.save(fromFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (deathChests.isEmpty() && !fromFile.renameTo(new File(fromFile.getParent(), fromFile.getName() + ".old"))) {
            logger.severe("Failed to rename the old storage file");
        }


    }

    @Override
    public void init(@NotNull DeathChestPlugin plugin, @NotNull ConfigurationSection section) throws IOException {

        String filename = section.getString("folder", "chests");
        this.chestsFolder = new File(plugin.getDataFolder(), filename);
        if (!this.chestsFolder.isDirectory() && !this.chestsFolder.mkdirs())
            throw new IOException("Cannot create folder \"" + this.chestsFolder + "\"");


        // Migration
        String savedChests = section.getString("file");
        if (savedChests != null) {
            File file = new File(plugin.getDataFolder(), savedChests);
            if (file.isFile()) {
                migrateChests(plugin, file, plugin.getLogger());
            }
        } else {
            plugin.getLogger().info("No save file found");
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
