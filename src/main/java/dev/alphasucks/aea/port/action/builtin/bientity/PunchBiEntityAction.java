package dev.alphasucks.aea.port.action.builtin.bientity;

import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.data.Hand;
import dev.alphasucks.aea.port.data.ItemStackData;
import dev.alphasucks.aea.port.mixin.damage.LivingEntityAttackStrengthAccessor;
import java.util.Optional;
import net.minecraft.class_1268;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1310;
import net.minecraft.class_1320;
import net.minecraft.class_1322;
import net.minecraft.class_1324;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1890;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_5134;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_7924;
import net.minecraft.class_8110;
import net.minecraft.class_6880.class_6883;

public final class PunchBiEntityAction implements ActionType<BiEntityCtx, PunchBiEntityAction.Cfg> {
   @Override
   public MapCodec<PunchBiEntityAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.strict("stack", ItemStackData.CODEC).forGetter(PunchBiEntityAction.Cfg::stack),
               LoggedOptionalField.strict("damage_type", IdCodecs.ID).forGetter(PunchBiEntityAction.Cfg::damageType),
               Hand.CODEC.optionalFieldOf("hand", Hand.MAIN_HAND).forGetter(PunchBiEntityAction.Cfg::hand),
               Codec.BOOL.optionalFieldOf("swing_hand", true).forGetter(PunchBiEntityAction.Cfg::swingHand),
               Codec.BOOL.optionalFieldOf("reset_cooldown", true).forGetter(PunchBiEntityAction.Cfg::resetCooldown),
               Codec.BOOL.optionalFieldOf("ignore_cooldown", false).forGetter(PunchBiEntityAction.Cfg::ignoreCooldown)
            )
            .apply(i, PunchBiEntityAction.Cfg::new)
      );
   }

   public void run(PunchBiEntityAction.Cfg cfg, BiEntityCtx ctx) {
      class_1297 target = ctx.target();
      class_1309 actor = ctx.livingActor();
      if (target != null && actor != null) {
         if (ctx.level() instanceof class_3218 level) {
            class_1799 substitute = (class_1799)cfg.stack.map(ItemStackData::stack).map(class_1799::method_7972).orElse(null);
            if (substitute == null && cfg.hand == Hand.OFF_HAND) {
               substitute = actor.method_5998(class_1268.field_5810);
            }

            class_1799 original = null;
            if (substitute != null) {
               original = actor.method_5998(class_1268.field_5808);
               hold(actor, original, substitute);
            }

            LivingEntityAttackStrengthAccessor cooldown = null;
            int restoreTicker = 0;
            if (actor instanceof class_1657 player && (cfg.ignoreCooldown || !cfg.resetCooldown)) {
               cooldown = (LivingEntityAttackStrengthAccessor)actor;
               restoreTicker = cooldown.apoli$getAttackStrengthTicker();
               if (cfg.ignoreCooldown) {
                  cooldown.apoli$setAttackStrengthTicker((int)Math.ceil(player.method_7279()) + 1);
               }
            }

            try {
               if (cfg.damageType.isPresent()) {
                  typed((class_2960)cfg.damageType.get(), actor, target, level);
               } else if (actor instanceof class_1657 player) {
                  player.method_7324(target);
               } else if (actor instanceof class_1308 mob && mob.method_5996(class_5134.field_23721) != null) {
                  mob.method_6121(target);
               } else {
                  hurt(actor, target, actor.method_48923().method_48812(actor));
               }

               if (cfg.swingHand) {
                  actor.method_23667(cfg.hand.vanilla(), true);
               }
            } finally {
               if (cooldown != null && !cfg.resetCooldown) {
                  cooldown.apoli$setAttackStrengthTicker(restoreTicker);
               }

               if (substitute != null) {
                  hold(actor, substitute, original);
               }
            }
         }
      }
   }

   private static void hold(class_1309 actor, class_1799 previous, class_1799 next) {
      Multimap<class_1320, class_1322> removed = previous.method_7926(class_1304.field_6173);
      if (!removed.isEmpty()) {
         actor.method_6127().method_26847(removed);
      }

      actor.method_6122(class_1268.field_5808, next);
      Multimap<class_1320, class_1322> added = next.method_7926(class_1304.field_6173);
      if (!added.isEmpty()) {
         actor.method_6127().method_26854(added);
      }
   }

   private static void typed(class_2960 damageType, class_1309 actor, class_1297 target, class_3218 level) {
      class_5321<class_8110> key = class_5321.method_29179(class_7924.field_42534, damageType);
      Optional<class_6883<class_8110>> holder = level.method_30349().method_30530(class_7924.field_42534).method_40264(key);
      if (!holder.isEmpty()) {
         hurt(actor, target, new class_1282((class_6880<class_8110>)holder.get(), actor));
      }
   }

   private static void hurt(class_1309 actor, class_1297 target, class_1282 source) {
      float base = (float)attackDamage(actor);
      float charge = actor instanceof class_1657 player ? player.method_7261(0.5F) : 1.0F;
      class_1310 mobType = target instanceof class_1309 living ? living.method_6046() : class_1310.field_6290;
      float enchanted = class_1890.method_8218(actor.method_6047(), mobType);
      float amount = base * (0.2F + charge * charge * 0.8F) + enchanted * charge;
      if (actor instanceof class_1657 player) {
         player.method_7350();
      }

      if (amount > 0.0F) {
         target.method_5643(source, amount);
      }
   }

   private static double attackDamage(class_1309 actor) {
      class_1324 own = actor.method_5996(class_5134.field_23721);
      if (own != null) {
         return own.method_6194();
      }

      class_1324 detached = new class_1324(class_5134.field_23721, instance -> {});

      for (class_1322 modifier : actor.method_6047().method_7926(class_1304.field_6173).get(class_5134.field_23721)) {
         if (!detached.method_6196(modifier)) {
            detached.method_26835(modifier);
         }
      }

      return detached.method_6194();
   }

   public record Cfg(
      Optional<ItemStackData> stack, Optional<class_2960> damageType, Hand hand, boolean swingHand, boolean resetCooldown, boolean ignoreCooldown
   ) {
   }
}
