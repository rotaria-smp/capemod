package dev.thestaticvoid.capejs;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CapeJS.MOD_ID)
public class CapeJS {
    public static final String MOD_ID = "capejs";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public CapeJS(IEventBus modEventBus, ModContainer modContainer) {
        CapeRegistry.initialize();
        LOGGER.info("CapeJS initialized.");
    }
}
