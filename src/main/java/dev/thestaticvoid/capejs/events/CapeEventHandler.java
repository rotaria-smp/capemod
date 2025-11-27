package dev.thestaticvoid.capejs.events;

import dev.thestaticvoid.capejs.core.CapeManager;
import dev.thestaticvoid.capejs.network.NetworkSender;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class CapeEventHandler {

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

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
}
