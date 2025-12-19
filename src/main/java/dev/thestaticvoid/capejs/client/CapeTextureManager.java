package dev.thestaticvoid.capejs.client;

import net.minecraft.resources.ResourceLocation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CapeTextureManager {

    private static final Map<String, ResourceLocation> CACHE = new ConcurrentHashMap<>();

    public static ResourceLocation get(String capeId) {
        return CACHE.computeIfAbsent(capeId, id ->
                ResourceLocation.fromNamespaceAndPath("capejs", "textures/capes/" + id + ".png")
        );
    }

    public static void clear() {
        CACHE.clear();
    }
}