package dev.thestaticvoid.capejs.Commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.thestaticvoid.capejs.core.CapeManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;

public class DebugCapeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("debugcape")
                        .requires(cs -> cs.hasPermission(0))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();

                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("§e=== CAPE DEBUG INFO ==="), false
                            );

                            CompoundTag nbt = player.getPersistentData();
                            String currentCape = nbt.getString("current_cape");
                            ListTag unlocks = nbt.getList("cape_unlocks", Tag.TAG_STRING);

                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("§eNBT Data:"), false
                            );

                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("  Current Cape: " +
                                            (currentCape.isEmpty() ? "§c(none)" : "§a" + currentCape)), false
                            );

                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("  Unlocked Capes: " + unlocks.size()), false
                            );

                            for (int i = 0; i < unlocks.size(); i++) {
                                String cape = unlocks.getString(i);
                                ctx.getSource().sendSuccess(
                                        () -> Component.literal("    - " + cape), false
                                );
                            }

                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("§eCapeManager:"), false
                            );

                            String managerCape = CapeManager.get(player.getUUID());
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("  Your cape in CapeManager: " +
                                            (managerCape == null ? "§c(not registered)" : "§a" + managerCape)), false
                            );

                            Map<UUID, String> allCapes = CapeManager.dump();
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("  Total players with capes: " + allCapes.size()), false
                            );

                            boolean mismatch = false;
                            if (!currentCape.isEmpty() && !currentCape.equals(managerCape)) {
                                ctx.getSource().sendSuccess(
                                        () -> Component.literal("§c⚠ MISMATCH DETECTED!"), false
                                );
                                ctx.getSource().sendSuccess(
                                        () -> Component.literal("§c  NBT says: " + currentCape), false
                                );
                                ctx.getSource().sendSuccess(
                                        () -> Component.literal("§c  CapeManager says: " + managerCape), false
                                );
                                mismatch = true;
                            }

                            if (!currentCape.isEmpty()) {
                                boolean isUnlocked = false;
                                for (int i = 0; i < unlocks.size(); i++) {
                                    if (unlocks.getString(i).equals(currentCape)) {
                                        isUnlocked = true;
                                        break;
                                    }
                                }

                                if (!isUnlocked) {
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("§c⚠ CAPE NOT UNLOCKED!"), false
                                    );
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("§c  You have '" + currentCape +
                                                    "' equipped but it's not unlocked!"), false
                                    );
                                    mismatch = true;
                                }
                            }

                            if (!mismatch) {
                                ctx.getSource().sendSuccess(
                                        () -> Component.literal("§a✓ No issues detected"), false
                                );
                            } else {
                                ctx.getSource().sendSuccess(
                                        () -> Component.literal("§eRun §6/fixcape §eto repair"), false
                                );
                            }

                            return 1;
                        })
        );
    }
}