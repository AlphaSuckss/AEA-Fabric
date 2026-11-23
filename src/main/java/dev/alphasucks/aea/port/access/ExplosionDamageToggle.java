package dev.alphasucks.aea.port.access;

import java.util.function.Predicate;
import net.minecraft.class_1297;
import org.jetbrains.annotations.Nullable;

public interface ExplosionDamageToggle {
   void apoli$sparePassersby(@Nullable class_1297 var1);

   void apoli$setDamageFilter(@Nullable Predicate<class_1297> var1);
}
