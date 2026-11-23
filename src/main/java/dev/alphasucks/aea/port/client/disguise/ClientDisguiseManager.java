package dev.alphasucks.aea.port.client.disguise;

import com.mojang.authlib.GameProfile;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.client.render.DisguiseCustomModelLayer;
import dev.alphasucks.aea.port.entity.disguise.DisguiseData;
import dev.alphasucks.aea.port.entity.disguise.DisguiseManager;
import dev.alphasucks.aea.port.entity.disguise.DisguiseManager.ClientView;
import dev.alphasucks.aea.port.mixin.disguise.LivingEntityRendererAddLayerAccessor;
import dev.alphasucks.aea.port.mixin.disguise.WalkAnimationStateAccessor;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_310;
import net.minecraft.class_3883;
import net.minecraft.class_572;
import net.minecraft.class_583;
import net.minecraft.class_634;
import net.minecraft.class_640;
import net.minecraft.class_7923;
import net.minecraft.class_8080;
import net.minecraft.class_897;
import net.minecraft.class_898;
import net.minecraft.class_922;
import org.jetbrains.annotations.Nullable;

public final class ClientDisguiseManager {
   private static final byte ATTACK_EVENT = 4;
   private static final String FALLBACK_NAME = "Player";
   private static final class_1304[] MIRRORED_SLOTS = new class_1304[]{
      class_1304.field_6173, class_1304.field_6171, class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166
   };
   private static final Map<Integer, DisguiseData> DISGUISES = new ConcurrentHashMap();
   private static final Map<Integer, class_1297> DUMMIES = new ConcurrentHashMap();
   private static final Set<Integer> UNTICKABLE = ConcurrentHashMap.newKeySet();
   @Nullable
   private static class_1297 renderActor;
   private static final Set<Object> LAYERED = Collections.newSetFromMap(new WeakHashMap());

   private ClientDisguiseManager() {
   }

   public static void install() {
      DisguiseManager.setClientView(new ClientView() {
         @Override
         public boolean isDisguised(int netId) {
            return ClientDisguiseManager.DISGUISES.containsKey(netId);
         }

         @Override
         public boolean isDisguisedAs(int actorNetId, class_1297 target) {
            return DisguiseManager.matches((DisguiseData)ClientDisguiseManager.DISGUISES.get(actorNetId), target);
         }
      });
   }

   public static void apply(int netId, DisguiseData data) {
      DISGUISES.put(netId, data);
      DUMMIES.remove(netId);
      UNTICKABLE.remove(netId);
      if (!data.isPlayerDisguise()) {
         createDummy(netId, data);
      }
   }

   public static void remove(int netId) {
      DISGUISES.remove(netId);
      DUMMIES.remove(netId);
      UNTICKABLE.remove(netId);
   }

   public static void clear() {
      DISGUISES.clear();
      DUMMIES.clear();
      UNTICKABLE.clear();
      renderActor = null;
   }

   public static boolean hasAny() {
      return !DISGUISES.isEmpty();
   }

   @Nullable
   public static DisguiseData get(int netId) {
      return DISGUISES.isEmpty() ? null : (DisguiseData)DISGUISES.get(netId);
   }

   @Nullable
   public static class_640 playerInfo(int netId) {
      DisguiseData data = get(netId);
      if (data != null && data.isPlayerDisguise() && !data.playerUuid().isEmpty()) {
         class_634 connection = class_310.method_1551().method_1562();
         return connection == null ? null : connection.method_2871((UUID)data.playerUuid().get());
      } else {
         return null;
      }
   }

   public static void tick(class_310 mc) {
      if (!DUMMIES.isEmpty()) {
         class_1937 level = mc.field_1687;
         if (level == null) {
            DUMMIES.clear();
            UNTICKABLE.clear();
         } else if (!mc.method_1493()) {
            Iterator<Entry<Integer, class_1297>> it = DUMMIES.entrySet().iterator();

            while (it.hasNext()) {
               Entry<Integer, class_1297> entry = (Entry<Integer, class_1297>)it.next();
               Integer netId = (Integer)entry.getKey();
               DisguiseData data = (DisguiseData)DISGUISES.get(netId);
               if (data == null) {
                  it.remove();
                  UNTICKABLE.remove(netId);
               } else {
                  class_1297 dummy = (class_1297)entry.getValue();
                  if (dummy.method_37908() != level) {
                     it.remove();
                     UNTICKABLE.remove(netId);
                  } else {
                     class_1297 actor = level.method_8469(netId);
                     if (actor != null) {
                        mirrorPose(actor, dummy);
                        mirrorState(actor, dummy);
                        if (dummy instanceof class_1309) {
                           if (!UNTICKABLE.contains(netId)) {
                              try {
                                 dummy.method_5773();
                              } catch (Throwable t) {
                                 UNTICKABLE.add(netId);
                                 Apoli.LOGGER.warn("[Apoli] Disguise dummy {} threw while ticking; its animations are disabled", data.entityTypeId(), t);
                              }

                              if (dummy.method_31481()) {
                                 UNTICKABLE.add(netId);
                              }
                           }

                           mirrorAnimation(actor, dummy);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void onSwing(class_1297 actor, class_1268 hand) {
      class_1297 dummy = dummyFor(actor);
      if (dummy != null) {
         dummy.field_6012 = actor.field_6012;
         if (dummy instanceof class_1309 living) {
            living.method_6104(hand);
         }

         fireEvent(dummy, (byte)4);
      }
   }

   public static void onEntityEvent(class_1297 actor, byte eventId) {
      class_1297 dummy = dummyFor(actor);
      if (dummy != null) {
         dummy.field_6012 = actor.field_6012;
         fireEvent(dummy, eventId);
      }
   }

   @Nullable
   private static class_1297 dummyFor(class_1297 actor) {
      if (DUMMIES.isEmpty()) {
         return null;
      } else {
         class_1297 dummy = (class_1297)DUMMIES.get(actor.method_5628());
         if (dummy == null) {
            return null;
         } else {
            return dummy.method_37908() == actor.method_37908() ? dummy : null;
         }
      }
   }

   private static void fireEvent(class_1297 dummy, byte eventId) {
      try {
         dummy.method_5711(eventId);
      } catch (Throwable var3) {
      }
   }

   @Nullable
   public static class_1297 syncedDummy(int netId, class_1297 actor) {
      class_1297 dummy = (class_1297)DUMMIES.get(netId);
      boolean created = false;
      if (dummy == null || dummy.method_37908() != actor.method_37908()) {
         DisguiseData data = (DisguiseData)DISGUISES.get(netId);
         if (data == null || data.isPlayerDisguise() && actor instanceof class_1657) {
            return null;
         }

         dummy = createDummy(netId, data);
         if (dummy == null) {
            return null;
         }

         mirrorState(actor, dummy);
         created = true;
      }

      mirrorPose(actor, dummy);
      if (created) {
         mirrorAnimation(actor, dummy);
         if (dummy instanceof DisguisePlayerDummy playerDummy) {
            playerDummy.snapCloak();
         }
      }

      return dummy;
   }

   @Nullable
   public static class_1297 renderActor() {
      return renderActor;
   }

   public static void ensureCustomModelLayer(class_898 dispatcher, class_1297 dummy) {
      class_897<?> renderer = dispatcher.method_3953(dummy);
      if (renderer instanceof class_922<?, ?> living) {
         if (living.method_4038() instanceof class_572) {
            if (LAYERED.add(renderer)) {
               ((LivingEntityRendererAddLayerAccessor)renderer)
                  .apoli$addLayer(new DisguiseCustomModelLayer<>((class_3883<class_1309, class_583<class_1309>>)living));
            }
         }
      }
   }

   @Nullable
   public static class_1297 beginDisguiseRender(class_1297 actor) {
      class_1297 previous = renderActor;
      renderActor = actor;
      return previous;
   }

   public static void endDisguiseRender(@Nullable class_1297 previous) {
      renderActor = previous;
   }

   public static boolean hidesName(class_1297 rendered) {
      class_1297 actor = renderActor;
      if (actor != null && rendered.method_5628() == dummyId(actor.method_5628())) {
         DisguiseData data = (DisguiseData)DISGUISES.get(actor.method_5628());
         return data != null && data.name().isPresent() && ((String)data.name().get()).isEmpty();
      } else {
         return false;
      }
   }

   public static class_1297 powerSource(class_1297 rendered) {
      class_1297 actor = renderActor;
      return actor != null && rendered.method_5628() == dummyId(actor.method_5628()) ? actor : rendered;
   }

   private static int dummyId(int netId) {
      return -netId - 1;
   }

   private static void mirrorPose(class_1297 actor, class_1297 dummy) {
      dummy.method_5814(actor.method_23317(), actor.method_23318(), actor.method_23321());
      dummy.field_6014 = actor.field_6014;
      dummy.field_6036 = actor.field_6036;
      dummy.field_5969 = actor.field_5969;
      dummy.field_6038 = actor.field_6038;
      dummy.field_5971 = actor.field_5971;
      dummy.field_5989 = actor.field_5989;
      dummy.method_36456(actor.method_36454());
      dummy.method_36457(actor.method_36455());
      dummy.field_5982 = actor.field_5982;
      dummy.field_6004 = actor.field_6004;
      dummy.field_6012 = actor.field_6012;
      dummy.method_20803(actor.method_5809() ? 1 : 0);
      if (dummy instanceof class_1309 dummyLiving && actor instanceof class_1309 actorLiving) {
         dummyLiving.field_6283 = actorLiving.field_6283;
         dummyLiving.field_6220 = actorLiving.field_6220;
         dummyLiving.field_6241 = actorLiving.field_6241;
         dummyLiving.field_6259 = actorLiving.field_6259;
         dummyLiving.field_6235 = actorLiving.field_6235;
         dummyLiving.field_6213 = actorLiving.field_6213;
      }
   }

   private static void mirrorAnimation(class_1297 actor, class_1297 dummy) {
      if (dummy instanceof class_1657 dummyPlayer && actor instanceof class_1309 actorLiving) {
         class_8080 source = actorLiving.field_42108;
         class_8080 target = dummyPlayer.field_42108;
         ((WalkAnimationStateAccessor)target).apoli$setSpeedOld(((WalkAnimationStateAccessor)source).apoli$getSpeedOld());
         target.method_48567(source.method_48566());
         ((WalkAnimationStateAccessor)target).apoli$setPosition(source.method_48569());
         dummyPlayer.field_6229 = actorLiving.field_6229;
         dummyPlayer.field_6251 = actorLiving.field_6251;
      }
   }

   private static void mirrorState(class_1297 actor, class_1297 dummy) {
      dummy.method_18799(class_243.field_1353);
      dummy.method_24830(actor.method_24828());
      dummy.method_18380(actor.method_18376());
      dummy.method_5728(actor.method_5624());
      dummy.method_5660(actor.method_5715());
      if (dummy instanceof class_1657 dummyPlayer && actor instanceof class_1309 actorLiving) {
         for (int i = 0; i < MIRRORED_SLOTS.length; i++) {
            class_1304 slot = MIRRORED_SLOTS[i];
            dummyPlayer.method_5673(slot, actorLiving.method_6118(slot));
         }
      }
   }

   @Nullable
   private static class_1297 createDummy(int netId, DisguiseData data) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1687 == null) {
         return null;
      }

      try {
         class_1297 dummy = data.isPlayerDisguise() ? createPlayerDummy(mc, data) : createEntityDummy(mc, data);
         if (dummy == null) {
            return null;
         }

         dummy.method_5838(dummyId(netId));
         dummy.method_5803(true);
         dummy.method_5875(true);
         dummy.field_5960 = true;
         DUMMIES.put(netId, dummy);
         UNTICKABLE.remove(netId);
         return dummy;
      } catch (Exception ignored) {
         return null;
      }
   }

   @Nullable
   private static class_1297 createEntityDummy(class_310 mc, DisguiseData data) {
      class_1299<?> type = class_7923.field_41177.method_10223(data.entityTypeId());
      if (type == null) {
         return null;
      }

      class_1297 dummy = type.method_5883(mc.field_1687);
      if (dummy == null) {
         return null;
      }

      data.nbt().ifPresent(nbt -> {
         class_2487 merged = dummy.method_5647(new class_2487());
         merged.method_10543(nbt);
         dummy.method_5651(merged);
      });
      return dummy;
   }

   @Nullable
   private static class_1297 createPlayerDummy(class_310 mc, DisguiseData data) {
      UUID uuid = (UUID)data.playerUuid().orElse(null);
      if (uuid == null) {
         return null;
      }

      class_634 connection = mc.method_1562();
      class_640 info = connection == null ? null : connection.method_2871(uuid);
      GameProfile profile = info != null ? info.method_2966() : new GameProfile(uuid, (String)data.name().filter(name -> !name.isEmpty()).orElse("Player"));
      return new DisguisePlayerDummy(mc.field_1687, profile);
   }
}
