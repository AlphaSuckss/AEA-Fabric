package dev.alphasucks.aea.port.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1297;
import net.minecraft.class_7923;
import org.jetbrains.annotations.Nullable;

public record EffectSpec(class_1291 effect, Expression duration, Expression amplifier, boolean ambient, boolean showParticles, boolean showIcon) {
   public static final MapCodec<EffectSpec> MAP_CODEC = AliasingMapCodec.wrap(
      RecordCodecBuilder.mapCodec(
         instance -> instance.group(
               class_7923.field_41174.method_39673().fieldOf("effect").forGetter(EffectSpec::effect),
               Expression.INT_OR_EXPR.optionalFieldOf("duration", Expression.constant(100.0)).forGetter(EffectSpec::duration),
               Expression.INT_OR_EXPR.optionalFieldOf("amplifier", Expression.constant(0.0)).forGetter(EffectSpec::amplifier),
               Codec.BOOL.optionalFieldOf("is_ambient", false).forGetter(EffectSpec::ambient),
               Codec.BOOL.optionalFieldOf("show_particles", true).forGetter(EffectSpec::showParticles),
               Codec.BOOL.optionalFieldOf("show_icon", true).forGetter(EffectSpec::showIcon)
            )
            .apply(instance, EffectSpec::new)
      ),
      Map.of("id", "effect", "ambient", "is_ambient")
   );
   public static final Codec<EffectSpec> CODEC = MAP_CODEC.codec();
   public static final Codec<List<EffectSpec>> LIST_OR_SINGLE = Codec.either(CODEC, Codec.list(CODEC))
      .xmap(either -> (List)either.map(List::of, list -> list), list -> list.size() == 1 ? Either.left((EffectSpec)list.get(0)) : Either.right(list));

   public class_1293 resolve(@Nullable class_1297 target) {
      return new class_1293(this.effect, this.duration.evalInt(target), this.amplifier.evalInt(target), this.ambient, this.showParticles, this.showIcon);
   }

   public class_1293 resolveWithDuration(@Nullable class_1297 target, int durationTicks) {
      return new class_1293(this.effect, durationTicks, this.amplifier.evalInt(target), this.ambient, this.showParticles, this.showIcon);
   }
}
