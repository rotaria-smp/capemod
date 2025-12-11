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

public class SwitchCape {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("equipcape")
                        .requires(cs -> cs.hasPermission(0))
                        .then(
                                Commands.argument("cape", StringArgumentType.string())
                                        .executes(ctx -> {

                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            String capeId = StringArgumentType.getString(ctx, "cape");

                                            // Check if cape is unlocked
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

                                            // Get current cape to see if we need to remove it first
                                            String oldCape = player.getPersistentData().getString("current_cape");

                                            // If they're already wearing a different cape, remove it first
                                            if (!oldCape.isEmpty() && !oldCape.equals(capeId)) {
                                                NetworkSender.sendCapePacket(player, oldCape, true);
                                            }

                                            // Apply new cape
                                            NetworkSender.sendCapePacket(player, capeId, false);

                                            // Save to persistent data
                                            player.getPersistentData().putString("current_cape", capeId);

                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("Equipped cape " + capeId),
                                                    false
                                            );

                                            return 1;
                                        })
                        )
        );
    }
}