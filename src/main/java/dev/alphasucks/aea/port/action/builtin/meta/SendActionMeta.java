package dev.alphasucks.aea.port.action.builtin.meta;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.power.builtin.ReceiveActionPower;
import net.minecraft.class_2960;

public final class SendActionMeta {
   private SendActionMeta() {
   }

   public static final class BiEntity implements ActionType<BiEntityCtx, SendActionMeta.Cfg> {
      @Override
      public MapCodec<SendActionMeta.Cfg> codec() {
         return SendActionMeta.Cfg.CODEC;
      }

      public void run(SendActionMeta.Cfg cfg, BiEntityCtx ctx) {
         if (ctx.rawActor() != null) {
            ReceiveActionPower.receiveBiEntity(ctx.rawActor(), cfg.receiver, ctx);
         }
      }
   }

   public record Cfg(class_2960 receiver) {
      public static final MapCodec<SendActionMeta.Cfg> CODEC = RecordCodecBuilder.mapCodec(
         i -> i.group(IdCodecs.ID.fieldOf("receiver").forGetter(SendActionMeta.Cfg::receiver)).apply(i, SendActionMeta.Cfg::new)
      );
   }

   public static final class Entity implements ActionType<EntityCtx, SendActionMeta.Cfg> {
      @Override
      public MapCodec<SendActionMeta.Cfg> codec() {
         return SendActionMeta.Cfg.CODEC;
      }

      public void run(SendActionMeta.Cfg cfg, EntityCtx ctx) {
         if (ctx.raw() != null) {
            ReceiveActionPower.receiveEntity(ctx.raw(), cfg.receiver, ctx);
         }
      }
   }

   public static final class Item implements ActionType<ItemCtx, SendActionMeta.Cfg> {
      @Override
      public MapCodec<SendActionMeta.Cfg> codec() {
         return SendActionMeta.Cfg.CODEC;
      }

      public void run(SendActionMeta.Cfg cfg, ItemCtx ctx) {
         if (ctx.holder() != null) {
            ReceiveActionPower.receiveItem(ctx.holder(), cfg.receiver, ctx);
         }
      }
   }
}
