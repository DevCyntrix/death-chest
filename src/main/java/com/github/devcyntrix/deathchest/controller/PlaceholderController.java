package com.github.devcyntrix.deathchest.controller;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import com.github.devcyntrix.deathchest.util.ChestModelStringLookup;
import com.github.devcyntrix.deathchest.util.DurationFormatter;
import com.google.inject.Singleton;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.text.StringSubstitutor;

@Singleton
public class PlaceholderController {

    private final DeathChestConfig config;
    private final DeathChestController controller;
    private final DurationFormatter durationFormatter;

    public PlaceholderController(DeathChestConfig config, DeathChestController controller) {
        this.config = config;
        this.controller = controller;
        this.durationFormatter = new DurationFormatter(config.durationFormat());
    }

    public String replace(DeathChestModel model, String base) {
        StringSubstitutor substitutor = new StringSubstitutor(new ChestModelStringLookup(controller, config, model, durationFormatter));
        base = substitutor.replace(base);
        if (DeathChestPlugin.isPlaceholderAPIEnabled()) base = PlaceholderAPI.setPlaceholders(model.getOwner(), base);
        return base;
    }

}
