package com.github.devcyntrix.deathchest;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import com.github.devcyntrix.deathchest.config.NoExpirationPermission;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@DisplayName("No expiration permission")
public class NoExpirationChestPermissionTest {

    private ServerMock server;
    private DeathChestPlugin plugin;

    private List<ItemStack> content;
    private PlayerMock player;

    @BeforeEach
    public void setUp() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("no-expiration-chest-permission-config.yml");
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
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    private DeathChestModel createChest(Player player) {
        DeathChestConfig config = plugin.getDeathChestConfig();
        Duration expiration = config.chestOptions().expiration();
        if (expiration == null)
            expiration = Duration.ofSeconds(-1);

        plugin.debug(1, "Checking no expiration permission...");
        NoExpirationPermission permission = config.chestOptions().noExpirationPermission();
        boolean expires = permission == null || !permission.enabled() || !player.hasPermission(permission.permission());
        long createdAt = System.currentTimeMillis();
        long expireAt = !expiration.isNegative() && !expiration.isZero() && expires ? createdAt + expiration.toMillis() : -1;

        DeathChestModel model = plugin.createDeathChest(player.getLocation(), createdAt, expireAt, player, content.toArray(ItemStack[]::new));
        server.getScheduler().performOneTick();

        Assertions.assertFalse(model.getLocation().getBlock().isEmpty());
        return model;
    }

    @Test
    @DisplayName("Has permission and doesn't expires")
    public void noExpirationPermissionSuccess() {
        this.player.addAttachment(plugin, plugin.getDeathChestConfig().chestOptions().noExpirationPermission().permission(), true);

        DeathChestModel model = createChest(this.player);

        long remainingSeconds = (model.getExpireAt() - model.getCreatedAt()) / 1000;
        System.out.printf("Skipping %d seconds%n", remainingSeconds);
        server.getScheduler().performTicks(remainingSeconds * 20 + 10);
        Assertions.assertFalse(model.getLocation().getBlock().isEmpty(), "Chest removed after %d seconds".formatted(remainingSeconds));
    }

    @Test
    @DisplayName("Has no permission and expires")
    public void noExpirationPermissionFail() {
        DeathChestModel model = createChest(player);

        long remainingSeconds = (model.getExpireAt() - model.getCreatedAt()) / 1000;
        System.out.printf("Skipping %d seconds%n", remainingSeconds);
        server.getScheduler().performTicks(remainingSeconds * 20 + 10);
        Assertions.assertTrue(model.getLocation().getBlock().isEmpty(), "Chest not removed after %d seconds".formatted(remainingSeconds));
    }

}
