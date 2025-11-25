package dev.thestaticvoid.capejs.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.thestaticvoid.capejs.core.CapeManager;
import dev.thestaticvoid.capejs.network.NetworkSender;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class RemoveCape {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("removecape")
                        .requires(cs -> cs.hasPermission(3))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("cape", StringArgumentType.string())
                                        .executes(ctx -> {

                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                            String capeId = StringArgumentType.getString(ctx, "cape");

                                            System.out.println(
                                                    "Removing cape " + capeId + " from player " +
                                                            player.getName().getString() + " (" + player.getUUID() + ")"
                                            );

                                            ListTag list = player.getPersistentData().getList("cape_unlocks", Tag.TAG_STRING);

                                            // Check if player actually has the cape
                                            boolean unlocked = list.stream()
                                                    .anyMatch(tag -> tag.getAsString().equals(capeId));

                                            if (!unlocked) {
                                                ctx.getSource().sendFailure(
                                                        Component.literal(player.getName().getString() + " does not have cape: " + capeId)
                                                );
                                                return 0;
                                            }
                                            // Remove cape from user
                                            ListTag newList = new ListTag();
                                            for (Tag t : list) {
                                                if (!t.getAsString().equals(capeId)) {
                                                    newList.add(t);
                                                }
                                            }

                                            // Save back into player NBT
                                            player.getPersistentData().put("cape_unlocks", newList);

                                            String currentCape = player.getPersistentData().getString("current_cape");
                                            if (currentCape.equals(capeId)) {
                                                NetworkSender.sendCapePacket(player, capeId, true);
                                                CapeManager.unregister(player.getUUID());
                                                player.getPersistentData().putString("current_cape", "");
                                                System.out.println("Also unequipped currently worn cape");
                                            }

                                            System.out.println("Successfully removed cape " + capeId + " from " + player.getName().getString());

                                            return 1;
                                        })))
        );
    }
}
