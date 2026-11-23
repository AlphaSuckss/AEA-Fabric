package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.builtin.FunctionPower;
import dev.alphasucks.aea.port.power.builtin.FunctionWarnings;
import java.util.Map;
import net.minecraft.class_2960;
import org.slf4j.Logger;

public final class RunFunctionAction implements ActionType<EntityCtx, RunFunctionAction.Cfg> {
   private static final Logger LOG = LogUtils.getLogger();

   @Override
   public MapCodec<RunFunctionAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.fieldOf("function").forGetter(RunFunctionAction.Cfg::function),
               Codec.unboundedMap(Codec.STRING, Codec.PASSTHROUGH).optionalFieldOf("arguments", Map.of()).forGetter(RunFunctionAction.Cfg::arguments)
            )
            .apply(i, RunFunctionAction.Cfg::new)
      );
   }

   public void run(RunFunctionAction.Cfg cfg, EntityCtx ctx) {
      Power power = ApoliPowers.get(cfg.function());
      if (power == null) {
         if (FunctionWarnings.first(cfg.function(), "unknown")) {
            LOG.warn("[Apoli] apoli:run_function referenced unknown power {}.", cfg.function());
         }
      } else if (power.config() instanceof FunctionPower.Cfg function) {
         if (!power.condition().isPresent() || ((EntityCondition)power.condition().get()).test(ctx)) {
            FunctionPower.run(cfg.function(), function, cfg.arguments(), ctx);
         }
      } else {
         if (FunctionWarnings.first(cfg.function(), "not_function")) {
            LOG.warn("[Apoli] apoli:run_function referenced {}, which is not an apoli:function.", cfg.function());
         }
      }
   }

   public record Cfg(class_2960 function, Map<String, Dynamic<?>> arguments) {
      public Cfg {
         arguments = Map.copyOf(arguments);
      }
   }
}
