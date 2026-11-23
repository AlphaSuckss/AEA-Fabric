package dev.alphasucks.aea.port.data;

import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;

public final class BodyParts {
   private static final Map<String, BodyPart> BY_KEY = new HashMap();
   private static final Map<String, BodyPart> CANONICAL = new LinkedHashMap();
   private static final Map<String, BodyPart> CUSTOM = new ConcurrentHashMap();
   private static final float INF = Float.POSITIVE_INFINITY;
   public static final BodyPart HEAD = limb("head", 3, 0, "head");
   public static final BodyPart HAT = limb("hat", 2, 0, "hat", "headwear", "hat_layer", "head_layer");
   public static final BodyPart BODY = limb("body", 12, 1, "body", "torso", "waist");
   public static final BodyPart JACKET = limb("jacket", 8, 1, "body", "body_layer");
   public static final BodyPart RIGHT_ARM = limb("right_arm", 48, 2, "rightarm", "arm_right");
   public static final BodyPart RIGHT_SLEEVE = limb("right_sleeve", 32, 2, "rightarm", "right_arm_layer");
   public static final BodyPart LEFT_ARM = limb("left_arm", 192, 3, "leftarm", "arm_left");
   public static final BodyPart LEFT_SLEEVE = limb("left_sleeve", 128, 3, "leftarm", "left_arm_layer");
   public static final BodyPart RIGHT_LEG = limb("right_leg", 768, 4, "rightleg", "leg_right");
   public static final BodyPart RIGHT_PANTS = limb("right_pants", 512, 4, "rightleg", "right_leg_layer");
   public static final BodyPart LEFT_LEG = limb("left_leg", 3072, 5, "leftleg", "leg_left");
   public static final BodyPart LEFT_PANTS = limb("left_pants", 2048, 5, "leftleg", "left_leg_layer");
   public static final BodyPart RIGHT_HAND = region(
      "right_hand",
      4,
      Float.NEGATIVE_INFINITY,
      7.0F,
      Float.NEGATIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      1,
      2,
      0.0F,
      10.0F,
      0.0F,
      -1,
      0.0F,
      0.0F,
      0.0F,
      "hand_right",
      "right_fist"
   );
   public static final BodyPart LEFT_HAND = region(
      "left_hand",
      8,
      Float.NEGATIVE_INFINITY,
      7.0F,
      Float.NEGATIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      1,
      3,
      0.0F,
      10.0F,
      0.0F,
      -1,
      0.0F,
      0.0F,
      0.0F,
      "hand_left",
      "left_fist"
   );
   public static final BodyPart MAIN_HAND = handed("main_hand", RIGHT_HAND, LEFT_HAND, "hand_main");
   public static final BodyPart OFF_HAND = handed("off_hand", LEFT_HAND, RIGHT_HAND, "hand_off");
   public static final BodyPart HANDS = region(
      "hands",
      12,
      Float.NEGATIVE_INFINITY,
      7.0F,
      Float.NEGATIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      2,
      2,
      0.0F,
      10.0F,
      0.0F,
      3,
      0.0F,
      10.0F,
      0.0F
   );
   public static final BodyPart RIGHT_FOOT = region(
      "right_foot",
      16,
      Float.NEGATIVE_INFINITY,
      9.0F,
      Float.NEGATIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      1,
      4,
      0.0F,
      12.0F,
      0.0F,
      -1,
      0.0F,
      0.0F,
      0.0F,
      "foot_right"
   );
   public static final BodyPart LEFT_FOOT = region(
      "left_foot",
      32,
      Float.NEGATIVE_INFINITY,
      9.0F,
      Float.NEGATIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      1,
      5,
      0.0F,
      12.0F,
      0.0F,
      -1,
      0.0F,
      0.0F,
      0.0F,
      "foot_left"
   );
   public static final BodyPart FEET = region(
      "feet",
      48,
      Float.NEGATIVE_INFINITY,
      9.0F,
      Float.NEGATIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      2,
      4,
      0.0F,
      12.0F,
      0.0F,
      5,
      0.0F,
      12.0F,
      0.0F
   );
   public static final BodyPart CHEST = region(
      "chest",
      2,
      Float.NEGATIVE_INFINITY,
      Float.NEGATIVE_INFINITY,
      Float.NEGATIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      6.0F,
      0.0F,
      1,
      1,
      0.0F,
      3.0F,
      -2.0F,
      -1,
      0.0F,
      0.0F,
      0.0F
   );
   public static final BodyPart BACK = region(
      "back",
      2,
      Float.NEGATIVE_INFINITY,
      Float.NEGATIVE_INFINITY,
      0.0F,
      Float.POSITIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      1,
      1,
      0.0F,
      6.0F,
      2.0F,
      -1,
      0.0F,
      0.0F,
      0.0F
   );
   public static final BodyPart ACHILLES_HEEL = region(
      "achilles_heel",
      48,
      Float.NEGATIVE_INFINITY,
      9.0F,
      0.0F,
      Float.POSITIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      2,
      4,
      0.0F,
      12.0F,
      2.0F,
      5,
      0.0F,
      12.0F,
      2.0F
   );
   public static final BodyPart ARMS = group("arms", 240, 12, false, 2, 2, 0.0F, 0.0F, 0.0F, 3, 0.0F, 0.0F, 0.0F);
   public static final BodyPart LEGS = group("legs", 3840, 48, false, 2, 4, 0.0F, 0.0F, 0.0F, 5, 0.0F, 0.0F, 0.0F);
   public static final BodyPart UPPER = group("upper", 255, 15, false, 1, 1, 0.0F, 12.0F, 0.0F, -1, 0.0F, 0.0F, 0.0F, "upper_body");
   public static final BodyPart LOWER = group("lower", 3852, 50, false, 1, 1, 0.0F, 0.0F, 0.0F, -1, 0.0F, 0.0F, 0.0F, "lower_body");
   public static final BodyPart WHOLE = group("whole", 4095, 63, true, 3, -1, 0.0F, 24.0F, 0.0F, -1, 0.0F, 0.0F, 0.0F, "any", "all");
   public static final BodyPart RIGHT_WING = attachment("right_wing", 1, "wing_right", "icarus_right_wing");
   public static final BodyPart LEFT_WING = attachment("left_wing", 2, "wing_left", "icarus_left_wing");
   public static final BodyPart WINGS = attachment("wings", 7, "icarus_wings");
   public static final BodyPart RIGHT_EAR = attachment("right_ear", 8, "ear_right");
   public static final BodyPart LEFT_EAR = attachment("left_ear", 16, "ear_left");
   public static final BodyPart EARS = attachment("ears", 56);
   public static final BodyPart HORNS = attachment("horns", 64, "horn");
   public static final BodyPart SNOUT = attachment("snout", 128);
   public static final BodyPart TAIL = attachment("tail", 256);
   public static final BodyPart RIGHT_ARM_CLAW = attachment("right_arm_claw", 512, "claw_right_arm");
   public static final BodyPart LEFT_ARM_CLAW = attachment("left_arm_claw", 1024, "claw_left_arm");
   public static final BodyPart RIGHT_LEG_CLAW = attachment("right_leg_claw", 2048, "claw_right_leg");
   public static final BodyPart LEFT_LEG_CLAW = attachment("left_leg_claw", 4096, "claw_left_leg");
   public static final BodyPart CLAWS = attachment("claws", 7680);
   public static final BodyPart EARS_CHEST = attachment("ears_chest", 8192);
   public static final BodyPart EARS_CAPE = attachment("ears_cape", 16384);
   public static final BodyPart HALO = attachment("halo", 131072);
   public static final BodyPart RIGHT_DIGITIGRADE_LEG = attachment("right_digitigrade_leg", 32768, "digitigrade_right_leg");
   public static final BodyPart LEFT_DIGITIGRADE_LEG = attachment("left_digitigrade_leg", 65536, "digitigrade_left_leg");
   public static final BodyPart DIGITIGRADE_LEGS = attachment("digitigrade_legs", 98304);
   public static final BodyPart RIGHT_ITEM = attachment("right_item", 262144, "item_right", "right_hand_item", "right_held_item");
   public static final BodyPart LEFT_ITEM = attachment("left_item", 524288, "item_left", "left_hand_item", "left_held_item");
   public static final BodyPart MAIN_HAND_ITEM = handedAttachment("main_hand_item", RIGHT_ITEM, LEFT_ITEM, "main_item", "item_main", "mainhand_item");
   public static final BodyPart OFF_HAND_ITEM = handedAttachment("off_hand_item", LEFT_ITEM, RIGHT_ITEM, "off_item", "item_off", "offhand_item");
   public static final BodyPart ITEMS = attachment("items", 786432, "held_items", "held_item");

   private BodyParts() {
   }

   private static BodyPart limb(String name, int models, int limb, String bindKey, String... aliases) {
      return register(
         new BodyPart(
            name,
            ModelParts.normalize(name),
            models,
            1 << limb,
            false,
            false,
            false,
            new BodyPart.Region[]{BodyPart.Region.whole(1 << limb)},
            1,
            limb,
            0.0F,
            0.0F,
            0.0F,
            -1,
            0.0F,
            0.0F,
            0.0F,
            bindKey
         ),
         aliases
      );
   }

   private static BodyPart region(
      String name,
      int limbs,
      float minX,
      float minY,
      float minZ,
      float maxX,
      float maxY,
      float maxZ,
      int pointKind,
      int limbA,
      float ax,
      float ay,
      float az,
      int limbB,
      float bx,
      float by,
      float bz,
      String... aliases
   ) {
      return register(
         new BodyPart(
            name,
            ModelParts.normalize(name),
            0,
            limbs,
            false,
            false,
            false,
            new BodyPart.Region[]{new BodyPart.Region(limbs, minX, minY, minZ, maxX, maxY, maxZ)},
            pointKind,
            limbA,
            ax,
            ay,
            az,
            limbB,
            bx,
            by,
            bz,
            null
         ),
         aliases
      );
   }

   private static BodyPart group(
      String name,
      int models,
      int limbs,
      boolean everything,
      int pointKind,
      int limbA,
      float ax,
      float ay,
      float az,
      int limbB,
      float bx,
      float by,
      float bz,
      String... aliases
   ) {
      return register(
         new BodyPart(
            name,
            ModelParts.normalize(name),
            models,
            limbs,
            true,
            everything,
            false,
            new BodyPart.Region[]{BodyPart.Region.whole(limbs)},
            pointKind,
            limbA,
            ax,
            ay,
            az,
            limbB,
            bx,
            by,
            bz,
            null
         ),
         aliases
      );
   }

   private static BodyPart handed(String name, BodyPart rightHanded, BodyPart leftHanded, String... aliases) {
      BodyPart part = new BodyPart(
         name,
         ModelParts.normalize(name),
         0,
         rightHanded.limbs() | leftHanded.limbs(),
         false,
         false,
         false,
         new BodyPart.Region[0],
         0,
         -1,
         0.0F,
         0.0F,
         0.0F,
         -1,
         0.0F,
         0.0F,
         0.0F,
         null
      );
      part.handed(rightHanded, leftHanded);
      return register(part, aliases);
   }

   private static BodyPart handedAttachment(String name, BodyPart rightHanded, BodyPart leftHanded, String... aliases) {
      BodyPart part = new BodyPart(
         name, ModelParts.normalize(name), 0, 0, false, false, true, new BodyPart.Region[0], 0, -1, 0.0F, 0.0F, 0.0F, -1, 0.0F, 0.0F, 0.0F, null
      );
      part.handed(rightHanded, leftHanded);
      part.attach(rightHanded.attachments() | leftHanded.attachments());
      return register(part, aliases);
   }

   private static BodyPart attachment(String name, int attachments, String... aliases) {
      BodyPart part = new BodyPart(
         name, ModelParts.normalize(name), 0, 0, false, false, true, new BodyPart.Region[0], 0, -1, 0.0F, 0.0F, 0.0F, -1, 0.0F, 0.0F, 0.0F, null
      );
      part.attach(attachments);
      return register(part, aliases);
   }

   private static BodyPart register(BodyPart part, String... aliases) {
      BY_KEY.put(part.key(), part);
      CANONICAL.put(part.name(), part);

      for (String alias : aliases) {
         BY_KEY.put(ModelParts.normalize(alias), part);
      }

      return part;
   }

   @Nullable
   public static BodyPart lookup(String name) {
      return (BodyPart)BY_KEY.get(ModelParts.normalize(name));
   }

   public static BodyPart lookupOrCustom(String name) {
      String key = ModelParts.normalize(name);
      BodyPart known = (BodyPart)BY_KEY.get(key);
      return known != null
         ? known
         : (BodyPart)CUSTOM.computeIfAbsent(
            key, k -> new BodyPart(name, k, 0, 0, false, false, true, new BodyPart.Region[0], 0, -1, 0.0F, 0.0F, 0.0F, -1, 0.0F, 0.0F, 0.0F, null)
         );
   }

   public static DataResult<BodyPart> strict(String name) {
      BodyPart known = (BodyPart)BY_KEY.get(ModelParts.normalize(name));
      if (known != null && known.attachments() == 0) {
         return DataResult.success(known);
      }

      String reason = known != null ? "Body part '" + name + "' is drawn by another mod and has no hitbox." : "Unknown body part '" + name + "'.";
      return DataResult.error(() -> reason + " Expected one of: " + String.join(", ", hittableNames()));
   }

   private static List<String> hittableNames() {
      List<String> names = new ArrayList();

      for (BodyPart part : CANONICAL.values()) {
         if (part.attachments() == 0) {
            names.add(part.name());
         }
      }

      return names;
   }

   public static List<BodyPart> all() {
      return Collections.unmodifiableList(new ArrayList(CANONICAL.values()));
   }
}
