package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.mixin.raid.RaidsAccessor;
import java.util.Optional;
import net.minecraft.class_1267;
import net.minecraft.class_1297;
import net.minecraft.class_174;
import net.minecraft.class_1928;
import net.minecraft.class_2338;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3468;
import net.minecraft.class_3532;
import net.minecraft.class_3765;
import net.minecraft.class_3767;
import net.minecraft.class_4156;
import net.minecraft.class_7473;
import net.minecraft.class_4153.class_4155;

public final class StartRaidAction implements ActionType<EntityCtx, StartRaidAction.Cfg> {
   private static final Expression ONE = Expression.constant(1.0);
   private static final int VILLAGE_SEARCH_RADIUS = 64;

   @Override
   public MapCodec<StartRaidAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Expression.INT_OR_EXPR.optionalFieldOf("omen_level", ONE).forGetter(StartRaidAction.Cfg::omenLevel),
               LoggedOptionalField.of("success_action", EntityAction.CODEC).forGetter(StartRaidAction.Cfg::successAction),
               LoggedOptionalField.of("fail_action", EntityAction.CODEC).forGetter(StartRaidAction.Cfg::failAction)
            )
            .apply(i, StartRaidAction.Cfg::new)
      );
   }

   public void run(StartRaidAction.Cfg cfg, EntityCtx ctx) {
      class_1297 entity = ctx.raw();
      if (entity != null && entity.method_37908() instanceof class_3218 level) {
         if (startOrExtend(level, entity, cfg.omenLevel)) {
            cfg.successAction.ifPresent(a -> a.run(ctx));
         } else {
            cfg.failAction.ifPresent(a -> a.run(ctx));
         }
      }
   }

   private static boolean startOrExtend(class_3218 level, class_1297 entity, Expression omenLevel) {
      if (!entity.method_7325()
         && level.method_8407() != class_1267.field_5801
         && !level.method_8450().method_8355(class_1928.field_19422)
         && level.method_8597().method_44221()) {
         class_2338 pos = entity.method_24515();
         if (!level.method_19500(pos)) {
            return false;
         }

         class_3767 raids = level.method_19495();
         RaidsAccessor access = (RaidsAccessor)raids;
         class_3765 raid = access.apoli$getOrCreateRaid(level, villageCenter(level, pos));
         int max = raid.method_16514();
         if (!raid.method_16832() && (!raid.method_16524() || raid.method_16493() < max)) {
            if (!raid.method_16524()) {
               access.apoli$raidMap().putIfAbsent(raid.method_16494(), raid);
            }

            int omen = class_3532.method_15340(omenLevel.evalInt(entity), 1, max);
            raid.method_35211(Math.min(raid.method_16493() + omen, max));
            if (entity instanceof class_3222 player && !raid.method_20021()) {
               player.method_7281(class_3468.field_19256);
               class_174.field_19251.method_9141(player);
            }

            raids.method_80();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static class_2338 villageCenter(class_3218 level, class_2338 pos) {
      double x = 0.0;
      double y = 0.0;
      double z = 0.0;
      int count = 0;

      for (class_4156 record : level.method_19494()
         .method_19125(holder -> holder.method_40220(class_7473.field_39263), pos, 64, class_4155.field_18488)
         .toList()) {
         class_2338 poi = record.method_19141();
         x += poi.method_10263();
         y += poi.method_10264();
         z += poi.method_10260();
         count++;
      }

      if (count == 0) {
         return pos;
      }

      double scale = 1.0 / count;
      return class_2338.method_49637(x * scale, y * scale, z * scale);
   }

   public record Cfg(Expression omenLevel, Optional<EntityAction> successAction, Optional<EntityAction> failAction) {
   }
}
