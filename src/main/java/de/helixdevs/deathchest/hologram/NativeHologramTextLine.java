package de.helixdevs.deathchest.hologram;

import com.google.common.base.Preconditions;
import de.helixdevs.deathchest.api.hologram.IHologramTextLine;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.jetbrains.annotations.NotNull;

public class NativeHologramTextLine implements IHologramTextLine {

    private final ArmorStand armorStand;

    public NativeHologramTextLine(@NotNull Location location, @NotNull String text) {
        Preconditions.checkNotNull(location.getWorld());
        this.armorStand = location.getWorld().spawn(location, ArmorStand.class, stand -> {
            stand.setMarker(true);
            stand.setInvisible(true);
            stand.setCustomName(text);
            stand.setCustomNameVisible(true);
        });
    }

    public Location getLocation() {
        return armorStand.getLocation();
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    @Override
    public void rename(@NotNull String text) {
        if (this.armorStand.isDead())
            return;
        this.armorStand.setCustomName(text);
    }

    @Override
    public void remove() {
        this.armorStand.remove();
    }
}
