package de.helixdevs.deathchest.support.animation;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import de.helixdevs.deathchest.api.animation.IAnimationService;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class ProtocolLibAnimation implements IAnimationService {

    @Override
    public void spawnBlockBreakAnimation(@NotNull Vector location, @Range(from = 0, to = 9) byte state, @NotNull Stream<Player> players) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = manager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        packet.getIntegers().write(0, ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE));
        packet.getBlockPositionModifier().write(0, new BlockPosition(location));
        packet.getIntegers().write(1, (int) state);

        players.forEach(player -> {
            try {
                manager.sendServerPacket(player, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }
}
