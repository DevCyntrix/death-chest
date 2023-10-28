---
description: This feature protects the chest against thieves.
---

# Thief Protection

If you enable this feature, all players with this permission will be protected from thieves without the bypass permission. You can also specify a time when the protection will expire if you want to protect the chest only for a certain time.

```yaml
# This option protects the death chest against thieves if the player has the specific permission and the thief
# not the bypass permission.
chest-protection:
  enabled: false # Set this value to true if you want to enable it.
  permission: 'deathchest.thiefprotected'
  bypass-permission: 'deathchest.thiefprotected.bypass'
  # To enable the expiration set the number to number greater than 0. The unit of this variable is seconds.
  expiration: 0
  # On this website you can find a list of all sounds which is implemented in SpigotMC
  # @see https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html
  # If you want to disable the sound or the message, you can delete these options
  sound: BLOCK_CHEST_LOCKED;1.0;1.0
  message: |-
    &cYou are not permitted to open this chest
```

By default this feature is disabled.
