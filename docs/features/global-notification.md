# Global notification

If you want to notifiy the whole server about the death of a player you can activate this feature. You can also notify the server by using the death message change of this plugin.

{% content-ref url="change-death-message.md" %}
[change-death-message.md](change-death-message.md)
{% endcontent-ref %}

```yaml
# Available Placeholders:
# ${player_name} : shows the player name
# ${player_displayname} : shows the player display name (Maybe with prefix)
# ${x}, ${y}, ${z}, ${world} : shows the coordinates of the death location
# ${chest_x}, ${chest_y}, ${chest_z} : shows the coordinates of the chest
global-notification:
  enabled: false
  message: |-
    &7${player_name} died at ${x} ${y} ${z} in ${world}
```

By default this feature is deactivated.
