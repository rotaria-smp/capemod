package dev.thestaticvoid.capejs.kubejs;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;

public class CapeJSPlugin implements KubeJSPlugin {
    @Override
    public void init() {}

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(CapeJSEvents.EVENT_GROUP);
    }
}
