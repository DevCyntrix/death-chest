package de.helixdevs.deathchest.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.Collection;

public record HologramOptions(boolean enabled, double height, Collection<String> lines) {

    public static HologramOptions load(ConfigurationSection section) {
        if (section == null)
            return null;

        boolean enabled = section.getBoolean("enabled", true);
        double height = section.getDouble("height", 2.3);
        Collection<String> lines = section.getList("lines", Arrays.asList("&7&lR.I.P", "${player_name}", "&3-&6-&3-&6-&3-&6-&3-", "${duration}"))
                .stream()
                .map(Object::toString)
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .toList();

        return new HologramOptions(enabled, height, lines);
    }
}
