package dev.alphasucks.aea.port.client;

import dev.alphasucks.aea.port.tick.TickRates;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public final class ClientTickRates {
   private static final Int2IntOpenHashMap RATES = new Int2IntOpenHashMap();
   private static final Int2ObjectOpenHashMap<ClientTickRates.Lerp> LERPS = new Int2ObjectOpenHashMap();
   private static int baseRate = 20;
   private static long gateTick;

   private ClientTickRates() {
   }

   public static void clear() {
      RATES.clear();
      LERPS.clear();
      baseRate = 20;
      gateTick = 0L;
   }

   public static void set(int entityId, int rate, int base) {
      baseRate = Math.max(1, base);
      if (entityId < 0) {
         RATES.clear();
         LERPS.clear();
      } else {
         if (rate >= 0 && rate < baseRate) {
            RATES.put(entityId, Math.max(0, rate));
         } else {
            RATES.remove(entityId);
            LERPS.remove(entityId);
         }
      }
   }

   public static void beginClientTick(long gameTime) {
      gateTick = gameTime;
   }

   public static boolean idle() {
      return RATES.isEmpty();
   }

   private static class_1297 root(class_1297 entity) {
      return entity.method_5765() ? entity.method_5668() : entity;
   }

   public static int rateOf(class_1297 entity) {
      if (RATES.isEmpty()) {
         return -1;
      }

      int own = RATES.get(entity.method_5628());
      return own < 0 && entity.method_5765() ? RATES.get(entity.method_5668().method_5628()) : own;
   }

   private static int rateOfRoot(class_1297 root) {
      return RATES.isEmpty() ? -1 : RATES.get(root.method_5628());
   }

   public static void fadeHurt(class_1297 entity) {
      if (entity instanceof class_1309 living && living.field_6235 > 0 && frozen(entity)) {
         living.field_6235--;
      }
   }

   public static boolean frozen(class_1297 entity) {
      return rateOf(entity) == 0;
   }

   public static int baseRate() {
      return baseRate;
   }

   private static boolean gated(class_1297 root, int rate) {
      if (rate < 0) {
         return false;
      }

      class_746 self = class_310.method_1551().field_1724;
      return self == null || self == root || rate == 0 || root(self) != root;
   }

   public static boolean holdsPassenger(class_1297 passenger) {
      if (RATES.isEmpty()) {
         return false;
      } else if (passenger == class_310.method_1551().field_1724) {
         return false;
      } else {
         int rate = RATES.get(passenger.method_5628());
         if (rate < 0) {
            return false;
         } else {
            return rate == 0 ? true : !TickRates.gate(rate, baseRate, passenger.method_37908().method_8510());
         }
      }
   }

   public static boolean isGated(class_1297 entity) {
      class_1297 root = root(entity);
      return gated(root, rateOfRoot(root));
   }

   public static boolean simulatedOnClient(class_1297 entity) {
      return root(entity) instanceof class_1309;
   }

   public static boolean shouldTick(class_1297 entity) {
      class_1297 root = root(entity);
      int rate = rateOfRoot(root);
      if (!gated(root, rate)) {
         return true;
      } else {
         return rate == 0 ? false : TickRates.gate(rate, baseRate, entity.method_37908().method_8510());
      }
   }

   public static boolean slotDriven(class_1297 entity) {
      if (RATES.isEmpty()) {
         return false;
      }

      class_1297 root = root(entity);
      return root == class_310.method_1551().field_1724 && rateOfRoot(root) >= 0;
   }

   public static float slotPartial(class_1297 entity, float partial) {
      if (RATES.isEmpty()) {
         return partial;
      }

      if (!(root(entity) instanceof class_1309)) {
         return partial;
      }

      int rate = rateOf(entity);
      if (rate < 0 || rate >= baseRate) {
         return partial;
      }

      if (rate == 0) {
         return 0.0F;
      }

      long slot = Math.floorDiv(gateTick * rate, baseRate);
      long start = ceilDiv(slot * baseRate, rate);
      long span = ceilDiv((slot + 1L) * baseRate, rate) - start;
      return span <= 1L ? partial : class_3532.method_15363(((float)(gateTick - start) + partial) / (float)span, 0.0F, 1.0F);
   }

   public static float cameraPartial(float partial) {
      if (RATES.isEmpty()) {
         return partial;
      }

      class_1297 camera = class_310.method_1551().method_1560();
      return camera != null && slotDriven(camera) ? slotPartial(camera, partial) : partial;
   }

   private static long ceilDiv(long value, int divisor) {
      return -Math.floorDiv(-value, divisor);
   }

   public static boolean captureLerp(class_1297 entity, double x, double y, double z, float yRot, float xRot) {
      if (RATES.isEmpty()) {
         return false;
      }

      class_1297 root = root(entity);
      int rate = rateOfRoot(root);
      if (!gated(root, rate)) {
         LERPS.remove(entity.method_5628());
         return false;
      }

      ClientTickRates.Lerp lerp = (ClientTickRates.Lerp)LERPS.get(entity.method_5628());
      if (lerp == null) {
         lerp = new ClientTickRates.Lerp();
         LERPS.put(entity.method_5628(), lerp);
      }

      boolean rotationOnly = lerp.steps > 0
         && Math.abs(x - entity.method_23317()) < 1.0E-6
         && Math.abs(y - entity.method_23318()) < 1.0E-6
         && Math.abs(z - entity.method_23321()) < 1.0E-6;
      if (!rotationOnly) {
         lerp.x = x;
         lerp.y = y;
         lerp.z = z;
      }

      lerp.yRot = yRot;
      lerp.xRot = xRot;
      lerp.steps = rate <= 0 ? 1 : Math.max(1, (baseRate + rate - 1) / rate);
      return true;
   }

   public static int headLerpSteps(class_1297 entity, int steps) {
      if (RATES.isEmpty()) {
         return steps;
      }

      class_1297 root = root(entity);
      return gated(root, rateOfRoot(root)) ? 1 : steps;
   }

   public static float walkAnimationInput(class_1297 entity, float distance) {
      if (RATES.isEmpty()) {
         return distance;
      }

      class_1297 root = root(entity);
      int rate = rateOfRoot(root);
      return rate > 0 && gated(root, rate) ? distance * Math.max(1, (baseRate + rate - 1) / rate) : distance;
   }

   public static void advanceLerp(class_1297 entity) {
      if (!LERPS.isEmpty()) {
         int id = entity.method_5628();
         ClientTickRates.Lerp lerp = (ClientTickRates.Lerp)LERPS.get(id);
         if (lerp != null) {
            if (lerp.steps <= 0) {
               LERPS.remove(id);
            } else {
               double step = 1.0 / lerp.steps;
               entity.method_5814(
                  class_3532.method_16436(step, entity.method_23317(), lerp.x),
                  class_3532.method_16436(step, entity.method_23318(), lerp.y),
                  class_3532.method_16436(step, entity.method_23321(), lerp.z)
               );
               entity.method_36456(class_3532.method_17821((float)step, entity.method_36454(), lerp.yRot));
               entity.method_36457(class_3532.method_17821((float)step, entity.method_36455(), lerp.xRot));
               lerp.steps--;
               if (lerp.steps <= 0) {
                  LERPS.remove(id);
               }
            }
         }
      }
   }

   public static void keepRenderPose(class_1297 entity) {
      if (frozen(entity) || !slotDriven(entity)) {
         entity.method_22862();
      }

      keepViewBob(entity);
      List<class_1297> passengers = entity.method_5685();
      int i = 0;

      for (int n = passengers.size(); i < n; i++) {
         keepRenderPose((class_1297)passengers.get(i));
      }
   }

   public static void keepViewBob(class_1297 entity) {
      if (entity instanceof class_746 player) {
         if (class_310.method_1551().method_1560() == player) {
            player.field_3931 = player.field_3932;
            player.field_3914 = player.field_3916;
            player.field_3916 = player.field_3916 + (player.method_36455() - player.field_3916) * 0.5F;
            player.field_3932 = player.field_3932 + (player.method_36454() - player.field_3932) * 0.5F;
         }
      }
   }

   static {
      RATES.defaultReturnValue(-1);
   }

   private static final class Lerp {
      double x;
      double y;
      double z;
      float yRot;
      float xRot;
      int steps;
   }
}
