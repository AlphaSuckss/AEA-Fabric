package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.data.ModelPartTimeline;
import dev.alphasucks.aea.port.data.ModelPartTimeline.Slot;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import org.jetbrains.annotations.Nullable;

public final class ModelPartAnimator {
   private static final ModelPartTimeline TIMELINE = new ModelPartTimeline();

   private ModelPartAnimator() {
   }

   public static List<Slot> update(@Nullable class_1309 entity) {
      return entity == null ? List.of() : TIMELINE.update(entity, now(entity));
   }

   public static boolean overridesPose(@Nullable class_1309 entity) {
      return entity == null ? false : TIMELINE.overridesPose(entity, now(entity));
   }

   public static float ageInTicks(@Nullable class_1297 entity) {
      return entity == null ? 0.0F : (float)now(entity);
   }

   public static ModelPartTimeline timeline() {
      return TIMELINE;
   }

   public static float partialTick() {
      return class_310.method_1551().method_1488();
   }

   public static double now(class_1297 entity) {
      return (double)entity.field_6012 + partialTick();
   }
}
