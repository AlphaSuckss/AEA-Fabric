package dev.alphasucks.aea.port.compat.walkers;

import dev.alphasucks.aea.port.Apoli;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import org.jetbrains.annotations.Nullable;

public final class WalkersBridge {
   private static final String PLAYER_SHAPE = "tocraft.walkers.api.PlayerShape";
   private static final String PLAYER_ABILITIES = "tocraft.walkers.api.PlayerAbilities";
   private static final String ABILITY_REGISTRY = "tocraft.walkers.ability.AbilityRegistry";
   private static final MethodHandle GET_CURRENT_SHAPE;
   private static final MethodHandle UPDATE_SHAPES;
   private static final MethodHandle GET_COOLDOWN;
   private static final MethodHandle CAN_USE_ABILITY;
   private static final MethodHandle SET_COOLDOWN;
   private static final MethodHandle SYNC_ABILITY;
   private static final MethodHandle HAS_ABILITY;
   private static final boolean PRESENT;

   private WalkersBridge() {
   }

   private static boolean classExists(String name) {
      return WalkersBridge.class.getClassLoader().getResource(name.replace('.', '/') + ".class") != null;
   }

   public static boolean present() {
      return PRESENT;
   }

   @Nullable
   public static class_1309 currentShape(@Nullable class_1297 entity) {
      if (PRESENT && entity instanceof class_1657 player) {
         try {
            return (class_1309)GET_CURRENT_SHAPE.invoke(player);
         } catch (Throwable t) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static class_1297 shapeOrSelf(class_1297 entity) {
      class_1309 shape = currentShape(entity);
      return shape == null ? entity : shape;
   }

   public static boolean hasShapeAbility(@Nullable class_1297 entity) {
      if (PRESENT && entity instanceof class_1309 living) {
         if (shapeOrSelf(living) instanceof class_1309 shapeLiving) {
            try {
               return (boolean)HAS_ABILITY.invoke(shapeLiving);
            } catch (Throwable t) {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean canUseAbility(@Nullable class_1297 entity) {
      if (PRESENT && entity instanceof class_1657 player) {
         try {
            return (boolean)CAN_USE_ABILITY.invoke(player);
         } catch (Throwable t) {
            return false;
         }
      } else {
         return false;
      }
   }

   public static int abilityCooldown(@Nullable class_1297 entity) {
      if (PRESENT && entity instanceof class_1657 player) {
         try {
            return (int)GET_COOLDOWN.invoke(player);
         } catch (Throwable t) {
            return 0;
         }
      } else {
         return 0;
      }
   }

   public static void setAbilityCooldown(class_1297 entity, int cooldown) {
      if (PRESENT && entity instanceof class_1657 player) {
         try {
            SET_COOLDOWN.invoke(player, cooldown);
            if (player instanceof class_3222 serverPlayer) {
               SYNC_ABILITY.invoke(serverPlayer);
            }
         } catch (Throwable var4) {
         }
      }
   }

   public static boolean switchShape(class_1297 entity, @Nullable class_2960 shapeId, @Nullable class_2487 nbt) {
      if (PRESENT && entity instanceof class_3222 player) {
         if (player.method_37908() instanceof class_3218 level) {
            class_1309 var10 = null;
            if (shapeId != null) {
               class_2487 tag = nbt == null ? new class_2487() : nbt.method_10553();
               tag.method_10582("id", shapeId.toString());
               if (!(class_1299.method_17842(tag, level, e -> e) instanceof class_1309 living)) {
                  return false;
               }

               var10 = living;
            }

            try {
               return (boolean)UPDATE_SHAPES.invoke(player, var10);
            } catch (Throwable t) {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   static {
      MethodHandle getCurrentShape = null;
      MethodHandle updateShapes = null;
      MethodHandle getCooldown = null;
      MethodHandle canUseAbility = null;
      MethodHandle setCooldown = null;
      MethodHandle syncAbility = null;
      MethodHandle hasAbility = null;
      boolean present = false;
      if (classExists("tocraft.walkers.api.PlayerShape")) {
         try {
            Lookup lookup = MethodHandles.publicLookup();
            Class<?> shapeClass = Class.forName("tocraft.walkers.api.PlayerShape");
            Class<?> abilitiesClass = Class.forName("tocraft.walkers.api.PlayerAbilities");
            Class<?> registryClass = Class.forName("tocraft.walkers.ability.AbilityRegistry");
            getCurrentShape = lookup.findStatic(shapeClass, "getCurrentShape", MethodType.methodType(class_1309.class, class_1657.class));
            updateShapes = lookup.findStatic(shapeClass, "updateShapes", MethodType.methodType(boolean.class, class_3222.class, new Class[]{class_1309.class}));
            getCooldown = lookup.findStatic(abilitiesClass, "getCooldown", MethodType.methodType(int.class, class_1657.class));
            canUseAbility = lookup.findStatic(abilitiesClass, "canUseAbility", MethodType.methodType(boolean.class, class_1657.class));
            setCooldown = lookup.findStatic(abilitiesClass, "setCooldown", MethodType.methodType(void.class, class_1657.class, new Class[]{int.class}));
            syncAbility = lookup.findStatic(abilitiesClass, "sync", MethodType.methodType(void.class, class_3222.class));
            hasAbility = lookup.findStatic(registryClass, "has", MethodType.methodType(boolean.class, class_1309.class));
            present = true;
         } catch (Throwable t) {
            Apoli.LOGGER.warn("[Apoli] Walkers is installed but its API did not match; shape compat is disabled", t);
            present = false;
         }
      }

      GET_CURRENT_SHAPE = getCurrentShape;
      UPDATE_SHAPES = updateShapes;
      GET_COOLDOWN = getCooldown;
      CAN_USE_ABILITY = canUseAbility;
      SET_COOLDOWN = setCooldown;
      SYNC_ABILITY = syncAbility;
      HAS_ABILITY = hasAbility;
      PRESENT = present;
   }
}
