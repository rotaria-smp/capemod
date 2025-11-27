package dev.thestaticvoid.capejs.core;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public final class CapeManager {
    private static final Map<UUID, String> CAPES = new ConcurrentHashMap<>();
    private static volatile BiConsumer<UUID, String> UPDATE_LISTENER = (u, c) -> {};

    private CapeManager() {}

    public static void register(UUID uuid, String capeId) {
        if (capeId == null) {
            CAPES.remove(uuid);
        } else {
            CAPES.put(uuid, capeId);
            System.out.println("Capes PUT ");
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
