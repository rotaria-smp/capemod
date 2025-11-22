package dev.thestaticvoid.capejs.network;

import dev.thestaticvoid.capejs.CapeJS;
import dev.thestaticvoid.capejs.CapeRegistry;
import dev.thestaticvoid.capejs.client.RendererAccess;
import dev.thestaticvoid.capejs.core.CapeManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class ClientPayloadHandler {
    public static void handleCape(final NetworkHandler.CapeData data, final IPayloadContext ctx) {

        // Always enqueue work on client thread
        ctx.enqueueWork(() -> {

            System.out.println("[CLIENT] Received CapeData packet:");
            System.out.println("  Player UUID: " + data.playerId());
            System.out.println("  Cape ID:     " + data.capeId());
            UUID uuid = UUID.fromString(data.playerId());
            String capeId = data.capeId();

            ResourceLocation res = CapeJS.id(CapeRegistry.locationString(capeId));
            CapeRegistry.addCapeToMap(uuid.toString(), res);

            RendererAccess.refresh();
        });
    }
}
