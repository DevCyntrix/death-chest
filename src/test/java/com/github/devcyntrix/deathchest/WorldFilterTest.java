package com.github.devcyntrix.deathchest;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@DisplayName("World filter test")
public class WorldFilterTest {

    private ServerMock server;

    private PlayerMock player;

    @BeforeEach
    public void setUp() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("default-config.yml");
        if (stream == null)
            throw new IllegalStateException("Missing config");
        DeathChestConfig config;
        try (InputStreamReader reader = new InputStreamReader(stream)) {
            config = DeathChestConfig.load(YamlConfiguration.loadConfiguration(reader));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.server = MockBukkit.getOrCreateMock();
        this.server.setSpawnRadius(0);
        MockBukkit.load(DeathChestPlugin.class, true, config);
        List<ItemStack> content = new ArrayList<>(List.of(new ItemStack(Material.OAK_LOG)));

        this.player = server.addPlayer();
        PlayerInventory inventory = player.getInventory();
        inventory.addItem(content.toArray(ItemStack[]::new));
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Try to spawn in filtered world")
    public void dieInFilteredWorld() {

        WorldMock mock = server.addSimpleWorld("disabled_world");
        player.teleport(player.getLocation().toLocation(mock));
        @NotNull Location location = player.getLocation();
        player.setHealth(0.0);
        server.getScheduler().performOneTick();

        Block block = location.getBlock();
        Assertions.assertTrue(block.isEmpty());

        // Cannot check drops because of missing drop functionality of mock bukkit
//        WorldMock world = (WorldMock) location.getWorld();
//        Collection<Item> items = world.getEntitiesByClass(Item.class);
//        System.out.println(items);
//        items.forEach(item -> content.remove(item.getItemStack()));
//        Assertions.assertTrue(content.isEmpty());
    }

    @Test
    @DisplayName("Try to spawn in normal world")
    public void dieInNormalWorld() {
        System.out.println("Killing player...");
        player.setHealth(0.0);
        System.out.println("Perform tick");
        server.getScheduler().performOneTick();

        @NotNull Location location = player.getLocation();
        Block block = location.getBlock();
        System.out.println("Checking block...");
        Assertions.assertFalse(block.isEmpty());
        Assertions.assertTrue(location.getWorld().getEntitiesByClass(Item.class).isEmpty());

        // Cannot check drops because of missing drop functionality of mock bukkit
//        WorldMock world = (WorldMock) location.getWorld();
//        Collection<Item> items = world.getEntitiesByClass(Item.class);
//        System.out.println(items);
//        items.forEach(item -> content.remove(item.getItemStack()));
//        Assertions.assertTrue(content.isEmpty());
    }

}
