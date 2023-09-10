---
description: https://www.spigotmc.org/resources/placeholderapi.6245/
---

# PlaceHolder API

<figure><img src="../.gitbook/assets/papi-hologram-with-chest" alt=""><figcaption><p>Shows a hologram with PAPI support if the player has a death chest</p></figcaption></figure>

<figure><img src="../.gitbook/assets/papi-hologram-without-chest" alt=""><figcaption><p>Shows a hologram with PAPI support if the player has no death chest</p></figcaption></figure>

### Placeholders

```
%deathchest_last_location%
```

You can use this placeholder in PAPI supported plugins to show the last death position of the player.&#x20;

### PAPI Configuration

You can configure the format of the location and the fallback message in the papi config by adding this properties:

```yaml
expansions:
  DeathChest:
    location_format: '<x> <y> <z> <world>'
    fallback_message: '&cChest not found'
```
