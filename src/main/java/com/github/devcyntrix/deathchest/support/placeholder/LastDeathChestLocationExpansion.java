package com.github.devcyntrix.deathchest.support.placeholder;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class LastDeathChestLocationExpansion extends PlaceholderExpansion {

    public final DeathChestPlugin plugin;
    private final String locationFormat, fallbackMessage;

    public LastDeathChestLocationExpansion(DeathChestPlugin plugin) {
        this.plugin = plugin;
        this.locationFormat = getString("location_format", "<x> <y> <z> <world>");
        this.fallbackMessage = getString("fallback_message", "&cChest not found");
    }

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getDescription().getName();
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @NotNull List<String> getPlaceholders() {
        return Collections.singletonList("last_location");
    }

    @Nullable
    public String onRequest(final OfflinePlayer player, @NotNull final String params) {
        if (player != null && player.isOnline()) {
            return onPlaceholderRequest(player.getPlayer(), params);
        }

        return onPlaceholderRequest(null, params);
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null)
            return null;

        DeathChestModel deathChest = plugin.getLastChest(player);
        if (deathChest == null) {
            return ChatColor.translateAlternateColorCodes('&', this.fallbackMessage);
        }

        if (params.equalsIgnoreCase("last_location")) {
            Location location = deathChest.getLocation();
            String locationFormat = this.locationFormat
                    .replace("<x>", String.valueOf(location.getBlockX()))
                    .replace("<y>", String.valueOf(location.getBlockY()))
                    .replace("<z>", String.valueOf(location.getBlockZ()));
            if (location.getWorld() != null) {
                DeathChestConfig config = plugin.getDeathChestConfig();
                locationFormat = locationFormat.replace("<world>", config.worldAlias().getAlias(location.getWorld().getName()));
            }
            return ChatColor.translateAlternateColorCodes('&', locationFormat);
        }

        return null;
    }

}
