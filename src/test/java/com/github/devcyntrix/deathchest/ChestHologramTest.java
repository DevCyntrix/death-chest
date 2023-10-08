package com.github.devcyntrix.deathchest;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import com.github.devcyntrix.deathchest.config.HologramOptions;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChestHologramTest {

    private ServerMock server;
    private DeathChestPlugin plugin;

    private List<ItemStack> content;
    private PlayerMock player;
    private DeathChestModel model;

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
        this.plugin = MockBukkit.load(DeathChestPlugin.class, true, config);

        this.player = server.addPlayer();
        this.content = new ArrayList<>(List.of(new ItemStack(Material.OAK_LOG)));
        this.model = plugin.createDeathChest(player.getLocation(), content.toArray(ItemStack[]::new));
        server.getScheduler().performOneTick();

        Assertions.assertFalse(model.getLocation().getBlock().isEmpty());
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void spawnHologram() {
        World world = model.getWorld();
        Assertions.assertNotNull(world);

        Collection<ArmorStand> armorStands = world.getEntitiesByClass(ArmorStand.class);
        HologramOptions hologramOptions = plugin.getDeathChestConfig().hologramOptions();
        boolean enabled = hologramOptions.enabled();

        Assertions.assertEquals(hologramOptions.enabled(), !armorStands.isEmpty());
        if (!enabled)
            return;
        Assertions.assertEquals(hologramOptions.lines().size(), armorStands.size());
    }

    @Test
    public void removeHologram() {
        World world = model.getWorld();
        Assertions.assertNotNull(world);

        plugin.getDeathChestController().destroyChest(model);

        Collection<ArmorStand> armorStands = world.getEntitiesByClass(ArmorStand.class);
        Assertions.assertTrue(armorStands.isEmpty());
    }

}
