package dev.alphasucks.aea.port.compat.walkers.power;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.compat.walkers.WalkersBridge;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class ShapePowers {
   public static final class_2960 ACTION_ON_SHAPE_CHANGE = Apoli.id("action_on_shape_change");
   public static final class_2960 ACTION_ON_SHAPE_ABILITY_USE = Apoli.id("action_on_shape_ability_use");
   public static final class_2960 PREVENT_SHAPE_CHANGE = Apoli.id("prevent_shape_change");
   public static final class_2960 PREVENT_SHAPE_ABILITY_USE = Apoli.id("prevent_shape_ability_use");
   private static final MapCodec<ShapePowers.ActionCfg> ACTION_CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            BiEntityAction.CODEC.fieldOf("bientity_action").forGetter(ShapePowers.ActionCfg::bientityAction),
            LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ShapePowers.ActionCfg::bientityCondition)
         )
         .apply(i, ShapePowers.ActionCfg::new)
   );
   private static final MapCodec<ShapePowers.PreventCfg> PREVENT_CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ShapePowers.PreventCfg::bientityCondition))
         .apply(i, ShapePowers.PreventCfg::new)
   );

   private ShapePowers() {
   }

   public static boolean preventShapeChange(class_1297 player, @Nullable class_1297 shape) {
      return prevented(player, shape == null ? player : shape, PREVENT_SHAPE_CHANGE);
   }

   public static boolean preventShapeAbilityUse(class_1297 player) {
      return prevented(player, WalkersBridge.shapeOrSelf(player), PREVENT_SHAPE_ABILITY_USE);
   }

   private static boolean prevented(class_1297 player, class_1297 shape, class_2960 typeId) {
      if (player == null) {
         return false;
      }

      BiEntityCtx ctx = new BiEntityCtx(player, shape, player.method_37908());
      boolean[] hit = new boolean[]{false};
      PowerLookup.forEach(player, typeId, ShapePowers.PreventCfg.class, cfg -> {
         if (!hit[0]) {
            if (cfg.bientityCondition().isEmpty() || ((BiEntityCondition)cfg.bientityCondition().get()).test(ctx)) {
               hit[0] = true;
            }
         }
      });
      return hit[0];
   }

   public static void fireShapeChange(class_1297 player, @Nullable class_1297 shape) {
      fire(player, shape == null ? player : shape, ACTION_ON_SHAPE_CHANGE);
   }

   public static void fireShapeAbilityUse(class_1297 player) {
      fire(player, WalkersBridge.shapeOrSelf(player), ACTION_ON_SHAPE_ABILITY_USE);
   }

   private static void fire(class_1297 player, class_1297 shape, class_2960 typeId) {
      if (player != null && !player.method_37908().method_8608()) {
         BiEntityCtx ctx = new BiEntityCtx(player, shape, player.method_37908());
         PowerLookup.forEach(player, typeId, ShapePowers.ActionCfg.class, cfg -> {
            if (!cfg.bientityCondition().isPresent() || ((BiEntityCondition)cfg.bientityCondition().get()).test(ctx)) {
               cfg.bientityAction().run(ctx);
            }
         });
      }
   }

   public static void useAbility(class_1297 entity, boolean force, boolean applyCooldown) {
      if (WalkersBridge.present() && WalkersBridge.hasShapeAbility(entity)) {
         int before = WalkersBridge.abilityCooldown(entity);
         if (force && before > 0) {
            WalkersBridge.setAbilityCooldown(entity, 0);
         }

         WalkersAbilityInvoker.invoke(entity);
         if (!applyCooldown) {
            WalkersBridge.setAbilityCooldown(entity, force ? 0 : before);
         }
      }
   }

   public record ActionCfg(BiEntityAction bientityAction, Optional<BiEntityCondition> bientityCondition) {
   }

   public static final class ActionOnShapeAbilityUse extends PowerType<ShapePowers.ActionCfg> {
      @Override
      public MapCodec<ShapePowers.ActionCfg> configCodec() {
         return ShapePowers.ACTION_CODEC;
      }
   }

   public static final class ActionOnShapeChange extends PowerType<ShapePowers.ActionCfg> {
      @Override
      public MapCodec<ShapePowers.ActionCfg> configCodec() {
         return ShapePowers.ACTION_CODEC;
      }
   }

   public record PreventCfg(Optional<BiEntityCondition> bientityCondition) {
   }

   public static final class PreventShapeAbilityUse extends PowerType<ShapePowers.PreventCfg> {
      @Override
      public MapCodec<ShapePowers.PreventCfg> configCodec() {
         return ShapePowers.PREVENT_CODEC;
      }
   }

   public static final class PreventShapeChange extends PowerType<ShapePowers.PreventCfg> {
      @Override
      public MapCodec<ShapePowers.PreventCfg> configCodec() {
         return ShapePowers.PREVENT_CODEC;
      }
   }
}
