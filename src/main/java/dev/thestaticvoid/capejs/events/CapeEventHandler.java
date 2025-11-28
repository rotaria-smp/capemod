package dev.thestaticvoid.capejs.events;

import dev.thestaticvoid.capejs.core.CapeManager;
import dev.thestaticvoid.capejs.network.CapeManifestData;
import dev.thestaticvoid.capejs.network.NetworkSender;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CapeEventHandler {

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        CapeManifestData manifest = new CapeManifestData(buildCapeManifest());
        player.connection.send(manifest);

        String currentCape = player.getPersistentData().getString("current_cape");

        if (!currentCape.isEmpty()) {
            player.getPersistentData().remove("current_cape");
            NetworkSender.sendCapePacket(player, currentCape, false);
            CapeManager.register(player.getUUID(), currentCape);
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
