package dev.alphasucks.aea.port.rope;

import java.util.UUID;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_5321;
import org.jetbrains.annotations.Nullable;

public final class Rope {
   public final int id;
   public final RopeAnchor from;
   public final RopeAnchor to;
   @Nullable
   public final UUID fromEntity;
   @Nullable
   public final UUID toEntity;
   @Nullable
   public final String slot;
   @Nullable
   public final UUID owner;
   public final RopeParams params;
   public final class_2960 texture;
   public final class_5321<class_1937> dimension;
   public double length;
   public int flightTicks = 0;

   public Rope(
      int id,
      RopeAnchor from,
      RopeAnchor to,
      @Nullable UUID fromEntity,
      @Nullable UUID toEntity,
      @Nullable String slot,
      @Nullable UUID owner,
      RopeParams params,
      class_2960 texture,
      class_5321<class_1937> dimension,
      double length
   ) {
      this.id = id;
      this.from = from;
      this.to = to;
      this.fromEntity = fromEntity;
      this.toEntity = toEntity;
      this.slot = slot;
      this.owner = owner;
      this.params = params;
      this.texture = texture;
      this.dimension = dimension;
      this.length = length;
   }

   public boolean touches(UUID entity) {
      return entity.equals(this.fromEntity) || entity.equals(this.toEntity);
   }
}
