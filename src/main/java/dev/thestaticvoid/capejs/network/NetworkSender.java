package dev.thestaticvoid.capejs.network;

import dev.thestaticvoid.capejs.CapeJS;
import dev.thestaticvoid.capejs.core.CapeManager;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class NetworkSender {

    public static void sendCapePacket(ServerPlayer sourcePlayer, String capeId, Boolean remove) {
        // Update CapeManager first
        if (remove) {
            CapeManager.unregister(sourcePlayer.getUUID());
        } else {
            CapeManager.register(sourcePlayer.getUUID(), capeId);
        }

        NetworkHandler.CapeData payload = new NetworkHandler.CapeData(
                sourcePlayer.getUUID().toString(),
                capeId,
                remove
        );

        // Broadcast to all players
        if (sourcePlayer.getServer() == null) return;

        List<ServerPlayer> players = sourcePlayer.getServer().getPlayerList().getPlayers();
        for (ServerPlayer sp : players) {
            try {
                sp.connection.send(payload);
            } catch (Exception e) {
                CapeJS.LOGGER.error("[NETWORK] Failed to send cape packet to {}",
                        sp.getName().getString(), e);
            }
        }

        CapeJS.LOGGER.info("[NETWORK] Broadcasted cape change: player={}, cape={}, remove={}",
                sourcePlayer.getName().getString(), capeId, remove);
    }
}