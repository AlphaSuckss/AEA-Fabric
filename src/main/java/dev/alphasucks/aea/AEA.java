package dev.alphasucks.aea;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Alpha's Everything Addon.
 *
 * Bundles Sync (by Overgrown, MIT) and ports the features of Overgrown's Apoli/Origins
 * (A/O License) onto Origins 1.10.2 / Apoli 2.9.2 for Fabric 1.20.1.
 * Type ids keep their original namespaces (sync:, apoli:, origins:).
 */
public class AEA implements ModInitializer {
    public static final String MOD_ID = "aea";
    public static final Logger LOGGER = LoggerFactory.getLogger("AEA");

    @Override
    public void onInitialize() {
        String version = FabricLoader.getInstance().getModContainer(MOD_ID)
            .map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("?");
        LOGGER.info("Alpha's Everything Addon {} initialising (Sync + Overgrown Apoli/Origins port)", version);
        AEAContent.init();
    }
}
