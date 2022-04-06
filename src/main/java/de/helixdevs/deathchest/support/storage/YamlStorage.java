package de.helixdevs.deathchest.support.storage;

import de.helixdevs.deathchest.DeathChestPlugin;
import de.helixdevs.deathchest.api.DeathChest;
import de.helixdevs.deathchest.api.storage.DeathChestStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class YamlStorage implements DeathChestStorage {

    private File file;
    private List<DeathChest> deathChests;

    @Override
    public ConfigurationSection getDefaultOptions() {
        ConfigurationSection section = new MemoryConfiguration();
        section.addDefault("file", "chests.yml");
        return section;
    }

    @Override
    public void init(JavaPlugin plugin, ConfigurationSection section) throws IOException {
        String filename = section.getString("file", "chests.yml");
        this.file = new File(plugin.getDataFolder(), filename);
        if (!this.file.isFile()) {
            this.file.createNewFile();
            this.deathChests = new LinkedList<>();
            return;
        }
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(this.file);
        List<?> chests = configuration.getList("chests", Collections.emptyList());

        this.deathChests = chests.stream()
                .filter(o -> o instanceof ConfigurationSection)
                .map(o -> (ConfigurationSection) o)
                .map(this::load)
                .collect(Collectors.toCollection(LinkedList::new));

    }

    private DeathChest load(ConfigurationSection section) {

        String worldId = section.getString("world");
        if (worldId == null)
            return null;
        UUID uuid = UUID.fromString(worldId);
        double x = section.getDouble("x"), y = section.getDouble("y"), z = section.getDouble("z");

        World world = Bukkit.getWorld(uuid);
        if (world == null)
            return null;

        Location location = new Location(world, x, y, z);
        Block block = location.getBlock();
        BlockState blockState = block.getState();

        if (!(blockState instanceof Chest chest))
            return null;

        long createdAt = section.getLong("createdAt");
        long expireAt = section.getLong("expireAt");

        OfflinePlayer player = null;
        String userId = section.getString("playerId");
        if (userId != null) {
            UUID playerUUID = UUID.fromString(userId);
            player = Bukkit.getOfflinePlayer(playerUUID);
        }

        List<Map<?, ?>> itemMap = section.getMapList("items");
        ItemStack[] items = itemMap.stream()
                .map(map -> ConfigurationSerialization.deserializeObject((Map<String, ?>) map, ItemStack.class))
                .filter(c -> c instanceof ItemStack)
                .map(c -> (ItemStack) c)
                .toArray(ItemStack[]::new);

        DeathChestPlugin plugin = JavaPlugin.getPlugin(DeathChestPlugin.class);
        return plugin.createDeathChest(location, createdAt, expireAt, player, items);
    }

    @Override
    public void put(DeathChest chest) {
        this.deathChests.add(chest);
    }

    @Override
    public Collection<DeathChest> getChests() {
        return this.deathChests;
    }

    @Override
    public void remove(DeathChest chest) {
        this.deathChests.remove(chest);
    }

    @Override
    public void save() {

    }

    @Override
    public void close() throws IOException {

    }
}
