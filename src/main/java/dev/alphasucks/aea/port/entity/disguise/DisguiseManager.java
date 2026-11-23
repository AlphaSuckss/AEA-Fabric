package dev.alphasucks.aea.port.entity.disguise;

import dev.alphasucks.aea.port.compat.walkers.WalkersBridge;
import dev.alphasucks.aea.port.power.builtin.ModifyLabelRenderPower;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_2487;
import net.minecraft.class_3222;
import net.minecraft.class_7923;
import org.jetbrains.annotations.Nullable;

public final class DisguiseManager {
   private static final Map<UUID, DisguiseData> DISGUISES = new ConcurrentHashMap();
   @Nullable
   private static BiConsumer<class_1297, Optional<DisguiseData>> broadcaster;
   @Nullable
   private static DisguiseManager.ClientView clientView;

   private DisguiseManager() {
   }

   public static void setBroadcaster(BiConsumer<class_1297, Optional<DisguiseData>> sender) {
      broadcaster = sender;
   }

   public static void setClientView(DisguiseManager.ClientView view) {
      clientView = view;
   }

   public static void apply(class_1297 actor, DisguiseData data, boolean overwrite) {
      if (!actor.method_37908().field_9236) {
         if (overwrite || !DISGUISES.containsKey(actor.method_5667())) {
            DISGUISES.put(actor.method_5667(), data);
            if (broadcaster != null) {
               broadcaster.accept(actor, Optional.of(data));
            }

            refreshTabName(actor);
         }
      }
   }

   public static void applyAs(class_1297 actor, class_1297 target, boolean overwrite) {
      applyAs(actor, target, overwrite, true);
   }

   public static void applyAs(class_1297 actor, class_1297 target, boolean overwrite, boolean changeName) {
      if (!actor.method_37908().field_9236) {
         if (overwrite || !DISGUISES.containsKey(actor.method_5667())) {
            Optional<UUID> playerUuid = target instanceof class_1657 ? Optional.of(target.method_5667()) : Optional.empty();
            class_2487 nbt = new class_2487();
            target.method_5647(nbt);
            String name = target.method_5477().getString();
            Optional<String> disguiseName = changeName ? Optional.ofNullable(name.isEmpty() ? null : name) : Optional.of("");
            apply(actor, new DisguiseData(class_7923.field_41177.method_10221(target.method_5864()), playerUuid, Optional.of(nbt), disguiseName), true);
         }
      }
   }

   public static void remove(class_1297 actor) {
      if (!actor.method_37908().field_9236) {
         if (DISGUISES.remove(actor.method_5667()) != null && broadcaster != null) {
            broadcaster.accept(actor, Optional.empty());
            refreshTabName(actor);
         }
      }
   }

   private static void refreshTabName(class_1297 actor) {
      if (actor instanceof class_3222 player) {
         ModifyLabelRenderPower.refreshTabName(player);
      }
   }

   public static void onPlayerLeave(UUID uuid) {
      DISGUISES.remove(uuid);
   }

   public static boolean isDisguised(@Nullable class_1297 entity) {
      if (entity == null) {
         return false;
      }

      if (entity.method_37908().field_9236) {
         if (clientView != null && clientView.isDisguised(entity.method_5628())) {
            return true;
         }
      } else if (DISGUISES.containsKey(entity.method_5667())) {
         return true;
      }

      return WalkersBridge.currentShape(entity) != null;
   }

   public static boolean isDisguisedAs(@Nullable class_1297 actor, @Nullable class_1297 target) {
      if (actor != null && target != null) {
         if (actor.method_37908().field_9236) {
            if (clientView != null && clientView.isDisguisedAs(actor.method_5628(), target)) {
               return true;
            }
         } else if (matches((DisguiseData)DISGUISES.get(actor.method_5667()), target)) {
            return true;
         }

         class_1309 shape = WalkersBridge.currentShape(actor);
         return shape != null && shape.method_5864() == target.method_5864();
      } else {
         return false;
      }
   }

   public static boolean matches(@Nullable DisguiseData data, class_1297 target) {
      if (data == null) {
         return false;
      } else {
         return target instanceof class_1657 && data.playerUuid().isPresent()
            ? ((UUID)data.playerUuid().get()).equals(target.method_5667())
            : data.entityTypeId().equals(class_7923.field_41177.method_10221(target.method_5864()));
      }
   }

   public static Map<UUID, DisguiseData> all() {
      return Collections.unmodifiableMap(DISGUISES);
   }

   @Nullable
   public static DisguiseData get(UUID uuid) {
      return (DisguiseData)DISGUISES.get(uuid);
   }

   public interface ClientView {
      boolean isDisguised(int var1);

      boolean isDisguisedAs(int var1, class_1297 var2);
   }
}
