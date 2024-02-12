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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@DisplayName("Chest spawn tests")
public class ChestSpawnTest {

    private ServerMock server;

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
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("No chest if inventory was empty")
    public void doesntSpawnEmptyChest() {
        PlayerMock player = server.addPlayer();
        @NotNull Location location = player.getLocation();
        player.setHealth(0.0);
        Assertions.assertTrue(location.getBlock().isEmpty());
    }

    @Test
    @DisplayName("Spawn filled chest")
    public void spawnFilledChest() {
        List<ItemStack> list = new ArrayList<>(List.of(new ItemStack(Material.OAK_LOG)));

        PlayerMock player = server.addPlayer();
        PlayerInventory inventory = player.getInventory();
        System.out.println("Adding item to the inventory...");
        inventory.addItem(list.toArray(ItemStack[]::new));

        @NotNull Location location = player.getLocation();
        System.out.println("Killing player...");
        player.setHealth(0.0);
        server.getScheduler().performOneTick();
        Block block = location.getBlock();
        Assertions.assertFalse(block.isEmpty());
        System.out.println("Breaking block...");

        BlockBreakEvent blockBreakEvent = player.simulateBlockBreak(block);
        Assertions.assertNotNull(blockBreakEvent);
        Assertions.assertTrue(blockBreakEvent.isCancelled());
        Assertions.assertTrue(block.isEmpty());

        WorldMock world = player.getWorld();
        Collection<Item> items = world.getEntitiesByClass(Item.class);
        items.forEach(item -> list.remove(item.getItemStack()));
        Assertions.assertTrue(list.isEmpty());
    }

}
