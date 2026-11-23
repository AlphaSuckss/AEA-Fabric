package dev.alphasucks.aea.port.rope;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.compat.sable.SableSubLevels;
import dev.alphasucks.aea.port.data.Vector;
import dev.alphasucks.aea.port.dev.DevMode;
import dev.alphasucks.aea.port.dev.DevParticles;
import dev.alphasucks.aea.port.dev.DevParticles.Ray;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3218;
import net.minecraft.class_3542;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.jetbrains.annotations.Nullable;

public record RopeEndpointSource(RopeEndpointSource.Type type, float distance, boolean entities, boolean blocks, Optional<Vector> position) {
   public static final MapCodec<RopeEndpointSource> CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            RopeEndpointSource.Type.CODEC.optionalFieldOf("source", RopeEndpointSource.Type.SELF).forGetter(RopeEndpointSource::type),
            Codec.FLOAT.optionalFieldOf("distance", 30.0F).forGetter(RopeEndpointSource::distance),
            Codec.BOOL.optionalFieldOf("entities", false).forGetter(RopeEndpointSource::entities),
            Codec.BOOL.optionalFieldOf("blocks", true).forGetter(RopeEndpointSource::blocks),
            Vector.CODEC.optionalFieldOf("position").forGetter(RopeEndpointSource::position)
         )
         .apply(i, RopeEndpointSource::new)
   );

   public static RopeEndpointSource self() {
      return new RopeEndpointSource(RopeEndpointSource.Type.SELF, 30.0F, false, true, Optional.empty());
   }

   public static RopeEndpointSource raycastBlock(float distance) {
      return new RopeEndpointSource(RopeEndpointSource.Type.RAYCAST, distance, false, true, Optional.empty());
   }

   @Nullable
   public RopeAnchor resolve(@Nullable class_1297 actor, @Nullable class_1297 target, class_1937 level) {
      return switch (this.type) {
         case SELF -> actor == null ? null : new RopeAnchor.OfEntity(actor.method_5628(), class_243.field_1353);
         case TARGET -> target == null ? null : new RopeAnchor.OfEntity(target.method_5628(), class_243.field_1353);
         case RAYCAST -> this.raycast(actor, level);
         case POSITION -> (RopeAnchor)this.position.map(v -> new RopeAnchor.Position(new class_243(v.x(), v.y(), v.z()))).orElse(null);
      };
   }

   @Nullable
   private RopeAnchor raycast(@Nullable class_1297 actor, class_1937 level) {
      if (actor == null) {
         return null;
      }

      class_243 origin = actor.method_5836(1.0F);
      class_243 dir = actor.method_5828(1.0F);
      class_243 end = origin.method_1019(dir.method_1021(this.distance));
      class_243 blockHitPos = null;
      UUID blockHitSubLevel = null;
      class_2338 worldBlock = null;
      double blockDistSq = Double.POSITIVE_INFINITY;
      double maxDistSq = (this.distance + 2.0) * (this.distance + 2.0);
      if (this.blocks) {
         class_3965 blockHit = level.method_17742(new class_3959(origin, end, class_3960.field_17558, class_242.field_1348, actor));
         if (blockHit.method_17783() == class_240.field_1332) {
            blockHitPos = blockHit.method_17784();
            blockHitSubLevel = SableSubLevels.subLevelAt(level, blockHitPos);
            class_243 worldPos = blockHitSubLevel == null ? blockHitPos : SableSubLevels.toWorld(level, blockHitSubLevel, blockHitPos);
            blockDistSq = worldPos == null ? Double.POSITIVE_INFINITY : origin.method_1025(worldPos);
            if (blockHitSubLevel == null) {
               worldBlock = blockHit.method_17777();
            }

            if (blockDistSq > maxDistSq) {
               blockHitPos = null;
               blockHitSubLevel = null;
               worldBlock = null;
               blockDistSq = Double.POSITIVE_INFINITY;
            }
         }
      }

      class_1297 best = null;
      double bestSq = blockDistSq;
      if (this.entities) {
         class_238 box = new class_238(origin, end).method_1014(1.0);

         for (class_1297 cand : level.method_8333(actor, box, e -> e != actor && e instanceof class_1309 && e.method_5863())) {
            Optional<class_243> inter = cand.method_5829().method_992(origin, end);
            if (!inter.isEmpty()) {
               double dSq = origin.method_1025((class_243)inter.get());
               if (dSq < bestSq) {
                  bestSq = dSq;
                  best = cand;
               }
            }
         }
      }

      if (DevMode.any() && level instanceof class_3218 devLevel) {
         Ray kind = Ray.ACTION;
         DevParticles.ray(devLevel, kind, origin, dir, Double.isInfinite(bestSq) ? this.distance : Math.sqrt(bestSq), this.distance);
         if (best != null) {
            DevParticles.mark(devLevel, kind, best, true);
         } else if (worldBlock != null) {
            DevParticles.mark(devLevel, kind, worldBlock, true);
         }
      }

      if (best != null) {
         return new RopeAnchor.OfEntity(best.method_5628(), class_243.field_1353);
      } else if (blockHitPos == null) {
         return null;
      } else {
         return blockHitSubLevel == null ? new RopeAnchor.Position(blockHitPos) : new RopeAnchor.OfSubLevel(blockHitSubLevel, blockHitPos);
      }
   }

   public enum Type implements class_3542 {
      SELF("self"),
      TARGET("target"),
      RAYCAST("raycast"),
      POSITION("position");

      public static final Codec<RopeEndpointSource.Type> CODEC = class_3542.method_28140(RopeEndpointSource.Type::values);
      private final String name;

      Type(String name) {
         this.name = name;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }
}
