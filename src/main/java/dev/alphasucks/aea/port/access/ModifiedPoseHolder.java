package dev.alphasucks.aea.port.access;

import dev.alphasucks.aea.port.data.ArmPoseReference;
import net.minecraft.class_1268;
import net.minecraft.class_4050;
import org.jetbrains.annotations.Nullable;

public interface ModifiedPoseHolder {
   @Nullable
   class_4050 apoli$getModifiedEntityPose();

   void apoli$setModifiedEntityPose(@Nullable class_4050 var1);

   @Nullable
   ArmPoseReference apoli$getModifiedArmPose(class_1268 var1);

   void apoli$setModifiedArmPose(@Nullable ArmPoseReference var1, @Nullable ArmPoseReference var2);

   @Nullable
   class_4050 apoli$getPreviousEntityPose();

   void apoli$setPreviousEntityPose(@Nullable class_4050 var1);

   int apoli$getPoseTicks();
}
