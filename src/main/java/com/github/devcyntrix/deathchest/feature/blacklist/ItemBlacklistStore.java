package com.github.devcyntrix.deathchest.feature.blacklist;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ItemBlacklistStore implements Closeable {

    private final File file;
    private final Set<ItemStack> list = new HashSet<>();

    public ItemBlacklistStore(File file) {
        this.file = file;
        if (file.isFile()) {
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
            this.list.addAll((Collection<? extends ItemStack>) yamlConfiguration.getList("blacklist", new ArrayList<>()));
        }
        this.list.removeIf(Objects::isNull);
    }

    public Set<ItemStack> getList() {
        return list;
    }

    public void save() throws IOException {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("blacklist", this.list.stream().toList());
        configuration.save(file);
    }

    @Override
    public void close() throws IOException {
        save();
    }
}
