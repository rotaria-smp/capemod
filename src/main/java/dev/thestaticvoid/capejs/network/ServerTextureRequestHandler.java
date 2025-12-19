package dev.thestaticvoid.capejs.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ServerTextureRequestHandler {

    public static void handle(RequestCapeDownload req, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player == null) return;

            Path file = Paths.get("kubejs/assets/capejs/textures/capes")
                    .resolve(req.capeId() + ".png");

            try {
                byte[] bytes = Files.readAllBytes(file);
                player.connection.send(new CapeTextureData(req.capeId(), bytes));
            } catch (Exception ignored) {}
        });
    }
}