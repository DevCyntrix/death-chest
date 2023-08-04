package com.github.devcyntrix.deathchest.controller;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import com.github.devcyntrix.deathchest.util.PlayerStringLookup;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.text.StringSubstitutor;

import java.util.function.Function;

public class PlaceHolderController {

    private final Function<Long, String> duration;

    public PlaceHolderController(DeathChestConfig config) {
        this.duration = expiresAt -> {
            long duration = expiresAt - System.currentTimeMillis();
            if (duration <= 0) duration = 0;
            return DurationFormatUtils.formatDuration(duration, config.durationFormat());
        };
    }

    public String replace(DeathChestModel model, String s) {
        StringSubstitutor substitutor = new StringSubstitutor(new PlayerStringLookup(model, duration));
        s = substitutor.replace(s);
        if (DeathChestPlugin.isPlaceholderAPIEnabled()) s = PlaceholderAPI.setPlaceholders(model.getOwner(), s);
        return s;
    }

}
