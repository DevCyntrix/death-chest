package de.helixdevs.deathchest.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;

public record HologramOptions(boolean enabled, double height, Collection<String> lines) {

    public static HologramOptions load(ConfigurationSection section) {
        if (section == null)
            return null;

        boolean enabled = section.getBoolean("enabled");
        double height = section.getDouble("height");
        Collection<String> lines = section.getStringList("lines")
                .stream().map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .toList();
        return new HologramOptions(enabled, height, lines);
    }
}
