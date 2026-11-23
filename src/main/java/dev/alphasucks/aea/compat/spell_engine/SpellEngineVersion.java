package dev.alphasucks.aea.compat.spell_engine;

import net.fabricmc.loader.api.FabricLoader;

/**
 * AEA's Spell Engine types are written for Spell Engine 0.x, which is what most 1.20.1 packs run. Spell Engine 1.x for
 * 1.20.1 is a different API; with it the types and their mixins stay off. Uses only Fabric Loader, so it's safe to call
 * whether Spell Engine is installed or not.
 */
public final class SpellEngineVersion {
    private SpellEngineVersion() {}

    public static boolean installed() {
        return FabricLoader.getInstance().isModLoaded("spell_engine");
    }

    public static boolean supported() {
        return FabricLoader.getInstance().getModContainer("spell_engine")
            .map(c -> c.getMetadata().getVersion().getFriendlyString().startsWith("0."))
            .orElse(false);
    }
}
