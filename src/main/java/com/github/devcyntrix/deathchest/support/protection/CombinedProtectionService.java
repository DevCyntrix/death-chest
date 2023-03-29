package com.github.devcyntrix.deathchest.support.protection;

import com.github.devcyntrix.deathchest.api.protection.IProtectionService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class CombinedProtectionService implements IProtectionService {

    private final IProtectionService[] services;

    public CombinedProtectionService(IProtectionService[] services) {
        this.services = services;
    }

    @Override
    public boolean canBuild(@NotNull Player player, @NotNull Location location, @NotNull Material material) {
        return Arrays.stream(services)
                .map(s -> s.canBuild(player, location, material))
                .reduce(Boolean::equals)
                .orElse(true);
    }
}
