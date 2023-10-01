---
layout:
  title:
    visible: false
  description:
    visible: false
  tableOfContents:
    visible: true
  outline:
    visible: true
  pagination:
    visible: true
---

# Death Chest

<figure><img src=".gitbook/assets/deathchest-logo.png" alt=""><figcaption></figcaption></figure>

[![https://img.shields.io/discord/899027136046841958?label=Discord](https://img.shields.io/discord/899027136046841958?label=Discord)](https://discord.com/invite/xv4fswzrea) [![https://img.shields.io/badge/Documentation-green?label=Click%20to%20read](https://img.shields.io/badge/Documentation-green?label=Click%20to%20read)](https://deathchest.helixdevs.de/)

A spigot plugin for spawning a chest when a player dies to save the items. By default a expiration time of 5 minutes is configured. In the configuration file you have a lot of properties to fit the plugin to your server design. It also supports holograms, particle and block breaking effects.

### Configuration

The configuration file lays in plugins/DeathChest.

### Particle

This plugin supports particle spawning in a circle around the chest. In the future it will be fully customizable but currently you can only customize the radius, particle count and the speed.

### Inventory

You can customize the inventory which opens by right-clicking the chest. Properties like title and size are customizable in the configuration file.

### Compatibility

#### Hologram

This plugin has a native hologram support for spawning a hologram above the chest which is fully customizable in the configuration file. It has also a support with specific placeholders like `{player_name}` , `{player_displayname}` and `{duration}`.

#### Block break animation

This plugin has a support for ProtocolLib which is needed to activate the block breaking animation.
