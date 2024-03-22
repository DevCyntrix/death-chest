package com.github.devcyntrix.deathchest.dropconditions;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@DisplayName("Void drop condition check")
public class VoidDropConditionCheck {

    private ServerMock server;

    @BeforeEach
    public void setUp() {
        this.server = MockBukkit.getOrCreateMock();
        Assertions.assertNotNull(server);
        this.server.setSpawnRadius(0);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Doesn't drop items if player dies in lava")
    public void doesntDropItemsInLava() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("drop-conditions/void-drop.yml");
        if (stream == null) throw new IllegalStateException("Missing config");

        DeathChestConfig config;
        try (InputStreamReader reader = new InputStreamReader(stream)) {
            config = DeathChestConfig.load(YamlConfiguration.loadConfiguration(reader));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        DeathChestPlugin plugin = MockBukkit.load(DeathChestPlugin.class, true, config);

        List<ItemStack> list = new ArrayList<>(List.of(new ItemStack(Material.OAK_LOG)));

        PlayerMock player = server.addPlayer();
        Location location = player.getLocation();

        // Give items
        PlayerInventory inventory = player.getInventory();
        inventory.addItem(list.toArray(ItemStack[]::new));

        // Set the player block to fire
        Block block = location.getBlock();
        block.setType(Material.LAVA);

        // Kill the player
        player.setHealth(0.0);
        server.getScheduler().performOneTick();

        // Check that no chest spawned
        DeathChestModel lastChest = plugin.getLastChest(player);
        Assertions.assertNotNull(lastChest);
    }

    @Test
    @DisplayName("Drop items if player dies in void")
    public void dropItemsInVoid() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("drop-conditions/void-drop.yml");
        if (stream == null) throw new IllegalStateException("Missing config");

        DeathChestConfig config;
        try (InputStreamReader reader = new InputStreamReader(stream)) {
            config = DeathChestConfig.load(YamlConfiguration.loadConfiguration(reader));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        DeathChestPlugin plugin = MockBukkit.load(DeathChestPlugin.class, true, config);

        List<ItemStack> list = new ArrayList<>(List.of(new ItemStack(Material.OAK_LOG)));

        PlayerMock player = server.addPlayer();
        Location location = player.getLocation();
        WorldMock world = player.getWorld();

        // Give items
        PlayerInventory inventory = player.getInventory();
        inventory.addItem(list.toArray(ItemStack[]::new));

        // Teleport the player to the void
        location.setY(world.getMinHeight() - 1);
        player.teleport(location);

        // Kill the player
        player.setHealth(0.0);
        server.getScheduler().performOneTick();

        // Check that no chest spawned
        DeathChestModel lastChest = plugin.getLastChest(player);
        Assertions.assertNull(lastChest);
    }

    @Test
    @DisplayName("Doesn't drop items if player dies in fire")
    public void doesntDropItemsInFire() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("drop-conditions/void-drop.yml");
        if (stream == null) throw new IllegalStateException("Missing config");

        DeathChestConfig config;
        try (InputStreamReader reader = new InputStreamReader(stream)) {
            config = DeathChestConfig.load(YamlConfiguration.loadConfiguration(reader));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        DeathChestPlugin plugin = MockBukkit.load(DeathChestPlugin.class, true, config);

        List<ItemStack> list = new ArrayList<>(List.of(new ItemStack(Material.OAK_LOG)));

        PlayerMock player = server.addPlayer();
        Location location = player.getLocation();

        // Give items
        PlayerInventory inventory = player.getInventory();
        inventory.addItem(list.toArray(ItemStack[]::new));

        // Set the player block to fire
        Block block = location.getBlock();
        block.setType(Material.FIRE);

        // Kill the player
        player.setHealth(0.0);
        server.getScheduler().performOneTick();

        // Check that chest spawned
        DeathChestModel lastChest = plugin.getLastChest(player);
        Assertions.assertNotNull(lastChest);
    }

}
