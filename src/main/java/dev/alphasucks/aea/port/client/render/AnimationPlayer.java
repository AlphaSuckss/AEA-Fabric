package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.client.model.BedrockAnimation;
import dev.alphasucks.aea.port.client.model.CustomModel;
import dev.alphasucks.aea.port.client.model.BedrockAnimation.Bone;
import dev.alphasucks.aea.port.data.ModelAnimation;
import dev.alphasucks.aea.port.data.ModelAnimation.Entry;
import dev.alphasucks.aea.port.data.ModelAnimation.LoopMode;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower.GeometryRender;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_572;
import net.minecraft.class_630;
import org.jetbrains.annotations.Nullable;

public final class AnimationPlayer {
   private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);
   private static final String ROOT_BONE = "root";
   private static final ThreadLocal<float[]> SCRATCH = ThreadLocal.withInitial(() -> new float[3]);
   private static final ThreadLocal<List<class_630>> PARTS = ThreadLocal.withInitial(() -> new ArrayList(2));
   private static final Map<class_2960, Set<String>> WARNED = new HashMap();

   private AnimationPlayer() {
   }

   public static void clearWarnings() {
      WARNED.clear();
   }

   public static void apply(class_1297 entity, GeometryRender render, CustomModel model, float partialTick) {
      Entry entry = entry(entity, render);
      if (entry != null) {
         BedrockAnimation animation = animation(entry);
         if (animation != null) {
            float time = time(entry, animation, AnimationPlayback.elapsed(entity.method_5628(), render.model(), entry, entity.field_6012, partialTick));
            if (!(time < 0.0F)) {
               apply(model, animation, time);
            }
         }
      }
   }

   @Nullable
   public static Entry entry(class_1297 entity, GeometryRender render) {
      if (render.animations().isEmpty()) {
         return null;
      }

      Entry entry = ((ModelAnimation)render.animations().get()).select(entity);
      if (entry == null && warnOnce(render.model(), "")) {
         Apoli.LOGGER
            .warn(
               "[Apoli] No animation entry for the custom model '{}' matched, so it stays in its bind pose. Every entry has a condition and none of them passed — add a last entry with no condition as the fallback.",
               render.model()
            );
      }

      return entry;
   }

   @Nullable
   public static BedrockAnimation animation(Entry entry) {
      BedrockAnimation animation = AnimationManager.get(entry.animation(), (String)entry.name().orElse(null));
      if (animation != null && animation.length() <= 0.0F) {
         String still = (String)entry.name().orElse("");
         if (warnOnce(entry.animation(), "static:" + still)) {
            Apoli.LOGGER
               .warn(
                  "[Apoli] The animation '{}'{} holds a single pose and never moves — every channel is one value with no timecode, and the file declares no 'animation_length'. Apoli is applying that pose. If you meant it to move, the clip needs keyframes at more than one time in Blockbench.",
                  entry.animation(),
                  still.isEmpty() ? "" : " ('" + still + "')"
               );
         }
      }

      return animation;
   }

   public static float time(Entry entry, BedrockAnimation animation, float elapsed) {
      float time = animation.timeFor(elapsed, (LoopMode)entry.loop().orElse(null));
      if (time < 0.0F) {
         String clip = (String)entry.name().orElse("");
         if (warnOnce(entry.animation(), clip)) {
            Apoli.LOGGER
               .warn(
                  "[Apoli] The animation '{}'{} has already finished and does not loop, so the model is back in its bind pose. Blockbench writes no loop flag for a 'Play Once' animation, and playback only restarts when the selected entry changes — set the clip to Loop in Blockbench, or put \"loop\": true on the animations entry.",
                  entry.animation(),
                  clip.isEmpty() ? "" : " ('" + clip + "')"
               );
         }
      }

      return time;
   }

   private static boolean warnOnce(class_2960 id, String detail) {
      return ((Set)WARNED.computeIfAbsent(id, key -> new HashSet(2))).add(detail);
   }

   public static void apply(CustomModel model, BedrockAnimation animation, float time) {
      Bone[] bones = animation.bones();
      float[] scratch = (float[])SCRATCH.get();

      for (int i = 0; i < bones.length; i++) {
         Bone bone = bones[i];
         dev.alphasucks.aea.port.client.model.CustomModel.Bone[] bound = model.bones(bone.name);

         for (int j = 0; j < bound.length; j++) {
            applyTo(bound[j].part, bone, time, scratch);
         }
      }
   }

   public static void apply(class_572<?> model, class_1297 entity, ModelAnimation animations, class_2960 slot, float partialTick) {
      Entry entry = animations.select(entity);
      if (entry != null) {
         BedrockAnimation animation = AnimationManager.get(entry.animation(), (String)entry.name().orElse(null));
         if (animation != null) {
            float elapsed = AnimationPlayback.elapsed(entity.method_5628(), slot, entry, entity.field_6012, partialTick);
            float time = animation.timeFor(elapsed, (LoopMode)entry.loop().orElse(null));
            if (!(time < 0.0F)) {
               apply(model, animation, time);
            }
         }
      }
   }

   public static void apply(class_572<?> model, BedrockAnimation animation, float time) {
      Bone[] bones = animation.bones();
      float[] scratch = (float[])SCRATCH.get();
      List<class_630> parts = (List<class_630>)PARTS.get();

      for (int i = 0; i < bones.length; i++) {
         Bone bone = bones[i];
         parts.clear();
         if ("root".equals(bone.part.key())) {
            if (bone.position != null) {
               bone.position.sample(time, scratch);
               ModelPartLookup.allPartsInto(model, parts);

               for (int j = 0; j < parts.size(); j++) {
                  class_630 part = (class_630)parts.get(j);
                  part.field_3657 = part.field_3657 + scratch[0];
                  part.field_3656 = part.field_3656 - scratch[1];
                  part.field_3655 = part.field_3655 + scratch[2];
               }
            }
         } else {
            ModelPartLookup.resolveInto(model, bone.part, parts);

            for (int j = 0; j < parts.size(); j++) {
               applyTo((class_630)parts.get(j), bone, time, scratch);
            }
         }
      }

      parts.clear();
   }

   private static void applyTo(class_630 part, Bone bone, float time, float[] scratch) {
      if (bone.position != null) {
         bone.position.sample(time, scratch);
         part.field_3657 = part.field_3657 + scratch[0];
         part.field_3656 = part.field_3656 - scratch[1];
         part.field_3655 = part.field_3655 + scratch[2];
      }

      if (bone.rotation != null) {
         bone.rotation.sample(time, scratch);
         part.field_3654 = part.field_3654 + scratch[0] * (float) (Math.PI / 180.0);
         part.field_3675 = part.field_3675 + scratch[1] * (float) (Math.PI / 180.0);
         part.field_3674 = part.field_3674 + scratch[2] * (float) (Math.PI / 180.0);
      }

      if (bone.scale != null) {
         bone.scale.sample(time, scratch);
         part.field_37938 = part.field_37938 * scratch[0];
         part.field_37939 = part.field_37939 * scratch[1];
         part.field_37940 = part.field_37940 * scratch[2];
      }
   }
}
