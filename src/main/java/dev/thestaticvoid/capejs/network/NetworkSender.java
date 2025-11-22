package dev.thestaticvoid.capejs.network;

import net.minecraft.server.level.ServerPlayer;

public class NetworkSender {

    public static void sendCapePacket(ServerPlayer player, String capeId, Boolean remove) {
        NetworkHandler.CapeData payload = new NetworkHandler.CapeData(player.getUUID().toString(), capeId, remove);
        player.connection.send(payload);
    }
}
