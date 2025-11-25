package dev.thestaticvoid.capejs;

import com.mojang.logging.LogUtils;
import dev.thestaticvoid.capejs.events.CapeEventHandler;
import dev.thestaticvoid.capejs.network.NetworkHandler;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

@Mod(CapeJS.MOD_ID)
public class CapeJS {
    public static final String MOD_ID = "capejs";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @SubscribeEvent
    public void registerPayloads(RegisterPayloadHandlersEvent event) {
        NetworkHandler.register(event);
    }
    public CapeJS(IEventBus modEventBus, ModContainer modContainer) {
        CapeRegistry.initialize();
        NeoForge.EVENT_BUS.register(new CapeEventHandler());
        LOGGER.info("CapeJS initialized.");
    }
}
