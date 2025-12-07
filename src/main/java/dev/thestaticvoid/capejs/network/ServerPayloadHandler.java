package dev.thestaticvoid.capejs.network;

import dev.thestaticvoid.capejs.core.CapeManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

/**
 * Server-side handler: when a client sends a cape change, broadcast it to everyone.
 */
public class ServerPayloadHandler {

    public static void handleCape(final NetworkHandler.CapeData data, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = (ServerPlayer) ctx.player();
            if (sender == null) return;

            // Update the server-side CapeManager
            try {
                UUID targetUUID = UUID.fromString(data.playerId());
                if (data.remove()) {
                    CapeManager.unregister(targetUUID);
                } else {
                    CapeManager.register(targetUUID, data.capeId());
                }
            } catch (IllegalArgumentException e) {
                System.err.println("[SERVER] Invalid UUID in cape packet: " + data.playerId());
            }

            // Broadcast to all connected players (so clients will update remote players)
            NetworkHandler.CapeData payload = new NetworkHandler.CapeData(data.playerId(), data.capeId(), data.remove());

            if (sender.getServer() == null) return;
            List<ServerPlayer> players = sender.getServer().getPlayerList().getPlayers();
            for (ServerPlayer sp : players) {
                try {
                    sp.connection.send(payload);
                } catch (Exception e) {
                    System.err.println("[SERVER] Failed to forward cape packet to " + sp.getName().getString());
                    e.printStackTrace();
                }
            }
        });
    }
}