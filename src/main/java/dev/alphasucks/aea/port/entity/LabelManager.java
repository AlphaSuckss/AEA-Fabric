package dev.alphasucks.aea.port.entity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class LabelManager {
   private static final Map<UUID, LabelManager.EntityLabels> LABELS = new ConcurrentHashMap();
   @Nullable
   private static BiConsumer<class_1297, Map<class_2960, class_2561>> broadcaster;

   private LabelManager() {
   }

   public static void setBroadcaster(BiConsumer<class_1297, Map<class_2960, class_2561>> sender) {
      broadcaster = sender;
   }

   public static boolean setText(class_1297 entity, class_2960 powerId, @Nullable class_2561 text) {
      if (text == null) {
         LabelManager.EntityLabels labels = (LabelManager.EntityLabels)LABELS.get(entity.method_5667());
         if (labels == null) {
            return false;
         }

         boolean removed = labels.texts.remove(powerId) != null;
         if (removed && labels.texts.isEmpty() && labels.chatName == null && labels.tabName == null) {
            LABELS.remove(entity.method_5667());
         }

         return removed;
      } else {
         LabelManager.EntityLabels labels = (LabelManager.EntityLabels)LABELS.computeIfAbsent(entity.method_5667(), k -> new LabelManager.EntityLabels());
         class_2561 previous = (class_2561)labels.texts.put(powerId, text);
         return previous == null || !previous.equals(text);
      }
   }

   public static boolean setNameOverrides(class_1297 entity, @Nullable class_2561 chatName, int chatPriority, @Nullable class_2561 tabName, int tabPriority) {
      LabelManager.EntityLabels labels = (LabelManager.EntityLabels)LABELS.get(entity.method_5667());
      if (labels == null) {
         if (chatName == null && tabName == null) {
            return false;
         }

         labels = (LabelManager.EntityLabels)LABELS.computeIfAbsent(entity.method_5667(), k -> new LabelManager.EntityLabels());
      }

      boolean changed = !Objects.equals(labels.chatName, chatName) || !Objects.equals(labels.tabName, tabName);
      labels.chatName = chatName;
      labels.chatPriority = chatPriority;
      labels.tabName = tabName;
      labels.tabPriority = tabPriority;
      if (labels.texts.isEmpty() && chatName == null && tabName == null) {
         LABELS.remove(entity.method_5667());
      }

      return changed;
   }

   public static void broadcast(class_1297 entity) {
      if (broadcaster != null) {
         LabelManager.EntityLabels labels = (LabelManager.EntityLabels)LABELS.get(entity.method_5667());
         broadcaster.accept(entity, labels == null ? Map.of() : labels.texts);
      }
   }

   public static Map<class_2960, class_2561> textsFor(UUID entity) {
      LabelManager.EntityLabels labels = (LabelManager.EntityLabels)LABELS.get(entity);
      return labels == null ? Map.of() : Collections.unmodifiableMap(labels.texts);
   }

   @Nullable
   public static class_2561 chatName(UUID entity) {
      LabelManager.EntityLabels labels = (LabelManager.EntityLabels)LABELS.get(entity);
      return labels == null ? null : labels.chatName;
   }

   @Nullable
   public static class_2561 tabName(UUID entity) {
      LabelManager.EntityLabels labels = (LabelManager.EntityLabels)LABELS.get(entity);
      return labels == null ? null : labels.tabName;
   }

   public static boolean isEmptyFor(UUID entity) {
      return !LABELS.containsKey(entity);
   }

   public static void onEntityGone(UUID entity) {
      LABELS.remove(entity);
   }

   public static final class EntityLabels {
      final Map<class_2960, class_2561> texts = new LinkedHashMap(4);
      @Nullable
      class_2561 chatName;
      @Nullable
      class_2561 tabName;
      int chatPriority = Integer.MIN_VALUE;
      int tabPriority = Integer.MIN_VALUE;
   }
}
