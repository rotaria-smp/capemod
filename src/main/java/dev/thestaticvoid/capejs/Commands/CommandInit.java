package dev.thestaticvoid.capejs.Commands;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import static dev.thestaticvoid.capejs.CapeJS.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class CommandInit {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        GiveCape.register(event.getDispatcher());
        SwitchCape.register(event.getDispatcher());
        UnEquipCape.register(event.getDispatcher());
        RemoveCape.register(event.getDispatcher());
        DebugCapeCommand.register(event.getDispatcher());
        FixCapeCommand.register(event.getDispatcher());
        BackupCapeCommand.register(event.getDispatcher());
        OpenCapeGUI.register(event.getDispatcher());
    }
}