package com.github.devcyntrix.deathchest.config;

import com.github.devcyntrix.deathchest.util.FilterType;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public record WorldFilterConfig(FilterType filterType, Set<String> worlds) implements Predicate<World> {

    public static final FilterType DEFAULT_TYPE = FilterType.BLACKLIST;

    @Contract("null -> new")
    public static @NotNull WorldFilterConfig load(@Nullable ConfigurationSection section) {
        if (section == null)
            return new WorldFilterConfig(DEFAULT_TYPE, Collections.emptySet());

        String filterString = section.getString("filter");
        if (filterString != null) {
            try {
                FilterType filterType = FilterType.valueOf(filterString.toUpperCase());
                List<String> worlds = section.getStringList("worlds");
                return new WorldFilterConfig(filterType, new HashSet<>(worlds));
            } catch (IllegalArgumentException e) {
                System.err.println("Unknown world filter in DeathChest/config.yml");
                e.printStackTrace();
            }
        }
        return new WorldFilterConfig(DEFAULT_TYPE, Collections.emptySet());
    }


    @Override
    public boolean test(World world) {
        return (filterType() == FilterType.WHITELIST && worlds().contains(world.getName())) || (filterType() == FilterType.BLACKLIST && !worlds().contains(world.getName()));
    }
}
