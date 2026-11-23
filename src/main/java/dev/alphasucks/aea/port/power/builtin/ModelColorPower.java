package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.BodyPart;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class ModelColorPower extends PowerType<ModelColorPower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("model_color");
   public static final float[] IDENTITY = new float[]{1.0F, 1.0F, 1.0F, 1.0F, 0.0F};
   private static final ThreadLocal<List<ModelColorPower.Config>> CONFIGS = ThreadLocal.withInitial(() -> new ArrayList(2));
   private static final MapCodec<ModelColorPower.PartColor> PART_COLOR_CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            BodyPart.CODEC.fieldOf("part").forGetter(ModelColorPower.PartColor::part),
            ModelColorPower.Tint.MAP_CODEC.forGetter(ModelColorPower.PartColor::tint),
            LoggedOptionalField.strict("condition", EntityCondition.CODEC).forGetter(ModelColorPower.PartColor::condition)
         )
         .apply(i, ModelColorPower.PartColor::new)
   );
   private static final ThreadLocal<List<ModelColorPower.PartColor>> PART_COLORS = ThreadLocal.withInitial(() -> new ArrayList(4));

   @Override
   public MapCodec<ModelColorPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               ModelColorPower.Tint.MAP_CODEC.forGetter(ModelColorPower.Config::tint),
               PART_COLOR_CODEC.codec().listOf().optionalFieldOf("parts", List.of()).forGetter(ModelColorPower.Config::parts)
            )
            .apply(i, ModelColorPower.Config::new)
      );
   }

   public static float[] colorFor(@Nullable class_1297 entity) {
      List<ModelColorPower.Config> configs = (List<ModelColorPower.Config>)CONFIGS.get();
      configs.clear();
      PowerLookup.collect(entity, CANONICAL, ModelColorPower.Config.class, configs);
      float[] rgba = null;

      for (int i = 0; i < configs.size(); i++) {
         ModelColorPower.Config cfg = (ModelColorPower.Config)configs.get(i);
         if (!cfg.hasParts()) {
            if (rgba == null) {
               rgba = new float[]{1.0F, 1.0F, 1.0F, 1.0F, 0.0F};
            }

            cfg.tint().applyTo(entity, rgba);
         }
      }

      configs.clear();
      return rgba == null ? IDENTITY : rgba;
   }

   public static boolean resolve(class_1297 entity, float[] whole, List<ModelColorPower.PartColor> parts) {
      whole[0] = 1.0F;
      whole[1] = 1.0F;
      whole[2] = 1.0F;
      whole[3] = 1.0F;
      whole[4] = 0.0F;
      List<ModelColorPower.Config> configs = (List<ModelColorPower.Config>)CONFIGS.get();
      configs.clear();
      PowerLookup.collect(entity, CANONICAL, ModelColorPower.Config.class, configs);
      if (configs.isEmpty()) {
         return false;
      }

      EntityCtx ctx = null;

      for (int i = 0; i < configs.size(); i++) {
         ModelColorPower.Config cfg = (ModelColorPower.Config)configs.get(i);
         if (!cfg.hasParts()) {
            cfg.tint().applyTo(entity, whole);
         } else {
            List<ModelColorPower.PartColor> partList = cfg.parts();

            for (int p = 0; p < partList.size(); p++) {
               ModelColorPower.PartColor pc = (ModelColorPower.PartColor)partList.get(p);
               if (pc.condition().isPresent()) {
                  if (ctx == null) {
                     ctx = EntityCtx.of(entity, entity.method_37908());
                  }

                  if (!((EntityCondition)pc.condition().get()).test(ctx)) {
                     continue;
                  }
               }

               parts.add(pc);
            }
         }
      }

      configs.clear();
      return true;
   }

   public static boolean hasPartColors(@Nullable class_1297 entity) {
      return PowerLookup.anyActive(entity, CANONICAL, ModelColorPower.Config.class, ModelColorPower.Config::hasParts);
   }

   public static void collectPartColors(@Nullable class_1297 entity, List<ModelColorPower.PartColor> out) {
      if (entity != null) {
         List<ModelColorPower.Config> configs = (List<ModelColorPower.Config>)CONFIGS.get();
         configs.clear();
         PowerLookup.collect(entity, CANONICAL, ModelColorPower.Config.class, configs);
         EntityCtx ctx = null;

         for (int i = 0; i < configs.size(); i++) {
            List<ModelColorPower.PartColor> parts = ((ModelColorPower.Config)configs.get(i)).parts();

            for (int p = 0; p < parts.size(); p++) {
               ModelColorPower.PartColor pc = (ModelColorPower.PartColor)parts.get(p);
               if (pc.condition().isPresent()) {
                  if (ctx == null) {
                     ctx = EntityCtx.of(entity, entity.method_37908());
                  }

                  if (!((EntityCondition)pc.condition().get()).test(ctx)) {
                     continue;
                  }
               }

               out.add(pc);
            }
         }

         configs.clear();
      }
   }

   public static float minAlpha(@Nullable class_1297 entity) {
      float min = colorFor(entity)[3];
      if (!hasPartColors(entity)) {
         return min;
      }

      List<ModelColorPower.PartColor> parts = (List<ModelColorPower.PartColor>)PART_COLORS.get();
      parts.clear();
      collectPartColors(entity, parts);

      for (int i = 0; i < parts.size(); i++) {
         min = Math.min(min, ((ModelColorPower.PartColor)parts.get(i)).tint().alpha(entity));
      }

      parts.clear();
      return min;
   }

   public record Config(ModelColorPower.Tint tint, List<ModelColorPower.PartColor> parts) {
      public boolean hasParts() {
         return !this.parts.isEmpty();
      }
   }

   public record PartColor(BodyPart part, ModelColorPower.Tint tint, Optional<EntityCondition> condition) {
   }

   public record Tint(Expression red, Expression green, Expression blue, Expression alpha, boolean explicitRgb) {
      private static final Expression ONE = Expression.constant(1.0);
      public static final MapCodec<ModelColorPower.Tint> MAP_CODEC = RecordCodecBuilder.mapCodec(
         i -> i.group(
               Expression.FLOAT_OR_EXPR.optionalFieldOf("red").forGetter(t -> t.channel(t.red())),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("green").forGetter(t -> t.channel(t.green())),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("blue").forGetter(t -> t.channel(t.blue())),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("alpha", ONE).forGetter(ModelColorPower.Tint::alpha)
            )
            .apply(
               i,
               (r, g, b, alpha) -> new ModelColorPower.Tint(
                  (Expression)r.orElse(ONE), (Expression)g.orElse(ONE), (Expression)b.orElse(ONE), alpha, r.isPresent() && g.isPresent() && b.isPresent()
               )
            )
      );

      private Optional<Expression> channel(Expression value) {
         boolean one = value.constantValue().isPresent() && value.constantValue().getAsDouble() == 1.0;
         return !this.explicitRgb && one ? Optional.empty() : Optional.of(value);
      }

      public float red(@Nullable class_1297 entity) {
         return (float)this.red.eval(entity);
      }

      public float green(@Nullable class_1297 entity) {
         return (float)this.green.eval(entity);
      }

      public float blue(@Nullable class_1297 entity) {
         return (float)this.blue.eval(entity);
      }

      public float alpha(@Nullable class_1297 entity) {
         return (float)this.alpha.eval(entity);
      }

      public boolean whitens(float r, float g, float b) {
         return this.explicitRgb && r >= 0.999F && g >= 0.999F && b >= 0.999F;
      }

      public void resolve(@Nullable class_1297 entity, float[] out) {
         float r = this.red(entity);
         float g = this.green(entity);
         float b = this.blue(entity);
         out[0] = r;
         out[1] = g;
         out[2] = b;
         out[3] = this.alpha(entity);
         out[4] = this.whitens(r, g, b) ? 1.0F : 0.0F;
      }

      public void applyTo(@Nullable class_1297 entity, float[] rgba) {
         float r = this.red(entity);
         float g = this.green(entity);
         float b = this.blue(entity);
         rgba[0] *= r;
         rgba[1] *= g;
         rgba[2] *= b;
         rgba[3] *= this.alpha(entity);
         if (this.whitens(r, g, b)) {
            rgba[4] = 1.0F;
         }
      }
   }
}
