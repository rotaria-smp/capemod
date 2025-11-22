package dev.thestaticvoid.capejs.client;

import dev.thestaticvoid.capejs.core.CapeManager;
import net.minecraft.client.Minecraft;

import java.util.UUID;

public final class ClientCapeSync {

    public static void receive(UUID uuid, String capeId) {
        if (capeId == null) {
            CapeManager.unregister(uuid);
        } else {
            CapeManager.register(uuid, capeId);
        }

        Minecraft.getInstance().execute(RendererAccess::refresh);
    }
}
