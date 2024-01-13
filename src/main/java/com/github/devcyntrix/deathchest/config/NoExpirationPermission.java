package com.github.devcyntrix.deathchest.config;

import com.google.gson.annotations.SerializedName;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record NoExpirationPermission(
        @SerializedName("enabled") boolean enabled,
        @SerializedName("permission") String permission) {

    public static final String DEFAULT_PERMISSION = "deathchest.stays-forever";

    @Contract("null -> new")
    public static @NotNull NoExpirationPermission load(@Nullable ConfigurationSection section) {
        if (section == null)
            section = new MemoryConfiguration();

        boolean enabled = section.getBoolean("enabled", false);
        String permission = section.getString("permission", DEFAULT_PERMISSION);
        return new NoExpirationPermission(enabled, permission);
    }
}
