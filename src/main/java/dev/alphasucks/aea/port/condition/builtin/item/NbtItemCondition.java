package dev.alphasucks.aea.port.condition.builtin.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.Nbt;
import dev.alphasucks.aea.port.data.NbtComparison;
import net.minecraft.class_2487;

public final class NbtItemCondition implements ConditionType<ItemCtx, NbtItemCondition.Cfg> {
   @Override
   public MapCodec<NbtItemCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(i -> i.group(Nbt.CODEC.fieldOf("nbt").forGetter(NbtItemCondition.Cfg::nbt)).apply(i, NbtItemCondition.Cfg::new));
   }

   public boolean test(NbtItemCondition.Cfg cfg, ItemCtx ctx) {
      class_2487 tag = ctx.stack().method_7969();
      return tag == null ? cfg.nbt.tag().method_33133() : NbtComparison.matches(cfg.nbt.tag(), tag);
   }

   public record Cfg(Nbt nbt) {
   }
}
