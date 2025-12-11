package dev.thestaticvoid.capejs.network;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClientTexturePayloadHandler {

    private ClientTexturePayloadHandler() {}

    public static void handleTexture(CapeTextureData data, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            String capeId = data.capeId();
            byte[] png = data.data();

            Path dir = mc.gameDirectory.toPath()
                    .resolve("kubejs/assets/capejs/textures/capes");

            try {
                Files.createDirectories(dir);
                Files.write(dir.resolve(capeId + ".png"), png);
                mc.reloadResourcePacks();
            } catch (IOException ignored) {}
        });
    }
}
