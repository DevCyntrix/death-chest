package com.github.devcyntrix.deathchest.config;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.controller.PlaceholderController;
import com.google.gson.annotations.SerializedName;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record GlobalNotificationOptions(
        @SerializedName("enabled") boolean enabled,
        @SerializedName("message") String[] message) {

    public static @NotNull GlobalNotificationOptions load(@Nullable ConfigurationSection section) {
        if (section == null)
            return new GlobalNotificationOptions(false, null);

        boolean enabled = section.getBoolean("enabled", false);
        String message = section.getString("message");
        String[] coloredMessage = null;
        if (message != null) {
            TextComponent deserialize = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
            String serialize = MiniMessage.miniMessage().serialize(deserialize)
                    .replace("\\", ""); // Necessary to combine legacy color codes with mini message
            coloredMessage = serialize.split("\n");
        }

        return new GlobalNotificationOptions(enabled, coloredMessage);
    }

    public void showNotification(DeathChestModel model, PlaceholderController controller) {

        for (String message : message()) {
            message = controller.replace(model, message);
            Component deserialize = MiniMessage.miniMessage().deserialize(message);
            JavaPlugin.getPlugin(DeathChestPlugin.class).getAudiences().all().sendMessage(deserialize);
        }

    }

}
