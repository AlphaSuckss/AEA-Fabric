package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.class_1297;
import org.jetbrains.annotations.Nullable;

public final class ModelPartTransformation {
   private static final Expression FULL_REACH = Expression.constant(1.0);
   private final BodyPart bodyPart;
   private final Optional<Vector> pivot;
   private final Optional<Vector> target;
   private final ModelPartTransformation.Type type;
   private final Optional<Expression> rawValue;
   private final Expression value;
   private final boolean overrideAnimation;
   private final List<ModelPartTransformation.Keyframe> keyframes;
   private final boolean loop;
   private final float duration;
   private final Optional<Float> rawFadeOutDuration;
   private final float fadeOutDuration;
   private final Easing easing;
   private final Optional<List<Perspective>> perspectives;
   private final int perspectiveMask;
   private final float timelineStart;
   private final float timelineEnd;
   private final float[] valueTimes;
   private final Expression[] values;
   private final Easing[] valueEasings;
   private final float[] targetTimes;
   private final float[] targets;
   private final Easing[] targetEasings;
   public static final Codec<ModelPartTransformation> CODEC = RecordCodecBuilder.<ModelPartTransformation>create(
         instance -> instance.group(
               BodyPart.CODEC.fieldOf("model_part").forGetter(ModelPartTransformation::bodyPart),
               ModelPartTransformation.Type.CODEC.fieldOf("type").forGetter(ModelPartTransformation::type),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("value").forGetter(ModelPartTransformation::rawValue),
               Codec.BOOL.optionalFieldOf("override_animation", false).forGetter(ModelPartTransformation::overrideAnimation),
               ModelPartTransformation.Keyframe.CODEC.listOf().optionalFieldOf("keyframes", List.of()).forGetter(ModelPartTransformation::keyframes),
               Codec.BOOL.optionalFieldOf("loop", false).forGetter(ModelPartTransformation::loop),
               Codec.FLOAT.optionalFieldOf("duration", 0.0F).forGetter(ModelPartTransformation::duration),
               Codec.FLOAT.optionalFieldOf("fade_out_duration").forGetter(ModelPartTransformation::rawFadeOutDuration),
               Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(ModelPartTransformation::easing),
               Perspective.LIST_CODEC.optionalFieldOf("perspectives").forGetter(ModelPartTransformation::perspectives),
               LoggedOptionalField.of("pivot", Vector.CODEC).forGetter(ModelPartTransformation::pivot),
               LoggedOptionalField.of("target", Vector.CODEC).forGetter(ModelPartTransformation::target)
            )
            .apply(instance, ModelPartTransformation::new)
      )
      .comapFlatMap(ModelPartTransformation::validate, Function.identity());

   public ModelPartTransformation(
      BodyPart bodyPart,
      ModelPartTransformation.Type type,
      Optional<Expression> value,
      boolean overrideAnimation,
      List<ModelPartTransformation.Keyframe> keyframes,
      boolean loop,
      float duration,
      Optional<Float> fadeOutDuration,
      Easing easing,
      Optional<List<Perspective>> perspectives,
      Optional<Vector> pivot,
      Optional<Vector> target
   ) {
      this.bodyPart = bodyPart;
      this.pivot = pivot;
      this.target = target;
      this.type = type;
      this.rawValue = value;
      this.value = (Expression)value.orElse(type == ModelPartTransformation.Type.REACH ? FULL_REACH : Expression.constant(0.0));
      this.overrideAnimation = overrideAnimation;
      this.keyframes = sortByTime(keyframes);
      this.loop = loop;
      this.duration = duration;
      this.rawFadeOutDuration = fadeOutDuration;
      this.fadeOutDuration = (Float)fadeOutDuration.orElse(duration);
      this.easing = easing;
      this.perspectives = perspectives;
      this.perspectiveMask = (Integer)perspectives.map(Perspective::maskOf).orElse(-1);
      this.timelineStart = this.keyframes.isEmpty() ? 0.0F : ((ModelPartTransformation.Keyframe)this.keyframes.get(0)).time();
      this.timelineEnd = this.keyframes.isEmpty() ? 0.0F : ((ModelPartTransformation.Keyframe)this.keyframes.get(this.keyframes.size() - 1)).time();
      int valueCount = 0;
      int targetCount = 0;

      for (ModelPartTransformation.Keyframe keyframe : this.keyframes) {
         if (keyframe.value().isPresent()) {
            valueCount++;
         }

         if (keyframe.target().isPresent()) {
            targetCount++;
         }
      }

      boolean fixedTarget = targetCount == 0 && target.isPresent();
      this.valueTimes = new float[valueCount];
      this.values = new Expression[valueCount];
      this.valueEasings = new Easing[valueCount];
      this.targetTimes = new float[fixedTarget ? 1 : targetCount];
      this.targets = new float[this.targetTimes.length * 3];
      this.targetEasings = new Easing[this.targetTimes.length];
      int v = 0;
      int t = 0;

      for (ModelPartTransformation.Keyframe keyframe : this.keyframes) {
         Easing curve = (Easing)keyframe.easing().orElse(easing);
         if (keyframe.value().isPresent()) {
            this.valueTimes[v] = keyframe.time();
            this.values[v] = (Expression)keyframe.value().get();
            this.valueEasings[v] = curve;
            v++;
         }

         if (keyframe.target().isPresent()) {
            Vector point = (Vector)keyframe.target().get();
            this.targetTimes[t] = keyframe.time();
            this.targets[t * 3] = point.x();
            this.targets[t * 3 + 1] = point.y();
            this.targets[t * 3 + 2] = point.z();
            this.targetEasings[t] = curve;
            t++;
         }
      }

      if (fixedTarget) {
         Vector point = (Vector)target.get();
         this.targets[0] = point.x();
         this.targets[1] = point.y();
         this.targets[2] = point.z();
         this.targetEasings[0] = easing;
      }
   }

   private static List<ModelPartTransformation.Keyframe> sortByTime(List<ModelPartTransformation.Keyframe> keyframes) {
      if (keyframes.size() < 2) {
         return keyframes;
      }

      List<ModelPartTransformation.Keyframe> sorted = new ArrayList(keyframes);
      sorted.sort((a, b) -> Float.compare(a.time(), b.time()));
      return List.copyOf(sorted);
   }

   public BodyPart bodyPart() {
      return this.bodyPart;
   }

   public Optional<Vector> pivot() {
      return this.pivot;
   }

   public Optional<Vector> target() {
      return this.target;
   }

   public ModelPartTransformation.Type type() {
      return this.type;
   }

   public Optional<Expression> rawValue() {
      return this.rawValue;
   }

   public Expression value() {
      return this.value;
   }

   public float valueFor(@Nullable class_1297 entity) {
      return (float)this.value.eval(entity);
   }

   public boolean overrideAnimation() {
      return this.overrideAnimation;
   }

   public List<ModelPartTransformation.Keyframe> keyframes() {
      return this.keyframes;
   }

   public boolean loop() {
      return this.loop;
   }

   public float duration() {
      return this.duration;
   }

   public Optional<Float> rawFadeOutDuration() {
      return this.rawFadeOutDuration;
   }

   public float fadeOutDuration() {
      return this.fadeOutDuration;
   }

   public Easing easing() {
      return this.easing;
   }

   public Optional<List<Perspective>> perspectives() {
      return this.perspectives;
   }

   public int perspectiveMask() {
      return this.perspectiveMask;
   }

   public float sample(float elapsed, @Nullable class_1297 entity) {
      int size = this.values.length;
      if (size == 0) {
         return this.valueFor(entity);
      }

      if (size == 1) {
         return this.valueAt(0, entity);
      }

      float t = this.localTime(elapsed);
      float[] times = this.valueTimes;
      if (t <= times[0]) {
         return this.valueAt(0, entity);
      }

      if (t >= times[size - 1]) {
         return this.valueAt(size - 1, entity);
      }

      int index = segment(times, t);
      float local = progress(times, index, t);
      Easing curve = this.valueEasings[index];
      float fromValue = this.valueAt(index - 1, entity);
      float toValue = this.valueAt(index, entity);
      return curve == Easing.CATMULLROM
         ? catmullRom(this.valueAt(this.wrap(index - 2, size), entity), fromValue, toValue, this.valueAt(this.wrap(index + 1, size), entity), local)
         : fromValue + (toValue - fromValue) * curve.apply(local);
   }

   public boolean sampleTarget(float elapsed, float[] out) {
      int size = this.targetTimes.length;
      if (size == 0) {
         return false;
      }

      float[] points = this.targets;
      if (size == 1) {
         this.copyTarget(0, out);
         return true;
      }

      float t = this.localTime(elapsed);
      float[] times = this.targetTimes;
      if (t <= times[0]) {
         this.copyTarget(0, out);
         return true;
      }

      if (t >= times[size - 1]) {
         this.copyTarget(size - 1, out);
         return true;
      }

      int index = segment(times, t);
      float local = progress(times, index, t);
      Easing curve = this.targetEasings[index];
      int from = (index - 1) * 3;
      int to = index * 3;
      if (curve == Easing.CATMULLROM) {
         int before = this.wrap(index - 2, size) * 3;
         int after = this.wrap(index + 1, size) * 3;

         for (int axis = 0; axis < 3; axis++) {
            out[axis] = catmullRom(points[before + axis], points[from + axis], points[to + axis], points[after + axis], local);
         }

         return true;
      } else {
         float eased = curve.apply(local);

         for (int axis = 0; axis < 3; axis++) {
            out[axis] = points[from + axis] + (points[to + axis] - points[from + axis]) * eased;
         }

         return true;
      }
   }

   private void copyTarget(int index, float[] out) {
      out[0] = this.targets[index * 3];
      out[1] = this.targets[index * 3 + 1];
      out[2] = this.targets[index * 3 + 2];
   }

   private float valueAt(int index, @Nullable class_1297 entity) {
      return (float)this.values[index].eval(entity);
   }

   private float localTime(float elapsed) {
      float span = this.timelineEnd - this.timelineStart;
      return this.loop && span > 0.0F ? this.timelineStart + ((elapsed - this.timelineStart) % span + span) % span : elapsed;
   }

   private static int segment(float[] times, float t) {
      int index = 1;

      while (index < times.length - 1 && times[index] <= t) {
         index++;
      }

      return index;
   }

   private static float progress(float[] times, int index, float t) {
      float segment = times[index] - times[index - 1];
      return segment <= 0.0F ? 1.0F : (t - times[index - 1]) / segment;
   }

   private int wrap(int index, int size) {
      return this.loop ? (index % size + size) % size : Math.max(0, Math.min(size - 1, index));
   }

   private static float catmullRom(float p0, float p1, float p2, float p3, float t) {
      float t2 = t * t;
      float t3 = t2 * t;
      return 0.5F * (2.0F * p1 + (p2 - p0) * t + (2.0F * p0 - 5.0F * p1 + 4.0F * p2 - p3) * t2 + (3.0F * p1 - p0 - 3.0F * p2 + p3) * t3);
   }

   private DataResult<ModelPartTransformation> validate() {
      if (this.type == ModelPartTransformation.Type.REACH && this.targetTimes.length == 0) {
         return DataResult.error(
            () -> "Model part transformation for '"
               + this.bodyPart
               + "' has type 'reach' but no point to reach: set 'target', or give its keyframes a 'target'"
         );
      }

      if (this.rawValue.isEmpty() && this.keyframes.isEmpty() && this.type != ModelPartTransformation.Type.REACH) {
         return DataResult.error(() -> "Model part transformation for '" + this.bodyPart + "' needs either a 'value' or a non-empty 'keyframes' list");
      }

      for (ModelPartTransformation.Keyframe keyframe : this.keyframes) {
         if (!keyframe.value().isPresent()) {
            if (this.type != ModelPartTransformation.Type.REACH) {
               return DataResult.error(
                  () -> "Model part transformation for '" + this.bodyPart + "' has a keyframe at time " + keyframe.time() + " with no 'value'"
               );
            }

            if (keyframe.target().isEmpty()) {
               return DataResult.error(
                  () -> "Model part transformation for '"
                     + this.bodyPart
                     + "' has a keyframe at time "
                     + keyframe.time()
                     + " with neither a 'value' nor a 'target'"
               );
            }
         }
      }

      return !(this.duration < 0.0F) && !(this.fadeOutDuration < 0.0F)
         ? DataResult.success(this)
         : DataResult.error(() -> "Model part transformation for '" + this.bodyPart + "' has a negative duration");
   }

   public record Keyframe(float time, Optional<Expression> value, Optional<Vector> target, Optional<Easing> easing) {
      public static final Codec<ModelPartTransformation.Keyframe> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               Codec.FLOAT.fieldOf("time").forGetter(ModelPartTransformation.Keyframe::time),
               LoggedOptionalField.strict("value", Expression.FLOAT_OR_EXPR).forGetter(ModelPartTransformation.Keyframe::value),
               LoggedOptionalField.strict("target", Vector.CODEC).forGetter(ModelPartTransformation.Keyframe::target),
               Easing.CODEC.optionalFieldOf("easing").forGetter(ModelPartTransformation.Keyframe::easing)
            )
            .apply(instance, ModelPartTransformation.Keyframe::new)
      );
   }

   public enum Type {
      PITCH,
      YAW,
      ROLL,
      VISIBLE,
      HIDDEN,
      X_SCALE,
      Y_SCALE,
      Z_SCALE,
      PIVOT_X,
      PIVOT_Y,
      PIVOT_Z,
      REACH;

      private static final Map<String, ModelPartTransformation.Type> BY_NAME = new HashMap();
      public static final Codec<ModelPartTransformation.Type> CODEC;

      static {
         for (ModelPartTransformation.Type type : values()) {
            BY_NAME.put(ModelParts.normalize(type.name()), type);
         }

         CODEC = Codec.STRING.comapFlatMap(string -> {
            ModelPartTransformation.Type typex = (ModelPartTransformation.Type)BY_NAME.get(ModelParts.normalize(string));
            return typex != null ? DataResult.success(typex) : DataResult.error(() -> "Unknown model part transformation type: '" + string + "'");
         }, typex -> typex.name().toLowerCase(Locale.ROOT));
      }
   }
}
