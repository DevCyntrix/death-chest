package com.github.devcyntrix.deathchest.config;

import com.google.gson.annotations.SerializedName;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public record ThiefProtectionOptions(
        @SerializedName("enabled") boolean enabled,
        @SerializedName("permission") String permission,
        @SerializedName("bypass-permission") String bypassPermission,
        @SerializedName("expiration") @NotNull Duration expiration,
        @SerializedName("sound") Sound sound,
        @SerializedName("volume") float volume,
        @SerializedName("pitch") float pitch,
        @SerializedName("message") String[] message) {

    public static final String DEFAULT_PERMISSION = "deathchest.thiefprotected";
    public static final String DEFAULT_BYPASS_PERMISSION = "deathchest.thiefprotected.bypass";
    public static final Duration DEFAULT_DURATION = Duration.ofSeconds(0);

    @Contract("null -> new")
    public static @NotNull ThiefProtectionOptions load(@Nullable ConfigurationSection section) {
        if (section == null)
            return new ThiefProtectionOptions(false, DEFAULT_PERMISSION, DEFAULT_BYPASS_PERMISSION, DEFAULT_DURATION, null, 1F, 1F, null);

        boolean enabled = section.getBoolean("enabled", false);
        String permission = section.getString("permission", DEFAULT_PERMISSION);
        String bypassPermission = section.getString("bypass-permission", DEFAULT_BYPASS_PERMISSION);
        long expirationInSeconds = section.getLong("expiration", 0);
        Duration expiration = Duration.ofSeconds(Math.max(expirationInSeconds, 0));
        String soundString = section.getString("sound");
        Sound sound = null;
        float volume = 1.0F;
        float pitch = 1.0F;
        if (soundString != null) {
            String[] soundArray = soundString.split(";", 3);
            sound = Sound.valueOf(soundArray[0].toUpperCase());
            volume = Float.parseFloat(soundArray[1]);
            pitch = Float.parseFloat(soundArray[2]);
        }

        String message = section.getString("message");
        String[] messageArray = null;
        if (message != null) {
            message = ChatColor.translateAlternateColorCodes('&', message);
            messageArray = message.split(System.lineSeparator());
        }

        return new ThiefProtectionOptions(enabled, permission, bypassPermission, expiration, sound, volume, pitch, messageArray);
    }

    public void playSound(Player player, Location location) {
        if (sound == null) return;
        player.playSound(location, sound, volume, pitch);
    }

    public void notify(Player player) {
        if (message == null) return;
        player.sendMessage(message);
    }

}
