package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Key;
import dev.alphasucks.aea.port.keybind.HeldKeys;
import dev.alphasucks.aea.port.keybind.KeyDispatch;
import net.minecraft.class_1309;
import net.minecraft.class_1937;
import net.minecraft.server.MinecraftServer;

public final class KeyPressedCondition implements ConditionType<EntityCtx, KeyPressedCondition.Config> {
   @Override
   public MapCodec<KeyPressedCondition.Config> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Key.CODEC.optionalFieldOf("key", Key.DEFAULT_PRIMARY).forGetter(KeyPressedCondition.Config::key),
               Codec.intRange(0, 40).optionalFieldOf("grace", 0).forGetter(KeyPressedCondition.Config::grace)
            )
            .apply(i, KeyPressedCondition.Config::new)
      );
   }

   public boolean test(KeyPressedCondition.Config cfg, EntityCtx ctx) {
      class_1309 entity = ctx.living();
      if (entity == null) {
         return false;
      }

      class_1937 level = ctx.level();
      String key = cfg.key().key();
      if (level != null && level.method_8608()) {
         return HeldKeys.clientHeld(entity, key, cfg.grace());
      }

      if (KeyDispatch.blocked(entity, key)) {
         return false;
      }

      MinecraftServer server = level == null ? null : level.method_8503();
      return HeldKeys.serverHeld(entity.method_5667(), key, cfg.grace(), server == null ? Long.MIN_VALUE : server.method_3780());
   }

   public record Config(Key key, int grace) {
   }
}
