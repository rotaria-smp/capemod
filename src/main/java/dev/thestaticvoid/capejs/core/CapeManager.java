package dev.thestaticvoid.capejs.core;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Central registry for active capes
 * Used on both server and client
 */
public final class CapeManager {
    private static final Map<UUID, String> CAPES = new ConcurrentHashMap<>();
    private static volatile BiConsumer<UUID, String> UPDATE_LISTENER = (u, c) -> {};

    private CapeManager() {}

    public static void register(UUID uuid, String capeId) {
        if (capeId == null || capeId.isEmpty()) {
            CAPES.remove(uuid);
        } else {
            CAPES.put(uuid, capeId);
        }
        UPDATE_LISTENER.accept(uuid, capeId);
    }

    public static void unregister(UUID uuid) {
        CAPES.remove(uuid);
        UPDATE_LISTENER.accept(uuid, null);
    }

    public static String get(UUID uuid) {
        return CAPES.get(uuid);
    }

    public static Map<UUID, String> dump() {
        return CAPES;
    }

    public static void setUpdateListener(BiConsumer<UUID, String> listener) {
        UPDATE_LISTENER = listener != null ? listener : (u, c) -> {};
    }
}