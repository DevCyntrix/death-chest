package de.helixdevs.deathchest.config;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ChestProtectionOptions(boolean enabled, String permission, String bypassPermission, Sound sound,
                                     float volume, float pitch, String[] message) {

    public static @NotNull ChestProtectionOptions load(@Nullable ConfigurationSection section) {
        if (section == null)
            section = new MemoryConfiguration();

        boolean enabled = section.getBoolean("enabled", false);
        String permission = section.getString("permission", "deathchest.thiefprotected");
        String bypassPermission = section.getString("bypass-permission", "deathchest.thiefprotected.bypass");
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

        return new ChestProtectionOptions(enabled, permission, bypassPermission, sound, volume, pitch, messageArray);
    }

    public void playSound(Player player, Location location) {
        if (sound == null)
            return;
        player.playSound(location, sound, volume, pitch);
    }

    public void sendMessage(Player player) {
        if (message == null)
            return;
        player.sendMessage(message);
    }

}
