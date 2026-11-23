package dev.alphasucks.aea.port.power.builtin;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.BodyPart;
import dev.alphasucks.aea.port.data.EquipmentSlot;
import dev.alphasucks.aea.port.data.ModelAnimation;
import dev.alphasucks.aea.port.data.RenderMode;
import dev.alphasucks.aea.port.data.TextureRef;
import dev.alphasucks.aea.port.data.Vector;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import net.minecraft.class_3542;
import org.jetbrains.annotations.Nullable;

public final class CustomModelRenderPower extends PowerType<CustomModelRenderPower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("custom_model_render");
   private static final MapCodec<CustomModelRenderPower.Base> BASE_CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
            CustomModelRenderPower.Mode.CODEC.optionalFieldOf("mode", CustomModelRenderPower.Mode.TEXTURE).forGetter(CustomModelRenderPower.Base::mode),
            TextureRef.ID_CODEC.optionalFieldOf("wide_texture_location").forGetter(CustomModelRenderPower.Base::wideTexture),
            TextureRef.ID_CODEC.optionalFieldOf("slim_texture_location").forGetter(CustomModelRenderPower.Base::slimTexture),
            IdCodecs.ID.optionalFieldOf("model_location").forGetter(CustomModelRenderPower.Base::model),
            TextureRef.ID_CODEC.optionalFieldOf("texture_location").forGetter(CustomModelRenderPower.Base::texture),
            Codec.BOOL.optionalFieldOf("render_as_overlay", false).forGetter(CustomModelRenderPower.Base::renderAsOverlay),
            Codec.BOOL.optionalFieldOf("hide_cape", false).forGetter(CustomModelRenderPower.Base::hideCape),
            EquipmentSlot.CODEC.listOf().optionalFieldOf("hidden_slots", List.of()).forGetter(CustomModelRenderPower.Base::hiddenSlots),
            RenderMode.CODEC.optionalFieldOf("render_type", RenderMode.TRANSLUCENT).forGetter(CustomModelRenderPower.Base::renderType),
            BodyPart.LIST_CODEC.optionalFieldOf("body_parts", List.of()).forGetter(CustomModelRenderPower.Base::bodyParts),
            Codec.FLOAT.optionalFieldOf("red", 1.0F).forGetter(CustomModelRenderPower.Base::red),
            Codec.FLOAT.optionalFieldOf("green", 1.0F).forGetter(CustomModelRenderPower.Base::green),
            Codec.FLOAT.optionalFieldOf("blue", 1.0F).forGetter(CustomModelRenderPower.Base::blue),
            Codec.FLOAT.optionalFieldOf("alpha", 1.0F).forGetter(CustomModelRenderPower.Base::alpha),
            Codec.BOOL.optionalFieldOf("show_first_person", false).forGetter(CustomModelRenderPower.Base::showFirstPerson),
            Codec.FLOAT.optionalFieldOf("scale", 1.0F).forGetter(CustomModelRenderPower.Base::scale)
         )
         .apply(instance, CustomModelRenderPower.Base::new)
   );
   private static final MapCodec<CustomModelRenderPower.Extras> EXTRAS_CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
            LoggedOptionalField.of("animations", ModelAnimation.CODEC).forGetter(CustomModelRenderPower.Extras::animations),
            LoggedOptionalField.of("scroll_speed", Codec.FLOAT, 0.0F).forGetter(CustomModelRenderPower.Extras::scrollSpeed),
            LoggedOptionalField.of("bind_body_parts", Codec.BOOL, true).forGetter(CustomModelRenderPower.Extras::bindBodyParts),
            LoggedOptionalField.of("offset", Vector.CODEC, Vector.ZERO).forGetter(CustomModelRenderPower.Extras::offset)
         )
         .apply(instance, CustomModelRenderPower.Extras::new)
   );
   private static final MapCodec<CustomModelRenderPower.Config> CONFIG_CODEC = Codec.mapPair(BASE_CODEC, EXTRAS_CODEC)
      .xmap(
         pair -> merge((CustomModelRenderPower.Base)pair.getFirst(), (CustomModelRenderPower.Extras)pair.getSecond()),
         config -> Pair.of(split(config), new CustomModelRenderPower.Extras(config.animations(), config.scrollSpeed(), config.bindBodyParts(), config.offset()))
      );

   private static CustomModelRenderPower.Config merge(CustomModelRenderPower.Base base, CustomModelRenderPower.Extras extras) {
      return new CustomModelRenderPower.Config(
         base.mode(),
         base.wideTexture(),
         base.slimTexture(),
         base.model(),
         base.texture(),
         base.renderAsOverlay(),
         base.hideCape(),
         base.hiddenSlots(),
         base.renderType(),
         base.bodyParts(),
         base.red(),
         base.green(),
         base.blue(),
         base.alpha(),
         base.showFirstPerson(),
         base.scale(),
         extras.animations(),
         extras.scrollSpeed(),
         extras.bindBodyParts(),
         extras.offset()
      );
   }

   private static CustomModelRenderPower.Base split(CustomModelRenderPower.Config config) {
      return new CustomModelRenderPower.Base(
         config.mode(),
         config.wideTexture(),
         config.slimTexture(),
         config.model(),
         config.texture(),
         config.renderAsOverlay(),
         config.hideCape(),
         config.hiddenSlots(),
         config.renderType(),
         config.bodyParts(),
         config.red(),
         config.green(),
         config.blue(),
         config.alpha(),
         config.showFirstPerson(),
         config.scale()
      );
   }

   @Override
   public MapCodec<CustomModelRenderPower.Config> configCodec() {
      return CONFIG_CODEC;
   }

   private static boolean hiddenByEquipment(class_1309 entity, List<EquipmentSlot> slots) {
      for (int i = 0; i < slots.size(); i++) {
         if (!entity.method_6118(((EquipmentSlot)slots.get(i)).vanilla()).method_7960()) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public static CustomModelRenderPower.Config firstReplace(@Nullable class_1309 entity) {
      if (entity == null) {
         return null;
      }

      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(CANONICAL);
         if (powers.isEmpty()) {
            return null;
         }

         EntityCtx ctx = null;
         int i = 0;

         for (int n = powers.size(); i < n; i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null
                  && power.config() instanceof CustomModelRenderPower.Config cfg
                  && cfg.mode() == CustomModelRenderPower.Mode.TEXTURE
                  && !cfg.renderAsOverlay()
                  && cfg.wide() != null) {
                  if (power.condition().isPresent()) {
                     if (ctx == null) {
                        ctx = EntityCtx.of(entity, entity.method_37908());
                     }

                     if (!((EntityCondition)power.condition().get()).test(ctx)) {
                        continue;
                     }
                  }

                  if (!hiddenByEquipment(entity, cfg.hiddenSlots())) {
                     return cfg;
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static boolean replacesModel(@Nullable class_1309 entity, Predicate<class_2960> loaded) {
      if (entity == null) {
         return false;
      }

      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(CANONICAL);
         if (powers.isEmpty()) {
            return false;
         }

         EntityCtx ctx = null;
         int i = 0;

         for (int n = powers.size(); i < n; i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null
                  && power.config() instanceof CustomModelRenderPower.Config cfg
                  && cfg.mode() == CustomModelRenderPower.Mode.GEOMETRY
                  && !cfg.renderAsOverlay()
                  && !cfg.model().isEmpty()
                  && !cfg.texture().isEmpty()) {
                  if (power.condition().isPresent()) {
                     if (ctx == null) {
                        ctx = EntityCtx.of(entity, entity.method_37908());
                     }

                     if (!((EntityCondition)power.condition().get()).test(ctx)) {
                        continue;
                     }
                  }

                  if (!hiddenByEquipment(entity, cfg.hiddenSlots()) && loaded.test((class_2960)cfg.model().get())) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean replacesSkin(@Nullable class_1309 entity) {
      boolean[] hit = new boolean[]{false};
      PowerLookup.forEach(entity, CANONICAL, CustomModelRenderPower.Config.class, cfg -> {
         if (!hit[0] && !cfg.renderAsOverlay() && !hiddenByEquipment(entity, cfg.hiddenSlots())) {
            hit[0] = cfg.mode() == CustomModelRenderPower.Mode.TEXTURE ? cfg.wide() != null : cfg.model().isPresent() && cfg.texture().isPresent();
         }
      });
      return hit[0];
   }

   public static boolean shouldHideCape(@Nullable class_1309 entity) {
      boolean[] hide = new boolean[]{false};
      PowerLookup.forEach(entity, CANONICAL, CustomModelRenderPower.Config.class, cfg -> {
         if (cfg.hideCape()) {
            hide[0] = true;
         }
      });
      return hide[0];
   }

   public static List<CustomModelRenderPower.ResolvedLayer> collectTextureOverlays(@Nullable class_1309 entity) {
      if (!PowerLookup.hasActive(entity, CANONICAL)) {
         return List.of();
      }

      List<CustomModelRenderPower.ResolvedLayer> out = new ArrayList();
      PowerLookup.forEach(
         entity,
         CANONICAL,
         CustomModelRenderPower.Config.class,
         cfg -> {
            if (cfg.mode() == CustomModelRenderPower.Mode.TEXTURE && cfg.renderAsOverlay()) {
               class_2960 wide = cfg.wide();
               if (wide != null && !hiddenByEquipment(entity, cfg.hiddenSlots())) {
                  class_2960 slim = cfg.slim();
                  out.add(
                     new CustomModelRenderPower.ResolvedLayer(
                        wide,
                        slim != null ? slim : wide,
                        cfg.renderType(),
                        cfg.bodyParts(),
                        cfg.red(),
                        cfg.green(),
                        cfg.blue(),
                        cfg.alpha(),
                        cfg.showFirstPerson(),
                        cfg.scale(),
                        cfg.scrollSpeed()
                     )
                  );
               }
            }
         }
      );
      return out;
   }

   public static List<CustomModelRenderPower.GeometryRender> collectGeometry(@Nullable class_1297 entity) {
      if (!PowerLookup.hasActive(entity, CANONICAL)) {
         return List.of();
      }

      List<CustomModelRenderPower.GeometryRender> out = new ArrayList();
      PowerLookup.forEach(entity, CANONICAL, CustomModelRenderPower.Config.class, cfg -> {
         if (cfg.mode() == CustomModelRenderPower.Mode.GEOMETRY && !cfg.model().isEmpty() && !cfg.texture().isEmpty()) {
            if (!(entity instanceof class_1309 living && hiddenByEquipment(living, cfg.hiddenSlots()))) {
               out.add(toRender(cfg));
            }
         }
      });
      return out;
   }

   private static CustomModelRenderPower.GeometryRender toRender(CustomModelRenderPower.Config cfg) {
      return new CustomModelRenderPower.GeometryRender(
         (class_2960)cfg.model().get(),
         (class_2960)cfg.texture().get(),
         cfg.renderType(),
         cfg.bodyParts(),
         cfg.red(),
         cfg.green(),
         cfg.blue(),
         cfg.alpha(),
         cfg.showFirstPerson(),
         cfg.scale(),
         cfg.renderAsOverlay(),
         cfg.animations(),
         cfg.scrollSpeed(),
         cfg.bindBodyParts(),
         cfg.offset()
      );
   }

   public static List<class_2960> geometryPowerIds(@Nullable class_1297 entity) {
      if (entity == null) {
         return List.of();
      }

      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(CANONICAL);
         List<class_2960> found = null;

         for (int i = 0; i < powers.size(); i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId) && geometryOf(powerId) != null) {
               if (found == null) {
                  found = new ArrayList(2);
               }

               found.add(powerId);
            }
         }

         return found == null ? List.of() : found;
      } else {
         return List.of();
      }
   }

   @Nullable
   public static CustomModelRenderPower.GeometryRender geometryOf(@Nullable class_2960 powerId) {
      if (powerId == null) {
         return null;
      } else {
         Power power = ApoliPowers.get(powerId);
         if (!(power != null && power.config() instanceof CustomModelRenderPower.Config cfg)) {
            return null;
         } else {
            return cfg.mode() == CustomModelRenderPower.Mode.GEOMETRY && !cfg.model().isEmpty() && !cfg.texture().isEmpty() ? toRender(cfg) : null;
         }
      }
   }

   private record Base(
      CustomModelRenderPower.Mode mode,
      Optional<class_2960> wideTexture,
      Optional<class_2960> slimTexture,
      Optional<class_2960> model,
      Optional<class_2960> texture,
      boolean renderAsOverlay,
      boolean hideCape,
      List<EquipmentSlot> hiddenSlots,
      RenderMode renderType,
      List<BodyPart> bodyParts,
      float red,
      float green,
      float blue,
      float alpha,
      boolean showFirstPerson,
      float scale
   ) {
   }

   public record Config(
      CustomModelRenderPower.Mode mode,
      Optional<class_2960> wideTexture,
      Optional<class_2960> slimTexture,
      Optional<class_2960> model,
      Optional<class_2960> texture,
      boolean renderAsOverlay,
      boolean hideCape,
      List<EquipmentSlot> hiddenSlots,
      RenderMode renderType,
      List<BodyPart> bodyParts,
      float red,
      float green,
      float blue,
      float alpha,
      boolean showFirstPerson,
      float scale,
      Optional<ModelAnimation> animations,
      float scrollSpeed,
      boolean bindBodyParts,
      Vector offset
   ) {
      @Nullable
      public class_2960 wide() {
         return (class_2960)this.wideTexture.or(() -> this.texture).orElse(null);
      }

      @Nullable
      public class_2960 slim() {
         return (class_2960)this.slimTexture.or(() -> this.wideTexture).or(() -> this.texture).orElse(null);
      }
   }

   private record Extras(Optional<ModelAnimation> animations, float scrollSpeed, boolean bindBodyParts, Vector offset) {
   }

   public record GeometryRender(
      class_2960 model,
      class_2960 texture,
      RenderMode mode,
      List<BodyPart> bodyParts,
      float red,
      float green,
      float blue,
      float alpha,
      boolean showFirstPerson,
      float scale,
      boolean renderAsOverlay,
      Optional<ModelAnimation> animations,
      float scrollSpeed,
      boolean bindBodyParts,
      Vector offset
   ) {
   }

   public enum Mode implements class_3542 {
      TEXTURE("texture"),
      GEOMETRY("geometry");

      public static final Codec<CustomModelRenderPower.Mode> CODEC = class_3542.method_28140(CustomModelRenderPower.Mode::values);
      private final String name;

      Mode(String name) {
         this.name = name;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }

   public record ResolvedLayer(
      class_2960 wide,
      class_2960 slim,
      RenderMode mode,
      List<BodyPart> bodyParts,
      float red,
      float green,
      float blue,
      float alpha,
      boolean showFirstPerson,
      float scale,
      float scrollSpeed
   ) {
      public class_2960 texture(boolean slimModel) {
         return slimModel ? this.slim : this.wide;
      }

      public boolean wholeModel() {
         return this.bodyParts.isEmpty();
      }
   }
}
