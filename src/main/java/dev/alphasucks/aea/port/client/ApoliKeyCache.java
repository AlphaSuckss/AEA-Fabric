package dev.alphasucks.aea.port.client;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_304;
import net.minecraft.class_310;
import org.slf4j.Logger;

/**
 * Apoli 2.9.2 looks up the key of an active power once and caches the {@link class_304} object
 * ({@code ApoliClient.idToKeyBindingMap}). Data-driven keybinds are re-created whenever the server re-sends them
 * (join, respawn, reload), so that cache would keep pointing at the old, unregistered objects and the key would stop
 * working. This puts the current objects back into Apoli's cache after every change.
 */
@Environment(EnvType.CLIENT)
public final class ApoliKeyCache {
    private static final Logger LOG = LogUtils.getLogger();
    private static Field field;
    private static boolean unavailable;

    private ApoliKeyCache() {
    }

    public static void refresh() {
        Map<String, class_304> cache = cache();
        if (cache == null || cache.isEmpty()) {
            return; // not built yet: Apoli reads the current keybinds itself when it first needs one
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1690 == null) {
            return;
        }
        Map<String, class_304> current = new HashMap<>();
        for (class_304 key : mc.field_1690.field_1839) {
            current.put(key.method_1431(), key);
        }
        cache.keySet().removeIf(id -> !current.containsKey(id));
        cache.putAll(current);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, class_304> cache() {
        if (unavailable) {
            return null;
        }
        try {
            if (field == null) {
                field = Class.forName("io.github.apace100.apoli.ApoliClient").getDeclaredField("idToKeyBindingMap");
                field.setAccessible(true);
            }
            return (Map<String, class_304>) field.get(null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            unavailable = true;
            LOG.warn("[AEA] Could not reach Apoli's keybind cache ({}); data-driven keys may stop working after a respawn", e.toString());
            return null;
        }
    }
}
