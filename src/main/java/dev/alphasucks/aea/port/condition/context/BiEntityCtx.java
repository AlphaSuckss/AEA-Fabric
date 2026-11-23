package dev.alphasucks.aea.port.condition.context;

import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1937;

public final class BiEntityCtx {
   private final class_1297 actor;
   private final class_1297 target;
   private final class_1937 level;

   public BiEntityCtx(class_1297 actor, class_1297 target, class_1937 level) {
      this.actor = actor;
      this.target = target;
      this.level = level;
   }

   public static BiEntityCtx of(class_1297 actor, class_1297 target, class_1937 level) {
      return new BiEntityCtx(actor, target, level);
   }

   public class_1297 actor() {
      return this.actor;
   }

   public class_1297 target() {
      return this.target;
   }

   public class_1309 livingActor() {
      return this.actor instanceof class_1309 le ? le : null;
   }

   public class_1309 livingTarget() {
      return this.target instanceof class_1309 le ? le : null;
   }

   public class_1937 level() {
      return this.level;
   }

   public class_1297 rawActor() {
      return this.actor;
   }

   public class_1297 rawTarget() {
      return this.target;
   }

   public EntityCtx asActor() {
      return new EntityCtx(this.actor, this.level);
   }

   public EntityCtx asTarget() {
      return new EntityCtx(this.target, this.level);
   }

   public BiEntityCtx swap() {
      return new BiEntityCtx(this.target, this.actor, this.level);
   }
}
