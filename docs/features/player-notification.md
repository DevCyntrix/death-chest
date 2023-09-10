---
description: A notification to the player where he died and where the chest spawns
---

# Player notification

By editing the configuration file (plugins/DeathChest/config.yml), you can notify the player with a message where the chest spawned:

```yaml
# This section is for handling the notification function in this plugin. You can remove this section to disable the feature,
# or you set the 'enabled' option to false.
# This feature sends a message to the dead player. Here you can inform the player about the chest and their expiration.
# You can send the death coordination by using the placeholders: ${x} ${y}, ${z} and ${world}
# Also you can send the chest coordination by using the placeholders: ${chest_x}, ${chest_y} and ${chest_z}
player-notification:
  enabled: true # Set this value to false if you don't want this feature
  message: |-
    &7You died. Your items were put into a chest which disappears after &c10 minutes&7! ${x} ${y} ${z}
```

By default this feature is activated.
