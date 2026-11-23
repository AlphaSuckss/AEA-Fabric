package dev.alphasucks.aea.port.condition.builtin.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.data.Comparison;
import net.minecraft.class_2165;
import net.minecraft.class_2168;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_3218;
import net.minecraft.server.MinecraftServer;

public final class CommandBlockCondition implements ConditionType<BlockCtx, CommandBlockCondition.Cfg> {
   private static final class_2165 SILENT_SOURCE = new class_2165() {
      @Override
      public void method_43496(class_2561 component) {
      }

      @Override
      public boolean method_9200() {
         return false;
      }

      @Override
      public boolean method_9202() {
         return false;
      }

      @Override
      public boolean method_9201() {
         return false;
      }
   };

   @Override
   public MapCodec<CommandBlockCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.STRING.fieldOf("command").forGetter(CommandBlockCondition.Cfg::command),
               Comparison.CODEC.fieldOf("comparison").forGetter(CommandBlockCondition.Cfg::comparison),
               Codec.INT.fieldOf("compare_to").forGetter(CommandBlockCondition.Cfg::compareTo)
            )
            .apply(i, CommandBlockCondition.Cfg::new)
      );
   }

   public boolean test(CommandBlockCondition.Cfg cfg, BlockCtx ctx) {
      if (ctx.level() instanceof class_3218 serverLevel) {
         MinecraftServer server = serverLevel.method_8503();
         if (server == null) {
            return false;
         }

         class_243 pos = class_243.method_24953(ctx.pos());
         class_2168 source = new class_2168(
            SILENT_SOURCE, pos, class_241.field_1340, serverLevel, 4, "ApoliBlockCondition", class_2561.method_43470("ApoliBlockCondition"), server, null
         );
         int result = server.method_3734().method_44252(source, cfg.command);
         return cfg.comparison.compare(result, cfg.compareTo);
      } else {
         return false;
      }
   }

   public record Cfg(String command, Comparison comparison, int compareTo) {
   }
}
