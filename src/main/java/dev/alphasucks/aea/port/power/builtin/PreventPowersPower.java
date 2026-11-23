package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_2960;

public final class PreventPowersPower extends PowerType<PreventPowersPower.Cfg> {
   @Override
   public MapCodec<PreventPowersPower.Cfg> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.listOf().fieldOf("powers").forGetter(PreventPowersPower.Cfg::powers),
               Codec.INT.optionalFieldOf("update_rate", 5).forGetter(PreventPowersPower.Cfg::updateRate)
            )
            .apply(i, PreventPowersPower.Cfg::new)
      );
   }

   @Override
   public boolean ticksNonLivingEntities() {
      return true;
   }

   public void tick(class_2960 powerId, PreventPowersPower.Cfg cfg, PowerContainer holder) {
      class_1297 owner = holder.rawOwner();
      if (owner != null) {
         int rate = Math.max(1, cfg.updateRate());
         if (rate <= 1 || Math.floorMod(owner.field_6012, rate) == Math.floorMod(powerId.hashCode() + owner.method_5628(), rate)) {
            boolean active = conditionHolds(owner, powerId);

            for (class_2960 target : cfg.powers()) {
               if (active) {
                  holder.suppressPower(target, powerId);
               } else {
                  holder.unsuppressPower(target, powerId);
               }
            }
         }
      }
   }

   private static boolean conditionHolds(class_1297 owner, class_2960 powerId) {
      Power loaded = ApoliPowers.get(powerId);
      return loaded != null && !loaded.condition().isEmpty()
         ? ((EntityCondition)loaded.condition().get()).test(EntityCtx.of(owner, owner.method_37908()))
         : true;
   }

   public record Cfg(List<class_2960> powers, int updateRate) {
   }
}
