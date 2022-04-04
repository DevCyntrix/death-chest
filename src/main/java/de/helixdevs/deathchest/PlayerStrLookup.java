package de.helixdevs.deathchest;

import org.apache.commons.lang.text.StrLookup;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

public class PlayerStrLookup extends StrLookup {

    private final OfflinePlayer player;
    private final Supplier<String> duration;

    public PlayerStrLookup(OfflinePlayer player, Supplier<String> duration) {
        this.player = player;
        this.duration = duration;
    }

    @Override
    public String lookup(String key) {
        if (key.equals("player_name"))
            return player.getName();
        if (key.equals("player_displayname")) {
            Player oP = player.getPlayer();
            return oP != null ? oP.getDisplayName() : player.getName();
        }
        if (key.equals("duration"))
            return duration.get();
        return null;
    }
}
