package dev.thestaticvoid.capejs.network;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;

public final class ClientManifestPayloadHandler {

    private ClientManifestPayloadHandler() {}

    public static void handle(CapeManifestData data, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            Path base = mc.gameDirectory.toPath()
                    .resolve("kubejs/assets/capejs/textures/capes");

            try {
                Files.createDirectories(base);
            } catch (IOException ignored) {}

            data.hashes().forEach((capeId, serverHash) -> {
                Path local = base.resolve(capeId + ".png");
                String localHash = "";

                if (Files.exists(local)) {
                    localHash = compute(local);
                }

                if (!serverHash.equals(localHash)) {
                    PacketDistributor.sendToServer(new RequestCapeDownload(capeId));
                }
            });
        });
    }

    private static String compute(Path file) {
        try {
            return Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-1")
                            .digest(Files.readAllBytes(file))
            );
        } catch (Exception e) {
            return "";
        }
    }
}
