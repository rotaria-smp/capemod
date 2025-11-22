package dev.thestaticvoid.capejs.Commands;

import dev.thestaticvoid.capejs.Commands.CapeCommands;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import static com.mojang.text2speech.Narrator.LOGGER;
import static dev.thestaticvoid.capejs.CapeJS.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class CommandInit {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CapeCommands.register(event.getDispatcher());
        LOGGER.info("Registered server commands.");
    }
}
