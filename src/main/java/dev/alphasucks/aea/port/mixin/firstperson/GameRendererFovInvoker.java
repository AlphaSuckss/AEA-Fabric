package dev.alphasucks.aea.port.mixin.firstperson;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4184;
import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_757.class)
@Environment(EnvType.CLIENT)
public interface GameRendererFovInvoker {
   @Invoker("method_3196")
   double apoli$getFov(class_4184 var1, float var2, boolean var3);
}
