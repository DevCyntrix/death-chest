package com.github.devcyntrix.deathchest.util;

import com.github.devcyntrix.deathchest.DeathChestModel;
import org.apache.commons.text.lookup.StringLookup;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class PlayerStringLookup implements StringLookup {

    @NotNull
    private final DeathChestModel model;
    @NotNull
    private final Function<Long, String> duration;

    public PlayerStringLookup(@NotNull DeathChestModel model, @NotNull Function<Long, String> duration) {
        this.model = model;
        this.duration = duration;
    }

    @Override
    public String lookup(String key) {
        if (key.equals("player_name")) {
            if (model.getOwner() == null)
                return "Unknown";
            return model.getOwner().getName();
        }
        if (key.equals("player_displayname")) {
            if (model.getOwner() == null)
                return "Unknown";
            Player oP = model.getOwner().getPlayer();
            return oP != null ? oP.getDisplayName() : model.getOwner().getName();
        }
        if (key.equals("duration"))
            return duration.apply(model.getExpireAt());
        return null;
    }
}
