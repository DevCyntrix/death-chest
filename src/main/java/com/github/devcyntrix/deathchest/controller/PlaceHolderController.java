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

    private final Function<DeathChestModel, String> durationSupplier;

    public PlaceHolderController(DeathChestConfig config) {
        this.durationSupplier = model -> {
            if (!model.isExpiring()) return DurationFormatUtils.formatDuration(0, config.durationFormat());
            long duration = model.getExpireAt() - System.currentTimeMillis();
            if (duration <= 0) duration = 0;
            return DurationFormatUtils.formatDuration(duration, config.durationFormat());
        };
    }

    public String replace(DeathChestModel model, String s) {
        StringSubstitutor substitutor = new StringSubstitutor(new PlayerStringLookup(model.getOwner(), () -> durationSupplier.apply(model)));
        s = substitutor.replace(s);
        if (DeathChestPlugin.isPlaceholderAPIEnabled()) s = PlaceholderAPI.setPlaceholders(model.getOwner(), s);
        return s;
    }

}
