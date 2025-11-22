package dev.thestaticvoid.capejs.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.thestaticvoid.capejs.core.CapeManager;
import dev.thestaticvoid.capejs.network.NetworkSender;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

public class CapeCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("givecape")
                        .requires(cs -> cs.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("cape", StringArgumentType.string())
                        .executes(ctx -> {
                            var player = ctx.getSource().getPlayerOrException();
                            String capeId = StringArgumentType.getString(ctx, "cape");
                            System.out.println("Player " + player  + "UUID: " + player.getUUID() + " Was granted Cape: " + capeId);
                            NetworkSender.sendCapePacket(player, capeId);
                            CapeManager.register(player.getUUID(), capeId);
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("Sent cape packet to " + player.getName().getString()),
                                    false
                            );

                            return 1;
                        })
        )));
}}
