package com.github.devcyntrix.deathchest.protection;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.io.InputStreamReader;

@DisplayName("Spawn radius check")
public class SpawnRadiusCheck {

    private ServerMock server;

    @BeforeEach
    public void setUp() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("default-config.yml");
        if (stream == null) throw new IllegalStateException("Missing config");

        DeathChestConfig config;
        try (InputStreamReader reader = new InputStreamReader(stream)) {
            config = DeathChestConfig.load(YamlConfiguration.loadConfiguration(reader));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.server = MockBukkit.getOrCreateMock();
        MockBukkit.load(DeathChestPlugin.class, true, config);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Cannot spawn chest in spawn radius")
    public void doesntSpawnInRadius() {
        PlayerMock player = server.addPlayer();
        ItemStack[] item = new ItemStack[]{new ItemStack(Material.OAK_LOG)};

        for (int x = -16; x <= 16; x++) {
            for (int z = -16; z <= 16; z++) {
                WorldMock world = player.getWorld();
                @NotNull Location location = new Location(world, x, world.getHighestBlockYAt(x, z) + 1, z);
                player.teleport(location);
                player.getInventory().addItem(item);
                player.setHealth(0.0);
                server.getScheduler().performOneTick();
                Assertions.assertEquals(Material.AIR, location.getBlock().getType());
                player.respawn();
            }
        }
    }

    @Test
    @DisplayName("Spawn chest in spawn radius")
    public void spawnInRadius() {
        PlayerMock player = server.addPlayer();
        ItemStack[] item = new ItemStack[]{new ItemStack(Material.OAK_LOG)};
        player.getInventory().addItem(item);

        WorldMock world = player.getWorld();

        Location location = new Location(world, 17, world.getHighestBlockYAt(17, 17) + 1, 17);
        player.teleport(location); // 17 because the default spawn radius is 16 blocks
        player.setHealth(0.0);
        server.getScheduler().performOneTick();
        Assertions.assertEquals(Material.CHEST, location.getBlock().getType());
    }

}
