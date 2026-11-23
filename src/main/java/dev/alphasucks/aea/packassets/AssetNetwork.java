package dev.alphasucks.aea.packassets;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

/**
 * Wire format of data pack assets. It has the same shape as data pack textures: the server lists its files, the
 * client asks for the ones it lacks, and the server sends them in chunks.
 */
public final class AssetNetwork {
    private AssetNetwork() {
    }

    public static final class_2960 MANIFEST = new class_2960("aea", "assets_manifest");
    public static final class_2960 REQUEST = new class_2960("aea", "assets_request");
    public static final class_2960 CHUNK = new class_2960("aea", "assets_chunk");
    public static final int MAX_PATH = 256;
    /** Files listed per manifest packet, and paths per request packet: both keep packets far below their size limits. */
    public static final int MANIFEST_BATCH = 1000;
    public static final int REQUEST_BATCH = 60;

    /** One file: its path under {@code assets/}, the SHA-1 of its contents and its size in bytes. */
    public record Listed(String path, String hash, int size) {
    }

    /**
     * One part of the list. {@code packHash} identifies the whole set, so a client that already applied it does
     * nothing. {@code first} starts a new list and {@code last} completes it.
     */
    public record Manifest(int generation, String packHash, boolean first, boolean last, List<Listed> files) {
        public void write(class_2540 buf) {
            buf.method_10804(generation);
            buf.method_10788(packHash, 40);
            buf.writeBoolean(first);
            buf.writeBoolean(last);
            buf.method_10804(files.size());
            for (Listed f : files) {
                buf.method_10788(f.path, MAX_PATH);
                buf.method_10788(f.hash, 40);
                buf.method_10804(f.size);
            }
        }

        public static Manifest read(class_2540 buf) {
            int generation = buf.method_10816();
            String packHash = buf.method_10800(40);
            boolean first = buf.readBoolean();
            boolean last = buf.readBoolean();
            int count = Math.max(0, Math.min(buf.method_10816(), MANIFEST_BATCH));
            List<Listed> files = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                files.add(new Listed(buf.method_10800(MAX_PATH), buf.method_10800(40), buf.method_10816()));
            }
            return new Manifest(generation, packHash, first, last, files);
        }
    }

    /** {@code first} starts a new request; later packets of the same request add to it. */
    public record Request(int generation, boolean first, List<String> paths) {
        public void write(class_2540 buf) {
            buf.method_10804(generation);
            buf.writeBoolean(first);
            buf.method_10804(paths.size());
            for (String path : paths) {
                buf.method_10788(path, MAX_PATH);
            }
        }

        public static Request read(class_2540 buf) {
            int generation = buf.method_10816();
            boolean first = buf.readBoolean();
            int count = Math.max(0, Math.min(buf.method_10816(), REQUEST_BATCH));
            List<String> paths = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                paths.add(buf.method_10800(MAX_PATH));
            }
            return new Request(generation, first, paths);
        }
    }

    /** {@code data} is the slice of the file that starts at {@code offset}. */
    public record Chunk(int generation, String path, int offset, byte[] data) {
        public void write(class_2540 buf) {
            buf.method_10804(generation);
            buf.method_10788(path, MAX_PATH);
            buf.method_10804(offset);
            buf.method_10813(data);
        }

        public static Chunk read(class_2540 buf) {
            return new Chunk(buf.method_10816(), buf.method_10800(MAX_PATH), buf.method_10816(), buf.method_10803(PackAssets.CHUNK));
        }
    }
}
