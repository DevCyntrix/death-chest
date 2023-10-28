# Get started

## Dependency

[![](https://jitpack.io/v/DevCyntrix/death-chest.svg)](https://jitpack.io/#DevCyntrix/death-chest)

### Maven

```xml
<repositories>
  <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
  </repository>
</repositories>
```

Add the dependency

```xml
<dependency>
    <groupId>com.github.DevCyntrix</groupId>
    <artifactId>death-chest</artifactId>
    <version>Tag</version>
</dependency>
```

### Gradle Groovy

```groovy
repositories {
  maven { url "https://jitpack.io" }
}
```

Add the dependency

```groovy
dependencies {
        compileOnly "com.github.DevCyntrix:death-chest:NEWEST-VERSION"
}
```

### Gradle Kotlin

```kotlin
repositories {
    maven("https://jitpack.io")
}
```

Add the dependency

```kotlin
dependencies {
    compileOnly("com.github.DevCyntrix:death-chest:NEWEST-VERSION")
}
```

## Integration

You should add the plugin to your dependency list of your plugin.

### Add it as a dependency

<details>

<summary>plugin.yml</summary>

```yaml
name: ...
version: ...
authors:
  - ...
  - ...
...
depend:
  - DeathChest
```

</details>

### Use it in your plugin

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
