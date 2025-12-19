package dev.thestaticvoid.capejs.network;

import dev.thestaticvoid.capejs.CapeJS;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;

public final class ClientManifestPayloadHandler {

    public static void handle(CapeManifestData data, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            Path base = mc.gameDirectory.toPath()
                    .resolve("kubejs/assets/capejs/textures/capes");

            try {
                Files.createDirectories(base);
            } catch (IOException e) {
                CapeJS.LOGGER.error("[CLIENT MANIFEST] Failed to create directories", e);
            }

            int missingCount = 0;

            CapeJS.LOGGER.info("[CLIENT MANIFEST] Checking {} capes", data.hashes().size());

            for (var entry : data.hashes().entrySet()) {
                String capeId = entry.getKey();
                String serverHash = entry.getValue();
                Path local = base.resolve(capeId + ".png");

                String localHash = "";
                if (Files.exists(local)) {
                    localHash = compute(local);
                }

                if (!serverHash.equals(localHash)) {
                    CapeJS.LOGGER.info("[CLIENT MANIFEST] Requesting download for: {}", capeId);
                    PacketDistributor.sendToServer(new RequestCapeDownload(capeId));
                    missingCount++;
                } else {
                    CapeJS.LOGGER.debug("[CLIENT MANIFEST] Cape {} already up to date", capeId);
                }
            }

            if (missingCount == 0) {
                CapeJS.LOGGER.info("[CLIENT MANIFEST] All capes up to date, textures ready");
            } else {
                CapeJS.LOGGER.info("[CLIENT MANIFEST] Requesting {} missing capes", missingCount);
            }
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