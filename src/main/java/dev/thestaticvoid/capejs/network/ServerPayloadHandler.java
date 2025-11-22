package dev.thestaticvoid.capejs.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class ServerPayloadHandler {
    public static void handleCape(final NetworkHandler.CapeData data, final IPayloadContext ctx) {

        ctx.enqueueWork(() -> {
            ServerPlayer sender = (ServerPlayer) ctx.player();

            System.out.println("[SERVER] Received cape packet from: " + sender.getName().getString());
            System.out.println("[SERVER] Target UUID: " + data.playerId());
            System.out.println("[SERVER] Cape: " + data.capeId());

            UUID uuid = UUID.fromString(data.playerId());
            ServerPlayer target = sender.server.getPlayerList().getPlayer(uuid);

            if (target != null) {
                System.out.println("[SERVER] Found target player: " + target.getName().getString());
            } else {
                System.out.println("[SERVER] Player not found on server.");
            }
        });
    }
}
