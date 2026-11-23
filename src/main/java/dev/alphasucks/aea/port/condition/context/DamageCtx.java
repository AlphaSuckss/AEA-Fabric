package dev.alphasucks.aea.port.condition.context;

import net.minecraft.class_1282;
import net.minecraft.class_1309;
import net.minecraft.class_1937;

public record DamageCtx(class_1282 source, class_1309 target, class_1937 level, float amount) {
}
