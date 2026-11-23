package dev.alphasucks.aea.port.client.rope;

import dev.alphasucks.aea.port.rope.RopeAnchor;
import dev.alphasucks.aea.port.rope.RopeAnchor.OfEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public class VerletRopeState {
   public final int id;
   public final RopeAnchor from;
   public final RopeAnchor to;
   @Nullable
   public final UUID owner;
   public final boolean controllable;
   public double length;
   public double targetLength;
   public float maxLength;
   public List<RopePoint> points;
   public double segmentLength;
   public class_2960 texture;
   public boolean visible;

   public VerletRopeState(
      int id, RopeAnchor from, RopeAnchor to, double length, float maxLength, class_2960 texture, @Nullable UUID owner, boolean controllable, class_1937 level
   ) {
      this.id = id;
      this.from = from;
      this.to = to;
      this.owner = owner;
      this.controllable = controllable;
      this.length = length;
      this.targetLength = length;
      this.maxLength = maxLength;
      this.texture = texture;
      int segments = Math.max(2, (int)Math.round(length / 0.5));
      this.segmentLength = length / segments;
      this.points = new ArrayList(segments + 1);
      class_243 a = from.position(level);
      class_243 b = to.position(level);
      this.visible = a != null && b != null;
      if (a == null) {
         a = b != null ? b.method_1031(0.0, length, 0.0) : class_243.field_1353;
      }

      if (b == null) {
         b = a.method_1023(0.0, length, 0.0);
      }

      class_243 jitter = new class_243((Math.random() - 0.5) * 1.0E-4, 0.0, (Math.random() - 0.5) * 1.0E-4);

      for (int i = 0; i <= segments; i++) {
         this.points.add(new RopePoint(a.method_35590(b, (double)i / segments).method_1019(jitter)));
      }
   }

   public void relay(class_243 a, class_243 b) {
      int last = this.points.size() - 1;

      for (int i = 0; i <= last; i++) {
         RopePoint p = (RopePoint)this.points.get(i);
         p.pos = a.method_35590(b, (double)i / last);
         p.prevPos = p.pos;
      }
   }

   @Nullable
   public class_243 anchorAwayFrom(int localEntityId, class_1937 level) {
      return this.from instanceof OfEntity e && e.networkId() == localEntityId ? this.to.position(level) : this.from.position(level);
   }

   public boolean drivenBy(UUID localUuid) {
      return this.controllable && localUuid.equals(this.owner);
   }
}
