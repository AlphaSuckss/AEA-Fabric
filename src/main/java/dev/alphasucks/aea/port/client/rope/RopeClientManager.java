package dev.alphasucks.aea.port.client.rope;

import dev.alphasucks.aea.port.network.payload.RopeChangeLengthC2S;
import dev.alphasucks.aea.port.network.payload.RopeCreateS2C;
import dev.alphasucks.aea.port.network.payload.RopeSwingC2S;
import dev.alphasucks.aea.port.rope.RopeConstants;
import io.netty.buffer.Unpooled;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.class_243;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_3532;
import net.minecraft.class_638;

public final class RopeClientManager {
   private static final Map<Integer, VerletRopeState> ROPES = new HashMap();

   private RopeClientManager() {
   }

   public static void attach(RopeCreateS2C p, class_638 level) {
      ROPES.put(p.id(), new VerletRopeState(p.id(), p.from(), p.to(), p.length(), p.maxLength(), p.texture(), p.owner(), p.controllable(), level));
   }

   public static void detach(int id) {
      ROPES.remove(id);
   }

   public static VerletRopeState get(int id) {
      return (VerletRopeState)ROPES.get(id);
   }

   public static Collection<VerletRopeState> getAll() {
      return ROPES.values();
   }

   public static void clear() {
      ROPES.clear();
   }

   private static void verletStep(VerletRopeState rope) {
      int last = rope.points.size() - 1;
      RopePoint first = (RopePoint)rope.points.get(0);
      first.prevPos = first.pos;
      RopePoint end = (RopePoint)rope.points.get(last);
      end.prevPos = end.pos;

      for (int i = 1; i < last; i++) {
         RopePoint p = (RopePoint)rope.points.get(i);
         class_243 vel = p.pos.method_1020(p.prevPos).method_1021(0.99F);
         p.prevPos = p.pos;
         p.pos = p.pos.method_1019(vel).method_1019(RopeConstants.GRAVITY);
      }
   }

   private static void applyConstraints(VerletRopeState rope, class_243 fromPos, class_243 toPos) {
      int last = rope.points.size() - 1;
      ((RopePoint)rope.points.get(0)).pos = fromPos;
      ((RopePoint)rope.points.get(last)).pos = toPos;

      for (int i = 0; i < last; i++) {
         RopePoint a = (RopePoint)rope.points.get(i);
         RopePoint b = (RopePoint)rope.points.get(i + 1);
         class_243 delta = b.pos.method_1020(a.pos);
         double dist = delta.method_1033();
         if (!(dist < 1.0E-6)) {
            double diff = (dist - rope.segmentLength) / dist;
            class_243 offset = delta.method_1021(0.98F * diff);
            a.pos = a.pos.method_1019(offset);
            b.pos = b.pos.method_1020(offset);
         }
      }

      ((RopePoint)rope.points.get(0)).pos = fromPos;
      ((RopePoint)rope.points.get(last)).pos = toPos;
   }

   private static void resegment(VerletRopeState rope) {
      if (rope.targetLength != rope.length) {
         rope.length = class_3532.method_16436(0.33, rope.length, rope.targetLength);
         int desiredSegments = Math.max(2, (int)Math.ceil(rope.length / 0.5));
         desiredSegments = Math.min(desiredSegments, (int)Math.ceil(rope.maxLength / 0.5F));
         if (rope.points.size() - 1 > desiredSegments && rope.points.size() > 2) {
            rope.points.remove(1);
         }

         if (rope.points.size() - 1 < desiredSegments) {
            RopePoint anchor = (RopePoint)rope.points.get(0);
            RopePoint next = (RopePoint)rope.points.get(1);
            RopePoint p = new RopePoint(anchor.pos.method_35590(next.pos, 0.5));
            p.prevPos = anchor.prevPos.method_35590(next.prevPos, 0.5);
            rope.points.add(1, p);
         }

         rope.segmentLength = rope.length / (rope.points.size() - 1);
      }
   }

   private static void driveLocalRope(VerletRopeState rope, class_310 client, class_638 level) {
      class_315 opts = client.field_1690;
      double delta = 0.0;
      if (opts.field_1903.method_1434()) {
         delta -= 0.2F;
      }

      if (opts.field_1832.method_1434()) {
         delta += 0.2F;
      }

      if (delta != 0.0) {
         sendC2S(RopeChangeLengthC2S.CHANNEL, new RopeChangeLengthC2S(rope.id, delta)::write);
      }

      if (!client.field_1724.method_24828()) {
         double x = 0.0;
         double z = 0.0;
         if (opts.field_1894.method_1434()) {
            z++;
         }

         if (opts.field_1881.method_1434()) {
            z--;
         }

         if (opts.field_1849.method_1434()) {
            x++;
         }

         if (opts.field_1913.method_1434()) {
            x--;
         }

         if (x != 0.0 || z != 0.0) {
            class_243 dir = new class_243(x, 0.0, z);
            sendC2S(RopeSwingC2S.CHANNEL, new RopeSwingC2S(rope.id, dir.method_1027() > 1.0 ? dir.method_1029() : dir)::write);
         }
      }
   }

   private static void sendC2S(class_2960 channel, Consumer<class_2540> writer) {
      class_2540 buf = new class_2540(Unpooled.buffer());
      writer.accept(buf);
      ClientPlayNetworking.send(channel, buf);
   }

   public static void tick() {
      class_310 client = class_310.method_1551();
      class_638 level = client.field_1687;
      if (level != null && client.field_1724 != null) {
         for (VerletRopeState rope : ROPES.values()) {
            if (rope.drivenBy(client.field_1724.method_5667())) {
               driveLocalRope(rope, client, level);
            }

            resegment(rope);
            class_243 fromPos = rope.from.position(level);
            class_243 toPos = rope.to.position(level);
            if (fromPos != null && toPos != null) {
               if (!rope.visible) {
                  rope.relay(fromPos, toPos);
                  rope.visible = true;
               }

               verletStep(rope);

               for (int i = 0; i < 10; i++) {
                  applyConstraints(rope, fromPos, toPos);
               }
            } else {
               rope.visible = false;
            }
         }
      }
   }
}
