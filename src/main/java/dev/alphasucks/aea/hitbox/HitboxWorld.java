package dev.alphasucks.aea.hitbox;

/** Implemented by every world through a mixin: the {@code aea:hitbox_part} boxes that exist in it. */
public interface HitboxWorld {
    /** A snapshot; never modified in place, so it can be read from any thread. */
    HitboxPart[] aea$hitboxParts();

    void aea$setHitboxParts(HitboxPart[] parts);

    static void add(Object world, HitboxPart part) {
        HitboxWorld w = (HitboxWorld) world;
        HitboxPart[] old = w.aea$hitboxParts();
        HitboxPart[] next = new HitboxPart[old.length + 1];
        System.arraycopy(old, 0, next, 0, old.length);
        next[old.length] = part;
        w.aea$setHitboxParts(next);
    }

    /** Takes the box out of the world and marks it removed, so anything still pointing at it lets go. */
    static void remove(Object world, HitboxPart part) {
        if (!part.method_31481()) {
            part.method_31745(net.minecraft.class_1297.class_5529.field_26999);
        }
        HitboxWorld w = (HitboxWorld) world;
        HitboxPart[] old = w.aea$hitboxParts();
        for (int i = 0; i < old.length; i++) {
            if (old[i] == part) {
                if (old.length == 1) {
                    w.aea$setHitboxParts(HitboxPart.NONE);
                    return;
                }
                HitboxPart[] next = new HitboxPart[old.length - 1];
                System.arraycopy(old, 0, next, 0, i);
                System.arraycopy(old, i + 1, next, i, old.length - i - 1);
                w.aea$setHitboxParts(next);
                return;
            }
        }
    }

    static HitboxPart byId(Object world, int id) {
        for (HitboxPart part : ((HitboxWorld) world).aea$hitboxParts()) {
            if (part.method_5628() == id) {
                return part;
            }
        }
        return null;
    }
}
