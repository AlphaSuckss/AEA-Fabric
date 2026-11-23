package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.AttributeModifierHelper;
import dev.alphasucks.aea.port.data.Stat;
import java.util.List;
import net.minecraft.class_3222;
import net.minecraft.class_3445;

public final class ModifyStatAction implements ActionType<EntityCtx, ModifyStatAction.Cfg> {
   @Override
   public MapCodec<ModifyStatAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Stat.CODEC.fieldOf("stat").forGetter(ModifyStatAction.Cfg::stat),
               AttributeModifier.CODEC.fieldOf("modifier").forGetter(ModifyStatAction.Cfg::modifier)
            )
            .apply(i, ModifyStatAction.Cfg::new)
      );
   }

   public void run(ModifyStatAction.Cfg cfg, EntityCtx ctx) {
      if (ctx.entity() instanceof class_3222 player) {
         class_3445<?> stat = cfg.stat.resolve();
         if (stat != null) {
            int current = player.method_14248().method_15025(stat);
            float modified = AttributeModifierHelper.apply(current, List.of(cfg.modifier), player);
            Stat.setValue(player, stat, Math.max(0, (int)modified));
         }
      }
   }

   public record Cfg(Stat stat, AttributeModifier modifier) {
   }
}
