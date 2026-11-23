package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_2735;
import net.minecraft.class_3222;
import net.minecraft.class_3532;

public final class ChangeSelectedSlotAction implements ActionType<EntityCtx, ChangeSelectedSlotAction.Cfg> {
   @Override
   public MapCodec<ChangeSelectedSlotAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.INT.fieldOf("slot").forGetter(ChangeSelectedSlotAction.Cfg::slot),
               Codec.BOOL.optionalFieldOf("relative", false).forGetter(ChangeSelectedSlotAction.Cfg::relative)
            )
            .apply(i, ChangeSelectedSlotAction.Cfg::new)
      );
   }

   public void run(ChangeSelectedSlotAction.Cfg cfg, EntityCtx ctx) {
      if (ctx.entity() instanceof class_1657 player) {
         int var8 = class_1661.method_7368();
         class_1661 inventory = player.method_31548();
         int slot = cfg.relative() ? Math.floorMod(inventory.field_7545 + cfg.slot(), var8) : class_3532.method_15340(cfg.slot(), 0, var8 - 1);
         if (inventory.field_7545 != slot) {
            inventory.field_7545 = slot;
            if (player instanceof class_3222 serverPlayer) {
               serverPlayer.field_13987.method_14364(new class_2735(slot));
               serverPlayer.field_7512.method_7623();
            }
         }
      }
   }

   public record Cfg(int slot, boolean relative) {
   }
}
