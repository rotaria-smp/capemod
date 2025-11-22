package dev.thestaticvoid.capejs.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.thestaticvoid.capejs.core.CapeManager;
import dev.thestaticvoid.capejs.network.NetworkSender;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SwitchCape {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("switchcape")
                        .requires(cs -> cs.hasPermission(2))
                        .then(
                                Commands.argument("cape", StringArgumentType.string())
                                        .executes(ctx -> {

                                            var player = ctx.getSource().getPlayerOrException();
                                            String capeId = StringArgumentType.getString(ctx, "cape");

                                            System.out.println(
                                                    "Player " + player.getName().getString() +
                                                            " UUID: " + player.getUUID() +
                                                            " requested cape: " + capeId
                                            );

                                            // First: remove cape
                                            NetworkSender.sendCapePacket(player, capeId, true);
                                            // Register removal (if your system uses the same method)
                                            CapeManager.unregister(player.getUUID());

                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("Removed current cape from " + player.getName().getString()),
                                                    false
                                            );

                                            // Second: add cape
                                            NetworkSender.sendCapePacket(player, capeId, false);

                                            CapeManager.register(player.getUUID(), capeId);

                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("Applied cape " + capeId + " to " + player.getName().getString()),
                                                    false
                                            );

                                            return 1;
                                        })
                        )
        );
    }
}
