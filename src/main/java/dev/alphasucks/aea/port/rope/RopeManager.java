package dev.alphasucks.aea.port.rope;

import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.network.payload.RopeCreateS2C;
import dev.alphasucks.aea.port.network.payload.RopeDeleteS2C;
import dev.alphasucks.aea.port.network.payload.RopeVerletLengthS2C;
import dev.alphasucks.aea.port.rope.RopeAnchor.OfEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3532;
import net.minecraft.class_5321;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class RopeManager {
   private static final Map<Integer, Rope> BY_ID = new HashMap();
   private static final Map<UUID, Set<Integer>> BY_ENTITY = new HashMap();
   private static final Map<UUID, Set<Integer>> BY_OWNER = new HashMap();
   private static int nextId = 1;
   @Nullable
   private static MinecraftServer server;
   private static final double MAX_CONSTRAINT_IMPULSE = 4.0;

   private RopeManager() {
   }

   public static Rope create(
      class_3218 level, RopeAnchor from, RopeAnchor to, @Nullable String slot, @Nullable UUID owner, RopeParams params, class_2960 texture
   ) {
      server = level.method_8503();
      if (slot != null && owner != null) {
         removeMatching(owner, slot, null);
      }

      UUID fromEntity = from.entity(level) != null ? from.entity(level).method_5667() : null;
      UUID toEntity = to.entity(level) != null ? to.entity(level).method_5667() : null;
      class_243 pa = from.position(level);
      class_243 pb = to.position(level);
      double dist = pa != null && pb != null ? pa.method_1022(pb) : params.maxLength();
      double length = params.initialLength(dist);
      int id = nextId++;
      Rope rope = new Rope(id, from, to, fromEntity, toEntity, slot, owner, params, texture, level.method_27983(), length);
      if (params.controllable() && owner != null) {
         class_1657 p = level.method_8503().method_3760().method_14602(owner);
         if (p != null && p.method_6128()) {
            if (p.method_5715()) {
               p.method_23670();
            } else {
               rope.length = class_3532.method_15350(rope.length - 5.0, params.minLength(), params.maxLength());
            }
         }
      }

      BY_ID.put(id, rope);
      if (fromEntity != null) {
         ((Set)BY_ENTITY.computeIfAbsent(fromEntity, k -> new HashSet())).add(id);
      }

      if (toEntity != null) {
         ((Set)BY_ENTITY.computeIfAbsent(toEntity, k -> new HashSet())).add(id);
      }

      if (owner != null) {
         ((Set)BY_OWNER.computeIfAbsent(owner, k -> new HashSet())).add(id);
      }

      ApoliNetwork.broadcastRopeCreate(level, createPayload(rope));
      return rope;
   }

   public static void syncTo(class_3222 player) {
      if (!BY_ID.isEmpty()) {
         class_5321<class_1937> dimension = player.method_37908().method_27983();

         for (Rope rope : BY_ID.values()) {
            if (rope.dimension.equals(dimension)) {
               ApoliNetwork.sendRopeCreate(player, createPayload(rope));
            }
         }
      }
   }

   private static RopeCreateS2C createPayload(Rope rope) {
      return new RopeCreateS2C(rope.id, rope.from, rope.to, rope.length, rope.params.maxLength(), rope.texture, rope.owner, rope.params.controllable());
   }

   public static void remove(int id) {
      Rope rope = (Rope)BY_ID.remove(id);
      if (rope != null) {
         unindex(BY_ENTITY, rope.fromEntity, id);
         unindex(BY_ENTITY, rope.toEntity, id);
         unindex(BY_OWNER, rope.owner, id);
         class_3218 level = serverLevel(rope);
         if (level != null) {
            ApoliNetwork.broadcastRopeDelete(level, new RopeDeleteS2C(id));
         }
      }
   }

   public static int removeMatching(@Nullable UUID owner, @Nullable String slot, @Nullable UUID attachedTo) {
      List<Integer> doomed = new ArrayList();

      for (Rope rope : BY_ID.values()) {
         if ((owner == null || owner.equals(rope.owner)) && (slot == null || slot.equals(rope.slot)) && (attachedTo == null || rope.touches(attachedTo))) {
            doomed.add(rope.id);
         }
      }

      doomed.forEach(RopeManager::remove);
      return doomed.size();
   }

   public static boolean detachControllable(UUID owner) {
      List<Integer> doomed = new ArrayList();

      for (Rope rope : forOwner(owner)) {
         if (rope.params.controllable()) {
            doomed.add(rope.id);
         }
      }

      doomed.forEach(RopeManager::remove);
      return !doomed.isEmpty();
   }

   public static void onEntityGone(UUID entity) {
      Set<Integer> ids = (Set<Integer>)BY_ENTITY.get(entity);
      if (ids != null) {
         for (int id : new ArrayList<Integer>(ids)) {
            remove(id);
         }
      }
   }

   public static void clear() {
      BY_ID.clear();
      BY_ENTITY.clear();
      BY_OWNER.clear();
      server = null;
   }

   @Nullable
   public static Rope get(int id) {
      return (Rope)BY_ID.get(id);
   }

   public static List<Rope> forEntity(UUID entity) {
      Set<Integer> ids = (Set<Integer>)BY_ENTITY.get(entity);
      if (ids == null) {
         return List.of();
      }

      List<Rope> out = new ArrayList(ids.size());

      for (int id : ids) {
         Rope r = (Rope)BY_ID.get(id);
         if (r != null) {
            out.add(r);
         }
      }

      return out;
   }

   public static List<Rope> forOwner(UUID owner) {
      Set<Integer> ids = (Set<Integer>)BY_OWNER.get(owner);
      if (ids == null) {
         return List.of();
      }

      List<Rope> out = new ArrayList(ids.size());

      for (int id : ids) {
         Rope r = (Rope)BY_ID.get(id);
         if (r != null) {
            out.add(r);
         }
      }

      return out;
   }

   public static boolean connected(UUID a, UUID b) {
      for (Rope rope : forEntity(a)) {
         if (rope.touches(b)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public static Rope controllableRopeOf(UUID owner) {
      for (Rope rope : forOwner(owner)) {
         if (rope.params.controllable()) {
            return rope;
         }
      }

      return null;
   }

   public static void handleLengthChange(class_3222 player, int ropeId, double delta) {
      Rope rope = (Rope)BY_ID.get(ropeId);
      if (rope != null && player.method_5667().equals(rope.owner)) {
         class_3218 level = serverLevel(rope);
         if (level != null) {
            double step = Math.signum(delta) * rope.params.reelStep();
            class_243 pa = rope.from.position(level);
            class_243 pb = rope.to.position(level);
            if (pa != null && pb != null) {
               double dist = pa.method_1022(pb);
               if (step < 0.0 && dist < 0.95 * rope.length) {
                  step *= rope.params.slackPullRate();
               }
            }

            rope.length = class_3532.method_15350(rope.length + step, rope.params.minLength(), rope.params.maxLength());
            ApoliNetwork.broadcastRopeVerletLength(level, new RopeVerletLengthS2C(ropeId, rope.length));
         }
      }
   }

   public static void reel(int ropeId, double amount) {
      Rope rope = (Rope)BY_ID.get(ropeId);
      if (rope != null) {
         rope.length = class_3532.method_15350(rope.length - amount, rope.params.minLength(), rope.params.maxLength());
         class_3218 level = serverLevel(rope);
         if (level != null) {
            ApoliNetwork.broadcastRopeVerletLength(level, new RopeVerletLengthS2C(ropeId, rope.length));
         }
      }
   }

   public static void applySwingInput(class_3222 player, int ropeId, class_243 inputDir) {
      Rope rope = (Rope)BY_ID.get(ropeId);
      if (rope != null && rope.params.controllable() && player.method_5667().equals(rope.owner)) {
         if (!player.method_24828()) {
            double x = class_3532.method_15350(inputDir.field_1352, -1.0, 1.0);
            double z = class_3532.method_15350(inputDir.field_1350, -1.0, 1.0);
            if (x != 0.0 || z != 0.0) {
               float yaw = (float)Math.toRadians(player.method_36454());
               double sin = class_3532.method_15374(yaw);
               double cos = class_3532.method_15362(yaw);
               class_243 forward = new class_243(-sin, 0.0, cos);
               class_243 right = new class_243(-cos, 0.0, -sin);
               class_243 wish = forward.method_1021(z).method_1019(right.method_1021(x));
               double wishSq = wish.method_1027();
               if (!(wishSq < 1.0E-6)) {
                  if (wishSq > 1.0) {
                     wish = wish.method_1021(1.0 / Math.sqrt(wishSq));
                  }

                  RopeParams p = rope.params;
                  class_243 vel = player.method_18798();
                  class_243 horizontal = new class_243(vel.field_1352, 0.0, vel.field_1350);
                  class_243 boosted = horizontal.method_1019(wish.method_1021(p.controlAccel()));
                  double cap = Math.max(p.maxSwingSpeed(), horizontal.method_1033());
                  if (boosted.method_1027() > cap * cap) {
                     boosted = boosted.method_1029().method_1021(cap);
                  }

                  player.method_18800(boosted.field_1352, vel.field_1351, boosted.field_1350);
                  player.field_6037 = true;
               }
            }
         }
      }
   }

   public static void tick(MinecraftServer mc) {
      server = mc;
      if (!BY_ID.isEmpty()) {
         for (Rope rope : new ArrayList<Rope>(BY_ID.values())) {
            if (BY_ID.containsKey(rope.id)) {
               class_3218 level = mc.method_3847(rope.dimension);
               if (level == null) {
                  remove(rope.id);
               } else if (!endpointDead(rope.from, level) && !endpointDead(rope.to, level)) {
                  class_243 pa = rope.from.position(level);
                  class_243 pb = rope.to.position(level);
                  if (pa != null && pb != null) {
                     if (rope.params.breakBeyond() > 0.0F && pa.method_1022(pb) > rope.params.breakBeyond()) {
                        remove(rope.id);
                     } else {
                        class_1297 ea = rope.from.entity(level);
                        class_1297 eb = rope.to.entity(level);
                        class_1657 controller = controllerOf(rope, ea, eb);
                        if (controller != null) {
                           if (controller.method_6128()) {
                              if (++rope.flightTicks >= 10) {
                                 remove(rope.id);
                                 continue;
                              }
                           } else {
                              rope.flightTicks = 0;
                           }
                        }

                        boolean moveFrom = rope.params.constrainFrom() && ea instanceof class_1309;
                        boolean moveTo = rope.params.constrainTo() && eb instanceof class_1309;
                        double share = moveFrom && moveTo ? 0.5 : 1.0;
                        if (moveFrom) {
                           constrain((class_1309)ea, pb, rope, share, controller == ea);
                        }

                        if (moveTo) {
                           constrain((class_1309)eb, pa, rope, share, controller == eb);
                        }
                     }
                  } else {
                     remove(rope.id);
                  }
               } else {
                  remove(rope.id);
               }
            }
         }
      }
   }

   private static void constrain(class_1309 e, class_243 anchor, Rope rope, double share, boolean controller) {
      RopeParams p = rope.params;
      class_243 pos = e.method_5829().method_1005();
      class_243 delta = pos.method_1020(anchor);
      double dist = delta.method_1033();
      if (!(dist < 1.0E-6)) {
         class_243 dir = delta.method_1029();
         switch (p.mode()) {
            case SPRING:
               if (dist <= rope.length) {
                  return;
               }

               double excess = dist - rope.length;
               e.method_18799(e.method_18798().method_1019(limited(dir.method_1021(-excess * p.stiffness() * share))));
               markMoved(e);
               break;
            case RIGID:
               double diff = dist - rope.length;
               if (Math.abs(diff) < 1.0E-4) {
                  return;
               }

               e.method_18799(e.method_18798().method_1019(limited(dir.method_1021(-diff * p.stiffness() * share))));
               markMoved(e);
               break;
            default:
               if (dist <= rope.length) {
                  return;
               }

               if (anchor.field_1351 > e.method_23318()) {
                  e.field_6017 = Math.max(0.0F, e.field_6017 - 1.0F);
               }

               double excessx = dist - rope.length;
               class_243 vel = e.method_18798();
               double radial = vel.method_1026(dir);
               class_243 radialVel = dir.method_1021(radial);
               class_243 tangentialVel = vel.method_1020(radialVel);
               if (radial > 0.0) {
                  radialVel = radialVel.method_1021(p.radialDamping());
               }

               if (controller && e instanceof class_1657 pl && pl.method_5624() && tangentialVel.method_1033() < p.maxSwingSpeed()) {
                  tangentialVel = tangentialVel.method_1029().method_1021(tangentialVel.method_1033() * p.swingBoost());
               }

               e.method_18799(tangentialVel.method_1019(radialVel));
               double springScale = radial < 0.0 ? p.springScaling() : 1.0;
               e.method_18799(e.method_18798().method_1019(limited(dir.method_1021(-excessx * p.stiffness() * springScale * share))));
               markMoved(e);
         }
      }
   }

   private static class_243 limited(class_243 impulse) {
      double lengthSq = impulse.method_1027();
      return lengthSq <= 16.0 ? impulse : impulse.method_1021(4.0 / Math.sqrt(lengthSq));
   }

   private static void markMoved(class_1309 e) {
      if (e instanceof class_3222) {
         e.field_6037 = true;
      } else {
         e.field_6007 = true;
      }
   }

   private static boolean endpointDead(RopeAnchor anchor, class_3218 level) {
      if (!(anchor instanceof OfEntity)) {
         return false;
      }

      class_1297 e = anchor.entity(level);
      return e == null || e.method_31481() || e instanceof class_1309 le && !le.method_5805();
   }

   @Nullable
   private static class_1657 controllerOf(Rope rope, @Nullable class_1297 ea, @Nullable class_1297 eb) {
      if (rope.params.controllable() && rope.owner != null) {
         if (ea instanceof class_1657 p && p.method_5667().equals(rope.owner)) {
            return p;
         } else {
            return eb instanceof class_1657 p && p.method_5667().equals(rope.owner) ? p : null;
         }
      } else {
         return null;
      }
   }

   @Nullable
   private static class_3218 serverLevel(Rope rope) {
      return server == null ? null : server.method_3847(rope.dimension);
   }

   private static void unindex(Map<UUID, Set<Integer>> index, @Nullable UUID key, int id) {
      if (key != null) {
         Set<Integer> set = (Set<Integer>)index.get(key);
         if (set != null) {
            set.remove(id);
            if (set.isEmpty()) {
               index.remove(key);
            }
         }
      }
   }
}
