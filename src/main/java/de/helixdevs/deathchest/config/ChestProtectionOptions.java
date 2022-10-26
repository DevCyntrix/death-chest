package de.helixdevs.deathchest.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

public record ChestProtectionOptions(boolean enabled, String permission, String bypassPermission) {

    public static @Nullable ChestProtectionOptions load(@Nullable ConfigurationSection section) {
        if (section == null)
            return null;

        boolean enabled = section.getBoolean("enabled", false);
        String permission = section.getString("permission", "deathchest.thiefprotected");
        String bypassPermission = section.getString("bypass-permission", "deathchest.thiefprotected.bypass");
        return new ChestProtectionOptions(enabled, permission, bypassPermission);
    }

}
