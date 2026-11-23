package dev.alphasucks.aea.port.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_5819;
import org.jetbrains.annotations.Nullable;

public final class BodyPart {
   public static final int HEAD = 1;
   public static final int HAT = 2;
   public static final int BODY = 4;
   public static final int JACKET = 8;
   public static final int RIGHT_ARM = 16;
   public static final int RIGHT_SLEEVE = 32;
   public static final int LEFT_ARM = 64;
   public static final int LEFT_SLEEVE = 128;
   public static final int RIGHT_LEG = 256;
   public static final int RIGHT_PANTS = 512;
   public static final int LEFT_LEG = 1024;
   public static final int LEFT_PANTS = 2048;
   public static final int ALL_MODELS = 4095;
   public static final int LIMB_HEAD = 1;
   public static final int LIMB_BODY = 2;
   public static final int LIMB_RIGHT_ARM = 4;
   public static final int LIMB_LEFT_ARM = 8;
   public static final int LIMB_RIGHT_LEG = 16;
   public static final int LIMB_LEFT_LEG = 32;
   public static final int ALL_LIMBS = 63;
   static final int POINT_NONE = 0;
   static final int POINT_LIMB = 1;
   static final int POINT_MIDPOINT = 2;
   static final int POINT_FIXED = 3;
   private final String name;
   private final String key;
   private final int models;
   private final int limbs;
   private final boolean group;
   private final boolean everything;
   private final boolean custom;
   private final BodyPart.Region[] regions;
   private final int pointKind;
   private final int pointLimbA;
   private final float pointAX;
   private final float pointAY;
   private final float pointAZ;
   private final int pointLimbB;
   private final float pointBX;
   private final float pointBY;
   private final float pointBZ;
   private final String bindKey;
   private BodyPart rightHanded;
   private BodyPart leftHanded;
   private int attachments;
   public static final Codec<BodyPart> CODEC = Codec.STRING.xmap(BodyParts::lookupOrCustom, BodyPart::name);
   public static final Codec<BodyPart> STRICT_CODEC = Codec.STRING.comapFlatMap(BodyParts::strict, BodyPart::name);
   public static final Codec<List<BodyPart>> LIST_CODEC = listOf(CODEC);
   public static final Codec<List<BodyPart>> STRICT_LIST_CODEC = listOf(STRICT_CODEC);

   BodyPart(
      String name,
      String key,
      int models,
      int limbs,
      boolean group,
      boolean everything,
      boolean custom,
      BodyPart.Region[] regions,
      int pointKind,
      int pointLimbA,
      float pointAX,
      float pointAY,
      float pointAZ,
      int pointLimbB,
      float pointBX,
      float pointBY,
      float pointBZ,
      @Nullable String bindKey
   ) {
      this.name = name;
      this.key = key;
      this.models = models;
      this.limbs = limbs;
      this.group = group;
      this.everything = everything;
      this.custom = custom;
      this.regions = regions;
      this.pointKind = pointKind;
      this.pointLimbA = pointLimbA;
      this.pointAX = pointAX;
      this.pointAY = pointAY;
      this.pointAZ = pointAZ;
      this.pointLimbB = pointLimbB;
      this.pointBX = pointBX;
      this.pointBY = pointBY;
      this.pointBZ = pointBZ;
      this.bindKey = bindKey;
   }

   void handed(BodyPart rightHanded, BodyPart leftHanded) {
      this.rightHanded = rightHanded;
      this.leftHanded = leftHanded;
   }

   void attach(int attachments) {
      this.attachments = attachments;
   }

   public int attachments() {
      return this.attachments;
   }

   public String name() {
      return this.name;
   }

   public String key() {
      return this.key;
   }

   public int models() {
      return this.models;
   }

   public int limbs() {
      return this.limbs;
   }

   public boolean isGroup() {
      return this.group;
   }

   public boolean isEverything() {
      return this.everything;
   }

   public boolean isCustom() {
      return this.custom;
   }

   public boolean isHanded() {
      return this.rightHanded != null;
   }

   @Nullable
   public String bindKey() {
      return this.bindKey;
   }

   public BodyPart sided(@Nullable class_1297 entity) {
      if (this.rightHanded == null) {
         return this;
      } else {
         return entity instanceof class_1309 living && living.method_6068() == class_1306.field_6182 ? this.leftHanded : this.rightHanded;
      }
   }

   public BodyPart sided(boolean leftHanded) {
      if (this.rightHanded == null) {
         return this;
      } else {
         return leftHanded ? this.leftHanded : this.rightHanded;
      }
   }

   public boolean contains(int limb, double x, double y, double z) {
      for (int i = 0; i < this.regions.length; i++) {
         if (this.regions[i].contains(limb, x, y, z)) {
            return true;
         }
      }

      return false;
   }

   public boolean hasPoint() {
      return this.pointKind != 0;
   }

   public boolean pointInto(float[] limbPoses, double[] scratch, float[] out) {
      switch (this.pointKind) {
         case 1:
            PoseMath.localToParent(limbPoses, this.pointLimbA * 9, this.pointAX, this.pointAY, this.pointAZ, scratch);
            out[0] = (float)scratch[0];
            out[1] = (float)scratch[1];
            out[2] = (float)scratch[2];
            return true;
         case 2:
            PoseMath.localToParent(limbPoses, this.pointLimbA * 9, this.pointAX, this.pointAY, this.pointAZ, scratch);
            double x = scratch[0];
            double y = scratch[1];
            double z = scratch[2];
            PoseMath.localToParent(limbPoses, this.pointLimbB * 9, this.pointBX, this.pointBY, this.pointBZ, scratch);
            out[0] = (float)((x + scratch[0]) * 0.5);
            out[1] = (float)((y + scratch[1]) * 0.5);
            out[2] = (float)((z + scratch[2]) * 0.5);
            return true;
         case 3:
            out[0] = this.pointAX;
            out[1] = this.pointAY;
            out[2] = this.pointAZ;
            return true;
         default:
            return false;
      }
   }

   public int pickLimb(class_5819 random) {
      int count = Integer.bitCount(this.limbs);
      if (count == 0) {
         return 1;
      }

      int skip = count == 1 ? 0 : random.method_43048(count);

      for (int limb = 0; limb < 6; limb++) {
         if ((this.limbs & 1 << limb) != 0 && skip-- == 0) {
            return limb;
         }
      }

      return 1;
   }

   public void centreInto(int limb, float[] out) {
      float minX = HumanoidPose.boxMin(limb, 0);
      float minY = HumanoidPose.boxMin(limb, 1);
      float minZ = HumanoidPose.boxMin(limb, 2);
      float maxX = HumanoidPose.boxMax(limb, 0);
      float maxY = HumanoidPose.boxMax(limb, 1);
      float maxZ = HumanoidPose.boxMax(limb, 2);

      for (int i = 0; i < this.regions.length; i++) {
         BodyPart.Region region = this.regions[i];
         if ((region.limbs() & 1 << limb) != 0) {
            minX = Math.max(minX, region.minX());
            minY = Math.max(minY, region.minY());
            minZ = Math.max(minZ, region.minZ());
            maxX = Math.min(maxX, region.maxX());
            maxY = Math.min(maxY, region.maxY());
            maxZ = Math.min(maxZ, region.maxZ());
            break;
         }
      }

      out[0] = (minX + maxX) * 0.5F;
      out[1] = (minY + maxY) * 0.5F;
      out[2] = (minZ + maxZ) * 0.5F;
   }

   public String toString() {
      return this.name;
   }

   private static Codec<List<BodyPart>> listOf(Codec<BodyPart> single) {
      final Codec<List<BodyPart>> list = single.listOf();
      return new Codec<List<BodyPart>>() {
         public <T> DataResult<Pair<List<BodyPart>, T>> decode(DynamicOps<T> ops, T input) {
            return ops.getStream(input).result().isPresent() ? list.decode(ops, input) : single.decode(ops, input).map(pair -> pair.mapFirst(List::of));
         }

         public <T> DataResult<T> encode(List<BodyPart> input, DynamicOps<T> ops, T prefix) {
            return input.size() == 1 ? single.encode((BodyPart)input.get(0), ops, prefix) : list.encode(input, ops, prefix);
         }
      };
   }

   public record Region(int limbs, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
      static BodyPart.Region whole(int limbs) {
         float inf = Float.POSITIVE_INFINITY;
         return new BodyPart.Region(limbs, -inf, -inf, -inf, inf, inf, inf);
      }

      public boolean contains(int limb, double x, double y, double z) {
         return (this.limbs & 1 << limb) != 0 && x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY && z >= this.minZ && z <= this.maxZ;
      }
   }
}
