package de.helixdevs.deathchest.api.report;

import com.google.gson.annotations.SerializedName;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.List;

public record PluginInfo(@SerializedName("name") String name, @SerializedName("version") String version,
                         @SerializedName("authors") List<String> authors,
                         @SerializedName("dependencies") List<String> dependencies) {

    public static PluginInfo of(Plugin plugin) {
        PluginDescriptionFile description = plugin.getDescription();
        return new PluginInfo(description.getName(), description.getVersion(), description.getAuthors(), description.getDepend());
    }

}
