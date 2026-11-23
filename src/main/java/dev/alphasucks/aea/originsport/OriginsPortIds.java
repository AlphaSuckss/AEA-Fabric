package dev.alphasucks.aea.originsport;

import net.minecraft.class_2960;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Id helpers for the Overgrown Origins port: type ids keep the {@code origins:} namespace, internals use {@code aea:}. */
public final class OriginsPortIds {
    public static final Logger LOGGER = LoggerFactory.getLogger("AEA/Origins");

    private OriginsPortIds() {
    }

    /** Type ids (conditions, actions, powers) as Overgrown's Origins registers them. */
    public static class_2960 origins(String path) {
        return new class_2960("origins", path);
    }

    /** AEA-internal ids: network channels, attachments, power sources. */
    public static class_2960 aea(String path) {
        return new class_2960("aea", path);
    }
}
