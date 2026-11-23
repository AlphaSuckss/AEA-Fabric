package dev.alphasucks.aea.port.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.data.ColorCodecs;
import dev.alphasucks.aea.port.data.Easing;
import dev.alphasucks.aea.port.data.Expression;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2394;
import net.minecraft.class_2396;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import net.minecraft.class_2394.class_2395;
import org.jetbrains.annotations.Nullable;

public record CustomParticleOptions(
   class_2960 texture,
   Expression lifetimeExpr,
   Expression lifetimeVariationExpr,
   Expression sizeExpr,
   Expression sizeVariationExpr,
   Optional<Expression> endSizeExpr,
   int color,
   Optional<Integer> endColor,
   Expression gravityExpr,
   Expression frictionExpr,
   Expression roll,
   Expression rollSpeed,
   Expression framesExpr,
   Expression frameTimeExpr,
   Optional<Boolean> loopFrames,
   boolean physics,
   boolean emissive,
   ParticleBlend blend,
   ParticleFacing facing,
   Easing easing,
   ParticleFrameLayout frameLayout,
   Expression colorVariationExpr,
   Expression hueVariationExpr,
   boolean alphaBleed
) implements class_2394 {
   private static final Expression NO_ROLL = Expression.constant(0.0);
   private static final Expression ZERO = Expression.constant(0.0);
   private static final Expression DEFAULT_LIFETIME = Expression.constant(20.0);
   private static final Expression DEFAULT_SIZE = Expression.constant(0.2);
   private static final Expression DEFAULT_FRICTION = Expression.constant(0.98);
   private static final int MAX_ROLL_SOURCE = 256;
   private static final Codec<Expression> ROLL_CODEC = Expression.FLOAT_OR_EXPR
      .flatXmap(
         expression -> expression.source().length() <= 256
            ? DataResult.success(expression)
            : DataResult.error(() -> "roll expression must be at most 256 characters"),
         DataResult::success
      );
   private static final int FLAG_LOOP_VALUE = 1;
   private static final int FLAG_PHYSICS = 2;
   private static final int FLAG_EMISSIVE = 4;
   private static final int FLAG_ADDITIVE = 8;
   private static final int FLAG_FACING_VERTICAL = 16;
   private static final int FLAG_LOOP_SET = 32;
   private static final int LAYOUT_SHIFT = 6;
   private static final int LAYOUT_MASK = 3;
   private static final int FLAG_ALPHA_BLEED = 256;
   private static final MapCodec<CustomParticleOptions> BODY_CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
            IdCodecs.ID.fieldOf("texture").forGetter(CustomParticleOptions::texture),
            Expression.INT_OR_EXPR.optionalFieldOf("lifetime", DEFAULT_LIFETIME).forGetter(CustomParticleOptions::lifetimeExpr),
            Expression.INT_OR_EXPR.optionalFieldOf("lifetime_variation", ZERO).forGetter(CustomParticleOptions::lifetimeVariationExpr),
            Expression.FLOAT_OR_EXPR.optionalFieldOf("size", DEFAULT_SIZE).forGetter(CustomParticleOptions::sizeExpr),
            Expression.FLOAT_OR_EXPR.optionalFieldOf("end_size").forGetter(CustomParticleOptions::endSizeExpr),
            ColorCodecs.ARGB.optionalFieldOf("color", -1).forGetter(CustomParticleOptions::color),
            ColorCodecs.ARGB.optionalFieldOf("end_color").forGetter(CustomParticleOptions::endColor),
            Expression.FLOAT_OR_EXPR.optionalFieldOf("gravity", ZERO).forGetter(CustomParticleOptions::gravityExpr),
            Expression.FLOAT_OR_EXPR.optionalFieldOf("friction", DEFAULT_FRICTION).forGetter(CustomParticleOptions::frictionExpr),
            ROLL_CODEC.optionalFieldOf("roll", NO_ROLL).forGetter(CustomParticleOptions::roll),
            ROLL_CODEC.optionalFieldOf("roll_speed", NO_ROLL).forGetter(CustomParticleOptions::rollSpeed),
            Expression.INT_OR_EXPR.optionalFieldOf("frames", ZERO).forGetter(CustomParticleOptions::framesExpr),
            Expression.INT_OR_EXPR.optionalFieldOf("frame_time", ZERO).forGetter(CustomParticleOptions::frameTimeExpr),
            Codec.BOOL.optionalFieldOf("loop_frames").forGetter(CustomParticleOptions::loopFrames),
            Codec.BOOL.optionalFieldOf("physics", true).forGetter(CustomParticleOptions::physics),
            Codec.BOOL.optionalFieldOf("emissive", false).forGetter(CustomParticleOptions::emissive)
         )
         .apply(
            instance,
            (texture, lifetime, lifetimeVariation, size, endSize, color, endColor, gravity, friction, roll, rollSpeed, frames, frameTime, loopFrames, physics, emissive) -> new CustomParticleOptions(
               texture,
               lifetime,
               lifetimeVariation,
               size,
               ZERO,
               endSize,
               color,
               endColor,
               gravity,
               friction,
               roll,
               rollSpeed,
               frames,
               frameTime,
               loopFrames,
               physics,
               emissive,
               ParticleBlend.TRANSLUCENT,
               ParticleFacing.CAMERA,
               Easing.LINEAR,
               ParticleFrameLayout.AUTO,
               ZERO,
               ZERO,
               false
            )
         )
   );
   private static final MapCodec<CustomParticleOptions.Extra> EXTRA_CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
            ParticleBlend.CODEC.optionalFieldOf("blend", ParticleBlend.TRANSLUCENT).forGetter(CustomParticleOptions.Extra::blend),
            ParticleFacing.CODEC.optionalFieldOf("facing", ParticleFacing.CAMERA).forGetter(CustomParticleOptions.Extra::facing),
            Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(CustomParticleOptions.Extra::easing),
            ParticleFrameLayout.CODEC.optionalFieldOf("frame_layout", ParticleFrameLayout.AUTO).forGetter(CustomParticleOptions.Extra::frameLayout),
            Expression.FLOAT_OR_EXPR.optionalFieldOf("size_variation", ZERO).forGetter(CustomParticleOptions.Extra::sizeVariation),
            Expression.FLOAT_OR_EXPR.optionalFieldOf("color_variation", ZERO).forGetter(CustomParticleOptions.Extra::colorVariation),
            Expression.FLOAT_OR_EXPR.optionalFieldOf("hue_variation", ZERO).forGetter(CustomParticleOptions.Extra::hueVariation),
            Codec.BOOL.optionalFieldOf("alpha_bleed", false).forGetter(CustomParticleOptions.Extra::alphaBleed)
         )
         .apply(instance, CustomParticleOptions.Extra::new)
   );
   public static final Codec<CustomParticleOptions> CODEC = Codec.mapPair(BODY_CODEC, EXTRA_CODEC)
      .xmap(
         pair -> ((CustomParticleOptions)pair.getFirst()).withExtra((CustomParticleOptions.Extra)pair.getSecond()),
         options -> Pair.of(
            options,
            new CustomParticleOptions.Extra(
               options.blend(),
               options.facing(),
               options.easing(),
               options.frameLayout(),
               options.sizeVariationExpr(),
               options.colorVariationExpr(),
               options.hueVariationExpr(),
               options.alphaBleed()
            )
         )
      )
      .codec();
   public static final class_2395<CustomParticleOptions> DESERIALIZER = new class_2395<CustomParticleOptions>() {
      @Override
      public CustomParticleOptions method_10296(class_2396<CustomParticleOptions> type, StringReader reader) throws CommandSyntaxException {
         reader.expect(' ');
         return CustomParticleOptions.of(class_2960.method_12835(reader));
      }

      @Override
      public CustomParticleOptions method_10297(class_2396<CustomParticleOptions> type, class_2540 buf) {
         return CustomParticleOptions.read(buf);
      }
   };

   public static CustomParticleOptions of(class_2960 texture) {
      return new CustomParticleOptions(
         texture,
         DEFAULT_LIFETIME,
         ZERO,
         DEFAULT_SIZE,
         ZERO,
         Optional.empty(),
         -1,
         Optional.empty(),
         ZERO,
         DEFAULT_FRICTION,
         NO_ROLL,
         NO_ROLL,
         ZERO,
         ZERO,
         Optional.empty(),
         false,
         false,
         ParticleBlend.TRANSLUCENT,
         ParticleFacing.CAMERA,
         Easing.LINEAR,
         ParticleFrameLayout.AUTO,
         ZERO,
         ZERO,
         false
      );
   }

   private CustomParticleOptions withExtra(CustomParticleOptions.Extra extra) {
      return new CustomParticleOptions(
         this.texture,
         this.lifetimeExpr,
         this.lifetimeVariationExpr,
         this.sizeExpr,
         extra.sizeVariation(),
         this.endSizeExpr,
         this.color,
         this.endColor,
         this.gravityExpr,
         this.frictionExpr,
         this.roll,
         this.rollSpeed,
         this.framesExpr,
         this.frameTimeExpr,
         this.loopFrames,
         this.physics,
         this.emissive,
         extra.blend(),
         extra.facing(),
         extra.easing(),
         extra.frameLayout(),
         extra.colorVariation(),
         extra.hueVariation(),
         extra.alphaBleed()
      );
   }

   public int lifetime() {
      return this.lifetimeExpr.evalInt((class_1297)null);
   }

   public int lifetimeVariation() {
      return this.lifetimeVariationExpr.evalInt((class_1297)null);
   }

   public float size() {
      return (float)this.sizeExpr.eval((class_1297)null);
   }

   public float sizeVariation() {
      return (float)this.sizeVariationExpr.eval((class_1297)null);
   }

   public Optional<Float> endSize() {
      return this.endSizeExpr.map(expression -> (float)expression.eval((class_1297)null));
   }

   public float gravity() {
      return (float)this.gravityExpr.eval((class_1297)null);
   }

   public float friction() {
      return (float)this.frictionExpr.eval((class_1297)null);
   }

   public int frames() {
      return this.framesExpr.evalInt((class_1297)null);
   }

   public int frameTime() {
      return this.frameTimeExpr.evalInt((class_1297)null);
   }

   public float colorVariation() {
      return (float)this.colorVariationExpr.eval((class_1297)null);
   }

   public float hueVariation() {
      return (float)this.hueVariationExpr.eval((class_1297)null);
   }

   public float endSizeOr() {
      return this.endSizeExpr.isPresent() ? (float)((Expression)this.endSizeExpr.get()).eval((class_1297)null) : this.size();
   }

   public int endColorOr() {
      return (Integer)this.endColor.orElse(this.color);
   }

   public boolean dynamic() {
      return varies(this.lifetimeExpr)
         || varies(this.lifetimeVariationExpr)
         || varies(this.sizeExpr)
         || varies(this.sizeVariationExpr)
         || this.endSizeExpr.isPresent() && varies((Expression)this.endSizeExpr.get())
         || varies(this.gravityExpr)
         || varies(this.frictionExpr)
         || varies(this.framesExpr)
         || varies(this.frameTimeExpr)
         || varies(this.colorVariationExpr)
         || varies(this.hueVariationExpr);
   }

   public CustomParticleOptions bake(@Nullable class_1297 actor) {
      return new CustomParticleOptions(
         this.texture,
         bake(this.lifetimeExpr, actor),
         bake(this.lifetimeVariationExpr, actor),
         bake(this.sizeExpr, actor),
         bake(this.sizeVariationExpr, actor),
         this.endSizeExpr.map(e -> bake(e, actor)),
         this.color,
         this.endColor,
         bake(this.gravityExpr, actor),
         bake(this.frictionExpr, actor),
         this.roll,
         this.rollSpeed,
         bake(this.framesExpr, actor),
         bake(this.frameTimeExpr, actor),
         this.loopFrames,
         this.physics,
         this.emissive,
         this.blend,
         this.facing,
         this.easing,
         this.frameLayout,
         bake(this.colorVariationExpr, actor),
         bake(this.hueVariationExpr, actor),
         this.alphaBleed
      );
   }

   private static boolean varies(Expression expression) {
      return expression.constantValue().isEmpty();
   }

   private static Expression bake(Expression expression, @Nullable class_1297 actor) {
      return expression.constantValue().isPresent() ? expression : Expression.constant(expression.eval(actor));
   }

   @Override
   public void method_10294(class_2540 buf) {
      buf.method_10812(this.texture);
      buf.method_10804(Math.max(0, this.lifetime()));
      buf.method_10804(Math.max(0, this.lifetimeVariation()));
      buf.writeFloat(this.size());
      buf.writeFloat(this.sizeVariation());
      buf.writeFloat(this.endSizeOr());
      buf.writeInt(this.color);
      buf.writeInt(this.endColorOr());
      buf.writeFloat(this.gravity());
      buf.writeFloat(this.friction());
      buf.method_10788(this.roll.source(), 256);
      buf.method_10788(this.rollSpeed.source(), 256);
      buf.method_10804(Math.max(0, this.frames()));
      buf.method_10804(Math.max(0, this.frameTime()));
      int flags = (this.loopFrames.orElse(false) ? 1 : 0)
         | (this.physics ? 2 : 0)
         | (this.emissive ? 4 : 0)
         | (this.blend == ParticleBlend.ADDITIVE ? 8 : 0)
         | (this.facing == ParticleFacing.VERTICAL ? 16 : 0)
         | (this.loopFrames.isPresent() ? 32 : 0)
         | (this.alphaBleed ? 256 : 0)
         | this.frameLayout.ordinal() << 6;
      buf.method_10804(flags);
      buf.writeByte(this.easing.ordinal());
      buf.writeFloat(this.colorVariation());
      buf.writeFloat(this.hueVariation());
   }

   private static CustomParticleOptions read(class_2540 buf) {
      class_2960 texture = buf.method_10810();
      Expression lifetime = Expression.constant(buf.method_10816());
      Expression lifetimeVariation = Expression.constant(buf.method_10816());
      Expression size = Expression.constant(buf.readFloat());
      Expression sizeVariation = Expression.constant(buf.readFloat());
      Expression endSize = Expression.constant(buf.readFloat());
      int color = buf.readInt();
      int endColor = buf.readInt();
      Expression gravity = Expression.constant(buf.readFloat());
      Expression friction = Expression.constant(buf.readFloat());
      Expression roll = Expression.cached(buf.method_10800(256));
      Expression rollSpeed = Expression.cached(buf.method_10800(256));
      Expression frames = Expression.constant(buf.method_10816());
      Expression frameTime = Expression.constant(buf.method_10816());
      int flags = buf.method_10816();
      Easing[] easings = Easing.values();
      int easingIndex = buf.readByte();
      Easing easing = easingIndex >= 0 && easingIndex < easings.length ? easings[easingIndex] : Easing.LINEAR;
      Expression colorVariation = Expression.constant(buf.readFloat());
      Expression hueVariation = Expression.constant(buf.readFloat());
      ParticleFrameLayout[] layouts = ParticleFrameLayout.values();
      ParticleFrameLayout layout = layouts[flags >> 6 & 3];
      Optional<Boolean> loopFrames = (flags & 32) != 0 ? Optional.of((flags & 1) != 0) : Optional.empty();
      return new CustomParticleOptions(
         texture,
         lifetime,
         lifetimeVariation,
         size,
         sizeVariation,
         Optional.of(endSize),
         color,
         Optional.of(endColor),
         gravity,
         friction,
         roll,
         rollSpeed,
         frames,
         frameTime,
         loopFrames,
         (flags & 2) != 0,
         (flags & 4) != 0,
         (flags & 8) != 0 ? ParticleBlend.ADDITIVE : ParticleBlend.TRANSLUCENT,
         (flags & 16) != 0 ? ParticleFacing.VERTICAL : ParticleFacing.CAMERA,
         easing,
         layout,
         colorVariation,
         hueVariation,
         (flags & 256) != 0
      );
   }

   @Override
   public class_2396<CustomParticleOptions> method_10295() {
      return ApoliParticles.CUSTOM;
   }

   @Override
   public String method_10293() {
      return class_7923.field_41180.method_10221(this.method_10295()) + " " + this.texture;
   }

   private record Extra(
      ParticleBlend blend,
      ParticleFacing facing,
      Easing easing,
      ParticleFrameLayout frameLayout,
      Expression sizeVariation,
      Expression colorVariation,
      Expression hueVariation,
      boolean alphaBleed
   ) {
   }
}
