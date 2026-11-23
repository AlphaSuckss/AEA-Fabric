package dev.alphasucks.aea.port.client.camera;

import dev.alphasucks.aea.port.client.ClientResolvedPowers;
import dev.alphasucks.aea.port.data.CameraAnimation;
import dev.alphasucks.aea.port.data.CameraLook;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.Space;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.builtin.ModifyCameraPower;
import dev.alphasucks.aea.port.power.builtin.ZoomPower;
import dev.alphasucks.aea.port.power.builtin.ModifyCameraPower.Config;
import dev.alphasucks.aea.port.power.builtin.ModifyCameraPower.Transform;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_4184;
import net.minecraft.class_5498;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class CameraController {
   private static final float DEG = (float) (Math.PI / 180.0);
   private static final double DETACH_DISTANCE_SQ = 0.0025;
   private static final float MIN_ZOOM = 0.05F;
   private static final float MAX_ZOOM = 100.0F;
   private static final CameraController.Result RESULT = new CameraController.Result();
   private static final Map<class_2960, CameraController.Snapshot> SNAPSHOTS = new HashMap();
   private static float fovScale = 1.0F;
   private static float roll;
   @Nullable
   private static class_1297 hiddenAnchor;
   private static boolean detached;
   private static boolean cinematic;
   private static float zoom = 1.0F;
   private static float zoomO = 1.0F;
   private static float zoomTarget = 1.0F;
   private static float zoomSpeed = 0.5F;
   private static boolean zoomScalesSensitivity;
   private static boolean zoomCinematic;
   private static boolean zoomHidesHand;

   private CameraController() {
   }

   @Nullable
   public static CameraController.Result compute(class_4184 camera, class_1297 entity, float partialTick) {
      fovScale = 1.0F;
      roll = 0.0F;
      hiddenAnchor = null;
      detached = false;
      cinematic = false;
      class_310 mc = class_310.method_1551();
      class_746 player = mc.field_1724;
      if (player != null && entity == player && !ClientResolvedPowers.isEmpty()) {
         PowerContainer container = PowerContainer.of(player);
         if (container != null && !container.isEmpty()) {
            List<class_2960> powers = container.powersOfType(ModifyCameraPower.CANONICAL);
            if (powers.isEmpty()) {
               return null;
            }

            class_2960 chosenId = null;
            Config chosen = null;
            int i = 0;

            for (int n = powers.size(); i < n; i++) {
               class_2960 powerId = (class_2960)powers.get(i);
               if (ClientResolvedPowers.isActive(powerId)) {
                  Power power = ApoliPowers.get(powerId);
                  if (power != null && power.config() instanceof Config cfg && (chosen == null || cfg.priority() > chosen.priority())) {
                     chosen = cfg;
                     chosenId = powerId;
                  }
               }
            }

            if (chosen == null) {
               return null;
            }

            float time = ClientResolvedPowers.age(chosenId, partialTick);
            CameraAnimation animation = chosen.animation();
            Transform transform = chosen.transform();
            float ox = channel(animation, 0, time, transform.x(), player);
            float oy = channel(animation, 1, time, transform.y(), player);
            float oz = channel(animation, 2, time, transform.z(), player);
            float pitchOffset = channel(animation, 3, time, transform.pitch(), player);
            float yawOffset = channel(animation, 4, time, transform.yaw(), player);
            float rollDegrees = channel(animation, 5, time, transform.roll(), player);
            float fov = channel(animation, 6, time, transform.fov(), player);
            class_1297 anchor = player;
            int targetId = ClientResolvedPowers.target(chosenId);
            if (chosen.targetsOther() && targetId >= 0 && mc.field_1687 != null) {
               class_1297 target = mc.field_1687.method_8469(targetId);
               if (target != null) {
                  anchor = target;
               }
            }

            CameraLook look = chosen.look();
            boolean ridesVanilla = anchor == player && chosen.follow() && (look == CameraLook.ANCHOR || look == CameraLook.HOLDER);
            double bx;
            double by;
            double bz;
            float baseYaw;
            float basePitch;
            if (ridesVanilla) {
               class_243 position = camera.method_19326();
               bx = position.field_1352;
               by = position.field_1351;
               bz = position.field_1350;
               baseYaw = camera.method_19330();
               basePitch = camera.method_19329();
            } else if (!chosen.follow()) {
               CameraController.Snapshot snapshot = (CameraController.Snapshot)SNAPSHOTS.get(chosenId);
               if (snapshot == null) {
                  snapshot = new CameraController.Snapshot(
                     eyeX(anchor, partialTick),
                     eyeY(anchor, partialTick),
                     eyeZ(anchor, partialTick),
                     anchor.method_5705(partialTick),
                     anchor.method_5695(partialTick)
                  );
                  SNAPSHOTS.put(chosenId, snapshot);
               }

               bx = snapshot.x();
               by = snapshot.y();
               bz = snapshot.z();
               baseYaw = snapshot.yaw();
               basePitch = snapshot.pitch();
            } else {
               bx = eyeX(anchor, partialTick);
               by = eyeY(anchor, partialTick);
               bz = eyeZ(anchor, partialTick);
               baseYaw = anchor.method_5705(partialTick);
               basePitch = anchor.method_5695(partialTick);
            }

            if (!ridesVanilla && look == CameraLook.HOLDER) {
               baseYaw = player.method_5705(partialTick);
               basePitch = player.method_5695(partialTick);
            }

            if (look == CameraLook.FIXED) {
               baseYaw = 0.0F;
               basePitch = 0.0F;
            }

            cinematic = !ridesVanilla;
            float frameYaw = baseYaw + yawOffset;
            float framePitch = basePitch + pitchOffset;
            Space space = transform.space();
            double wx;
            double wy;
            double wz;
            if (space == Space.WORLD) {
               wx = ox;
               wy = oy;
               wz = oz;
            } else if (space != Space.LOCAL && space != Space.LOCAL_HORIZONTAL && space != Space.LOCAL_HORIZONTAL_NORMALIZED) {
               class_243 global = space.toGlobal(anchor, new class_243(ox, oy, oz));
               wx = global.field_1352;
               wy = global.field_1351;
               wz = global.field_1350;
            } else {
               float framePitchUsed = space == Space.LOCAL ? framePitch : 0.0F;
               class_243 forward = class_243.method_1030(framePitchUsed, frameYaw);
               class_243 up = class_243.method_1030(framePitchUsed - 90.0F, frameYaw);
               class_243 left = up.method_1036(forward);
               wx = left.field_1352 * ox + up.field_1352 * oy + forward.field_1352 * oz;
               wy = left.field_1351 * ox + up.field_1351 * oy + forward.field_1351 * oz;
               wz = left.field_1350 * ox + up.field_1350 * oy + forward.field_1350 * oz;
            }

            if (chosen.collision() && (wx != 0.0 || wy != 0.0 || wz != 0.0) && mc.field_1687 != null) {
               double length = Math.sqrt(wx * wx + wy * wy + wz * wz);
               double allowed = clip(mc, anchor, bx, by, bz, wx / length, wy / length, wz / length, length);
               if (allowed < length) {
                  double scale = allowed / length;
                  wx *= scale;
                  wy *= scale;
                  wz *= scale;
               }
            }

            CameraController.Result result = RESULT;
            result.x = bx + wx;
            result.y = by + wy;
            result.z = bz + wz;
            result.yaw = frameYaw;
            result.pitch = framePitch;
            if (look.looksAt()) {
               class_1297 focus = (class_1297)(look == CameraLook.AT_HOLDER ? player : anchor);
               double dx = eyeX(focus, partialTick) - result.x;
               double dy = eyeY(focus, partialTick) - result.y;
               double dz = eyeZ(focus, partialTick) - result.z;
               double horizontal = Math.sqrt(dx * dx + dz * dz);
               if (horizontal > 1.0E-4 || Math.abs(dy) > 1.0E-4) {
                  result.yaw = (float)(class_3532.method_15349(dz, dx) * (180.0 / Math.PI)) - 90.0F;
                  result.pitch = (float)(-(class_3532.method_15349(dy, horizontal) * (180.0 / Math.PI)));
               }
            }

            result.roll = rollDegrees;
            result.ridesVanilla = ridesVanilla;
            roll = rollDegrees;
            double ex = eyeX(player, partialTick) - result.x;
            double ey = eyeY(player, partialTick) - result.y;
            double ez = eyeZ(player, partialTick) - result.z;
            result.detached = anchor != player || ex * ex + ey * ey + ez * ez > 0.0025;
            if (anchor != player && anchor.method_5829().method_1014(0.3).method_1008(result.x, result.y, result.z)) {
               hiddenAnchor = anchor;
            }

            detached = result.detached;
            fovScale = fov > 0.0F ? fov : 1.0F;
            return result;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static void clientTick(class_310 mc) {
      class_746 player = mc.field_1724;
      zoomO = zoom;
      float target = 1.0F;
      float speed = 0.5F;
      boolean scales = false;
      boolean smooth = false;
      boolean hides = false;
      if (player != null && !ClientResolvedPowers.isEmpty()) {
         PowerContainer container = PowerContainer.of(player);
         List<class_2960> zooms = container == null ? List.of() : container.powersOfType(ZoomPower.CANONICAL);
         int i = 0;

         for (int n = zooms.size(); i < n; i++) {
            class_2960 powerId = (class_2960)zooms.get(i);
            if (ClientResolvedPowers.isActive(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof dev.alphasucks.aea.port.power.builtin.ZoomPower.Config cfg) {
                  float amount = class_3532.method_15363((float)cfg.zoom().eval(player), 0.05F, 100.0F);
                  if (target == 1.0F || amount > target) {
                     target = amount;
                     speed = cfg.speed();
                     scales = cfg.scaleSensitivity();
                  }

                  smooth |= cfg.cinematic();
                  hides |= cfg.hideHand();
               }
            }
         }

         if (!SNAPSHOTS.isEmpty()) {
            SNAPSHOTS.keySet().removeIf(id -> !ClientResolvedPowers.isActive(id));
         }
      } else {
         SNAPSHOTS.clear();
      }

      zoomTarget = target;
      zoomSpeed = speed;
      zoomScalesSensitivity = scales;
      zoomCinematic = smooth;
      zoomHidesHand = hides;
      zoom = zoom + (zoomTarget - zoom) * zoomSpeed;
      if (Math.abs(zoom - zoomTarget) < 0.001F) {
         zoom = zoomTarget;
      }
   }

   public static double fov(double fov, float partialTick) {
      double result = fov * fovScale;
      float current = class_3532.method_16439(partialTick, zoomO, zoom);
      if (current != 1.0F) {
         result /= current;
      }

      return result;
   }

   public static float roll() {
      return roll;
   }

   public static boolean hides(class_1297 entity) {
      return entity == hiddenAnchor;
   }

   public static boolean isDetached() {
      return detached;
   }

   public static boolean hidesHand() {
      return detached || zoomHidesHand && zoom > 1.001F;
   }

   public static boolean suppressesBob() {
      return cinematic;
   }

   public static boolean smoothsMouse() {
      return zoomCinematic;
   }

   public static double sensitivity() {
      return zoomScalesSensitivity && zoom > 0.0F ? 1.0 / zoom : 1.0;
   }

   public static void setPerspective(class_310 mc, int perspective) {
      class_5498 next = switch (perspective) {
         case 1 -> class_5498.field_26665;
         case 2 -> class_5498.field_26666;
         default -> class_5498.field_26664;
      };
      class_5498 previous = mc.field_1690.method_31044();
      if (previous != next) {
         mc.field_1690.method_31043(next);
         if (previous.method_31034() != next.method_31034()) {
            mc.field_1773.method_3167(next.method_31034() ? mc.method_1560() : null);
         }

         mc.field_1769.method_3292();
      }
   }

   public static void reset() {
      SNAPSHOTS.clear();
      hiddenAnchor = null;
      fovScale = 1.0F;
      detached = false;
      cinematic = false;
      zoom = 1.0F;
      zoomO = 1.0F;
      zoomTarget = 1.0F;
      zoomScalesSensitivity = false;
      zoomCinematic = false;
      zoomHidesHand = false;
   }

   private static float channel(CameraAnimation animation, int channel, float time, Expression fallback, class_1297 holder) {
      return animation.has(channel) ? animation.sample(channel, time) : (float)fallback.eval(holder);
   }

   private static double clip(class_310 mc, class_1297 anchor, double bx, double by, double bz, double dx, double dy, double dz, double length) {
      double allowed = length;
      class_243 base = new class_243(bx, by, bz);

      for (int i = 0; i < 8; i++) {
         double cx = ((i & 1) * 2 - 1) * 0.1;
         double cy = ((i >> 1 & 1) * 2 - 1) * 0.1;
         double cz = ((i >> 2 & 1) * 2 - 1) * 0.1;
         class_243 from = new class_243(bx + cx, by + cy, bz + cz);
         class_243 to = new class_243(from.field_1352 + dx * length, from.field_1351 + dy * length, from.field_1350 + dz * length);
         class_239 hit = mc.field_1687.method_17742(new class_3959(from, to, class_3960.field_23142, class_242.field_1348, anchor));
         if (hit.method_17783() != class_240.field_1333) {
            double distance = hit.method_17784().method_1022(base);
            if (distance < allowed) {
               allowed = distance;
            }
         }
      }

      return allowed;
   }

   private static double eyeX(class_1297 entity, float partialTick) {
      return class_3532.method_16436(partialTick, entity.field_6014, entity.method_23317());
   }

   private static double eyeY(class_1297 entity, float partialTick) {
      return class_3532.method_16436(partialTick, entity.field_6036, entity.method_23318()) + entity.method_5751();
   }

   private static double eyeZ(class_1297 entity, float partialTick) {
      return class_3532.method_16436(partialTick, entity.field_5969, entity.method_23321());
   }

   public static final class Result {
      public double x;
      public double y;
      public double z;
      public float yaw;
      public float pitch;
      public float roll;
      public boolean detached;
      public boolean ridesVanilla;
   }

   private record Snapshot(double x, double y, double z, float yaw, float pitch) {
   }
}
