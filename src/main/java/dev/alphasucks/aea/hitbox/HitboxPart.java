package dev.alphasucks.aea.hitbox;

import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2487;
import net.minecraft.class_2596;
import net.minecraft.class_2602;
import net.minecraft.class_3532;
import net.minecraft.class_3619;
import net.minecraft.class_4048;
import net.minecraft.class_4050;

/**
 * One extra hit box of an {@code aea:hitbox_part} power. Like an ender dragon part it is never added to the world as
 * an entity of its own: the world only knows it through {@link HitboxWorld}.
 */
public final class HitboxPart extends class_1297 {
    public static final HitboxPart[] NONE = new HitboxPart[0];

    public final class_1309 owner;
    public final float width;
    public final float height;
    public final float forward;
    public final float up;
    public final float right;
    public final boolean scaleWithEntity;
    /** The owner's age the last time the owning power ticked (server only). */
    int seenAge;
    private class_4048 size;
    private float appliedScale = Float.NaN;

    public HitboxPart(class_1309 owner, float width, float height, float forward, float up, float right, boolean scaleWithEntity) {
        super(owner.method_5864(), owner.method_37908());
        this.owner = owner;
        this.width = width;
        this.height = height;
        this.forward = forward;
        this.up = up;
        this.right = right;
        this.scaleWithEntity = scaleWithEntity;
        reposition();
    }

    /** Moves the box to its place beside the owner. Returns true when its size changed. */
    public boolean reposition() {
        // The owner's standing size against its type's size: the same whatever pose it is in (sleeping, swimming).
        float typeWidth = owner.method_5864().method_17685();
        float scale = scaleWithEntity && typeWidth > 0.0F ? owner.method_18377(class_4050.field_18076).field_18067 / typeWidth : 1.0F;
        if (!(scale > 0.0F) || Float.isInfinite(scale)) {
            scale = 1.0F;
        }
        boolean resized = scale != appliedScale;
        if (resized) {
            appliedScale = scale;
            size = class_4048.method_18384(width * scale, height * scale);
            method_18382();
        }
        float yaw = owner.field_6283 * ((float) Math.PI / 180.0F);
        float sin = class_3532.method_15374(yaw);
        float cos = class_3532.method_15362(yaw);
        double oldX = method_23317();
        double oldY = method_23318();
        double oldZ = method_23321();
        // Yaw 0 faces +Z; the owner's right is then -X.
        method_5814(owner.method_23317() + (-sin * forward - cos * right) * scale,
            owner.method_23318() + up * scale,
            owner.method_23321() + (cos * forward - sin * right) * scale);
        field_6014 = oldX;
        field_6036 = oldY;
        field_5969 = oldZ;
        field_6038 = oldX;
        field_5971 = oldY;
        field_5989 = oldZ;
        return resized;
    }

    @Override
    public class_4048 method_18377(class_4050 pose) {
        return size != null ? size : super.method_18377(pose);
    }

    @Override
    public boolean method_5863() {
        return method_5805();
    }

    /** A box is gone as soon as it is dropped or its owner dies, so nothing keeps holding on to it. */
    @Override
    public boolean method_5805() {
        return !method_31481() && owner.method_5805();
    }

    /** Using the box (right click) uses the owner. */
    @Override
    public class_1269 method_5688(class_1657 player, class_1268 hand) {
        return owner == player ? class_1269.field_5811 : owner.method_5688(player, hand);
    }

    @Override
    public boolean method_5822() {
        return false;
    }

    @Override
    public class_3619 method_5657() {
        return class_3619.field_15975;
    }

    /** A hit on the box is a hit on the owner. The owner can never hurt itself through its own box. */
    @Override
    public boolean method_5643(class_1282 source, float amount) {
        if (source.method_5529() == owner || source.method_5526() == owner) {
            return false;
        }
        return owner.method_5643(source, amount);
    }

    @Override
    public boolean method_5779(class_1297 entity) {
        return this == entity || owner == entity;
    }

    @Override
    public void method_5762(double x, double y, double z) {
        owner.method_5762(x, y, z);
    }

    @Override
    public class_1799 method_31480() {
        return owner.method_31480();
    }

    @Override
    public boolean method_31746() {
        return false;
    }

    @Override
    public class_2596<class_2602> method_18002() {
        throw new UnsupportedOperationException("aea:hitbox_part boxes are not spawned as entities");
    }

    @Override
    protected void method_5693() {
    }

    @Override
    protected void method_5749(class_2487 nbt) {
    }

    @Override
    protected void method_5652(class_2487 nbt) {
    }
}
