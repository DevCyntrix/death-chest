package com.github.devcyntrix.deathchest.support.animation;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.github.devcyntrix.deathchest.api.animation.IAnimationService;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

public class ProtocolLibAnimation implements IAnimationService {

    @Override
    public void spawnBlockBreakAnimation(int entityId, @NotNull Vector location, @Range(from = -1, to = 9) int state, @NotNull Stream<Player> players) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = manager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        packet.getIntegers().write(0, entityId);
        packet.getBlockPositionModifier().write(0, new BlockPosition(location));
        packet.getIntegers().write(1, state);

        players.forEach(player -> {
            try {
                manager.sendServerPacket(player, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

}
