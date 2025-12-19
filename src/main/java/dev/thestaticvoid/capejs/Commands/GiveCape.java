package dev.thestaticvoid.capejs.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class GiveCape {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("givecape")
                        .requires(cs -> cs.hasPermission(3))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("cape", StringArgumentType.string())
                                        .executes(ctx -> {

                                            // Correct target player
                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");

                                            // Cape ID argument
                                            String capeId = StringArgumentType.getString(ctx, "cape");

                                            System.out.println(
                                                    "Player " + player.getName().getString() +
                                                            " UUID: " + player.getUUID() +
                                                            " was granted cape: " + capeId
                                            );

                                            // ----- PERSISTENT NBT UNLOCK LOGIC -----
                                            var nbt = player.getPersistentData();
                                            ListTag list = nbt.getList("cape_unlocks", Tag.TAG_STRING);

                                            boolean wasNew = false;
                                            if (!list.contains(StringTag.valueOf(capeId))) {
                                                list.add(StringTag.valueOf(capeId));
                                                nbt.put("cape_unlocks", list);
                                                wasNew = true;
                                            }
                                            // -----------------------------------------

                                            // Send unlock packet to client
                                            if (wasNew) {
                                                dev.thestaticvoid.capejs.network.NetworkHandler.CapeUnlockPayload unlockPayload =
                                                        new dev.thestaticvoid.capejs.network.NetworkHandler.CapeUnlockPayload(capeId);
                                                player.connection.send(unlockPayload);
                                                System.out.println("[CAPE CMD] Sent unlock packet to client for: " + capeId);
                                            }

                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("Cape " + capeId + " unlocked for " + player.getName().getString()),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )
        );
    }
}