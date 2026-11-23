package dev.alphasucks.aea.port.mixin.firstperson;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_759;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_759.class)
@Environment(EnvType.CLIENT)
public interface ItemInHandRendererAccessor {
   @Accessor("field_4047")
   class_1799 apoli$mainHandItem();

   @Accessor("field_4048")
   class_1799 apoli$offHandItem();

   @Accessor("field_4043")
   float apoli$mainHandHeight();

   @Accessor("field_4053")
   float apoli$oMainHandHeight();

   @Accessor("field_4052")
   float apoli$offHandHeight();

   @Accessor("field_4051")
   float apoli$oOffHandHeight();
}
