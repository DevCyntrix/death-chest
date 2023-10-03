package com.github.devcyntrix.deathchest.view.update;

import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;
import java.util.logging.Logger;

public class ConsoleNotificationView implements Consumer<String> {

    private final Plugin plugin;
    private final Logger logger;

    public ConsoleNotificationView(Plugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    @Override
    public void accept(String version) {
        logger.warning("New version " + version + " is out. You are still running " + plugin.getDescription().getVersion());
        logger.warning("Please update the plugin at " + plugin.getDescription().getWebsite());
    }
}
