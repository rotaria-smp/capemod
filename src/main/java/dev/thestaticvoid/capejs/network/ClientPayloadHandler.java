package dev.thestaticvoid.capejs.network;

import dev.thestaticvoid.capejs.CapeJS;
import dev.thestaticvoid.capejs.CapeRegistry;
import dev.thestaticvoid.capejs.client.RendererAccess;
import dev.thestaticvoid.capejs.core.CapeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class ClientPayloadHandler {

    public static void handleCape(final NetworkHandler.CapeData data, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            try {
                String playerIdStr = data.playerId();
                UUID uuid = UUID.fromString(playerIdStr);
                String capeId = data.capeId();
                Boolean remove = data.remove();

                CapeJS.LOGGER.info("[CLIENT HANDLER] Received cape packet: player={}, cape={}, remove={}",
                        uuid, capeId, remove);

                if (remove) {
                    // Remove cape from both registries
                    CapeRegistry.removeCapeFromMap(uuid.toString());
                    CapeManager.unregister(uuid);
                    CapeJS.LOGGER.info("[CLIENT HANDLER] Removed cape for {}", uuid);
                } else {
                    // Add cape to both registries
                    ResourceLocation capeResource = CapeJS.id(CapeRegistry.locationString(capeId));
                    CapeRegistry.addCapeToMap(uuid.toString(), capeResource);
                    CapeManager.register(uuid, capeId);
                    CapeJS.LOGGER.info("[CLIENT HANDLER] Applied cape {} for {}", capeId, uuid);
                }

                // Force renderer refresh so cape shows immediately
                RendererAccess.refresh();
                CapeJS.LOGGER.info("[CLIENT HANDLER] Refreshed renderer");

                // Also try to force skin refresh for the specific player
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && mc.level != null) {
                    mc.execute(() -> {
                        mc.level.players().forEach(player -> {
                            if (player.getUUID().equals(uuid)) {
                                CapeJS.LOGGER.info("[CLIENT HANDLER] Found matching player, forcing skin refresh");
                            }
                        });
                    });
                }

            } catch (IllegalArgumentException e) {
                CapeJS.LOGGER.error("[CLIENT HANDLER] Invalid UUID in packet: {}", data.playerId());
            } catch (Exception e) {
                CapeJS.LOGGER.error("[CLIENT HANDLER] Error handling cape packet", e);
            }
        });
    }
}