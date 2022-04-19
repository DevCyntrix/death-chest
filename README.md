# Death Chest
[![MIT License](https://img.shields.io/github/license/DevCyntrix/death-chest)](LICENSE)
[![Discord](https://img.shields.io/discord/899027136046841958?label=Discord)](https://discord.gg/SdDeWUB8F6)

A spigot plugin for spawning a chest when the player dies. It supports Holographic Displays, Decent Holograms,
ProtocolLib, WorldGuard, GriefPrevention and PlotSquared for additional features

## Configuration

The configuration file lays in plugins/DeathChest.

## Particle

This plugin supports particle spawning in a circle around the chest. In the future it will be fully customizable but
currently you can only customize the radius, particle count and the speed.

## Inventory

You can customize the inventory which opens by right-clicking the chest. Properties like title and size are customizable
in the configuration file.

## Compability

### Hologram

This plugin has a support for Holographic Displays and Decent Holograms for spawning a hologram above the chest which is
fully customizable in the configuration file. It has also a support with specific placeholders like `{player_name}`
, `{player_displayname}` and `{duration}`.

### Block break animation

This plugin has a support for ProtocolLib which is needed to activate the block breaking animation.

## API

You have to add this plugin as a dependency or soft dependency. Then you can get the death chest service.

```java
public class TestPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        DeathChestService load = getServer().getServicesManager().load(DeathChestService.class);
        if (load != null) {
            // Code
        }
    }

}
```

With the service you can create new death chests.

```java
public class TestPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        DeathChestService load = getServer().getServicesManager().load(DeathChestService.class);
        if (load != null) {
            DeathChest chest = load.createDeathChest(location, items); // This creates a new chest with the items in the world
        }
    }

}
```

Or you can use the service in other events.

```java
public class TestPlugin extends JavaPlugin implements Listener {

    private DeathChestService deathChestService;

    @Override
    public void onEnable() {
        this.deathChestService = getServer().getServicesManager().load(DeathChestService.class);
        if (this.deathChestService != null) {
            getServer().getPluginManager().registerEvents(this, this); // Registers only if the service is available
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ItemStack[] items = null; // Items code here
        this.deathChestService.createDeathChest(player.getLocation(), items); // This creates a new chest with the items in the world
    }

}
```
