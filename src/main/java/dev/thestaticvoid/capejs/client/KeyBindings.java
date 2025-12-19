package dev.thestaticvoid.capejs.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.thestaticvoid.capejs.client.gui.CapeSelectionScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

import static dev.thestaticvoid.capejs.CapeJS.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyBindings {
    
    public static final Lazy<KeyMapping> OPEN_CAPE_GUI = Lazy.of(() -> new KeyMapping(
            "key.capejs.open_cape_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.capejs"
    ));
    
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CAPE_GUI.get());
    }
    
    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class KeyInputHandler {
        
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            Minecraft mc = Minecraft.getInstance();
            
            if (OPEN_CAPE_GUI.get().consumeClick() && mc.screen == null) {
                mc.setScreen(new CapeSelectionScreen(null));
            }
        }
    }
}
