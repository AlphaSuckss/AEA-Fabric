package dev.alphasucks.aea.port.client.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_630;

public final class CustomModel {
   public static final CustomModel.Bone[] NONE = new CustomModel.Bone[0];
   public static final class_630[] NO_PARTS = new class_630[0];
   private final class_630 root;
   private final Map<String, CustomModel.Bone[]> bones;
   private final CustomModel.Bone[] all;
   private final boolean hoisted;

   public CustomModel(class_630 root, Map<String, CustomModel.Bone[]> bones, CustomModel.Bone[] all, boolean hoisted) {
      this.root = root;
      this.bones = bones;
      this.all = all;
      this.hoisted = hoisted;
   }

   public class_630 root() {
      return this.root;
   }

   public CustomModel.Bone[] bones() {
      return this.all;
   }

   public CustomModel.Bone[] bones(String normalizedName) {
      CustomModel.Bone[] found = (CustomModel.Bone[])this.bones.get(normalizedName);
      return found == null ? NONE : found;
   }

   public boolean hoisted() {
      return this.hoisted;
   }

   public CustomModel remapped(class_630 root, Map<class_630, class_630> parts) {
      Map<class_630, CustomModel.Bone> made = new IdentityHashMap(this.all.length * 2);
      CustomModel.Bone[] remappedAll = new CustomModel.Bone[this.all.length];

      for (int i = 0; i < remappedAll.length; i++) {
         remappedAll[i] = remap(this.all[i], parts, made);
      }

      Map<String, CustomModel.Bone[]> remappedBones = new HashMap(this.bones.size() * 2);

      for (Entry<String, CustomModel.Bone[]> entry : this.bones.entrySet()) {
         CustomModel.Bone[] source = (CustomModel.Bone[])entry.getValue();
         CustomModel.Bone[] target = new CustomModel.Bone[source.length];

         for (int i = 0; i < source.length; i++) {
            target[i] = remap(source[i], parts, made);
         }

         remappedBones.put((String)entry.getKey(), target);
      }

      return new CustomModel(root, remappedBones, remappedAll, this.hoisted);
   }

   private static CustomModel.Bone remap(CustomModel.Bone source, Map<class_630, class_630> parts, Map<class_630, CustomModel.Bone> made) {
      class_630 part = (class_630)parts.get(source.part);
      CustomModel.Bone existing = (CustomModel.Bone)made.get(part);
      if (existing != null) {
         return existing;
      }

      class_630[] ancestors = new class_630[source.ancestors.length];
      int kept = 0;

      for (class_630 ancestor : source.ancestors) {
         class_630 copy = (class_630)parts.get(ancestor);
         if (copy != null) {
            ancestors[kept++] = copy;
         }
      }

      CustomModel.Bone bone = new CustomModel.Bone(part, kept == ancestors.length ? ancestors : (class_630[])Arrays.copyOf(ancestors, kept));
      made.put(part, bone);
      return bone;
   }

   public static final class Bone {
      public final class_630 part;
      private final class_630[] ancestors;
      private final float x;
      private final float y;
      private final float z;
      private final float xRot;
      private final float yRot;
      private final float zRot;

      public Bone(class_630 part, class_630[] ancestors) {
         this.part = part;
         this.ancestors = ancestors;
         this.x = part.field_3657;
         this.y = part.field_3656;
         this.z = part.field_3655;
         this.xRot = part.field_3654;
         this.yRot = part.field_3675;
         this.zRot = part.field_3674;
      }

      public class_630[] ancestors() {
         return this.ancestors;
      }

      public float restXRot() {
         return this.xRot;
      }

      public float restYRot() {
         return this.yRot;
      }

      public float restZRot() {
         return this.zRot;
      }

      public void reset() {
         this.part.field_3657 = this.x;
         this.part.field_3656 = this.y;
         this.part.field_3655 = this.z;
         this.part.field_3654 = this.xRot;
         this.part.field_3675 = this.yRot;
         this.part.field_3674 = this.zRot;
         this.part.field_37938 = 1.0F;
         this.part.field_37939 = 1.0F;
         this.part.field_37940 = 1.0F;
      }
   }
}
