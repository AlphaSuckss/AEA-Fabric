package dev.alphasucks.aea.port.condition.builtin.meta;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.power.builtin.ReceiveConditionPower;
import net.minecraft.class_2960;

public final class SendConditionMeta {
   private SendConditionMeta() {
   }

   public static final class BiEntity implements ConditionType<BiEntityCtx, SendConditionMeta.Cfg> {
      @Override
      public MapCodec<SendConditionMeta.Cfg> codec() {
         return SendConditionMeta.Cfg.CODEC;
      }

      public boolean test(SendConditionMeta.Cfg cfg, BiEntityCtx ctx) {
         return ctx.rawActor() != null && ReceiveConditionPower.testBiEntity(ctx.rawActor(), cfg.receiver, ctx);
      }
   }

   public record Cfg(class_2960 receiver) {
      public static final MapCodec<SendConditionMeta.Cfg> CODEC = RecordCodecBuilder.mapCodec(
         i -> i.group(IdCodecs.ID.fieldOf("receiver").forGetter(SendConditionMeta.Cfg::receiver)).apply(i, SendConditionMeta.Cfg::new)
      );
   }

   public static final class Entity implements ConditionType<EntityCtx, SendConditionMeta.Cfg> {
      @Override
      public MapCodec<SendConditionMeta.Cfg> codec() {
         return SendConditionMeta.Cfg.CODEC;
      }

      public boolean test(SendConditionMeta.Cfg cfg, EntityCtx ctx) {
         return ctx.raw() != null && ReceiveConditionPower.testEntity(ctx.raw(), cfg.receiver, ctx);
      }
   }

   public static final class Item implements ConditionType<ItemCtx, SendConditionMeta.Cfg> {
      @Override
      public MapCodec<SendConditionMeta.Cfg> codec() {
         return SendConditionMeta.Cfg.CODEC;
      }

      public boolean test(SendConditionMeta.Cfg cfg, ItemCtx ctx) {
         return ctx.holder() != null && ReceiveConditionPower.testItem(ctx.holder(), cfg.receiver, ctx);
      }
   }
}
