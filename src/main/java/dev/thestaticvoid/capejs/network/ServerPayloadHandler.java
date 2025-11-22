package dev.thestaticvoid.capejs.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

/**
 * Server-side handler: when a client sends a cape change, broadcast it to everyone.
 */
public class ServerPayloadHandler {

    public static void handleCape(final NetworkHandler.CapeData data, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = (ServerPlayer) ctx.player();
            if (sender == null) return;

            System.out.println("[SERVER] Received cape packet from: " + sender.getName().getString());
            System.out.println("[SERVER] Target UUID: " + data.playerId() + " cape=" + data.capeId() + " remove=" + data.remove());

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
