package com.github.devcyntrix.deathchest.config;

import com.google.gson.annotations.SerializedName;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public record ChestOptions(
        @SerializedName("expiration") @Nullable Duration expiration,
        @SerializedName("no-expiration-permission") @NotNull NoExpirationPermission noExpirationPermission,
        @SerializedName("drop-items-after-expiration") boolean dropItemsAfterExpiration,
        @SerializedName("blast-protection") boolean blastProtection,
        @SerializedName("thief-protection") @NotNull ThiefProtectionOptions thiefProtectionOptions
) {

    @Contract("null -> new")
    public static @NotNull ChestOptions load(@Nullable ConfigurationSection section) {
        if (section == null)
            section = new MemoryConfiguration();

        Duration expiration = null;
        if (section.contains("expiration")) {
            long expirationInSeconds = section.getLong("expiration");
            if (expirationInSeconds > 0) {
                expiration = Duration.ofSeconds(expirationInSeconds);
            }
        }

        NoExpirationPermission noExpirationPermission = NoExpirationPermission.load(section.getConfigurationSection("no-expiration-permission"));

        boolean dropItemsAfterExpiration = section.getBoolean("drop-items-after-expiration", false);
        boolean blastProtection = section.getBoolean("blast-protection", false);
        ThiefProtectionOptions thiefProtectionOptions = ThiefProtectionOptions.load(section.getConfigurationSection("thief-protection"));

        return new ChestOptions(expiration, noExpirationPermission, dropItemsAfterExpiration, blastProtection, thiefProtectionOptions);
    }
}