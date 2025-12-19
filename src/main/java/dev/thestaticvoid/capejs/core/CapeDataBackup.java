package dev.thestaticvoid.capejs.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.thestaticvoid.capejs.CapeJS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class CapeDataBackup {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path BACKUP_DIR = Paths.get("config", "capejs", "backups");

    public static class PlayerCapeData {
        public String currentCape;
        public List<String> unlockedCapes;
        public long lastUpdated;

        public PlayerCapeData(String currentCape, List<String> unlockedCapes) {
            this.currentCape = currentCape;
            this.unlockedCapes = unlockedCapes;
            this.lastUpdated = System.currentTimeMillis();
        }
    }

    public static void initialize() {
        try {
            Files.createDirectories(BACKUP_DIR);
            CapeJS.LOGGER.info("[CAPE BACKUP] Backup system initialized at: {}", BACKUP_DIR);
        } catch (IOException e) {
            CapeJS.LOGGER.error("[CAPE BACKUP] Failed to create backup directory", e);
        }
    }

    public static void backupPlayerData(ServerPlayer player) {
        try {
            CompoundTag nbt = player.getPersistentData();
            String currentCape = nbt.getString("current_cape");
            ListTag unlocks = nbt.getList("cape_unlocks", Tag.TAG_STRING);

            List<String> unlockedCapes = new ArrayList<>();
            for (int i = 0; i < unlocks.size(); i++) {
                unlockedCapes.add(unlocks.getString(i));
            }

            PlayerCapeData data = new PlayerCapeData(currentCape, unlockedCapes);
            String json = GSON.toJson(data);

            Path backupFile = BACKUP_DIR.resolve(player.getUUID() + ".json");
            Files.writeString(backupFile, json);

            CapeJS.LOGGER.debug("[CAPE BACKUP] Backed up data for {}", player.getName().getString());

        } catch (Exception e) {
            CapeJS.LOGGER.error("[CAPE BACKUP] Failed to backup data for {}",
                    player.getName().getString(), e);
        }
    }

    public static boolean restorePlayerData(ServerPlayer player) {
        try {
            Path backupFile = BACKUP_DIR.resolve(player.getUUID() + ".json");

            if (!Files.exists(backupFile)) {
                return false;
            }

            String json = Files.readString(backupFile);
            PlayerCapeData data = GSON.fromJson(json, PlayerCapeData.class);

            if (data == null) {
                return false;
            }

            CompoundTag nbt = player.getPersistentData();

            ListTag unlockedList = new ListTag();
            for (String cape : data.unlockedCapes) {
                unlockedList.add(StringTag.valueOf(cape));
            }
            nbt.put("cape_unlocks", unlockedList);

            nbt.putString("current_cape", data.currentCape != null ? data.currentCape : "");

            CapeJS.LOGGER.info("[CAPE BACKUP] Restored data for {} from backup (age: {} days)",
                    player.getName().getString(),
                    (System.currentTimeMillis() - data.lastUpdated) / (1000 * 60 * 60 * 24)
            );

            return true;

        } catch (Exception e) {
            CapeJS.LOGGER.error("[CAPE BACKUP] Failed to restore data for {}",
                    player.getName().getString(), e);
            return false;
        }
    }

    public static boolean hasNBTData(ServerPlayer player) {
        CompoundTag nbt = player.getPersistentData();
        return nbt.contains("cape_unlocks") || !nbt.getString("current_cape").isEmpty();
    }

    public static boolean hasBackupData(UUID uuid) {
        return Files.exists(BACKUP_DIR.resolve(uuid + ".json"));
    }

    public static Set<UUID> getAllBackedUpPlayers() {
        Set<UUID> players = new HashSet<>();

        try (Stream<Path> stream = Files.list(BACKUP_DIR)) {
            stream.filter(p -> p.toString().endsWith(".json"))
                    .forEach(p -> {
                        try {
                            String filename = p.getFileName().toString();
                            String uuidStr = filename.replace(".json", "");
                            players.add(UUID.fromString(uuidStr));
                        } catch (IllegalArgumentException ignored) {}
                    });
        } catch (IOException e) {
            CapeJS.LOGGER.error("[CAPE BACKUP] Failed to list backup files", e);
        }

        return players;
    }

    public static void cleanupOldBackups() {
        long cutoffTime = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000);
        int deleted = 0;

        try (Stream<Path> stream = Files.list(BACKUP_DIR)) {
            var files = stream.filter(p -> p.toString().endsWith(".json")).toList();

            for (Path file : files) {
                try {
                    String json = Files.readString(file);
                    PlayerCapeData data = GSON.fromJson(json, PlayerCapeData.class);

                    if (data != null && data.lastUpdated < cutoffTime) {
                        Files.delete(file);
                        deleted++;
                    }
                } catch (Exception ignored) {}
            }

            if (deleted > 0) {
                CapeJS.LOGGER.info("[CAPE BACKUP] Cleaned up {} old backup files", deleted);
            }

        } catch (IOException e) {
            CapeJS.LOGGER.error("[CAPE BACKUP] Failed to cleanup old backups", e);
        }
    }
}