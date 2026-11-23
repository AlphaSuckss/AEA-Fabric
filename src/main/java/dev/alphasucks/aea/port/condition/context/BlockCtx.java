package dev.alphasucks.aea.port.condition.context;

import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import org.jetbrains.annotations.Nullable;

public record BlockCtx(class_2338 pos, class_2680 state, class_1937 level, @Nullable class_1297 actor, @Nullable class_243 hit) {
   public BlockCtx(class_2338 pos, class_2680 state, class_1937 level) {
      this(pos, state, level, null, null);
   }

   public BlockCtx(class_2338 pos, class_2680 state, class_1937 level, @Nullable class_1297 actor) {
      this(pos, state, level, actor, null);
   }

   public BlockCtx at(class_2338 pos, class_2680 state) {
      return new BlockCtx(pos, state, this.level, this.actor, null);
   }

   public BlockCtx withHit(@Nullable class_243 hit) {
      return new BlockCtx(this.pos, this.state, this.level, this.actor, hit);
   }
}
