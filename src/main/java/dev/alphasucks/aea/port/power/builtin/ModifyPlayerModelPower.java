package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.data.ModelAnimation;
import dev.alphasucks.aea.port.data.TextureRef;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class ModifyPlayerModelPower extends PowerType<ModifyPlayerModelPower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("modify_player_model");
   public static final class_2960 VANILLA_MODEL = Apoli.id("vanilla");

   @Override
   public MapCodec<ModifyPlayerModelPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.fieldOf("model").forGetter(ModifyPlayerModelPower.Config::model),
               TextureRef.ID_CODEC.optionalFieldOf("texture_location").forGetter(ModifyPlayerModelPower.Config::texture),
               TextureRef.ID_CODEC.optionalFieldOf("model_texture_location").forGetter(ModifyPlayerModelPower.Config::modelTexture),
               LoggedOptionalField.of("animations", ModelAnimation.CODEC).forGetter(ModifyPlayerModelPower.Config::animations)
            )
            .apply(i, ModifyPlayerModelPower.Config::new)
      );
   }

   @Nullable
   public static ModifyPlayerModelPower.Config firstActive(@Nullable class_1297 entity) {
      return PowerLookup.firstActive(entity, CANONICAL, ModifyPlayerModelPower.Config.class);
   }

   @Nullable
   public static class_2960 firstActiveModel(@Nullable class_1297 entity) {
      ModifyPlayerModelPower.Config config = firstActive(entity);
      return config == null ? null : config.model();
   }

   public static boolean replacesAppearance(@Nullable class_1297 entity) {
      ModifyPlayerModelPower.Config config = firstActive(entity);
      return config != null && !VANILLA_MODEL.equals(config.model());
   }

   @Nullable
   public static class_2960 firstActiveModelTexture(@Nullable class_1297 entity) {
      ModifyPlayerModelPower.Config config = firstActive(entity);
      return config == null ? null : (class_2960)config.modelTexture().orElse((class_2960)config.texture().orElse(null));
   }

   @Nullable
   public static ModelAnimation firstActiveAnimations(@Nullable class_1297 entity) {
      ModifyPlayerModelPower.Config config = firstActive(entity);
      return config == null ? null : (ModelAnimation)config.animations().orElse(null);
   }

   public record Config(class_2960 model, Optional<class_2960> texture, Optional<class_2960> modelTexture, Optional<ModelAnimation> animations) {
   }
}
