package dev.alphasucks.aea.port.entity.summon;

import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public interface Temporary {
   void setMaxLifeTime(int var1);

   int getMaxLifeTime();

   int getRemainingLifeTime();

   @Nullable
   class_2960 getSummonId();
}
