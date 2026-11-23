package dev.alphasucks.aea.packassets;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

/**
 * Wire format of data pack textures. The server lists what it has, the client asks for what it lacks, and the server
 * sends those files in chunks. Every message carries the generation of the list it belongs to.
 */
public final class TextureNetwork {
    private TextureNetwork() {
    }

    public static final class_2960 MANIFEST = new class_2960("aea", "textures_manifest");
    public static final class_2960 REQUEST = new class_2960("aea", "textures_request");
    public static final class_2960 CHUNK = new class_2960("aea", "textures_chunk");
    /** A request is split so that each packet stays far below the 32 KB limit of client packets. */
    public static final int REQUEST_BATCH = 100;

    /** One texture in the server's list: its id, the SHA-1 of the file and its size in bytes. */
    public record Listed(class_2960 id, String hash, int size) {
    }

    public record Manifest(int generation, List<Listed> textures) {
        public void write(class_2540 buf) {
            buf.method_10804(generation);
            buf.method_10804(textures.size());
            for (Listed t : textures) {
                buf.method_10812(t.id);
                buf.method_10788(t.hash, 40);
                buf.method_10804(t.size);
            }
        }

        public static Manifest read(class_2540 buf) {
            int generation = buf.method_10816();
            int count = Math.max(0, Math.min(buf.method_10816(), PackTextures.MAX_COUNT));
            List<Listed> textures = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                textures.add(new Listed(buf.method_10810(), buf.method_10800(40), buf.method_10816()));
            }
            return new Manifest(generation, textures);
        }
    }

    /** {@code first} starts a new request; later packets of the same request add to it. */
    public record Request(int generation, boolean first, List<class_2960> ids) {
        public void write(class_2540 buf) {
            buf.method_10804(generation);
            buf.writeBoolean(first);
            buf.method_10804(ids.size());
            for (class_2960 id : ids) {
                buf.method_10812(id);
            }
        }

        public static Request read(class_2540 buf) {
            int generation = buf.method_10816();
            boolean first = buf.readBoolean();
            int count = Math.max(0, Math.min(buf.method_10816(), REQUEST_BATCH));
            List<class_2960> ids = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                ids.add(buf.method_10810());
            }
            return new Request(generation, first, ids);
        }
    }

    /** {@code data} is the slice of the file that starts at {@code offset}. */
    public record Chunk(int generation, class_2960 id, int offset, byte[] data) {
        public void write(class_2540 buf) {
            buf.method_10804(generation);
            buf.method_10812(id);
            buf.method_10804(offset);
            buf.method_10813(data);
        }

        public static Chunk read(class_2540 buf) {
            return new Chunk(buf.method_10816(), buf.method_10810(), buf.method_10816(), buf.method_10803(PackTextures.CHUNK));
        }
    }
}
