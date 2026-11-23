package dev.alphasucks.aea.port.power.builtin;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Placeholders;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class FunctionPower extends PowerType<FunctionPower.Cfg> {
   private static final Logger LOG = LogUtils.getLogger();
   private static final int MAX_CACHE_ENTRIES = 8;
   private static final int MAX_DEPTH = 16;
   private static final ThreadLocal<int[]> DEPTH = ThreadLocal.withInitial(() -> new int[1]);
   public static final MapCodec<FunctionPower.Cfg> CODEC = RecordCodecBuilder.<FunctionPower.Raw>mapCodec(
         i -> i.group(
               Codec.PASSTHROUGH.fieldOf("entity_action").forGetter(FunctionPower.Raw::entityAction),
               Codec.STRING.listOf().optionalFieldOf("parameters").forGetter(FunctionPower.Raw::parameters)
            )
            .apply(i, FunctionPower.Raw::new)
      )
      .flatXmap(FunctionPower::build, cfg -> DataResult.success(new FunctionPower.Raw(cfg.source(), Optional.of(cfg.parameters()))));

   @Override
   public MapCodec<FunctionPower.Cfg> configCodec() {
      return CODEC;
   }

   private static DataResult<FunctionPower.Cfg> build(FunctionPower.Raw raw) {
      return Placeholders.parameters("apoli:function", raw.entityAction(), raw.parameters())
         .flatMap(
            parameters -> !parameters.isEmpty()
               ? DataResult.success(new FunctionPower.Cfg(raw.entityAction(), parameters, null))
               : parseAction(raw.entityAction()).map(action -> new FunctionPower.Cfg(raw.entityAction(), List.of(), action))
         );
   }

   private static <T> DataResult<EntityAction> parseAction(Dynamic<T> source) {
      return EntityAction.CODEC.parse(source.getOps(), source.getValue());
   }

   public static void run(class_2960 powerId, FunctionPower.Cfg cfg, Map<String, Dynamic<?>> arguments, EntityCtx ctx) {
      int[] depth = (int[])DEPTH.get();
      if (depth[0] >= 16) {
         if (FunctionWarnings.first(powerId, "depth")) {
            LOG.warn("[Apoli] apoli:run_function stopped at depth {} on {} — check for a function that calls itself.", 16, powerId);
         }
      } else {
         EntityAction action = instantiate(powerId, cfg, arguments);
         if (action != null) {
            depth[0]++;

            try {
               action.run(ctx);
            } finally {
               depth[0]--;
            }
         }
      }
   }

   @Nullable
   private static EntityAction instantiate(class_2960 powerId, FunctionPower.Cfg cfg, Map<String, Dynamic<?>> arguments) {
      if (cfg.constant() != null) {
         return cfg.constant();
      }

      List<Dynamic<?>> key = new ArrayList(cfg.parameters().size());

      for (String parameter : cfg.parameters()) {
         Dynamic<?> value = (Dynamic<?>)arguments.get(parameter);
         if (value == null) {
            if (FunctionWarnings.first(powerId, "missing:" + parameter)) {
               LOG.warn("[Apoli] apoli:run_function on {} is missing argument [{}].", powerId, parameter);
            }

            return null;
         }

         key.add(value);
      }

      synchronized (cfg.cache) {
         EntityAction cached = (EntityAction)cfg.cache.get(key);
         if (cached != null) {
            return cached;
         }
      }

      EntityAction parsed = (EntityAction)instantiate(cfg.source(), cfg.parameters(), arguments)
         .resultOrPartial(err -> LOG.error("[Apoli] apoli:function {} failed to build with {}: {}", new Object[]{powerId, arguments, err}))
         .orElse(null);
      if (parsed == null) {
         return null;
      }

      synchronized (cfg.cache) {
         cfg.cache.put(key, parsed);
         if (cfg.cache.size() > 8) {
            Iterator<List<Dynamic<?>>> oldest = cfg.cache.keySet().iterator();
            oldest.next();
            oldest.remove();
         }

         return parsed;
      }
   }

   private static <T> DataResult<EntityAction> instantiate(Dynamic<T> source, List<String> parameters, Map<String, Dynamic<?>> arguments) {
      DynamicOps<T> ops = source.getOps();
      return EntityAction.CODEC.parse(ops, Placeholders.substitute(ops, source.getValue(), parameters, arguments));
   }

   public static final class Cfg {
      private final Dynamic<?> source;
      private final List<String> parameters;
      @Nullable
      private final EntityAction constant;
      private final Map<List<Dynamic<?>>, EntityAction> cache = new LinkedHashMap(8, 0.75F, true);

      Cfg(Dynamic<?> source, List<String> parameters, @Nullable EntityAction constant) {
         this.source = source;
         this.parameters = List.copyOf(parameters);
         this.constant = constant;
      }

      public Dynamic<?> source() {
         return this.source;
      }

      public List<String> parameters() {
         return this.parameters;
      }

      @Nullable
      public EntityAction constant() {
         return this.constant;
      }
   }

   private record Raw(Dynamic<?> entityAction, Optional<List<String>> parameters) {
   }
}
