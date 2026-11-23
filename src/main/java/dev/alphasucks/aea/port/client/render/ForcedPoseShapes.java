package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.data.EntityPoses;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_3532;
import net.minecraft.class_4050;
import net.minecraft.class_572;

@Environment(EnvType.CLIENT)
public final class ForcedPoseShapes {
   private static final float[] LIMBS = new float[54];

   private ForcedPoseShapes() {
   }

   public static void apply(class_572<?> model, class_1309 entity, class_4050 pose, float ageInTicks) {
      ModelPartLookup.limbPosesInto(model, LIMBS);
      float progress = EntityPoses.progress(entity, class_3532.method_15363(ageInTicks - entity.field_6012, 0.0F, 1.0F));
      EntityPoses.shape(pose, LIMBS, progress, ageInTicks);
      ModelPartLookup.write(LIMBS, 0, model.field_3398);
      ModelPartLookup.write(LIMBS, 9, model.field_3391);
      ModelPartLookup.write(LIMBS, 18, model.field_3401);
      ModelPartLookup.write(LIMBS, 27, model.field_27433);
      ModelPartLookup.write(LIMBS, 36, model.field_3392);
      ModelPartLookup.write(LIMBS, 45, model.field_3397);
      model.field_3394.method_17138(model.field_3398);
   }
}
