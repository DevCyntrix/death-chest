package com.github.devcyntrix.deathchest.config;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.controller.PlaceholderController;
import com.google.gson.annotations.SerializedName;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record TeleportOptions(
        @SerializedName("enabled") boolean enabled,
        @SerializedName("message") String[] message,
        @SerializedName("not-found") String[] notFound) {

    @Contract("null -> new")
    public static @NotNull TeleportOptions load(@Nullable ConfigurationSection section) {
        if (section == null)
            section = new MemoryConfiguration();

        boolean enabled = section.getBoolean("enabled", false);

        return new TeleportOptions(enabled, deserialize("message"), deserialize("not-found"));
    }

    public void showNotification(Audience audience, DeathChestModel model, PlaceholderController controller) {
        for (String message : message()) {
            message = controller.replace(model, message);

            Component deserialize = MiniMessage.miniMessage()
                    .deserialize(message)
                    .clickEvent(ClickEvent.runCommand("/deathchest teleport"));
            audience.sendMessage(deserialize);
        }
    }

    public void showNotFound(Audience audience) {
        for (String message : message()) {
            Component deserialize = MiniMessage.miniMessage().deserialize(message);
            audience.sendMessage(deserialize);
        }
    }

    private static String[] deserialize(String stringMessage) {
        if (stringMessage == null) {
            return null;
        }

        TextComponent deserialize = LegacyComponentSerializer.legacyAmpersand().deserialize(stringMessage);
        String serialize = MiniMessage.miniMessage().serialize(deserialize)
                .replace("\\", ""); // Necessary to combine legacy color codes with mini message
        return serialize.split("\n");
    }

}
