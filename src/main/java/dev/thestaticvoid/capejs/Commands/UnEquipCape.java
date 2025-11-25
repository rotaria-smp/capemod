package dev.thestaticvoid.capejs.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.thestaticvoid.capejs.core.CapeManager;
import dev.thestaticvoid.capejs.network.NetworkSender;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class UnEquipCape {

        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

            dispatcher.register(
                    Commands.literal("unequipcape")
                            .requires(cs -> cs.hasPermission(0))
                            .then(
                                    Commands.argument("cape", StringArgumentType.string())
                                            .executes(ctx -> {

                                                ServerPlayer player = ctx.getSource().getPlayerOrException();
                                                String capeId = StringArgumentType.getString(ctx, "cape");

                                                System.out.println(
                                                        "Player " + player.getName().getString() +
                                                                " UUID: " + player.getUUID() +
                                                                " Unequipped cape: " + capeId
                                                );

                                                // ----- CHECK UNLOCK FROM PERSISTENT NBT -----
                                                ListTag list = player.getPersistentData()
                                                        .getList("cape_unlocks", Tag.TAG_STRING);

                                                boolean unlocked = list.stream()
                                                        .anyMatch(tag -> tag.getAsString().equals(capeId));

                                                if (!unlocked) {
                                                    ctx.getSource().sendFailure(
                                                            Component.literal("You have not unlocked cape: " + capeId)
                                                    );
                                                    return 0;
                                                }
                                                // ---------------------------------------------

                                                // Remove current cape
                                                NetworkSender.sendCapePacket(player, capeId, true);
                                                CapeManager.unregister(player.getUUID());

                                                System.out.println("Removed current cape from " + player.getName().getString());

                                                return 1;
                                            })
                            )
            );
        }
    }
