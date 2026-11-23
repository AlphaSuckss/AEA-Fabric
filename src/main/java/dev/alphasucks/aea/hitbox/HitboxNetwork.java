package dev.alphasucks.aea.hitbox;

import net.minecraft.class_2540;
import net.minecraft.class_2960;

/** Server → client: the full list of hit boxes one entity has right now. An empty list removes them. */
public final class HitboxNetwork {
    private HitboxNetwork() {
    }

    public static final class_2960 PARTS = new class_2960("aea", "hitbox_parts");
    private static final int MAX_PARTS = 64;

    /** {@code id} is the network id the server gave the box, so an attack on it reaches the right one. */
    public record Part(int id, float width, float height, float forward, float up, float right, boolean scaleWithEntity) {
    }

    public record Parts(int owner, Part[] parts) {
        public void write(class_2540 buf) {
            buf.method_10804(owner);
            buf.method_10804(parts.length);
            for (Part p : parts) {
                buf.method_10804(p.id);
                buf.writeFloat(p.width);
                buf.writeFloat(p.height);
                buf.writeFloat(p.forward);
                buf.writeFloat(p.up);
                buf.writeFloat(p.right);
                buf.writeBoolean(p.scaleWithEntity);
            }
        }

        public static Parts read(class_2540 buf) {
            int owner = buf.method_10816();
            Part[] parts = new Part[Math.max(0, Math.min(buf.method_10816(), MAX_PARTS))];
            for (int i = 0; i < parts.length; i++) {
                parts[i] = new Part(buf.method_10816(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readBoolean());
            }
            return new Parts(owner, parts);
        }
    }
}
