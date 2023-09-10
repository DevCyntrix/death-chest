# Change death message

```yaml
# Available Placeholders:
# ${player_name} : shows the player name
# ${player_displayname} : shows the player display name (Maybe with prefix)
# ${x}, ${y}, ${z}, ${world} : shows the coordinates of the death location
# (coordinates of the chest isn't available for here)
# To disable the death message change "enabled" to true and remove the message option.
change-death-message:
  enabled: false
  message: |-
    &7${player_name} died at ${x} ${y} ${z} in ${world}
```

By default this feature is disabled.
