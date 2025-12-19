package dev.thestaticvoid.capejs.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
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

                                            // Check if they have this cape unlocked
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

                                            // Check if this is actually their current cape
                                            String currentCape = player.getPersistentData().getString("current_cape");
                                            if (!currentCape.equals(capeId)) {
                                                ctx.getSource().sendFailure(
                                                        Component.literal("You are not wearing cape: " + capeId)
                                                );
                                                return 0;
                                            }

                                            // Remove the cape
                                            NetworkSender.sendCapePacket(player, capeId, true);
                                            player.getPersistentData().putString("current_cape", "");

                                            // Send unequip to client storage
                                            dev.thestaticvoid.capejs.network.NetworkHandler.CapeEquipPayload equipPayload =
                                                    new dev.thestaticvoid.capejs.network.NetworkHandler.CapeEquipPayload("");
                                            player.connection.send(equipPayload);

                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("Unequipped cape " + capeId),
                                                    false
                                            );

                                            return 1;
                                        })
                        )
        );
    }
}