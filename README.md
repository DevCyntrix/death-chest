# death-chest
A spigot plugin for spawning a chest when the player dies. It supports Holographic Displays, Decent Holograms, ProtocolLib, WorldGuard, and PlotSquared for additional features

## Configuration
The configuration file lays in plugins/DeathChest.

## Particle
This plugin supports particle spawning in a circle around the chest. In the future it will be fully customizable but currently you can only customize the radius, particle count and the speed. 

## Inventory
You can customize the inventory which opens by right-clicking the chest. Properties like title and size are customizable in the configuration file. 

## Compability

### Hologram
This plugin has a support for Holographic Displays and Decent Holograms for spawning a hologram above the chest which is fully customizable in the configuration file. It has also a support with specific placeholders like `{player_name}`, `{player_displayname}` and `{duration}`.

### Block break animation
This plugin has a support for ProtocolLib which is needed to activate the block breaking animation.
