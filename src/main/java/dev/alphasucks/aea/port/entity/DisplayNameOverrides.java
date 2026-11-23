package dev.alphasucks.aea.port.entity;

import dev.alphasucks.aea.port.entity.disguise.DisguiseData;
import dev.alphasucks.aea.port.entity.disguise.DisguiseManager;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_2561;
import net.minecraft.class_7923;
import org.jetbrains.annotations.Nullable;

public final class DisplayNameOverrides {
   private DisplayNameOverrides() {
   }

   @Nullable
   public static class_2561 chatNameFor(@Nullable class_1297 entity) {
      if (entity != null && !entity.method_37908().method_8608()) {
         class_2561 label = LabelManager.chatName(entity.method_5667());
         return label != null ? label : disguiseName(entity);
      } else {
         return null;
      }
   }

   @Nullable
   public static class_2561 tabNameFor(class_1297 player) {
      if (player.method_37908().method_8608()) {
         return null;
      }

      class_2561 label = LabelManager.tabName(player.method_5667());
      return label != null ? label : disguiseName(player);
   }

   @Nullable
   private static class_2561 disguiseName(class_1297 entity) {
      DisguiseData data = DisguiseManager.get(entity.method_5667());
      if (data == null) {
         return null;
      }

      if (data.name().isPresent()) {
         String name = (String)data.name().get();
         return name.isEmpty() ? null : class_2561.method_43470(name);
      }

      if (data.isPlayerDisguise()) {
         return null;
      }

      class_1299<?> type = class_7923.field_41177.method_10223(data.entityTypeId());
      return type != null ? class_2561.method_43471(type.method_5882()) : null;
   }
}
