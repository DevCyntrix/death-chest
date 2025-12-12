package com.github.devcyntrix.deathchest.feature.update.views;

import com.github.devcyntrix.deathchest.feature.update.NewUpdate;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;
import java.util.logging.Logger;

public class ConsoleNotificationView implements Consumer<NewUpdate> {

    private final Plugin plugin;
    private final Logger logger;

    public ConsoleNotificationView(Plugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    @Override
    public void accept(NewUpdate update) {
        logger.warning("New version " + update.version() + " is out. You are still running " + plugin.getDescription().getVersion());

        if (update.installed()) {
            logger.warning("Please restart the server to run the newest version.");
        } else {
            logger.warning("Please update the plugin from " + plugin.getDescription().getWebsite());
        }
    }
}
