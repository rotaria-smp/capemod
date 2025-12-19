package dev.thestaticvoid.capejs.events;

import dev.thestaticvoid.capejs.core.CapeManager;
import dev.thestaticvoid.capejs.network.CapeManifestData;
import dev.thestaticvoid.capejs.network.NetworkHandler;
import dev.thestaticvoid.capejs.network.NetworkSender;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CapeEventHandler {

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Send the cape manifest to the joining player
        CapeManifestData manifest = new CapeManifestData(buildCapeManifest());
        player.connection.send(manifest);

        // NEW: Send full cape list sync to client
        ListTag capeList = player.getPersistentData().getList("cape_unlocks", Tag.TAG_STRING);
        List<String> unlockedCapes = new ArrayList<>();
        for (Tag tag : capeList) {
            unlockedCapes.add(tag.getAsString());
        }
        String equippedCape = player.getPersistentData().getString("current_cape");

        NetworkHandler.CapeListSyncPayload syncPayload =
                new NetworkHandler.CapeListSyncPayload(unlockedCapes, equippedCape);
        player.connection.send(syncPayload);
        System.out.println("[CAPE LOGIN] Synced " + unlockedCapes.size() + " capes to " + player.getName().getString());

        // Restore the joining player's own cape if they had one
        String currentCape = player.getPersistentData().getString("current_cape");
        if (!currentCape.isEmpty()) {
            // Register in CapeManager (don't remove from persistent data)
            CapeManager.register(player.getUUID(), currentCape);

            // Send to all players (including the one who just joined)
            NetworkHandler.CapeData payload = new NetworkHandler.CapeData(
                    player.getUUID().toString(),
                    currentCape,
                    false
            );

            if (player.getServer() != null) {
                for (ServerPlayer sp : player.getServer().getPlayerList().getPlayers()) {
                    try {
                        sp.connection.send(payload);
                    } catch (Exception e) {
                        System.err.println("[CAPE] Failed to send restored cape to " + sp.getName().getString());
                    }
                }
            }
        }

        // Send all existing players' capes to the newly joined player
        Map<UUID, String> allCapes = CapeManager.dump();

        for (Map.Entry<UUID, String> entry : allCapes.entrySet()) {
            UUID uuid = entry.getKey();
            String capeId = entry.getValue();

            // Don't send the player their own cape again (we already did that above)
            if (uuid.equals(player.getUUID())) {
                continue;
            }

            // Send each other player's cape to the newly joined player
            NetworkHandler.CapeData payload = new NetworkHandler.CapeData(
                    uuid.toString(),
                    capeId,
                    false
            );

            try {
                player.connection.send(payload);
            } catch (Exception e) {
                System.err.println("[CAPE] Failed to send existing cape to newly joined player");
                e.printStackTrace();
            }
        }
    }


    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        String currentCape = player.getPersistentData().getString("current_cape");

        if (!currentCape.isEmpty()) {
            NetworkSender.sendCapePacket(player, currentCape, false);
            CapeManager.register(player.getUUID(), currentCape);
        }
    }

    private Map<String, String> buildCapeManifest() {
        Path dir = Paths.get("kubejs/assets/capejs/textures/capes");

        try (Stream<Path> files = Files.list(dir)) {
            return files
                    .filter(p -> p.toString().endsWith(".png"))
                    .collect(Collectors.toMap(
                            p -> p.getFileName().toString().replace(".png", ""),
                            this::hash
                    ));
        } catch (IOException ignored) {
            return Map.of();
        }
    }

    private String hash(Path file) {
        try {
            return Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-1")
                            .digest(Files.readAllBytes(file))
            );
        } catch (Exception e) {
            return "";
        }
    }

}