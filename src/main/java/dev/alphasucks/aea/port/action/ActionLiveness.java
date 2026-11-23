package dev.alphasucks.aea.port.action;

import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import java.util.function.Predicate;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import org.jetbrains.annotations.Nullable;

public final class ActionLiveness {
   private ActionLiveness() {
   }

   public static boolean alive(@Nullable class_1297 entity, @Nullable class_1937 level) {
      if (entity == null) {
         return true;
      } else {
         return entity.method_31481() ? false : level == null || entity.method_37908() == level;
      }
   }

   public static <CTX> Predicate<CTX> always() {
      return ctx -> true;
   }

   public static Predicate<EntityCtx> entity() {
      return ctx -> alive(ctx.entity(), ctx.level());
   }

   public static Predicate<BiEntityCtx> biEntity() {
      return ctx -> alive(ctx.actor(), ctx.level()) && alive(ctx.target(), ctx.level());
   }

   public static Predicate<ItemCtx> item() {
      return ctx -> alive(ctx.holder(), ctx.level());
   }
}
