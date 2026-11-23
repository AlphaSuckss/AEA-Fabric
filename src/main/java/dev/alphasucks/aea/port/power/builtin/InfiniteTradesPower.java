package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeGate;
import java.util.Optional;
import net.minecraft.class_1646;
import net.minecraft.class_1657;
import net.minecraft.class_1728;
import net.minecraft.class_3222;
import net.minecraft.class_3988;

public final class InfiniteTradesPower extends PowerType<InfiniteTradesPower.Config> {
   private static final PowerTypeGate IN_USE = new PowerTypeGate(ApoliIds.INFINITE_TRADES);

   @Override
   public MapCodec<InfiniteTradesPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(InfiniteTradesPower.Config::bientityCondition))
            .apply(i, InfiniteTradesPower.Config::new)
      );
   }

   public static boolean applies(class_3988 merchant) {
      if (!IN_USE.inUse()) {
         return false;
      } else {
         class_1657 player = merchant.method_8257();
         if (player != null && !player.method_37908().method_8608() && PowerLookup.holdsType(player, ApoliIds.INFINITE_TRADES)) {
            BiEntityCtx ctx = BiEntityCtx.of(player, merchant, merchant.method_37908());
            return PowerLookup.anyActive(
               player,
               ApoliIds.INFINITE_TRADES,
               InfiniteTradesPower.Config.class,
               cfg -> cfg.bientityCondition().isEmpty() || ((BiEntityCondition)cfg.bientityCondition().get()).test(ctx)
            );
         } else {
            return false;
         }
      }
   }

   public static void resendOffers(class_3988 merchant) {
      if (merchant.method_8257() instanceof class_3222 player) {
         if (player.field_7512 instanceof class_1728 menu) {
            int level = merchant instanceof class_1646 villager ? villager.method_7231().method_16925() : 1;
            player.method_17354(menu.field_7763, merchant.method_8264(), level, merchant.method_19269(), merchant.method_19270(), merchant.method_20708());
         }
      }
   }

   public record Config(Optional<BiEntityCondition> bientityCondition) {
   }
}
