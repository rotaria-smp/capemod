package dev.thestaticvoid.capejs.network;

import dev.thestaticvoid.capejs.CapeJS;
import dev.thestaticvoid.capejs.core.CapeManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class ServerPayloadHandler {

    public static void handleCape(final NetworkHandler.CapeData data, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = (ServerPlayer) ctx.player();
            if (sender == null) return;

            try {
                UUID targetUUID = UUID.fromString(data.playerId());
                if (data.remove()) {
                    CapeManager.unregister(targetUUID);
                } else {
                    CapeManager.register(targetUUID, data.capeId());
                }
            } catch (IllegalArgumentException e) {
                CapeJS.LOGGER.error("[SERVER] Invalid UUID", e);
            }

            // Broadcast to all players
            NetworkHandler.CapeData payload = new NetworkHandler.CapeData(
                    data.playerId(), data.capeId(), data.remove()
            );

            if (sender.getServer() != null) {
                for (ServerPlayer sp : sender.getServer().getPlayerList().getPlayers()) {
                    try {
                        sp.connection.send(payload);
                    } catch (Exception e) {
                        CapeJS.LOGGER.error("[SERVER] Broadcast failed", e);
                    }
                }
            }
        });
    }
}