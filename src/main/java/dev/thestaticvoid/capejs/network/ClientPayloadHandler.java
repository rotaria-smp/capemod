package dev.thestaticvoid.capejs.network;

import dev.thestaticvoid.capejs.CapeJS;
import dev.thestaticvoid.capejs.CapeRegistry;
import dev.thestaticvoid.capejs.client.RendererAccess;
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
            System.out.println("  Remove:      " + data.remove());
            UUID uuid = UUID.fromString(data.playerId());
            String capeId = data.capeId();
            Boolean remove = data.remove();
            if (remove == true) {
                capeId = null;
                CapeRegistry.removeCapeFromMap(uuid.toString());
                RendererAccess.refresh();
            }else {
                ResourceLocation res = CapeJS.id(CapeRegistry.locationString(capeId));
                CapeRegistry.addCapeToMap(uuid.toString(), res);
                RendererAccess.refresh();
            }
            RendererAccess.refresh();
        });
    }
}
