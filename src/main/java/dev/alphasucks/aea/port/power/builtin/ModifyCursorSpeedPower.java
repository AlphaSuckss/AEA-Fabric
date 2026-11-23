package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.AttributeModifierHelper;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class ModifyCursorSpeedPower extends PowerType<ModifyCursorSpeedPower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("modify_cursor_speed");

   @Override
   public MapCodec<ModifyCursorSpeedPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               AttributeModifier.CODEC.optionalFieldOf("modifier").forGetter(ModifyCursorSpeedPower.Config::modifier),
               AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("modifiers").forGetter(ModifyCursorSpeedPower.Config::modifiers),
               Codec.BOOL.optionalFieldOf("horizontal", true).forGetter(ModifyCursorSpeedPower.Config::horizontal),
               Codec.BOOL.optionalFieldOf("vertical", true).forGetter(ModifyCursorSpeedPower.Config::vertical)
            )
            .apply(i, ModifyCursorSpeedPower.Config::new)
      );
   }

   public static ModifyCursorSpeedPower.Multipliers compute(@Nullable class_1297 entity) {
      if (entity == null) {
         return ModifyCursorSpeedPower.Multipliers.NONE;
      }

      List<ModifyCursorSpeedPower.Config> configs = PowerLookup.active(entity, CANONICAL, ModifyCursorSpeedPower.Config.class);
      if (configs.isEmpty()) {
         return ModifyCursorSpeedPower.Multipliers.NONE;
      }

      double x = 1.0;
      double y = 1.0;
      int i = 0;

      for (int n = configs.size(); i < n; i++) {
         ModifyCursorSpeedPower.Config cfg = (ModifyCursorSpeedPower.Config)configs.get(i);
         List<AttributeModifier> mods = cfg.flattened();
         if (!mods.isEmpty()) {
            if (cfg.horizontal()) {
               x = AttributeModifierHelper.apply(x, mods, entity);
            }

            if (cfg.vertical()) {
               y = AttributeModifierHelper.apply(y, mods, entity);
            }
         }
      }

      return x == 1.0 && y == 1.0 ? ModifyCursorSpeedPower.Multipliers.NONE : new ModifyCursorSpeedPower.Multipliers(x, y);
   }

   public record Config(Optional<AttributeModifier> modifier, Optional<List<AttributeModifier>> modifiers, boolean horizontal, boolean vertical) {
      public List<AttributeModifier> flattened() {
         return AttributeModifierHelper.flatten(this.modifier, this.modifiers);
      }
   }

   public record Multipliers(double x, double y) {
      public static final ModifyCursorSpeedPower.Multipliers NONE = new ModifyCursorSpeedPower.Multipliers(1.0, 1.0);

      public boolean isIdentity() {
         return this.x == 1.0 && this.y == 1.0;
      }
   }
}
