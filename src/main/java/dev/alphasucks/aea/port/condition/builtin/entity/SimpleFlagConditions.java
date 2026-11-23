package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.entity.OfflinePlayers;
import dev.alphasucks.aea.port.shared.EmptyCfg;
import java.util.function.Predicate;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1321;
import net.minecraft.class_1656;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_1959;
import net.minecraft.class_2338;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;

public final class SimpleFlagConditions {
   private static long clientSkyTick = Long.MIN_VALUE;

   private SimpleFlagConditions() {
   }

   public static ConditionType<EntityCtx, EmptyCfg> climbing() {
      return new SimpleFlagConditions.FlagCondition(e -> e instanceof class_1309 le && le.method_6101());
   }

   public static ConditionType<EntityCtx, EmptyCfg> collidedHoriz() {
      return new SimpleFlagConditions.FlagCondition(e -> e.field_5976);
   }

   public static ConditionType<EntityCtx, EmptyCfg> creativeFly() {
      return new SimpleFlagConditions.FlagCondition(e -> e instanceof class_1657 p && p.method_31549().field_7479);
   }

   public static ConditionType<EntityCtx, EmptyCfg> daytime() {
      return new SimpleFlagConditions.FlagCondition(e -> syncedSky(e.method_37908()).method_8530());
   }

   public static ConditionType<EntityCtx, EmptyCfg> exists() {
      return new SimpleFlagConditions.FlagCondition(e -> e != null && !e.method_31481() && !OfflinePlayers.isSnapshot(e));
   }

   public static ConditionType<EntityCtx, EmptyCfg> exposedToSky() {
      return new SimpleFlagConditions.FlagCondition(e -> e.method_37908().method_8311(e.method_24515()));
   }

   public static ConditionType<EntityCtx, EmptyCfg> exposedToSun() {
      return new SimpleFlagConditions.FlagCondition(e -> {
         class_1937 level = syncedSky(e.method_37908());
         if (!level.method_8530()) {
            return false;
         } else {
            class_2338 pos = class_2338.method_49637(e.method_23317(), e.method_23320(), e.method_23321());
            if (!level.method_8311(pos)) {
               return false;
            } else {
               return level.method_8520(pos) ? false : e.method_5718() > 0.5F;
            }
         }
      });
   }

   public static class_1937 syncedSky(class_1937 level) {
      if (level.method_8608()) {
         long now = level.method_8510();
         if (now != clientSkyTick) {
            clientSkyTick = now;
            level.method_8533();
         }
      }

      return level;
   }

   public static ConditionType<EntityCtx, EmptyCfg> fallFlying() {
      return new SimpleFlagConditions.FlagCondition(e -> e instanceof class_1309 le && le.method_6128());
   }

   public static ConditionType<EntityCtx, EmptyCfg> glowing() {
      return new SimpleFlagConditions.FlagCondition(class_1297::method_5851);
   }

   public static ConditionType<EntityCtx, EmptyCfg> inRain() {
      return new SimpleFlagConditions.FlagCondition(e -> e.method_37908().method_8520(e.method_24515()));
   }

   public static ConditionType<EntityCtx, EmptyCfg> inSnow() {
      return new SimpleFlagConditions.FlagCondition(e -> {
         class_1937 level = e.method_37908();
         class_2338 pos = e.method_24515();
         return level.method_8419() && ((class_1959)level.method_23753(pos).comp_349()).method_33599(pos);
      });
   }

   public static ConditionType<EntityCtx, EmptyCfg> inThunder() {
      return new SimpleFlagConditions.FlagCondition(e -> e.method_37908().method_8546() && e.method_37908().method_8520(e.method_24515()));
   }

   public static ConditionType<EntityCtx, EmptyCfg> onFire() {
      return new SimpleFlagConditions.FlagCondition(class_1297::method_5809);
   }

   public static ConditionType<EntityCtx, EmptyCfg> sprinting() {
      return new SimpleFlagConditions.FlagCondition(class_1297::method_5624);
   }

   public static ConditionType<EntityCtx, EmptyCfg> swimming() {
      return new SimpleFlagConditions.FlagCondition(class_1297::method_5681);
   }

   public static ConditionType<EntityCtx, EmptyCfg> tamed() {
      return new SimpleFlagConditions.FlagCondition(e -> e instanceof class_1321 t && t.method_6181());
   }

   public static ConditionType<EntityCtx, EmptyCfg> usingEffectiveTool() {
      return new SimpleFlagConditions.FlagCondition(
         e -> {
            if (e instanceof class_1657 player) {
               return player.method_5745(4.5, 1.0F, false) instanceof class_3965 bhr && bhr.method_17783() == class_240.field_1332
                  ? player.method_7305(player.method_37908().method_8320(bhr.method_17777()))
                  : false;
            } else {
               return false;
            }
         }
      );
   }

   private static void keepImports(class_1656 a) {
   }

   private record FlagCondition(Predicate<class_1297> check) implements ConditionType<EntityCtx, EmptyCfg> {
      @Override
      public MapCodec<EmptyCfg> codec() {
         return MapCodec.unit(EmptyCfg.INSTANCE);
      }

      public boolean test(EmptyCfg cfg, EntityCtx ctx) {
         return this.check.test(ctx.entity());
      }
   }
}
