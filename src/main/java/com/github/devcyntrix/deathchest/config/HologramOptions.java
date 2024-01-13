package com.github.devcyntrix.deathchest.config;

import com.google.gson.annotations.SerializedName;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public record HologramOptions(
        @SerializedName("enabled") boolean enabled,
        @SerializedName("height") double height,
        @SerializedName("line-height") double lineHeight,
        @SerializedName("lines") Collection<String> lines) {

    public static final double DEFAULT_HEIGHT = 1.0;
    public static final double DEFAULT_LINE_HEIGHT = 0.25;

    @Contract("null -> new")
    public static @NotNull HologramOptions load(ConfigurationSection section) {
        if (section == null) return new HologramOptions(false, DEFAULT_HEIGHT, DEFAULT_LINE_HEIGHT, null);

        boolean enabled = section.getBoolean("enabled", true);
        double height = section.getDouble("height", DEFAULT_HEIGHT);
        double lineHeight = section.getDouble("line-height", DEFAULT_LINE_HEIGHT);

        Collection<String> lines = section.getList("lines", Arrays.asList("&7&lR.I.P", "${player_name}", "&3-&6-&3-&6-&3-&6-&3-", "${duration}")).stream().map(Object::toString).map(s -> ChatColor.translateAlternateColorCodes('&', s)).toList();

        return new HologramOptions(enabled, height, lineHeight, lines);
    }
}
