package dev.thestaticvoid.capejs.network;

import dev.thestaticvoid.capejs.CapeJS;
import dev.thestaticvoid.capejs.CapeRegistry;
import dev.thestaticvoid.capejs.client.RendererAccess;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Client-side handler: apply cape updates to the local client's player map (CapeRegistry)
 * and refresh the renderer so remote players show the cape.
 */
public class ClientPayloadHandler {

    public static void handleCape(final NetworkHandler.CapeData data, final IPayloadContext ctx) {

        // Always enqueue work on client thread
        ctx.enqueueWork(() -> {
            String playerIdStr = data.playerId();
            UUID uuid;
            try {
                uuid = UUID.fromString(playerIdStr);
            } catch (IllegalArgumentException ex) {
                System.err.println("[CLIENT] Invalid UUID in cape packet: " + playerIdStr);
                return;
            }

            String capeId = data.capeId();
            Boolean remove = data.remove();

            if (remove == Boolean.TRUE) {
                CapeRegistry.removeCapeFromMap(uuid.toString());
                RendererAccess.refresh();
            } else {
                ResourceLocation res = CapeJS.id(CapeRegistry.locationString(capeId));
                CapeRegistry.addCapeToMap(uuid.toString(), res);
                RendererAccess.refresh();
            }
            // Ensure a refresh at the end
            RendererAccess.refresh();
        });
    }
}
