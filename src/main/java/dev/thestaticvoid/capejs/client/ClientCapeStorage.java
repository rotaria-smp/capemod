package dev.thestaticvoid.capejs.client;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side storage for unlocked capes.
 * This is synced from the server via network packets.
 */
public class ClientCapeStorage {
    private static final Set<String> unlockedCapes = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static String equippedCape = "";

    /**
     * Add an unlocked cape to the client's storage
     */
    public static void unlockCape(String capeId) {
        if (capeId != null && !capeId.isEmpty()) {
            unlockedCapes.add(capeId);
            System.out.println("[ClientCapeStorage] Unlocked cape: " + capeId);
        }
    }

    /**
     * Add multiple unlocked capes at once
     */
    public static void unlockCapes(Collection<String> capeIds) {
        for (String capeId : capeIds) {
            unlockCape(capeId);
        }
    }

    /**
     * Set all unlocked capes (replaces existing list)
     */
    public static void setUnlockedCapes(Collection<String> capeIds) {
        unlockedCapes.clear();
        unlockCapes(capeIds);
        System.out.println("[ClientCapeStorage] Set unlocked capes: " + unlockedCapes.size() + " total");
    }

    /**
     * Remove a cape from unlocked list
     */
    public static void lockCape(String capeId) {
        unlockedCapes.remove(capeId);
        System.out.println("[ClientCapeStorage] Locked cape: " + capeId);
    }

    /**
     * Get all unlocked capes
     */
    public static List<String> getUnlockedCapes() {
        return new ArrayList<>(unlockedCapes);
    }

    /**
     * Check if a cape is unlocked
     */
    public static boolean isCapeUnlocked(String capeId) {
        return unlockedCapes.contains(capeId);
    }

    /**
     * Get the currently equipped cape
     */
    public static String getEquippedCape() {
        return equippedCape;
    }

    /**
     * Set the currently equipped cape
     */
    public static void setEquippedCape(String capeId) {
        equippedCape = capeId != null ? capeId : "";
        System.out.println("[ClientCapeStorage] Equipped cape: " + equippedCape);
    }

    /**
     * Clear all cape data (e.g., on disconnect)
     */
    public static void clear() {
        unlockedCapes.clear();
        equippedCape = "";
        System.out.println("[ClientCapeStorage] Cleared all cape data");
    }

    /**
     * Get total number of unlocked capes
     */
    public static int getUnlockedCapeCount() {
        return unlockedCapes.size();
    }
}