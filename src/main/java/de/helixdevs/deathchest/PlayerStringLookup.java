package de.helixdevs.deathchest;

import org.apache.commons.text.lookup.StringLookup;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class PlayerStringLookup implements StringLookup {

    @Nullable
    private final OfflinePlayer player;
    @NotNull
    private final Supplier<String> duration;

    public PlayerStringLookup(@Nullable OfflinePlayer player, @NotNull Supplier<String> duration) {
        this.player = player;
        this.duration = duration;
    }

    @Override
    public String lookup(String key) {
        if (key.equals("player_name")) {
            if (player == null)
                return "Unknown";
            return player.getName();
        }
        if (key.equals("player_displayname")) {
            if (player == null)
                return "Unknown";
            Player oP = player.getPlayer();
            return oP != null ? oP.getDisplayName() : player.getName();
        }
        if (key.equals("duration"))
            return duration.get();
        return null;
    }
}
