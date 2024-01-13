package com.github.devcyntrix.deathchest.support.storage;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.storage.DeathChestStorage;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MemoryStorage implements DeathChestStorage {

    private final Multimap<World, DeathChestModel> deathChestsCache = HashMultimap.create();

    @Override
    public ConfigurationSection getDefaultOptions() {
        return new MemoryConfiguration();
    }

    @Override
    public void init(DeathChestPlugin plugin, ConfigurationSection section) throws IOException {

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

    }
}
