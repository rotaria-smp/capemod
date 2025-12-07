package dev.thestaticvoid.capejs.network;

import dev.thestaticvoid.capejs.core.CapeManager;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;


public class NetworkSender {

    /**
     * Broadcast a cape update to all connected players.
     *
     * @param sourcePlayer the ServerPlayer who changed their cape (used to get the source UUID)
     * @param capeId       the cape identifier (nullable when remove==true)
     * @param remove       whether to remove the cape
     */
    public static void sendCapePacket(ServerPlayer sourcePlayer, String capeId, Boolean remove) {

        // Update the server-side CapeManager FIRST
        if (remove) {
            CapeManager.unregister(sourcePlayer.getUUID());
        } else {
            CapeManager.register(sourcePlayer.getUUID(), capeId);
        }

        NetworkHandler.CapeData payload = new NetworkHandler.CapeData(sourcePlayer.getUUID().toString(), capeId, remove);

        // Broadcast to all players by iterating server player list and sending the payload
        if (sourcePlayer.getServer() == null) return;
        List<ServerPlayer> players = sourcePlayer.getServer().getPlayerList().getPlayers();
        for (ServerPlayer sp : players) {
            try {
                sp.connection.send(payload);
            } catch (Exception e) {
                System.err.println("[NETWORK] Failed to send cape packet to " + sp.getName().getString());
                e.printStackTrace();
            }
        }
    }
}