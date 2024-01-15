package com.github.devcyntrix.deathchest.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record WorldAliasConfig(Map<String, Object> aliases) {

    @Contract("null -> new")
    public static @NotNull WorldAliasConfig load(ConfigurationSection section) {
        if (section == null)
            return new WorldAliasConfig(Map.of());

        Map<String, Object> aliases = section.getValues(false);
        if (aliases.isEmpty())
            return new WorldAliasConfig(Map.of());

        return new WorldAliasConfig(Map.copyOf(aliases));
    }

    public @NotNull String getAlias(@NotNull String worldName) {
        return (String) aliases.getOrDefault(worldName, worldName);
    }
}
