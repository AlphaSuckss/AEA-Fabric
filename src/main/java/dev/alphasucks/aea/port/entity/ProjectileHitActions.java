package dev.alphasucks.aea.port.entity;

import dev.alphasucks.aea.port.power.builtin.FireProjectilePower.Config;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public interface ProjectileHitActions {
   void apoli$setFireConfig(Config var1);

   void apoli$setMaxRange(double var1);

   void apoli$setLifetime(int var1);

   void apoli$setFireCause(@Nullable class_1297 var1, @Nullable class_2960 var2);

   boolean apoli$keepsFlying();

   void apoli$caught();
}
