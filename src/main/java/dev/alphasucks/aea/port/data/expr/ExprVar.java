package dev.alphasucks.aea.port.data.expr;

import dev.alphasucks.aea.port.power.PowerContainer;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ExprVar {
   double get(@Nullable class_1297 var1, @Nullable PowerContainer var2, @Nullable class_1937 var3, double var4);
}
