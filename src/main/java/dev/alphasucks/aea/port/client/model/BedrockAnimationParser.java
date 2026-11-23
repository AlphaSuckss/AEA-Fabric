package dev.alphasucks.aea.port.client.model;

import com.mojang.serialization.Dynamic;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.data.ModelParts;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class BedrockAnimationParser {
   private static final float[] ZERO = new float[]{0.0F, 0.0F, 0.0F};
   private static final float[] ONE = new float[]{1.0F, 1.0F, 1.0F};
   private static final String VECTOR = "vector";

   private BedrockAnimationParser() {
   }

   public static <T> Map<String, BedrockAnimation> parse(class_2960 id, Dynamic<T> json) {
      Map<Dynamic<T>, Dynamic<T>> declared = (Map<Dynamic<T>, Dynamic<T>>)json.get("animations")
         .result()
         .flatMap(node -> node.getMapValues().result())
         .orElse(Map.of());
      if (declared.isEmpty()) {
         return Map.of();
      }

      Map<String, BedrockAnimation> out = new LinkedHashMap(declared.size());

      for (Entry<Dynamic<T>, Dynamic<T>> entry : declared.entrySet()) {
         String name = (String)((Dynamic)entry.getKey()).asString().result().orElse(null);
         if (name != null) {
            try {
               out.put(name, parseAnimation((Dynamic<T>)entry.getValue()));
            } catch (Exception e) {
               Apoli.LOGGER.error("[Apoli] Failed to load animation '{}' from {}: {}", new Object[]{name, id, e.getMessage()});
            }
         }
      }

      return out;
   }

   private static <T> BedrockAnimation parseAnimation(Dynamic<T> json) {
      BedrockAnimation.Loop loop = parseLoop((Dynamic<T>)json.get("loop").result().orElse(null));
      List<BedrockAnimation.Bone> bones = new ArrayList();
      Map<Dynamic<T>, Dynamic<T>> declared = (Map<Dynamic<T>, Dynamic<T>>)json.get("bones")
         .result()
         .flatMap(node -> node.getMapValues().result())
         .orElse(Map.of());
      float longest = 0.0F;

      for (Entry<Dynamic<T>, Dynamic<T>> entry : declared.entrySet()) {
         String name = (String)((Dynamic)entry.getKey()).asString().result().orElse(null);
         if (name != null) {
            Dynamic<T> channels = (Dynamic<T>)entry.getValue();
            BedrockAnimation.Track position = parseTrack((Dynamic<T>)channels.get("position").result().orElse(null), ZERO);
            BedrockAnimation.Track rotation = parseTrack((Dynamic<T>)channels.get("rotation").result().orElse(null), ZERO);
            BedrockAnimation.Track scale = parseTrack((Dynamic<T>)channels.get("scale").result().orElse(null), ONE);
            if (position != null || rotation != null || scale != null) {
               longest = Math.max(longest, longest(position, rotation, scale));
               bones.add(new BedrockAnimation.Bone(ModelParts.normalize(name), position, rotation, scale));
            }
         }
      }

      float length = (Float)json.get("animation_length").asNumber().result().map(Number::floatValue).orElse(longest);
      return new BedrockAnimation(length, loop, (BedrockAnimation.Bone[])bones.toArray(new BedrockAnimation.Bone[0]));
   }

   private static float longest(@Nullable BedrockAnimation.Track... tracks) {
      float end = 0.0F;

      for (BedrockAnimation.Track track : tracks) {
         if (track != null) {
            end = Math.max(end, track.end());
         }
      }

      return end;
   }

   private static <T> BedrockAnimation.Loop parseLoop(@Nullable Dynamic<T> json) {
      if (json == null) {
         return BedrockAnimation.Loop.ONCE;
      } else if ((Boolean)json.asString().result().map("hold_on_last_frame"::equals).orElse(false)) {
         return BedrockAnimation.Loop.HOLD;
      } else {
         return json.asBoolean(false) ? BedrockAnimation.Loop.LOOP : BedrockAnimation.Loop.ONCE;
      }
   }

   @Nullable
   private static <T> BedrockAnimation.Track parseTrack(@Nullable Dynamic<T> json, float[] fallback) {
      if (json == null) {
         return null;
      }

      float[] direct = readVector(json, fallback);
      if (direct != null) {
         return new BedrockAnimation.Track(new float[]{0.0F}, direct, (float[])direct.clone());
      }

      Map<Dynamic<T>, Dynamic<T>> entries = (Map<Dynamic<T>, Dynamic<T>>)json.getMapValues().result().orElse(null);
      if (entries != null && !entries.isEmpty()) {
         if (entries.keySet()
            .stream()
            .anyMatch(key -> "pre".equals(key.asString().result().orElse(null)) || "post".equals(key.asString().result().orElse(null)))) {
            float[] pre = keyframe(json, fallback, true);
            float[] post = keyframe(json, fallback, false);
            return new BedrockAnimation.Track(new float[]{0.0F}, pre, post);
         }

         List<BedrockAnimationParser.Keyframe> keyframes = new ArrayList(entries.size());

         for (Entry<Dynamic<T>, Dynamic<T>> entry : entries.entrySet()) {
            String stamp = (String)((Dynamic)entry.getKey()).asString().result().orElse(null);
            if (stamp != null) {
               float time;
               try {
                  time = Float.parseFloat(stamp);
               } catch (NumberFormatException e) {
                  continue;
               }

               Dynamic<T> value = (Dynamic<T>)entry.getValue();
               keyframes.add(
                  new BedrockAnimationParser.Keyframe(
                     time, keyframe(value, fallback, true), keyframe(value, fallback, false), easingOf(value), easingArgOf(value)
                  )
               );
            }
         }

         if (keyframes.isEmpty()) {
            return null;
         }

         keyframes.sort((a, b) -> Float.compare(a.time, b.time));
         int count = keyframes.size();
         float[] times = new float[count];
         float[] pre = new float[count * 3];
         float[] post = new float[count * 3];
         byte[] easing = new byte[count];
         float[] easingArg = new float[count];
         boolean shaped = false;

         for (int i = 0; i < count; i++) {
            BedrockAnimationParser.Keyframe frame = (BedrockAnimationParser.Keyframe)keyframes.get(i);
            times[i] = frame.time;
            System.arraycopy(frame.pre, 0, pre, i * 3, 3);
            System.arraycopy(frame.post, 0, post, i * 3, 3);
            easing[i] = (byte)frame.easing.ordinal();
            easingArg[i] = frame.arg;
            if (i > 0 && frame.easing != BedrockEasing.LINEAR && frame.easing != BedrockEasing.NONE) {
               shaped = true;
            }
         }

         return shaped ? new BedrockAnimation.Track(times, pre, post, easing, easingArg) : new BedrockAnimation.Track(times, pre, post);
      } else {
         return null;
      }
   }

   private static <T> BedrockEasing easingOf(Dynamic<T> json) {
      String name = (String)json.get("easing").asString().result().orElse(null);
      if (name == null) {
         name = (String)json.get("lerp_mode").asString().result().orElse(null);
      }

      return name == null ? BedrockEasing.LINEAR : BedrockEasing.byName(name);
   }

   private static <T> float easingArgOf(Dynamic<T> json) {
      Dynamic<T> args = (Dynamic<T>)json.get("easingArgs").result().orElse(null);
      return args == null
         ? Float.NaN
         : (Float)args.asStreamOpt().result().flatMap(stream -> stream.findFirst()).map(BedrockAnimationParser::number).orElse(Float.NaN);
   }

   private static <T> float[] keyframe(Dynamic<T> json, float[] fallback, boolean pre) {
      float[] direct = readVector(json, fallback);
      if (direct != null) {
         return direct;
      }

      Dynamic<T> side = (Dynamic<T>)json.get(pre ? "pre" : "post").result().orElse(null);
      if (side == null) {
         side = (Dynamic<T>)json.get(pre ? "post" : "pre").result().orElse(null);
      }

      if (side != null) {
         float[] read = readVector(side, fallback);
         if (read != null) {
            return read;
         }
      }

      return (float[])fallback.clone();
   }

   @Nullable
   private static <T> float[] readVector(Dynamic<T> json, float[] fallback) {
      List<Dynamic<T>> array = (List<Dynamic<T>>)json.asStreamOpt().result().map(stream -> stream.toList()).orElse(null);
      if (array == null) {
         if (json.asNumber().result().isPresent()) {
            float uniform = ((Number)json.asNumber().result().get()).floatValue();
            return new float[]{uniform, uniform, uniform};
         } else {
            Dynamic<T> wrapped = (Dynamic<T>)json.get("vector").result().orElse(null);
            return wrapped == null ? null : readVector(wrapped, fallback);
         }
      } else {
         float[] out = (float[])fallback.clone();

         for (int i = 0; i < 3 && i < array.size(); i++) {
            out[i] = number((Dynamic<T>)array.get(i));
         }

         return out;
      }
   }

   private static <T> float number(Dynamic<T> json) {
      return (Float)json.asNumber().result().map(Number::floatValue).orElseGet(() -> {
         String raw = (String)json.asString().result().orElse(null);
         if (raw == null) {
            return 0.0F;
         }

         try {
            return Float.parseFloat(raw.trim());
         } catch (NumberFormatException e) {
            return 0.0F;
         }
      });
   }

   private record Keyframe(float time, float[] pre, float[] post, BedrockEasing easing, float arg) {
   }
}
