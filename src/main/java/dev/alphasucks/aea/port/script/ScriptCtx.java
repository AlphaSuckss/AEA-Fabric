package dev.alphasucks.aea.port.script;

import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.power.PowerContainer;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import org.jetbrains.annotations.Nullable;

public final class ScriptCtx {
   private final class_1297 entity;
   private final class_1297 target;
   private final class_1937 level;
   private final class_2338 pos;
   private final class_1799 stack;
   private final class_2487 params;
   private final class_2960 power;

   private ScriptCtx(
      @Nullable class_1297 entity,
      @Nullable class_1297 target,
      @Nullable class_1937 level,
      @Nullable class_2338 pos,
      @Nullable class_1799 stack,
      class_2487 params,
      @Nullable class_2960 power
   ) {
      this.entity = entity;
      this.target = target;
      this.level = level;
      this.pos = pos;
      this.stack = stack;
      this.params = params;
      this.power = power;
   }

   public static ScriptCtx of(EntityCtx ctx, class_2487 params) {
      return new ScriptCtx(ctx.raw(), null, ctx.level(), null, null, params, null);
   }

   public static ScriptCtx of(BiEntityCtx ctx, class_2487 params) {
      return new ScriptCtx(ctx.rawActor(), ctx.rawTarget(), ctx.level(), null, null, params, null);
   }

   public static ScriptCtx of(BlockCtx ctx, class_2487 params) {
      return new ScriptCtx(null, null, ctx.level(), ctx.pos(), null, params, null);
   }

   public static ScriptCtx of(ItemCtx ctx, class_2487 params) {
      return new ScriptCtx(ctx.holder(), null, ctx.level(), null, ctx.stack(), params, null);
   }

   public static ScriptCtx ofPower(PowerContainer holder, class_2960 powerId, class_2487 params) {
      class_1297 owner = holder.rawOwner();
      return new ScriptCtx(owner, null, owner == null ? null : owner.method_37908(), null, null, params, powerId);
   }

   @Nullable
   public class_1297 getEntity() {
      return this.entity;
   }

   @Nullable
   public class_1309 getLiving() {
      return this.entity instanceof class_1309 living ? living : null;
   }

   @Nullable
   public class_1297 getTarget() {
      return this.target;
   }

   @Nullable
   public class_1937 getLevel() {
      return this.level;
   }

   @Nullable
   public class_3218 getServerLevel() {
      return this.level instanceof class_3218 server ? server : null;
   }

   @Nullable
   public class_2338 getPos() {
      return this.pos;
   }

   @Nullable
   public class_2680 getBlockState() {
      return this.level != null && this.pos != null ? this.level.method_8320(this.pos) : null;
   }

   @Nullable
   public class_1799 getStack() {
      return this.stack;
   }

   public class_2487 getParams() {
      return this.params;
   }

   @Nullable
   public class_2960 getPower() {
      return this.power;
   }

   @Nullable
   public PowerContainer getPowers() {
      return this.entity == null ? null : PowerContainer.of(this.entity);
   }

   public boolean isServer() {
      return this.level != null && !this.level.method_8608();
   }
}
