package dev.thestaticvoid.capejs.Commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.thestaticvoid.capejs.core.CapeManager;
import dev.thestaticvoid.capejs.network.NetworkSender;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class FixCapeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("fixcape")
                        .requires(cs -> cs.hasPermission(0))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            CompoundTag nbt = player.getPersistentData();

                            String currentCape = nbt.getString("current_cape");
                            ListTag unlocks = nbt.getList("cape_unlocks", Tag.TAG_STRING);
                            String managerCape = CapeManager.get(player.getUUID());

                            boolean fixed = false;

                            if (!currentCape.isEmpty() && !currentCape.equals(managerCape)) {
                                boolean isUnlocked = false;
                                for (int i = 0; i < unlocks.size(); i++) {
                                    if (unlocks.getString(i).equals(currentCape)) {
                                        isUnlocked = true;
                                        break;
                                    }
                                }

                                if (isUnlocked) {
                                    CapeManager.register(player.getUUID(), currentCape);
                                    NetworkSender.sendCapePacket(player, currentCape, false);

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("§a✓ Re-registered cape: " + currentCape), false
                                    );
                                    fixed = true;
                                } else {
                                    nbt.putString("current_cape", "");
                                    CapeManager.unregister(player.getUUID());
                                    NetworkSender.sendCapePacket(player, currentCape, true);

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("§c✗ Removed invalid cape: " + currentCape), false
                                    );
                                    fixed = true;
                                }
                            }

                            if (managerCape != null && currentCape.isEmpty()) {
                                CapeManager.unregister(player.getUUID());
                                NetworkSender.sendCapePacket(player, managerCape, true);

                                ctx.getSource().sendSuccess(
                                        () -> Component.literal("§a✓ Cleared orphaned cape"), false
                                );
                                fixed = true;
                            }

                            if (!fixed) {
                                ctx.getSource().sendSuccess(
                                        () -> Component.literal("§a✓ No issues found!"), false
                                );
                            } else {
                                ctx.getSource().sendSuccess(
                                        () -> Component.literal("§a✓ Cape data repaired!"), false
                                );
                            }

                            return 1;
                        })
        );
    }
}
