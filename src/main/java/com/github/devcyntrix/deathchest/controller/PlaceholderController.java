package com.github.devcyntrix.deathchest.controller;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import com.github.devcyntrix.deathchest.util.ChestModelStringLookup;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.text.StringSubstitutor;

import java.util.function.Function;

public class PlaceholderController {

    private final Function<Long, String> duration;

    public PlaceholderController(DeathChestConfig config) {
        this.duration = expiresAt -> {
            long duration = expiresAt - System.currentTimeMillis();
            if (duration <= 0) duration = 0;
            return DurationFormatUtils.formatDuration(duration, config.durationFormat());
        };
    }

    public String replace(DeathChestModel model, String base) {
        StringSubstitutor substitutor = new StringSubstitutor(new ChestModelStringLookup(model, duration));
        base = substitutor.replace(base);
        if (DeathChestPlugin.isPlaceholderAPIEnabled()) base = PlaceholderAPI.setPlaceholders(model.getOwner(), base);
        return base;
    }

}
