package dev.alphasucks.aea.port.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2394;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_3218;
import net.minecraft.class_5819;
import org.jetbrains.annotations.Nullable;

public final class ParticlePlacement {
   public static final Codec<Either<Expression, ExprVector>> SPEED_CODEC = Codec.either(Expression.FLOAT_OR_EXPR, ExprVector.CODEC);
   public static final Either<Expression, ExprVector> NO_SPEED = Either.left(Expression.constant(0.0));
   private static final int MAX_DIRECTED_PACKETS = 64;

   private ParticlePlacement() {
   }

   @Nullable
   public static ModelPartAnchor.Frame frame(class_1297 entity, Optional<BodyPart> modelPart) {
      return modelPart.isPresent() ? ModelPartAnchor.frameOf(entity, (BodyPart)modelPart.get()) : null;
   }

   public static class_243 origin(class_1297 entity, @Nullable ModelPartAnchor.Frame frame, Optional<Space> space, float offsetX, float offsetY, float offsetZ) {
      return origin(entity.method_19538(), entity, frame, space, offsetX, offsetY, offsetZ);
   }

   public static class_243 origin(
      class_243 base, class_1297 entity, @Nullable ModelPartAnchor.Frame frame, Optional<Space> space, float offsetX, float offsetY, float offsetZ
   ) {
      class_243 offset = new class_243(offsetX, offsetY, offsetZ);
      if (frame == null) {
         return base.method_1019(((Space)space.orElse(Space.WORLD)).toGlobal(entity, offset));
      }

      class_243 at = base.method_1019(frame.pivot());
      return at.method_1019(space.isPresent() ? ((Space)space.get()).toGlobal(entity, offset) : frame.direction(offset));
   }

   @Nullable
   public static Vector speedVector(@Nullable class_1297 entity, Either<Expression, ExprVector> speed) {
      ExprVector vector = (ExprVector)speed.right().orElse(null);
      return vector == null ? null : vector.resolve(entity);
   }

   public static class_243 velocity(
      class_1297 entity,
      @Nullable ModelPartAnchor.Frame frame,
      Optional<Space> space,
      float velocityX,
      float velocityY,
      float velocityZ,
      @Nullable Vector speedVector
   ) {
      return velocity(entity, frame, space, localVelocity(velocityX, velocityY, velocityZ, speedVector));
   }

   public static class_243 localVelocity(float velocityX, float velocityY, float velocityZ, @Nullable Vector speedVector) {
      class_243 raw = new class_243(velocityX, velocityY, velocityZ);
      if (raw.method_1027() >= 1.0E-9) {
         return raw;
      }

      if (speedVector == null) {
         return class_243.field_1353;
      }

      raw = new class_243(speedVector.x(), speedVector.y(), speedVector.z());
      return raw.method_1027() < 1.0E-9 ? class_243.field_1353 : raw;
   }

   public static class_243 velocity(class_1297 entity, @Nullable ModelPartAnchor.Frame frame, Optional<Space> space, class_243 local) {
      if (local.method_1027() < 1.0E-9) {
         return class_243.field_1353;
      } else {
         return frame != null && space.isEmpty() ? frame.direction(local) : ((Space)space.orElse(Space.WORLD)).toGlobal(entity, local);
      }
   }

   public static float scalarSpeed(@Nullable class_1297 entity, Either<Expression, ExprVector> speed) {
      Expression expression = (Expression)speed.left().orElse(null);
      return expression == null ? 0.0F : (float)expression.eval(entity);
   }

   public static List<class_2596<?>> packets(
      class_2394 options,
      boolean force,
      class_243 origin,
      class_243 velocity,
      int count,
      Vector spread,
      float scalarSpeed,
      class_3218 level,
      class_1297 entity,
      @Nullable ModelPartAnchor.Frame frame,
      Optional<Space> space
   ) {
      boolean directed = velocity.method_1027() >= 1.0E-9;
      if (!directed && !orientedSpread(spread, frame, space)) {
         return List.of(
            ParticleBroadcast.packet(
               options, force, origin.field_1352, origin.field_1351, origin.field_1350, count, spread.x(), spread.y(), spread.z(), scalarSpeed
            )
         );
      }

      int emitted = Math.max(1, Math.min(count, 64));
      List<class_2596<?>> out = new ArrayList(emitted);

      for (int i = 0; i < emitted; i++) {
         class_243 at = origin.method_1019(spreadOffset(level.field_9229, spread, entity, frame, space));
         if (directed) {
            out.add(
               ParticleBroadcast.packet(
                  options,
                  force,
                  at.field_1352,
                  at.field_1351,
                  at.field_1350,
                  0,
                  (float)velocity.field_1352,
                  (float)velocity.field_1351,
                  (float)velocity.field_1350,
                  1.0F
               )
            );
         } else {
            int share = count / emitted + (i < count % emitted ? 1 : 0);
            out.add(ParticleBroadcast.packet(options, force, at.field_1352, at.field_1351, at.field_1350, share, 0.0F, 0.0F, 0.0F, scalarSpeed));
         }
      }

      return out;
   }

   public static void spawn(
      class_1937 level,
      class_5819 random,
      class_2394 options,
      boolean force,
      class_243 origin,
      class_243 velocity,
      int count,
      Vector spread,
      float scalarSpeed,
      class_1297 entity,
      @Nullable ModelPartAnchor.Frame frame,
      Optional<Space> space
   ) {
      boolean directed = velocity.method_1027() >= 1.0E-9;
      if (!directed && !orientedSpread(spread, frame, space)) {
         spawnBurst(
            level, random, options, force, origin.field_1352, origin.field_1351, origin.field_1350, count, spread.x(), spread.y(), spread.z(), scalarSpeed
         );
      } else {
         int emitted = Math.max(1, Math.min(count, 64));

         for (int i = 0; i < emitted; i++) {
            class_243 at = origin.method_1019(spreadOffset(random, spread, entity, frame, space));
            if (directed) {
               level.method_8466(options, force, at.field_1352, at.field_1351, at.field_1350, velocity.field_1352, velocity.field_1351, velocity.field_1350);
            } else {
               int share = count / emitted + (i < count % emitted ? 1 : 0);
               spawnBurst(level, random, options, force, at.field_1352, at.field_1351, at.field_1350, share, 0.0F, 0.0F, 0.0F, scalarSpeed);
            }
         }
      }
   }

   private static void spawnBurst(
      class_1937 level,
      class_5819 random,
      class_2394 options,
      boolean force,
      double x,
      double y,
      double z,
      int count,
      float spreadX,
      float spreadY,
      float spreadZ,
      float speed
   ) {
      if (count == 0) {
         level.method_8466(options, force, x, y, z, speed * spreadX, speed * spreadY, speed * spreadZ);
      } else {
         for (int i = 0; i < count; i++) {
            level.method_8466(
               options,
               force,
               x + random.method_43059() * spreadX,
               y + random.method_43059() * spreadY,
               z + random.method_43059() * spreadZ,
               random.method_43059() * speed,
               random.method_43059() * speed,
               random.method_43059() * speed
            );
         }
      }
   }

   private static boolean orientedSpread(Vector spread, @Nullable ModelPartAnchor.Frame frame, Optional<Space> space) {
      boolean rotates = space.isPresent() ? space.get() != Space.WORLD : frame != null;
      if (!rotates) {
         return false;
      }

      float x = spread.x();
      float y = spread.y();
      float z = spread.z();
      return x == 0.0F && y == 0.0F && z == 0.0F ? false : x != y || y != z;
   }

   private static class_243 spreadOffset(class_5819 random, Vector spread, class_1297 entity, @Nullable ModelPartAnchor.Frame frame, Optional<Space> space) {
      class_243 raw = new class_243(jitter(random, spread.x()), jitter(random, spread.y()), jitter(random, spread.z()));
      if (raw.method_1027() < 1.0E-12) {
         return class_243.field_1353;
      } else if (space.isPresent()) {
         return ((Space)space.get()).toGlobal(entity, raw);
      } else {
         return frame != null ? frame.direction(raw) : raw;
      }
   }

   private static double jitter(class_5819 random, float spread) {
      return spread == 0.0F ? 0.0 : random.method_43059() * spread;
   }
}
