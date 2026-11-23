package dev.alphasucks.aea.mindcontrol;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_1282;
import net.minecraft.class_1299;
import net.minecraft.class_1304;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1324;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2371;
import net.minecraft.class_2487;
import net.minecraft.class_2940;
import net.minecraft.class_2943;
import net.minecraft.class_2945;
import net.minecraft.class_3222;
import net.minecraft.class_4048;
import net.minecraft.class_4050;
import net.minecraft.class_5134;

/**
 * What a mind controller leaves behind while their mind is elsewhere: their body, wearing their skin, armour and held
 * items. It isn't saved with the world; damage it takes goes to the controller.
 */
public final class ControlBody extends class_1309 {
    private static final class_2940<Optional<UUID>> OWNER = class_2945.method_12791(ControlBody.class, class_2943.field_13313);

    private final class_2371<class_1799> hands = class_2371.method_10213(2, class_1799.field_8037);
    private final class_2371<class_1799> armor = class_2371.method_10213(4, class_1799.field_8037);

    public ControlBody(class_1299<? extends ControlBody> type, class_1937 world) {
        super(type, world);
    }

    static ControlBody of(class_3222 owner) {
        ControlBody body = new ControlBody(MindControl.BODY, owner.method_37908());
        body.field_6011.method_12778(OWNER, Optional.of(owner.method_5667()));
        body.method_5808(owner.method_23317(), owner.method_23318(), owner.method_23321(), owner.method_36454(), owner.method_36455());
        body.method_5847(owner.method_5791());
        body.field_6283 = owner.field_6283;
        body.method_5665(owner.method_5476());
        body.method_5880(true);
        for (class_1304 slot : class_1304.values()) {
            body.method_5673(slot, owner.method_6118(slot).method_7972());
        }
        class_1324 max = body.method_5996(class_5134.field_23716);
        if (max != null) max.method_6192(owner.method_6063());
        body.method_6033(owner.method_6032());
        return body;
    }

    public Optional<UUID> owner() {
        return field_6011.method_12789(OWNER);
    }

    @Override
    protected void method_5693() {
        super.method_5693();
        field_6011.method_12784(OWNER, Optional.empty());
    }

    @Override
    public boolean method_5643(class_1282 source, float amount) {
        if (method_37908().field_9236 || method_5679(source) || field_6008 > 10) return false;
        if (!MindControl.hurtBody(this, source, amount)) return false;
        field_6008 = 20;
        field_6254 = 10;
        field_6235 = 10;
        method_37908().method_48760(this, source);
        return true;
    }

    @Override
    public void method_5773() {
        super.method_5773();
        if (!method_37908().field_9236 && field_6012 % 20 == 0 && !MindControl.bodyInUse(this)) method_31472();
    }

    /** Bodies only exist during a session, so they are never saved. */
    @Override
    public boolean method_5662(class_2487 tag) {
        return false;
    }

    @Override
    public boolean method_5786(class_2487 tag) {
        return false;
    }

    @Override
    public void method_5652(class_2487 tag) {}

    @Override
    public void method_5749(class_2487 tag) {}

    @Override
    public Iterable<class_1799> method_5661() {
        return armor;
    }

    @Override
    public class_1799 method_6118(class_1304 slot) {
        return switch (slot.method_5925()) {
            case field_6177 -> hands.get(slot.method_5927());
            case field_6178 -> armor.get(slot.method_5927());
        };
    }

    @Override
    public void method_5673(class_1304 slot, class_1799 stack) {
        switch (slot.method_5925()) {
            case field_6177 -> hands.set(slot.method_5927(), stack);
            case field_6178 -> armor.set(slot.method_5927(), stack);
        }
    }

    @Override
    public class_1306 method_6068() {
        return class_1306.field_6183;
    }

    @Override
    protected float method_18394(class_4050 pose, class_4048 dimensions) {
        return 1.62F;
    }

    @Override
    public boolean method_5810() {
        return false;
    }
}
