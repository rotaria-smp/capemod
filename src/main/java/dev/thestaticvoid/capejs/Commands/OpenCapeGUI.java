package dev.thestaticvoid.capejs.Commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.thestaticvoid.capejs.client.gui.CapeSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class OpenCapeGUI {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("capes")
                        .executes(ctx -> {
                            // Execute on client side only
                            if (ctx.getSource().isPlayer()) {
                                // Schedule to run on client thread
                                ctx.getSource().getPlayer().getServer().execute(() -> {
                                    if (ctx.getSource().getPlayer().level().isClientSide) {
                                        openGuiOnClient();
                                    }
                                });

                                ctx.getSource().sendSuccess(
                                        () -> Component.literal("Opening cape selection GUI..."),
                                        false
                                );
                            }
                            return 1;
                        })
        );
    }

    @OnlyIn(Dist.CLIENT)
    private static void openGuiOnClient() {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> mc.setScreen(new CapeSelectionScreen(mc.screen)));
    }
}