package dev.alphasucks.aea.port.data;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.dev.DevMode;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_5819;
import net.minecraft.class_6862;
import net.minecraft.class_7924;
import net.minecraft.class_8110;

public final class BodyHit {
   public static final int PROJECTILE = 0;
   public static final int MELEE = 1;
   public static final int POSITION = 2;
   public static final int TAGGED = 3;
   public static final int RANDOM = 4;
   private static final String[] METHODS = new String[]{"projectile", "melee", "position", "damage type tag", "random"};
   private static final double GRAZE = 16.0;
   private static final double GRAZE_BLOCKS = 0.0625;
   public static final class_6862<class_1299<?>> HUMANOID = class_6862.method_40092(class_7924.field_41266, Apoli.id("humanoid"));
   private static final BodyHit.Route[] ROUTES = new BodyHit.Route[]{
      route("head", BodyParts.HEAD),
      route("body", BodyParts.BODY),
      route("arms", BodyParts.ARMS),
      route("hands", BodyParts.HANDS),
      route("legs", BodyParts.LEGS),
      route("feet", BodyParts.FEET)
   };
   private static final ThreadLocal<BodyHit.Cache> CACHE = ThreadLocal.withInitial(BodyHit.Cache::new);
   private final int limb;
   private final float x;
   private final float y;
   private final float z;
   private final double normalX;
   private final double normalY;
   private final double normalZ;
   private final int method;
   private final boolean precise;

   private static BodyHit.Route route(String name, BodyPart part) {
      return new BodyHit.Route(class_6862.method_40092(class_7924.field_42534, Apoli.id("body_part/" + name)), part);
   }

   private BodyHit(int limb, double x, double y, double z, double normalX, double normalY, double normalZ, int method, boolean precise) {
      this.limb = limb;
      this.x = (float)x;
      this.y = (float)y;
      this.z = (float)z;
      this.normalX = normalX;
      this.normalY = normalY;
      this.normalZ = normalZ;
      this.method = method;
      this.precise = precise && method <= 2;
   }

   public int limb() {
      return this.limb;
   }

   public int method() {
      return this.method;
   }

   public boolean precise() {
      return this.precise;
   }

   public boolean in(BodyPart part, class_1309 target) {
      return part.sided(target).contains(this.limb, this.x, this.y, this.z);
   }

   public boolean within(float minX, float maxX, float minY, float maxY, float minZ, float maxZ) {
      return this.normalX >= minX && this.normalX <= maxX && this.normalY >= minY && this.normalY <= maxY && this.normalZ >= minZ && this.normalZ <= maxZ;
   }

   public static BodyHit resolve(class_1309 target, class_1282 source) {
      BodyHit.Cache cache = (BodyHit.Cache)CACHE.get();
      long time = target.method_37908().method_8510();
      if (cache.target == target && cache.source == source && cache.time == time) {
         return cache.hit;
      }

      BodyHit hit = compute(target, source);
      cache.target = target;
      cache.source = source;
      cache.time = time;
      cache.hit = hit;
      if (DevMode.any()) {
         DevMode.report(target, hit.describe(target));
      }

      return hit;
   }

   public static boolean isHumanoid(class_1309 entity) {
      return entity instanceof class_1657 || entity.method_5864().method_20210(HUMANOID);
   }

   private static BodyHit compute(class_1309 target, class_1282 source) {
      class_5819 random = target.method_6051();

      for (BodyHit.Route route : ROUTES) {
         if (source.method_48789(route.tag())) {
            return tagged(target, route.part(), 3, random);
         }
      }

      class_238 box = target.method_5829();
      class_243 centre = box.method_1005();
      double reach = box.method_17939() + box.method_17940() + box.method_17941();
      class_1297 direct = source.method_5526();
      if (direct == null || direct == target) {
         class_243 position = source.method_5510();
         return position != null
            ? towards(target, position.field_1352, position.field_1351, position.field_1350, centre.field_1352, centre.field_1351, centre.field_1350, reach, 2)
            : tagged(target, BodyParts.WHOLE, 4, random);
      } else if (direct instanceof class_1657 player) {
         class_243 eye = player.method_33571();
         class_243 look = player.method_5828(1.0F);
         double length = eye.method_1022(centre) + reach;
         return cast(target, eye.field_1352, eye.field_1351, eye.field_1350, look.field_1352 * length, look.field_1351 * length, look.field_1350 * length, 1);
      } else if (direct instanceof class_1309 living) {
         double originY = living.method_23318() + living.method_17682() * 0.55;
         double aimY = class_3532.method_15350(originY, box.field_1322, box.field_1325);
         return towards(target, living.method_23317(), originY, living.method_23321(), centre.field_1352, aimY, centre.field_1350, reach, 1);
      } else {
         class_243 origin = direct.method_19538();
         class_243 motion = direct.method_18798();
         double length = origin.method_1022(centre) + reach;
         double speed = motion.method_1033();
         if (speed > 0.001) {
            double scale = length / speed;
            return cast(
               target,
               origin.field_1352,
               origin.field_1351,
               origin.field_1350,
               motion.field_1352 * scale,
               motion.field_1351 * scale,
               motion.field_1350 * scale,
               0
            );
         } else {
            return towards(target, origin.field_1352, origin.field_1351, origin.field_1350, centre.field_1352, centre.field_1351, centre.field_1350, reach, 0);
         }
      }
   }

   private static BodyHit towards(class_1309 target, double ox, double oy, double oz, double tx, double ty, double tz, double reach, int method) {
      double dx = tx - ox;
      double dy = ty - oy;
      double dz = tz - oz;
      double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (length < 1.0E-6) {
         return cast(target, ox, oy + reach, oz, 0.0, -reach * 2.0, 0.0, method);
      }

      double scale = (length + reach) / length;
      return cast(target, ox, oy, oz, dx * scale, dy * scale, dz * scale, method);
   }

   private static BodyHit cast(class_1309 target, double ox, double oy, double oz, double dx, double dy, double dz, int method) {
      if (!isHumanoid(target)) {
         return castBox(target, ox, oy, oz, dx, dy, dz, method);
      }

      HumanoidPose pose = HumanoidPose.of(target);
      BodyFrame frame = BodyFrame.of(target);
      float[] poses = pose.poses();
      double[] scratch = new double[3];
      frame.worldToModel(ox - target.method_23317(), oy - target.method_23318(), oz - target.method_23321(), scratch);
      double modelX = scratch[0];
      double modelY = scratch[1];
      double modelZ = scratch[2];
      frame.worldDirectionToModel(dx, dy, dz, scratch);
      double modelDX = scratch[0];
      double modelDY = scratch[1];
      double modelDZ = scratch[2];
      int hitLimb = -1;
      double hitT = Double.POSITIVE_INFINITY;
      double hitX = 0.0;
      double hitY = 0.0;
      double hitZ = 0.0;
      int nearLimb = 1;
      double nearDistance = Double.POSITIVE_INFINITY;
      double nearX = 0.0;
      double nearY = 0.0;
      double nearZ = 0.0;

      for (int limb = 0; limb < 6; limb++) {
         int at = limb * 9;
         PoseMath.parentToLocal(poses, at, modelX, modelY, modelZ, scratch);
         double localX = scratch[0];
         double localY = scratch[1];
         double localZ = scratch[2];
         PoseMath.directionToLocal(poses, at, modelDX, modelDY, modelDZ, scratch);
         double localDX = scratch[0];
         double localDY = scratch[1];
         double localDZ = scratch[2];
         double t = slab(limb, localX, localY, localZ, localDX, localDY, localDZ);
         if (t >= 0.0 && t < hitT) {
            hitT = t;
            hitLimb = limb;
            hitX = localX + localDX * t;
            hitY = localY + localDY * t;
            hitZ = localZ + localDZ * t;
         }

         double centreX = (HumanoidPose.boxMin(limb, 0) + HumanoidPose.boxMax(limb, 0)) * 0.5;
         double centreY = (HumanoidPose.boxMin(limb, 1) + HumanoidPose.boxMax(limb, 1)) * 0.5;
         double centreZ = (HumanoidPose.boxMin(limb, 2) + HumanoidPose.boxMax(limb, 2)) * 0.5;
         double lengthSqr = localDX * localDX + localDY * localDY + localDZ * localDZ;
         double along = lengthSqr < 1.0E-12
            ? 0.0
            : class_3532.method_15350(((centreX - localX) * localDX + (centreY - localY) * localDY + (centreZ - localZ) * localDZ) / lengthSqr, 0.0, 1.0);
         double closestX = localX + localDX * along;
         double closestY = localY + localDY * along;
         double closestZ = localZ + localDZ * along;
         double clampedX = clamp(limb, 0, closestX);
         double clampedY = clamp(limb, 1, closestY);
         double clampedZ = clamp(limb, 2, closestZ);
         double distance = sqr(closestX - clampedX) + sqr(closestY - clampedY) + sqr(closestZ - clampedZ);
         if (distance < nearDistance) {
            nearDistance = distance;
            nearLimb = limb;
            nearX = clampedX;
            nearY = clampedY;
            nearZ = clampedZ;
         }
      }

      int limb = hitLimb >= 0 ? hitLimb : nearLimb;
      double localX = hitLimb >= 0 ? clamp(limb, 0, hitX) : nearX;
      double localY = hitLimb >= 0 ? clamp(limb, 1, hitY) : nearY;
      double localZ = hitLimb >= 0 ? clamp(limb, 2, hitZ) : nearZ;
      PoseMath.localToParent(poses, limb * 9, localX, localY, localZ, scratch);
      frame.modelToWorld(scratch[0], scratch[1], scratch[2], scratch);
      return normalised(
         target,
         limb,
         localX,
         localY,
         localZ,
         target.method_23317() + scratch[0],
         target.method_23318() + scratch[1],
         target.method_23321() + scratch[2],
         method,
         hitLimb >= 0 || nearDistance <= 16.0
      );
   }

   private static BodyHit castBox(class_1309 target, double ox, double oy, double oz, double dx, double dy, double dz, int method) {
      class_238 box = target.method_5829();
      class_243 from = new class_243(ox, oy, oz);
      Optional<class_243> clip = box.method_992(from, from.method_1031(dx, dy, dz));
      boolean touched = true;
      double px;
      double py;
      double pz;
      if (clip.isPresent()) {
         px = ((class_243)clip.get()).field_1352;
         py = ((class_243)clip.get()).field_1351;
         pz = ((class_243)clip.get()).field_1350;
      } else if (box.method_1006(from)) {
         px = ox;
         py = oy;
         pz = oz;
      } else {
         class_243 centre = box.method_1005();
         double lengthSqr = dx * dx + dy * dy + dz * dz;
         double along = lengthSqr < 1.0E-12
            ? 0.0
            : class_3532.method_15350(((centre.field_1352 - ox) * dx + (centre.field_1351 - oy) * dy + (centre.field_1350 - oz) * dz) / lengthSqr, 0.0, 1.0);
         double rayX = ox + dx * along;
         double rayY = oy + dy * along;
         double rayZ = oz + dz * along;
         px = class_3532.method_15350(rayX, box.field_1323, box.field_1320);
         py = class_3532.method_15350(rayY, box.field_1322, box.field_1325);
         pz = class_3532.method_15350(rayZ, box.field_1321, box.field_1324);
         touched = sqr(rayX - px) + sqr(rayY - py) + sqr(rayZ - pz) <= 0.0625;
      }

      double[] normal = new double[3];
      normalise(target, px, py, pz, normal);
      double modelX = normal[0] * 5.0;
      double modelY = normal[1] <= 0.5
         ? 24.0 - normal[1] * 24.0
         : (normal[1] <= 0.88 ? 12.0 - (normal[1] - 0.5) / 0.38 * 12.0 : -(normal[1] - 0.88) / 0.12 * 8.0);
      double modelZ = normal[2] * 2.0;
      float[] rest = HumanoidPose.REST.poses();
      double[] scratch = new double[3];
      int bestLimb = 1;
      double bestDistance = Double.POSITIVE_INFINITY;
      double bestX = 0.0;
      double bestY = 0.0;
      double bestZ = 0.0;

      for (int limb = 0; limb < 6; limb++) {
         PoseMath.parentToLocal(rest, limb * 9, modelX, modelY, modelZ, scratch);
         double clampedX = clamp(limb, 0, scratch[0]);
         double clampedY = clamp(limb, 1, scratch[1]);
         double clampedZ = clamp(limb, 2, scratch[2]);
         double distance = sqr(scratch[0] - clampedX) + sqr(scratch[1] - clampedY) + sqr(scratch[2] - clampedZ);
         if (distance < bestDistance) {
            bestDistance = distance;
            bestLimb = limb;
            bestX = clampedX;
            bestY = clampedY;
            bestZ = clampedZ;
         }
      }

      return new BodyHit(bestLimb, bestX, bestY, bestZ, normal[0], normal[1], normal[2], method, touched);
   }

   private static BodyHit tagged(class_1309 target, BodyPart part, int method, class_5819 random) {
      int limb = part.pickLimb(random);
      float[] local = new float[3];
      part.centreInto(limb, local);
      double[] scratch = new double[3];
      if (isHumanoid(target)) {
         HumanoidPose pose = HumanoidPose.of(target);
         PoseMath.localToParent(pose.poses(), limb * 9, local[0], local[1], local[2], scratch);
         BodyFrame.of(target).modelToWorld(scratch[0], scratch[1], scratch[2], scratch);
         return normalised(
            target,
            limb,
            local[0],
            local[1],
            local[2],
            target.method_23317() + scratch[0],
            target.method_23318() + scratch[1],
            target.method_23321() + scratch[2],
            method,
            false
         );
      } else {
         PoseMath.localToParent(HumanoidPose.REST.poses(), limb * 9, local[0], local[1], local[2], scratch);
         double modelY = scratch[1];
         double normalY = modelY >= 12.0 ? (24.0 - modelY) / 24.0 : (modelY >= 0.0 ? 0.5 + (12.0 - modelY) / 12.0 * 0.38 : 0.88 - modelY / 8.0 * 0.12);
         return new BodyHit(
            limb,
            local[0],
            local[1],
            local[2],
            class_3532.method_15350(scratch[0] / 5.0, -1.0, 1.0),
            class_3532.method_15350(normalY, 0.0, 1.0),
            class_3532.method_15350(scratch[2] / 2.0, -1.0, 1.0),
            method,
            false
         );
      }
   }

   private static BodyHit normalised(
      class_1309 target, int limb, double localX, double localY, double localZ, double worldX, double worldY, double worldZ, int method, boolean precise
   ) {
      double[] normal = new double[3];
      normalise(target, worldX, worldY, worldZ, normal);
      return new BodyHit(limb, localX, localY, localZ, normal[0], normal[1], normal[2], method, precise);
   }

   private static void normalise(class_1309 target, double px, double py, double pz, double[] out) {
      class_238 box = target.method_5829();
      double height = Math.max(box.field_1325 - box.field_1322, 0.001);
      double halfWidth = Math.max(target.method_17681() * 0.5, 0.001);
      double raw = class_3532.method_15350((class_3532.method_15350(py, box.field_1322, box.field_1325) - box.field_1322) / height, 0.0, 1.0);
      double headStart = class_3532.method_15350((target.method_23320() - box.field_1322) / height, 0.0, 0.99);
      double normalY = raw <= headStart ? (headStart > 1.0E-6 ? raw / headStart * 0.88 : 0.0) : 0.88 + (raw - headStart) / (1.0 - headStart) * 0.12;
      double yaw = Math.toRadians(target.field_6283);
      double forwardX = -Math.sin(yaw);
      double forwardZ = Math.cos(yaw);
      double offsetX = px - (box.field_1323 + box.field_1320) * 0.5;
      double offsetZ = pz - (box.field_1321 + box.field_1324) * 0.5;
      out[0] = class_3532.method_15350((offsetX * forwardZ - offsetZ * forwardX) / halfWidth, -1.0, 1.0);
      out[1] = class_3532.method_15350(normalY, 0.0, 1.0);
      out[2] = class_3532.method_15350(-(offsetX * forwardX + offsetZ * forwardZ) / halfWidth, -1.0, 1.0);
   }

   private static double slab(int limb, double ox, double oy, double oz, double dx, double dy, double dz) {
      double enter = 0.0;
      double exit = 1.0;

      for (int axis = 0; axis < 3; axis++) {
         double origin = axis == 0 ? ox : (axis == 1 ? oy : oz);
         double direction = axis == 0 ? dx : (axis == 1 ? dy : dz);
         double min = HumanoidPose.boxMin(limb, axis);
         double max = HumanoidPose.boxMax(limb, axis);
         if (Math.abs(direction) < 1.0E-9) {
            if (origin < min || origin > max) {
               return -1.0;
            }
         } else {
            double near = (min - origin) / direction;
            double far = (max - origin) / direction;
            if (near > far) {
               double swap = near;
               near = far;
               far = swap;
            }

            if (near > enter) {
               enter = near;
            }

            if (far < exit) {
               exit = far;
            }

            if (enter > exit) {
               return -1.0;
            }
         }
      }

      return enter;
   }

   private static double clamp(int limb, int axis, double value) {
      return class_3532.method_15350(value, HumanoidPose.boxMin(limb, axis), HumanoidPose.boxMax(limb, axis));
   }

   private static double sqr(double value) {
      return value * value;
   }

   private String describe(class_1309 target) {
      StringBuilder text = new StringBuilder("body part hit: ").append(HumanoidPose.nameOf(this.limb));
      List<BodyPart> parts = BodyParts.all();

      for (int i = 0; i < parts.size(); i++) {
         BodyPart part = (BodyPart)parts.get(i);
         if (part.models() == 0 && !part.isHanded() && !part.isGroup() && part.sided(target).contains(this.limb, this.x, this.y, this.z)) {
            text.append(", ").append(part.name());
         }
      }

      return text.append(" (")
         .append(METHODS[this.method])
         .append(this.precise ? "" : ", imprecise")
         .append(String.format(Locale.ROOT, ", x=%.2f y=%.2f z=%.2f)", new Object[]{this.normalX, this.normalY, this.normalZ}))
         .toString();
   }

   private static final class Cache {
      class_1309 target;
      class_1282 source;
      long time = Long.MIN_VALUE;
      BodyHit hit;
   }

   private record Route(class_6862<class_8110> tag, BodyPart part) {
   }
}
