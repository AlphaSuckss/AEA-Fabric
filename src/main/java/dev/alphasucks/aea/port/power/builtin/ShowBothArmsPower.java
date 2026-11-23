package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Perspective;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class ShowBothArmsPower extends PowerType<ShowBothArmsPower.Config> {
   public static final int MAIN = 1;
   public static final int OFF = 2;
   public static final int MAX_GHOST_ARMS = 16;

   @Override
   public MapCodec<ShowBothArmsPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.BOOL.optionalFieldOf("main_hand", false).forGetter(ShowBothArmsPower.Config::mainHand),
               Codec.BOOL.optionalFieldOf("off_hand", true).forGetter(ShowBothArmsPower.Config::offHand),
               Codec.intRange(0, 16).optionalFieldOf("ghost_arms", 0).forGetter(ShowBothArmsPower.Config::ghostArms),
               Codec.FLOAT.optionalFieldOf("ghost_spacing", 0.12F).forGetter(ShowBothArmsPower.Config::ghostSpacing),
               Codec.FLOAT.optionalFieldOf("ghost_alpha", 0.35F).forGetter(ShowBothArmsPower.Config::ghostAlpha),
               Perspective.LIST_CODEC.optionalFieldOf("ghost_perspectives", Perspective.BOTH).forGetter(ShowBothArmsPower.Config::ghostPerspectives)
            )
            .apply(
               i,
               (mainHand, offHand, ghostArms, ghostSpacing, ghostAlpha, ghostPerspectives) -> new ShowBothArmsPower.Config(
                  mainHand, offHand, ghostArms, ghostSpacing, ghostAlpha, ghostPerspectives, Perspective.maskOf(ghostPerspectives)
               )
            )
      );
   }

   public static boolean resolve(@Nullable class_1297 entity, ShowBothArmsPower.Arms out) {
      out.clear();
      if (entity == null) {
         return false;
      }

      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(ApoliIds.SHOW_BOTH_ARMS);
         if (powers.isEmpty()) {
            return false;
         }

         EntityCtx ctx = null;

         for (int i = 0; i < powers.size(); i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof ShowBothArmsPower.Config cfg) {
                  if (power.condition().isPresent()) {
                     if (ctx == null) {
                        ctx = EntityCtx.of(entity, entity.method_37908());
                     }

                     if (!((EntityCondition)power.condition().get()).test(ctx)) {
                        continue;
                     }
                  }

                  if (cfg.mainHand) {
                     out.mask |= 1;
                  }

                  if (cfg.offHand) {
                     out.mask |= 2;
                  }

                  if (cfg.ghostArms > out.ghosts) {
                     out.ghosts = cfg.ghostArms;
                     out.spacing = cfg.ghostSpacing;
                     out.alpha = cfg.ghostAlpha;
                     out.perspectives = cfg.ghostPerspectiveMask;
                  }
               }
            }
         }

         return out.mask != 0 || out.ghosts > 0;
      } else {
         return false;
      }
   }

   public static final class Arms {
      public int mask;
      public int ghosts;
      public float spacing = 0.12F;
      public float alpha = 0.35F;
      public int perspectives = Perspective.MASK_BOTH;

      public boolean ghostsIn(boolean firstPerson) {
         return this.ghosts > 0 && Perspective.masked(this.perspectives, firstPerson);
      }

      private void clear() {
         this.mask = 0;
         this.ghosts = 0;
         this.spacing = 0.12F;
         this.alpha = 0.35F;
         this.perspectives = Perspective.MASK_BOTH;
      }
   }

   public record Config(
      boolean mainHand, boolean offHand, int ghostArms, float ghostSpacing, float ghostAlpha, List<Perspective> ghostPerspectives, int ghostPerspectiveMask
   ) {
   }
}
