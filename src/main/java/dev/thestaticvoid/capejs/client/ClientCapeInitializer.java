package dev.thestaticvoid.capejs.client;

import dev.thestaticvoid.capejs.CapeJS;
import dev.thestaticvoid.capejs.CapeRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import static dev.thestaticvoid.capejs.CapeJS.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientCapeInitializer {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            CapeRegistry.initialize();
            CapeJS.LOGGER.info("[CAPE CLIENT] CapeRegistry initialized");
        });
    }
}