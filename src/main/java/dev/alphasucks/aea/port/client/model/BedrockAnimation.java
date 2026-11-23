package dev.alphasucks.aea.port.client.model;

import dev.alphasucks.aea.port.data.BodyPart;
import dev.alphasucks.aea.port.data.BodyParts;
import dev.alphasucks.aea.port.data.ModelAnimation.LoopMode;
import org.jetbrains.annotations.Nullable;

public final class BedrockAnimation {
   private final float length;
   private final BedrockAnimation.Loop loop;
   private final BedrockAnimation.Bone[] bones;

   public BedrockAnimation(float length, BedrockAnimation.Loop loop, BedrockAnimation.Bone[] bones) {
      this.length = length;
      this.loop = loop;
      this.bones = bones;
   }

   public float length() {
      return this.length;
   }

   public BedrockAnimation.Loop loop() {
      return this.loop;
   }

   public BedrockAnimation.Bone[] bones() {
      return this.bones;
   }

   public float timeFor(float elapsed, @Nullable LoopMode override) {
      BedrockAnimation.Loop var10000;
      if (override == null) {
         var10000 = this.loop;
      } else {
         switch (override) {
            case LOOP:
               var10000 = BedrockAnimation.Loop.LOOP;
               break;
            case HOLD:
               var10000 = BedrockAnimation.Loop.HOLD;
               break;
            case ONCE:
               var10000 = BedrockAnimation.Loop.ONCE;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }
      }

      BedrockAnimation.Loop mode = var10000;
      if (this.length <= 0.0F) {
         return 0.0F;
      }

      return switch (mode) {
         case ONCE -> elapsed > this.length ? -1.0F : elapsed;
         case HOLD -> Math.min(elapsed, this.length);
         case LOOP -> elapsed % this.length;
      };
   }

   public static final class Bone {
      public final String name;
      public final BodyPart part;
      @Nullable
      public final BedrockAnimation.Track position;
      @Nullable
      public final BedrockAnimation.Track rotation;
      @Nullable
      public final BedrockAnimation.Track scale;

      public Bone(String name, @Nullable BedrockAnimation.Track position, @Nullable BedrockAnimation.Track rotation, @Nullable BedrockAnimation.Track scale) {
         this.name = name;
         this.part = BodyParts.lookupOrCustom(name);
         this.position = position;
         this.rotation = rotation;
         this.scale = scale;
      }
   }

   public enum Loop {
      ONCE,
      HOLD,
      LOOP;
   }

   public static final class Track {
      private final float[] times;
      private final float[] pre;
      private final float[] post;
      @Nullable
      private final byte[] easing;
      @Nullable
      private final float[] easingArg;

      public Track(float[] times, float[] pre, float[] post) {
         this(times, pre, post, null, null);
      }

      public Track(float[] times, float[] pre, float[] post, @Nullable byte[] easing, @Nullable float[] easingArg) {
         this.times = times;
         this.pre = pre;
         this.post = post;
         this.easing = easing;
         this.easingArg = easingArg;
      }

      public float end() {
         return this.times.length == 0 ? 0.0F : this.times[this.times.length - 1];
      }

      public void sample(float time, float[] out) {
         int count = this.times.length;
         if (count != 0) {
            if (time <= this.times[0]) {
               copy(this.pre, 0, out);
            } else {
               int last = count - 1;
               if (time >= this.times[last]) {
                  copy(this.post, last, out);
               } else {
                  int index = 0;

                  while (index < last && this.times[index + 1] <= time) {
                     index++;
                  }

                  float span = this.times[index + 1] - this.times[index];
                  float delta = span <= 0.0F ? 0.0F : (time - this.times[index]) / span;
                  int from = index * 3;
                  int to = (index + 1) * 3;
                  if (this.easing == null) {
                     this.lerp(from, to, delta, out);
                  } else {
                     BedrockEasing mode = BedrockEasing.byIndex(this.easing[index + 1]);
                     if (mode != BedrockEasing.CATMULLROM) {
                        this.lerp(from, to, mode.ease(delta, this.easingArg[index + 1]), out);
                     } else {
                        int before = index > 0 ? (index - 1) * 3 : from;
                        int after = index + 2 <= last ? (index + 2) * 3 : to;

                        for (int axis = 0; axis < 3; axis++) {
                           out[axis] = spline(delta, this.post[before + axis], this.post[from + axis], this.pre[to + axis], this.pre[after + axis]);
                        }
                     }
                  }
               }
            }
         }
      }

      private void lerp(int from, int to, float delta, float[] out) {
         out[0] = this.post[from] + (this.pre[to] - this.post[from]) * delta;
         out[1] = this.post[from + 1] + (this.pre[to + 1] - this.post[from + 1]) * delta;
         out[2] = this.post[from + 2] + (this.pre[to + 2] - this.post[from + 2]) * delta;
      }

      private static float spline(float delta, float p0, float p1, float p2, float p3) {
         return 0.5F
            * (
               2.0F * p1
                  + (p2 - p0) * delta
                  + (2.0F * p0 - 5.0F * p1 + 4.0F * p2 - p3) * delta * delta
                  + (3.0F * p1 - p0 - 3.0F * p2 + p3) * delta * delta * delta
            );
      }

      private static void copy(float[] source, int keyframe, float[] out) {
         int base = keyframe * 3;
         out[0] = source[base];
         out[1] = source[base + 1];
         out[2] = source[base + 2];
      }
   }
}
