package dev.thestaticvoid.capejs.network;

import dev.thestaticvoid.capejs.CapeJS;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ClientTexturePayloadHandler {

    public static void handleTexture(CapeTextureData data, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            String capeId = data.capeId();
            byte[] png = data.data();

            Path dir = mc.gameDirectory.toPath()
                    .resolve("kubejs/assets/capejs/textures/capes");

            try {
                // Create directory if needed
                Files.createDirectories(dir);

                // Write the PNG file
                Path file = dir.resolve(capeId + ".png");
                Files.write(file, png);

                CapeJS.LOGGER.info("[CLIENT TEXTURE] Downloaded and saved: {}", capeId);

                // Reload resource packs to load the new texture
                mc.execute(() -> {
                    CapeJS.LOGGER.info("[CLIENT TEXTURE] Reloading resource packs...");
                    mc.reloadResourcePacks().thenRun(() -> {
                        CapeJS.LOGGER.info("[CLIENT TEXTURE] Resource packs reloaded");
                    });
                });

            } catch (Exception e) {
                CapeJS.LOGGER.error("[CLIENT TEXTURE] Failed to save texture: {}", capeId, e);
            }
        });
    }
}