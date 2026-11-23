package dev.alphasucks.aea;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

/** {@code config/aea.toml}: a few on/off settings. Read once at start; a missing file is written with the defaults. */
public final class AeaConfig {
    private AeaConfig() {
    }

    private static final String DEFAULTS = """
        # Alpha's Everything Addon

        [data_pack_textures]
        # Send the PNG files under data/<namespace>/textures/ in data packs to players,
        # so powers can use them without a resource pack. (Server side.)
        enabled = true

        [data_pack_assets]
        # Send the assets/ folder of the world's data packs to players and load it on
        # their game as a resource pack. (Server side.)
        enabled = true
        """;

    private static Map<String, String> values;

    private static synchronized Map<String, String> values() {
        if (values != null) {
            return values;
        }
        Map<String, String> out = new HashMap<>();
        try {
            Path file = FabricLoader.getInstance().getConfigDir().resolve("aea.toml");
            if (!Files.isRegularFile(file)) {
                Files.createDirectories(file.getParent());
                Files.writeString(file, DEFAULTS);
            }
            parse(Files.readAllLines(file), out);
        } catch (IOException | RuntimeException e) {
            AEA.LOGGER.warn("[AEA] Could not read config/aea.toml, using the defaults: {}", e.toString());
        }
        values = out;
        return out;
    }

    private static void parse(List<String> lines, Map<String, String> out) {
        String section = "";
        for (String raw : lines) {
            String line = raw;
            int hash = line.indexOf('#');
            if (hash >= 0) {
                line = line.substring(0, hash);
            }
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("[") && line.endsWith("]")) {
                section = line.substring(1, line.length() - 1).trim();
                continue;
            }
            int eq = line.indexOf('=');
            if (eq > 0) {
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                out.put(section.isEmpty() ? key : section + "." + key, value);
            }
        }
    }

    private static boolean flag(String key, boolean fallback) {
        String value = values().get(key);
        return value == null ? fallback : value.equalsIgnoreCase("true");
    }

    public static boolean packTextures() {
        return flag("data_pack_textures.enabled", true);
    }

    public static boolean packAssets() {
        return flag("data_pack_assets.enabled", true);
    }
}
