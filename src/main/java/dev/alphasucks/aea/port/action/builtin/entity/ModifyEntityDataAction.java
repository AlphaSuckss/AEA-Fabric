package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_3542;

public final class ModifyEntityDataAction implements ActionType<EntityCtx, ModifyEntityDataAction.Cfg> {
   private static final Codec<Either<Boolean, Double>> VALUE_CODEC = Codec.either(Codec.BOOL, Codec.DOUBLE);

   @Override
   public MapCodec<ModifyEntityDataAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               ModifyEntityDataAction.DataKey.CODEC.fieldOf("data").forGetter(ModifyEntityDataAction.Cfg::data),
               VALUE_CODEC.optionalFieldOf("value").forGetter(ModifyEntityDataAction.Cfg::value)
            )
            .apply(i, ModifyEntityDataAction.Cfg::new)
      );
   }

   public void run(ModifyEntityDataAction.Cfg cfg, EntityCtx ctx) {
      class_1297 entity = ctx.raw();
      if (entity != null) {
         Optional<Boolean> bool = cfg.value.flatMap(either -> either.left());
         Optional<Double> number = cfg.value.flatMap(either -> either.right());
         switch (cfg.data) {
            case NO_GRAVITY:
               entity.method_5875((Boolean)bool.orElse(!entity.method_5740()));
               break;
            case INVULNERABLE:
               entity.method_5684((Boolean)bool.orElse(!entity.method_5655()));
               break;
            case SILENT:
               entity.method_5803((Boolean)bool.orElse(!entity.method_5701()));
               break;
            case GLOWING:
               entity.method_5834((Boolean)bool.orElse(!entity.method_36361()));
               break;
            case INVISIBLE:
               entity.method_5648((Boolean)bool.orElse(!entity.method_5767()));
               break;
            case CUSTOM_NAME_VISIBLE:
               entity.method_5880((Boolean)bool.orElse(!entity.method_5807()));
               break;
            case NO_AI:
               if (entity instanceof class_1308 mob) {
                  mob.method_5977((Boolean)bool.orElse(!mob.method_5987()));
               }
               break;
            case FALL_DISTANCE:
               number.ifPresent(d -> entity.field_6017 = d.floatValue());
               break;
            case AIR:
               number.ifPresent(d -> entity.method_5855(d.intValue()));
               break;
            case FIRE_TICKS:
               number.ifPresent(d -> entity.method_20803(d.intValue()));
         }
      }
   }

   public record Cfg(ModifyEntityDataAction.DataKey data, Optional<Either<Boolean, Double>> value) {
   }

   public enum DataKey implements class_3542 {
      NO_GRAVITY("no_gravity"),
      INVULNERABLE("invulnerable"),
      SILENT("silent"),
      GLOWING("glowing"),
      INVISIBLE("invisible"),
      CUSTOM_NAME_VISIBLE("custom_name_visible"),
      NO_AI("no_ai"),
      FALL_DISTANCE("fall_distance"),
      AIR("air"),
      FIRE_TICKS("fire_ticks");

      public static final Codec<ModifyEntityDataAction.DataKey> CODEC = class_3542.method_28140(ModifyEntityDataAction.DataKey::values);
      private final String name;

      DataKey(String name) {
         this.name = name;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }
}
