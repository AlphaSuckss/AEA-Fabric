package dev.alphasucks.aea.port.compat.walkers.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.compat.walkers.WalkersBridge;
import dev.alphasucks.aea.port.compat.walkers.power.ShapePowers;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Nbt;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public final class ShapeActions {
   private ShapeActions() {
   }

   public static final class ChangeShapeAbilityCooldown implements ActionType<EntityCtx, ShapeActions.ChangeShapeAbilityCooldown.Cfg> {
      @Override
      public MapCodec<ShapeActions.ChangeShapeAbilityCooldown.Cfg> codec() {
         return RecordCodecBuilder.mapCodec(
            i -> i.group(
                  ShapeActions.Operation.CODEC
                     .optionalFieldOf("operation", ShapeActions.Operation.ADD)
                     .forGetter(ShapeActions.ChangeShapeAbilityCooldown.Cfg::operation),
                  Codec.INT.fieldOf("change").forGetter(ShapeActions.ChangeShapeAbilityCooldown.Cfg::change)
               )
               .apply(i, ShapeActions.ChangeShapeAbilityCooldown.Cfg::new)
         );
      }

      public void run(ShapeActions.ChangeShapeAbilityCooldown.Cfg cfg, EntityCtx ctx) {
         class_1297 entity = ctx.raw();
         if (entity != null) {
            int current = WalkersBridge.abilityCooldown(entity);
            int next = cfg.operation == ShapeActions.Operation.SET ? cfg.change : current + cfg.change;
            if (next != current) {
               WalkersBridge.setAbilityCooldown(entity, Math.max(0, next));
            }
         }
      }

      public record Cfg(ShapeActions.Operation operation, int change) {
      }
   }

   public enum Operation {
      ADD,
      SET;

      public static final Codec<ShapeActions.Operation> CODEC = Codec.STRING
         .xmap(s -> valueOf(s.toUpperCase(Locale.ROOT)), o -> o.name().toLowerCase(Locale.ROOT));
   }

   public static final class ShapeAction implements ActionType<EntityCtx, ShapeActions.ShapeAction.Cfg> {
      @Override
      public MapCodec<ShapeActions.ShapeAction.Cfg> codec() {
         return RecordCodecBuilder.mapCodec(
            i -> i.group(BiEntityAction.CODEC.fieldOf("bientity_action").forGetter(ShapeActions.ShapeAction.Cfg::bientityAction))
               .apply(i, ShapeActions.ShapeAction.Cfg::new)
         );
      }

      public void run(ShapeActions.ShapeAction.Cfg cfg, EntityCtx ctx) {
         class_1297 entity = ctx.raw();
         if (entity != null && !ctx.level().method_8608()) {
            cfg.bientityAction.run(new BiEntityCtx(entity, WalkersBridge.shapeOrSelf(entity), ctx.level()));
         }
      }

      public record Cfg(BiEntityAction bientityAction) {
      }
   }

   public record SwitchCfg(Optional<class_2960> shape, Optional<Nbt> nbt, Optional<EntityAction> actionOnSuccess) {
      public static final MapCodec<ShapeActions.SwitchCfg> CODEC = RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.optionalFieldOf("shape").forGetter(ShapeActions.SwitchCfg::shape),
               Nbt.CODEC.optionalFieldOf("nbt").forGetter(ShapeActions.SwitchCfg::nbt),
               LoggedOptionalField.of("action_on_success", EntityAction.CODEC).forGetter(ShapeActions.SwitchCfg::actionOnSuccess)
            )
            .apply(i, ShapeActions.SwitchCfg::new)
      );
   }

   public static final class SwitchShape implements ActionType<EntityCtx, ShapeActions.SwitchCfg> {
      @Override
      public MapCodec<ShapeActions.SwitchCfg> codec() {
         return ShapeActions.SwitchCfg.CODEC;
      }

      public void run(ShapeActions.SwitchCfg cfg, EntityCtx ctx) {
         class_1297 entity = ctx.raw();
         if (entity != null) {
            class_2487 tag = (class_2487)cfg.nbt().map(Nbt::tag).orElse(null);
            if (WalkersBridge.switchShape(entity, (class_2960)cfg.shape().orElse(null), tag)) {
               cfg.actionOnSuccess().ifPresent(a -> a.run(ctx));
            }
         }
      }
   }

   public static final class SwitchShapeBiEntity implements ActionType<BiEntityCtx, ShapeActions.SwitchCfg> {
      @Override
      public MapCodec<ShapeActions.SwitchCfg> codec() {
         return ShapeActions.SwitchCfg.CODEC;
      }

      public void run(ShapeActions.SwitchCfg cfg, BiEntityCtx ctx) {
         class_1297 actor = ctx.rawActor();
         class_1297 target = ctx.rawTarget();
         if (actor != null) {
            class_2960 shapeId = (class_2960)cfg.shape().orElse(null);
            class_2487 tag = (class_2487)cfg.nbt().map(Nbt::tag).orElse(null);
            if (shapeId == null && target instanceof class_1309 living) {
               shapeId = class_7923.field_41177.method_10221(living.method_5864());
               if (tag == null) {
                  class_2487 copied = new class_2487();
                  living.method_5647(copied);
                  tag = copied;
               }
            }

            if (WalkersBridge.switchShape(actor, shapeId, tag)) {
               cfg.actionOnSuccess().ifPresent(a -> a.run(ctx.asActor()));
            }
         }
      }
   }

   public static final class UseShapeAbility implements ActionType<EntityCtx, ShapeActions.UseShapeAbility.Cfg> {
      @Override
      public MapCodec<ShapeActions.UseShapeAbility.Cfg> codec() {
         return RecordCodecBuilder.mapCodec(
            i -> i.group(
                  Codec.BOOL.optionalFieldOf("force", false).forGetter(ShapeActions.UseShapeAbility.Cfg::force),
                  Codec.BOOL.optionalFieldOf("apply_cooldown", true).forGetter(ShapeActions.UseShapeAbility.Cfg::applyCooldown)
               )
               .apply(i, ShapeActions.UseShapeAbility.Cfg::new)
         );
      }

      public void run(ShapeActions.UseShapeAbility.Cfg cfg, EntityCtx ctx) {
         class_1297 entity = ctx.raw();
         if (entity != null && !ctx.level().method_8608()) {
            ShapePowers.useAbility(entity, cfg.force, cfg.applyCooldown);
         }
      }

      public record Cfg(boolean force, boolean applyCooldown) {
      }
   }
}
