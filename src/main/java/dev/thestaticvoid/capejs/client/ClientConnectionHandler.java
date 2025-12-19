package dev.thestaticvoid.capejs.client;

import dev.thestaticvoid.capejs.CapeJS;
import dev.thestaticvoid.capejs.CapeRegistry;
import dev.thestaticvoid.capejs.core.CapeManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import static dev.thestaticvoid.capejs.CapeJS.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class ClientConnectionHandler {

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        CapeJS.LOGGER.info("[CAPE CLIENT] Player logged in, ready to receive capes");
    }

    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        CapeTextureManager.clear();
        CapeRegistry.clear();
        CapeManager.dump().keySet().forEach(CapeManager::unregister);
        CapeJS.LOGGER.info("[CAPE CLIENT] Disconnected, cleared all cape data");
    }
}