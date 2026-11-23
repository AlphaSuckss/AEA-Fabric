package dev.alphasucks.aea.hitbox;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import net.minecraft.class_1309;
import net.minecraft.class_2960;

/**
 * {@code aea:hitbox_part}: an extra hit box that follows the holder's body rotation. A hit that lands on it counts as
 * a hit on the holder. The box does not collide with blocks or push entities.
 */
public final class HitboxPartPower extends PowerType<HitboxPartPower.Cfg> {
    public static final class_2960 ID = new class_2960("aea", "hitbox_part");

    public record Cfg(float width, float height, float forward, float up, float right, boolean scaleWithEntity) {
    }

    private static final MapCodec<Cfg> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.floatRange(0.05F, 16.0F).optionalFieldOf("width", 1.0F).forGetter(Cfg::width),
        Codec.floatRange(0.05F, 16.0F).optionalFieldOf("height", 1.0F).forGetter(Cfg::height),
        Codec.floatRange(-16.0F, 16.0F).optionalFieldOf("forward", 0.0F).forGetter(Cfg::forward),
        Codec.floatRange(-16.0F, 16.0F).optionalFieldOf("up", 0.0F).forGetter(Cfg::up),
        Codec.floatRange(-16.0F, 16.0F).optionalFieldOf("right", 0.0F).forGetter(Cfg::right),
        Codec.BOOL.optionalFieldOf("scale_with_entity", true).forGetter(Cfg::scaleWithEntity)
    ).apply(i, Cfg::new));

    @Override
    public MapCodec<Cfg> configCodec() {
        return CODEC;
    }

    /** Runs only while the power's condition holds; the box goes away shortly after it stops being called. */
    @Override
    public void tick(class_2960 id, Cfg cfg, PowerContainer holder) {
        class_1309 owner = holder.owner();
        if (owner == null || owner.method_37908().field_9236) {
            return;
        }
        HitboxParts.of(owner).touch(owner, id, cfg);
    }

    @Override
    public void onRemoved(class_2960 id, Cfg cfg, PowerContainer holder, class_2960 source) {
        class_1309 owner = holder.owner();
        if (owner != null && !owner.method_37908().field_9236 && !holder.hasPower(id)) {
            HitboxParts.of(owner).drop(id);
        }
    }
}
