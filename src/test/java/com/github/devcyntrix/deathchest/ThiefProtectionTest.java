package com.github.devcyntrix.deathchest;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@DisplayName("Chest protection tests")
public class ThiefProtectionTest {

    private ServerMock server;
    private DeathChestPlugin plugin;

    private List<ItemStack> content;

    @BeforeEach
    public void setUp() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("thief-protection-config.yml");
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

    private DeathChestModel createChest(Player player) {

        Location loc = player.getLocation().clone();

        player.setHealth(0.0);
        server.getScheduler().performOneTick();

        System.out.println(plugin.getChests().collect(Collectors.toList()));

        Optional<@NotNull DeathChestModel> first = plugin.getChests(loc.getWorld()).findFirst();
        Assertions.assertFalse(first.isEmpty()); // There have to be a chest

        return first.get();
    }

    @Test
    @DisplayName("Thief interacts with unprotected chest")
    public void thiefInteractsUnprotected() {
        PlayerMock diedPlayer = server.addPlayer();
        PlayerMock thief = server.addPlayer();

        content.forEach(itemStack -> diedPlayer.getInventory().addItem(itemStack));

        DeathChestModel model = createChest(diedPlayer);
        Assertions.assertFalse(model.isProtected());
        Block block = model.getLocation().getBlock();

        PlayerInteractEvent event = new PlayerInteractEvent(thief, Action.RIGHT_CLICK_BLOCK, null, block, BlockFace.UP);
        server.getPluginManager().callEvent(event);
        Assertions.assertSame(event.useInteractedBlock(), Event.Result.DENY);

        thief.assertInventoryView(InventoryType.CHEST, inv -> inv.equals(model.getInventory()));
    }

    @Test
    @DisplayName("Thief destroys unprotected chest")
    public void thiefDestroyUnprotected() {
        PlayerMock diedPlayer = server.addPlayer();
        content.forEach(itemStack -> diedPlayer.getInventory().addItem(itemStack));
        PlayerMock thief = server.addPlayer();

        DeathChestModel model = createChest(diedPlayer);
        Assertions.assertFalse(model.isProtected());
        Block block = model.getLocation().getBlock();
        Assertions.assertFalse(block.isEmpty());

        BlockBreakEvent blockBreakEvent = thief.simulateBlockBreak(block);
        Assertions.assertTrue(blockBreakEvent.isCancelled());
        Assertions.assertTrue(block.isEmpty());

        WorldMock world = (WorldMock) model.getWorld();
        Assertions.assertNotNull(world);
        Collection<Item> items = world.getEntitiesByClass(Item.class);
        items.forEach(item -> content.remove(item.getItemStack()));
        Assertions.assertTrue(content.isEmpty());
    }

    @Test
    @DisplayName("Thief interacts with protected chest")
    public void thiefInteractsProtected() {
        PlayerMock diedPlayer = server.addPlayer();
        content.forEach(itemStack -> diedPlayer.getInventory().addItem(itemStack));
        diedPlayer.addAttachment(plugin, plugin.getDeathChestConfig().chestOptions().thiefProtectionOptions().permission(), true);
        PlayerMock thief = server.addPlayer();

        DeathChestModel chest = createChest(diedPlayer);
        Assertions.assertTrue(chest.isProtected());
        Block block = chest.getLocation().getBlock();

        PlayerInteractEvent event = new PlayerInteractEvent(thief, Action.RIGHT_CLICK_BLOCK, null, block, BlockFace.UP);
        server.getPluginManager().callEvent(event);
        Assertions.assertSame(event.useInteractedBlock(), Event.Result.DENY);
        Assertions.assertSame(null, thief.getOpenInventory().getTopInventory());
    }

    @Test
    @DisplayName("Thief destroys protected chest")
    public void thiefDestroyProtected() {
        PlayerMock diedPlayer = server.addPlayer();
        content.forEach(itemStack -> diedPlayer.getInventory().addItem(itemStack));
        diedPlayer.addAttachment(plugin, plugin.getDeathChestConfig().chestOptions().thiefProtectionOptions().permission(), true);
        PlayerMock thief = server.addPlayer();

        DeathChestModel model = createChest(diedPlayer);
        Assertions.assertTrue(model.isProtected());
        Block block = model.getLocation().getBlock();
        Assertions.assertFalse(block.isEmpty());

        BlockBreakEvent blockBreakEvent = thief.simulateBlockBreak(block);
        Assertions.assertTrue(blockBreakEvent.isCancelled());
        Assertions.assertFalse(block.isEmpty());


        WorldMock world = (WorldMock) model.getWorld();
        Assertions.assertNotNull(world);
        Collection<Item> items = world.getEntitiesByClass(Item.class);
        Assertions.assertTrue(items.isEmpty());
    }

    @Test
    @DisplayName("Bypassed thief interacts with protected chest")
    public void bypassedThiefInteractsProtected() {
        PlayerMock diedPlayer = server.addPlayer();
        content.forEach(itemStack -> diedPlayer.getInventory().addItem(itemStack));
        diedPlayer.addAttachment(plugin, plugin.getDeathChestConfig().chestOptions().thiefProtectionOptions().permission(), true);

        PlayerMock thief = server.addPlayer();
        thief.addAttachment(plugin, plugin.getDeathChestConfig().chestOptions().thiefProtectionOptions().bypassPermission(), true);

        DeathChestModel model = createChest(diedPlayer);
        Assertions.assertTrue(model.isProtected());
        Block block = model.getLocation().getBlock();

        PlayerInteractEvent event = new PlayerInteractEvent(thief, Action.RIGHT_CLICK_BLOCK, null, block, BlockFace.UP);
        server.getPluginManager().callEvent(event);
        Assertions.assertSame(event.useInteractedBlock(), Event.Result.DENY);

        thief.assertInventoryView(InventoryType.CHEST, inv -> inv.equals(model.getInventory()));
    }

    @Test
    @DisplayName("Bypassed thief destroys protected chest")
    public void bypassedThiefDestroyProtected() {
        PlayerMock diedPlayer = server.addPlayer();
        content.forEach(itemStack -> diedPlayer.getInventory().addItem(itemStack));

        diedPlayer.addAttachment(plugin, plugin.getDeathChestConfig().chestOptions().thiefProtectionOptions().permission(), true);
        PlayerMock thief = server.addPlayer();
        thief.addAttachment(plugin, plugin.getDeathChestConfig().chestOptions().thiefProtectionOptions().bypassPermission(), true);

        DeathChestModel model = createChest(diedPlayer);
        Assertions.assertTrue(model.isProtected());
        Block block = model.getLocation().getBlock();
        Assertions.assertFalse(block.isEmpty());

        BlockBreakEvent blockBreakEvent = thief.simulateBlockBreak(block);
        Assertions.assertTrue(blockBreakEvent.isCancelled());
        Assertions.assertTrue(block.isEmpty());


        WorldMock world = (WorldMock) model.getWorld();
        Assertions.assertNotNull(world);
        Collection<Item> items = world.getEntitiesByClass(Item.class);
        items.forEach(item -> content.remove(item.getItemStack()));
        Assertions.assertTrue(content.isEmpty());
    }
}
