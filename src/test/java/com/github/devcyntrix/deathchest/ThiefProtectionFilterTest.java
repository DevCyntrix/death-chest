package com.github.devcyntrix.deathchest;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@DisplayName("Chest protection tests")
public class ThiefProtectionFilterTest {

    private ServerMock server;
    private DeathChestPlugin plugin;

    private List<ItemStack> content;

    @BeforeEach
    public void setUp() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("thief-protection-filter-config.yml");
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
        this.plugin = MockBukkit.load(DeathChestPlugin.class, true, config);
        this.content = new ArrayList<>(List.of(new ItemStack(Material.OAK_LOG)));
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    private DeathChestModel createChest(PlayerMock player) {

        World world = player.getWorld();
        Location location = player.getLocation();
        Block block = location.getBlock();
        Assertions.assertFalse(block.isEmpty());
        return plugin.getChests(world).filter(deathChestModel -> player.equals(deathChestModel.getOwner())).findFirst().orElse(null);

//        DeathChestConfig config = plugin.getDeathChestConfig();
//        Duration expiration = config.chestOptions().expiration();
//        if (expiration == null)
//            expiration = Duration.ofSeconds(-1);
//
//        plugin.debug(1, "Checking no expiration permission...");
//        NoExpirationPermission permission = config.chestOptions().noExpirationPermission();
//        boolean expires = permission == null || !permission.enabled() || !player.hasPermission(permission.permission());
//        long createdAt = System.currentTimeMillis();
//        long expireAt = !expiration.isNegative() && !expiration.isZero() && expires ? createdAt + expiration.toMillis() : -1;
//
//        DeathChestModel model = plugin.createDeathChest(player.getLocation(), createdAt, expireAt, player, content.toArray(ItemStack[]::new));
//        server.getScheduler().performOneTick();
//
//        Assertions.assertFalse(model.getLocation().getBlock().isEmpty());
//        return model;
    }

    @Test
    @DisplayName("Chest spawns in a unfiltered world with permission")
    public void spawnInUnfilteredWorldWithPermission() {
        PlayerMock player = server.addPlayer();
        Location location = player.getLocation();
        player.getInventory().addItem(new ItemStack(Material.FEATHER));
        player.addAttachment(plugin, plugin.getDeathChestConfig().chestOptions().thiefProtectionOptions().permission(), true);

        player.setHealth(0.0);
        server.getScheduler().performOneTick();

        Optional<@NotNull DeathChestModel> first = plugin.getChests(location.getWorld()).filter(deathChestModel -> deathChestModel.getLocation().equals(location)).findFirst();
        Assertions.assertFalse(first.isEmpty()); // There have to be a chest

        DeathChestModel model = first.get();
        Assertions.assertNotNull(model);
        Assertions.assertTrue(model.isProtected());
    }

    @Test
    @DisplayName("Chest spawns in a unfiltered world without permission")
    public void spawnInUnfilteredWorldWithoutPermission() {
        PlayerMock player = server.addPlayer();
        Location location = player.getLocation();
        player.getInventory().addItem(new ItemStack(Material.FEATHER));

        player.setHealth(0.0);
        server.getScheduler().performOneTick();

        Optional<@NotNull DeathChestModel> first = plugin.getChests(location.getWorld()).filter(deathChestModel -> deathChestModel.getLocation().equals(location)).findFirst();
        Assertions.assertFalse(first.isEmpty()); // There have to be a chest

        DeathChestModel model = first.get();
        Assertions.assertNotNull(model);
        Assertions.assertFalse(model.isProtected());
    }


    @Test
    @DisplayName("Chest spawns in a filtered world without permission")
    public void thiefInteractsUnprotectedWithoutPermission() {
        WorldMock disabledWorld = server.addSimpleWorld("disabled_world");

        PlayerMock player = server.addPlayer();
        player.teleport(new Location(disabledWorld, 0, 1, 0));

        Location location = player.getLocation();
        player.getInventory().addItem(new ItemStack(Material.FEATHER));
        //player.addAttachment(plugin, plugin.getDeathChestConfig().chestOptions().thiefProtectionOptions().permission(), true);

        player.setHealth(0.0);
        server.getScheduler().performOneTick();

        Optional<@NotNull DeathChestModel> first = plugin.getChests(location.getWorld()).filter(deathChestModel -> deathChestModel.getLocation().equals(location)).findFirst();
        Assertions.assertFalse(first.isEmpty()); // There have to be a chest

        DeathChestModel model = first.get();
        Assertions.assertNotNull(model);
        Assertions.assertFalse(model.isProtected());
    }

    @Test
    @DisplayName("Chest spawns in a filtered world with permission")
    public void thiefInteractsUnprotected() {
        WorldMock disabledWorld = server.addSimpleWorld("disabled_world");

        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, plugin.getDeathChestConfig().chestOptions().thiefProtectionOptions().permission(), true);
        player.teleport(new Location(disabledWorld, 0, 1, 0));

        Location location = player.getLocation();
        player.getInventory().addItem(new ItemStack(Material.FEATHER));

        player.setHealth(0.0);
        server.getScheduler().performOneTick();

        Optional<@NotNull DeathChestModel> first = plugin.getChests(location.getWorld()).filter(deathChestModel -> deathChestModel.getLocation().equals(location)).findFirst();
        Assertions.assertFalse(first.isEmpty()); // There have to be a chest

        DeathChestModel model = first.get();
        Assertions.assertNotNull(model);
        Assertions.assertFalse(model.isProtected());
    }

}
