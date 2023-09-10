# Holograms

<figure><img src="../.gitbook/assets/chest-with-hologram" alt="" width="441"><figcaption><p>Depicts the holograms above the death chest</p></figcaption></figure>

The plugin supports a hologram to display the name of the dead player and the expiration time. You can configure the following properties in the configuration file (plugins/DeathChest/config.yml):

```yaml
hologram:
  enabled: true # Set this value to false to disable the hologram
  height: 2.3   # Set the height of the hologram above the chest
  # Support for ${player_name}: Name
  #             ${player_displayname}: Displayname
  #             ${duration}: Time left
  lines:
    - '&7&lR.I.P'
    - '${player_name}'
    - '&3-&6-&3-&6-&3-&6-&3-'
    - '${duration}'
```
