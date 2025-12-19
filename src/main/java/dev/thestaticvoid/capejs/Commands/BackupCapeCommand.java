package dev.thestaticvoid.capejs.Commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.thestaticvoid.capejs.core.CapeDataBackup;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.UUID;

public class BackupCapeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("capesbackup")
                        .requires(cs -> cs.hasPermission(0))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            CapeDataBackup.backupPlayerData(player);

                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("§a✓ Your cape data has been backed up!"),
                                    false
                            );

                            return 1;
                        })
        );

        dispatcher.register(
                Commands.literal("capesrestore")
                        .requires(cs -> cs.hasPermission(0))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();

                            if (!CapeDataBackup.hasBackupData(player.getUUID())) {
                                ctx.getSource().sendFailure(
                                        Component.literal("§cNo backup found for your account!")
                                );
                                return 0;
                            }

                            boolean restored = CapeDataBackup.restorePlayerData(player);

                            if (restored) {
                                ctx.getSource().sendSuccess(
                                        () -> Component.literal("§a✓ Your cape data has been restored from backup!"),
                                        false
                                );
                                ctx.getSource().sendSuccess(
                                        () -> Component.literal("§eRelog for changes to take effect."),
                                        false
                                );
                                return 1;
                            } else {
                                ctx.getSource().sendFailure(
                                        Component.literal("§cFailed to restore backup!")
                                );
                                return 0;
                            }
                        })
        );

        dispatcher.register(
                Commands.literal("capesbackupstatus")
                        .requires(cs -> cs.hasPermission(3))
                        .executes(ctx -> {
                            Set<UUID> backedUpPlayers = CapeDataBackup.getAllBackedUpPlayers();

                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("§e=== CAPE BACKUP STATUS ==="),
                                    false
                            );

                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("§eTotal backups: §a" + backedUpPlayers.size()),
                                    false
                            );

                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("§eBackup location: §7config/capejs/backups/"),
                                    false
                            );

                            return 1;
                        })
        );
    }
}