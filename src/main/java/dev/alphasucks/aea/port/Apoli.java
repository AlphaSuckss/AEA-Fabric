package dev.alphasucks.aea.port;

import net.minecraft.class_2960;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** AEA stand-in for Overgrown's Apoli main class: ported types keep the apoli namespace. */
public final class Apoli {
    public static final String MOD_ID = "apoli";
    public static final Logger LOGGER = LoggerFactory.getLogger("AEA/Apoli");

    private Apoli() {
    }

    public static class_2960 id(String path) {
        return new class_2960(MOD_ID, path);
    }

    /** ids for AEA-internal things (network channels, attachments) that must not clash with Apoli 2.9.2 */
    public static class_2960 internal(String path) {
        return new class_2960("aea", path);
    }
}
