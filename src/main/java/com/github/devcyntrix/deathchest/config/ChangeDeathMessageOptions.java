package com.github.devcyntrix.deathchest.config;

import com.google.gson.annotations.SerializedName;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ChangeDeathMessageOptions(
        @SerializedName("enabled") boolean enabled,
        @SerializedName("message") String[] message) {

    @Contract("null -> new")
    public static @NotNull ChangeDeathMessageOptions load(@Nullable ConfigurationSection section) {
        if (section == null)
            section = new MemoryConfiguration();

        boolean enabled = section.getBoolean("enabled", false);
        String message = section.getString("message");
        String[] coloredMessage = null;
        if (message != null)
            coloredMessage = ChatColor.translateAlternateColorCodes('&', message).split("\n");

        return new ChangeDeathMessageOptions(enabled, coloredMessage);
    }

}
