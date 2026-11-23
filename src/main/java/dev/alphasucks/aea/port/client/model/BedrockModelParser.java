package dev.alphasucks.aea.port.client.model;

import com.mojang.serialization.Dynamic;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.data.ModelParts;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import net.minecraft.class_2350;
import net.minecraft.class_2960;
import net.minecraft.class_5603;
import net.minecraft.class_630;
import net.minecraft.class_2350.class_2351;
import net.minecraft.class_630.class_628;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class BedrockModelParser {
   private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);
   private static final float BEDROCK_FLOOR = 24.0F;
   private static final int MAX_PARENT_DEPTH = 64;

   private BedrockModelParser() {
   }

   public static <T> CustomModel[] parseVariants(class_2960 id, Dynamic<T> json) {
      Dynamic<T> geo = (Dynamic<T>)json.get("minecraft:geometry")
         .asStreamOpt()
         .result()
         .flatMap(stream -> stream.findFirst())
         .orElseThrow(() -> new IllegalArgumentException("minecraft:geometry is empty"));
      Dynamic<T> description = (Dynamic<T>)geo.get("description").result().orElseGet(geo::emptyMap);
      int texWidth = description.get("texture_width").asInt(64);
      int texHeight = description.get("texture_height").asInt(64);
      List<BedrockModelParser.Bone> ordered = new ArrayList();
      Map<String, BedrockModelParser.Bone> byName = new LinkedHashMap();

      for (Dynamic<T> element : geo.get("bones").asList(Function.identity())) {
         BedrockModelParser.Bone bone = parseBone(element, id);
         if (byName.putIfAbsent(bone.name, bone) == null) {
            ordered.add(bone);
         } else {
            Apoli.LOGGER.warn("[Apoli] Custom model {} declares the bone '{}' more than once; ignoring the duplicate.", id, bone.name);
         }
      }

      for (BedrockModelParser.Bone bone : ordered) {
         BedrockModelParser.Bone parent = bone.parent == null ? null : (BedrockModelParser.Bone)byName.get(bone.parent);
         if (parent != null) {
            parent.children.add(bone);
            bone.linked = parent;
            bone.attachment = parent;
         } else if (bone.parent != null) {
            Apoli.LOGGER
               .warn("[Apoli] Custom model {} bone '{}' has unknown parent '{}'; treating it as a root bone.", new Object[]{id, bone.name, bone.parent});
         }
      }

      CustomModel bound = assemble(id, ordered, texWidth, texHeight, true);
      if (!bound.hoisted()) {
         return new CustomModel[]{bound, bound};
      }

      for (BedrockModelParser.Bone bone : ordered) {
         bone.resetAssembly();
      }

      return new CustomModel[]{bound, assemble(id, ordered, texWidth, texHeight, false)};
   }

   private static CustomModel assemble(class_2960 id, List<BedrockModelParser.Bone> ordered, int texWidth, int texHeight, boolean bindBodyParts) {
      boolean hoisted = false;

      for (BedrockModelParser.Bone bone : ordered) {
         bone.hoist = bindBodyParts && bone.attachment != null && bone.slot != null;
         hoisted |= bone.hoist;
         if (bone.hoist && hasRotatedAncestor(bone)) {
            Apoli.LOGGER
               .warn(
                  "[Apoli] Custom model {} bone '{}' tracks the vanilla '{}' part, so it is lifted out of its rotated parent '{}'; the parent's rotation no longer applies to it.",
                  new Object[]{id, bone.name, bone.slot, bone.parent}
               );
         }
      }

      for (BedrockModelParser.Bone bone : ordered) {
         if (bone.hoist) {
            bone.attachment = null;
         }
      }

      Set<String> built = new HashSet();
      Map<String, class_630> children = new LinkedHashMap();
      Vector3f origin = new Vector3f(0.0F, 24.0F, 0.0F);

      for (BedrockModelParser.Bone bone : ordered) {
         if (bone.attachment == null) {
            children.put(unique(children, bone.name), build(bone, origin, built, texWidth, texHeight));
         }
      }

      for (BedrockModelParser.Bone bone : ordered) {
         if (!built.contains(bone.name)) {
            if (bindBodyParts) {
               Apoli.LOGGER.warn("[Apoli] Custom model {} bone '{}' is part of a parent cycle; treating it as a root bone.", id, bone.name);
            }

            children.put(unique(children, bone.name), build(bone, origin, built, texWidth, texHeight));
         }
      }

      List<CustomModel.Bone> all = new ArrayList(ordered.size());
      Map<String, List<CustomModel.Bone>> grouped = new HashMap(ordered.size() * 2);

      for (BedrockModelParser.Bone bone : ordered) {
         if (bone.part != null) {
            bone.handle = new CustomModel.Bone(bone.part, ancestorsOf(bone));
            all.add(bone.handle);
            String normalized = ModelParts.normalize(bone.name);
            ((List)grouped.computeIfAbsent(normalized, key -> new ArrayList(1))).add(bone.handle);
            if (bone.slot != null && !bone.slot.equals(normalized)) {
               ((List)grouped.computeIfAbsent(bone.slot, key -> new ArrayList(1))).add(bone.handle);
            }
         }
      }

      Map<String, CustomModel.Bone[]> lookup = new HashMap(grouped.size() * 2);

      for (Entry<String, List<CustomModel.Bone>> entry : grouped.entrySet()) {
         lookup.put((String)entry.getKey(), (CustomModel.Bone[])((List)entry.getValue()).toArray(new CustomModel.Bone[0]));
      }

      return new CustomModel(new class_630(List.of(), children), lookup, (CustomModel.Bone[])all.toArray(new CustomModel.Bone[0]), hoisted);
   }

   private static class_630[] ancestorsOf(BedrockModelParser.Bone bone) {
      int depth = 0;

      for (BedrockModelParser.Bone parent = bone.builtParent; parent != null && depth < 64; parent = parent.builtParent) {
         depth++;
      }

      if (depth == 0) {
         return CustomModel.NO_PARTS;
      }

      class_630[] chain = new class_630[depth];
      BedrockModelParser.Bone parent = bone.builtParent;

      for (int i = depth - 1; i >= 0; i--) {
         chain[i] = parent.part;
         parent = parent.builtParent;
      }

      return chain;
   }

   private static boolean hasRotatedAncestor(BedrockModelParser.Bone bone) {
      BedrockModelParser.Bone parent = bone.attachment;

      for (int depth = 0; parent != null && depth < 64; depth++) {
         if (parent.rotX != 0.0F || parent.rotY != 0.0F || parent.rotZ != 0.0F) {
            return true;
         }

         parent = parent.attachment;
      }

      return parent != null;
   }

   private static class_630 build(BedrockModelParser.Bone bone, Vector3f parentPivot, Set<String> built, int texWidth, int texHeight) {
      built.add(bone.name);
      List<class_628> cubes = new ArrayList();
      Map<String, class_630> children = new LinkedHashMap();
      int rotated = 0;

      for (BedrockModelParser.Cube cube : bone.cubes) {
         if (cube.rotX == 0.0F && cube.rotY == 0.0F && cube.rotZ == 0.0F) {
            cubes.add(bake(cube, bone.pivot, texWidth, texHeight));
         } else {
            String name = unique(children, bone.name + "_r" + rotated++);
            children.put(
               name,
               posed(
                  new class_630(List.of(bake(cube, cube.pivot, texWidth, texHeight)), Map.of()),
                  cube.pivot.x - bone.pivot.x,
                  bone.pivot.y - cube.pivot.y,
                  cube.pivot.z - bone.pivot.z,
                  cube.rotX,
                  cube.rotY,
                  cube.rotZ
               )
            );
         }
      }

      for (BedrockModelParser.Bone child : bone.children) {
         if (child.attachment == bone && !built.contains(child.name)) {
            child.builtParent = bone;
            children.put(unique(children, child.name), build(child, bone.pivot, built, texWidth, texHeight));
         }
      }

      bone.part = posed(
         new class_630(cubes, children),
         bone.pivot.x - parentPivot.x,
         parentPivot.y - bone.pivot.y,
         bone.pivot.z - parentPivot.z,
         bone.rotX,
         bone.rotY,
         bone.rotZ
      );
      return bone.part;
   }

   private static class_630 posed(class_630 part, float x, float y, float z, float xRot, float yRot, float zRot) {
      part.method_41918(class_5603.method_32091(x, y, z, xRot, yRot, zRot));
      part.method_41923();
      return part;
   }

   private static class_628 bake(BedrockModelParser.Cube cube, Vector3f pivot, int texWidth, int texHeight) {
      float x = cube.origin.x - pivot.x;
      float y = pivot.y - cube.origin.y - cube.size.y;
      float z = cube.origin.z - pivot.z;
      return cube.faces != null
         ? new PerFaceCube(cube.faces, x, y, z, cube.size.x, cube.size.y, cube.size.z, cube.inflate, cube.mirror, texWidth, texHeight)
         : new PerFaceCube(cube.u, cube.v, x, y, z, cube.size.x, cube.size.y, cube.size.z, cube.inflate, cube.mirror, texWidth, texHeight);
   }

   private static String unique(Map<String, class_630> taken, String name) {
      if (!taken.containsKey(name)) {
         return name;
      }

      int suffix = 2;

      while (taken.containsKey(name + "_" + suffix)) {
         suffix++;
      }

      return name + "_" + suffix;
   }

   private static <T> BedrockModelParser.Bone parseBone(Dynamic<T> json, class_2960 id) {
      BedrockModelParser.Bone bone = new BedrockModelParser.Bone();
      bone.name = (String)json.get("name").asString().result().orElseThrow(() -> new IllegalArgumentException("a bone is missing its 'name'"));
      bone.slot = ModelParts.slot(ModelParts.normalize(bone.name));
      bone.parent = (String)json.get("parent").asString().result().orElse(null);
      bone.pivot = vector(json, "pivot");
      Vector3f rotation = vector(json, "rotation");
      bone.rotX = rotation.x * (float) (Math.PI / 180.0);
      bone.rotY = rotation.y * (float) (Math.PI / 180.0);
      bone.rotZ = rotation.z * (float) (Math.PI / 180.0);
      boolean mirror = json.get("mirror").asBoolean(false);
      float inflate = (Float)json.get("inflate").asNumber().result().map(Number::floatValue).orElse(0.0F);

      for (Dynamic<T> element : json.get("cubes").asList(Function.identity())) {
         bone.cubes.add(parseCube(element, mirror, inflate, id, bone.name));
      }

      return bone;
   }

   private static <T> BedrockModelParser.Cube parseCube(Dynamic<T> json, boolean boneMirror, float boneInflate, class_2960 id, String boneName) {
      BedrockModelParser.Cube cube = new BedrockModelParser.Cube();
      cube.origin = vector(json, "origin");
      cube.size = vector(json, "size");
      cube.pivot = vector(json, "pivot");
      Vector3f rotation = vector(json, "rotation");
      cube.rotX = rotation.x * (float) (Math.PI / 180.0);
      cube.rotY = rotation.y * (float) (Math.PI / 180.0);
      cube.rotZ = rotation.z * (float) (Math.PI / 180.0);
      cube.inflate = (Float)json.get("inflate").asNumber().result().map(Number::floatValue).orElse(boneInflate);
      cube.mirror = json.get("mirror").asBoolean(boneMirror);
      Dynamic<T> uv = (Dynamic<T>)json.get("uv").result().orElse(null);
      if (uv != null) {
         List<Float> array = numbers(uv);
         if (!array.isEmpty()) {
            cube.u = array.size() > 0 ? (Float)array.get(0) : 0.0F;
            cube.v = array.size() > 1 ? (Float)array.get(1) : 0.0F;
         } else if (uv.getMapValues().result().isPresent()) {
            cube.faces = parseFaces(uv, cube.size, id, boneName);
         }
      }

      return cube;
   }

   private static <T> Map<class_2350, float[]> parseFaces(Dynamic<T> json, Vector3f size, class_2960 id, String boneName) {
      Map<class_2350, float[]> faces = new EnumMap(class_2350.class);
      Map<Dynamic<T>, Dynamic<T>> entries = (Map<Dynamic<T>, Dynamic<T>>)json.getMapValues().result().orElse(Map.of());

      for (Entry<Dynamic<T>, Dynamic<T>> entry : entries.entrySet()) {
         String name = (String)((Dynamic)entry.getKey()).asString().result().orElse(null);
         if (name != null) {
            class_2350 bedrock = class_2350.method_10168(name);
            Dynamic<T> face = (Dynamic<T>)entry.getValue();
            if (bedrock != null && !face.getMapValues().result().isEmpty()) {
               List<Float> offset = numbers((Dynamic<T>)face.get("uv").result().orElse(null));
               if (!offset.isEmpty()) {
                  if (face.get("uv_rotation").result().isPresent()) {
                     Apoli.LOGGER
                        .warn(
                           "[Apoli] Custom model {} bone '{}' uses uv_rotation on the {} face; rotated face UVs are not supported and will render unrotated.",
                           new Object[]{id, boneName, name}
                        );
                  }

                  class_2350 java = bedrock.method_10166() == class_2351.field_11051 ? bedrock : bedrock.method_10153();
                  float[] uv = new float[]{
                     offset.size() > 0 ? (Float)offset.get(0) : 0.0F,
                     offset.size() > 1 ? (Float)offset.get(1) : 0.0F,
                     naturalWidth(java, size),
                     naturalHeight(java, size)
                  };
                  List<Float> extent = numbers((Dynamic<T>)face.get("uv_size").result().orElse(null));
                  if (extent.size() > 0) {
                     uv[2] = (Float)extent.get(0);
                  }

                  if (extent.size() > 1) {
                     uv[3] = (Float)extent.get(1);
                  }

                  faces.put(java, uv);
               }
            }
         }
      }

      return faces;
   }

   private static <T> List<Float> numbers(Dynamic<T> data) {
      return data == null
         ? List.of()
         : (List)data.asStreamOpt()
            .result()
            .map(stream -> stream.map(element -> (Float)element.asNumber().result().map(Number::floatValue).orElse(0.0F)).toList())
            .orElse(List.of());
   }

   private static float naturalWidth(class_2350 face, Vector3f size) {
      return face.method_10166() == class_2351.field_11048 ? size.z : size.x;
   }

   private static float naturalHeight(class_2350 face, Vector3f size) {
      return face.method_10166() == class_2351.field_11052 ? size.z : size.y;
   }

   private static <T> Vector3f vector(Dynamic<T> json, String key) {
      Vector3f out = new Vector3f();
      List<Float> array = numbers((Dynamic<T>)json.get(key).result().orElse(null));
      if (array.size() > 0) {
         out.x = (Float)array.get(0);
      }

      if (array.size() > 1) {
         out.y = (Float)array.get(1);
      }

      if (array.size() > 2) {
         out.z = (Float)array.get(2);
      }

      return out;
   }

   private static final class Bone {
      String name;
      String parent;
      @Nullable
      String slot;
      @Nullable
      BedrockModelParser.Bone linked;
      @Nullable
      BedrockModelParser.Bone attachment;
      @Nullable
      BedrockModelParser.Bone builtParent;
      boolean hoist;
      @Nullable
      class_630 part;
      @Nullable
      CustomModel.Bone handle;
      Vector3f pivot = new Vector3f();
      float rotX;
      float rotY;
      float rotZ;
      final List<BedrockModelParser.Bone> children = new ArrayList();
      final List<BedrockModelParser.Cube> cubes = new ArrayList();

      void resetAssembly() {
         this.attachment = this.linked;
         this.builtParent = null;
         this.hoist = false;
         this.part = null;
         this.handle = null;
      }
   }

   private static final class Cube {
      Vector3f origin = new Vector3f();
      Vector3f size = new Vector3f();
      Vector3f pivot = new Vector3f();
      float rotX;
      float rotY;
      float rotZ;
      float inflate;
      boolean mirror;
      float u;
      float v;
      Map<class_2350, float[]> faces;
   }
}
