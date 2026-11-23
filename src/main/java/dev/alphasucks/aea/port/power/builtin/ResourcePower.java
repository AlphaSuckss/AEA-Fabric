package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Comparison;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.HudRender;
import dev.alphasucks.aea.port.dev.DevMode;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.HudRendered;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerContainerImpl;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import org.jetbrains.annotations.Nullable;

public class ResourcePower extends PowerType<ResourcePower.Cfg> {
   public static final int WARN_SIZE = 65536;
   private static final int MAX_ACTION_DEPTH = 64;
   private static final int DEV_REPORT_TICKS = 20;
   private static final ThreadLocal<int[]> ACTION_DEPTH = ThreadLocal.withInitial(() -> new int[1]);
   private static final Set<class_2960> WARNED = ConcurrentHashMap.newKeySet();
   private static final MapCodec<ResourcePower.Cfg> INNER = RecordCodecBuilder.mapCodec(
      i -> i.group(
            Expression.INT_OR_EXPR.optionalFieldOf("min").forGetter(ResourcePower.Cfg::min),
            Expression.INT_OR_EXPR.optionalFieldOf("max").forGetter(ResourcePower.Cfg::max),
            Expression.INT_OR_EXPR.optionalFieldOf("start_value").forGetter(ResourcePower.Cfg::startValue),
            HudRender.CODEC.optionalFieldOf("hud_render", HudRender.DONT_RENDER).forGetter(ResourcePower.Cfg::hudRender),
            Codec.BOOL.optionalFieldOf("enforce_limits", true).forGetter(ResourcePower.Cfg::enforceLimits),
            Codec.BOOL.optionalFieldOf("retain_value", false).forGetter(ResourcePower.Cfg::retainValue),
            LoggedOptionalField.of("min_action", EntityAction.CODEC).forGetter(ResourcePower.Cfg::minAction),
            LoggedOptionalField.of("max_action", EntityAction.CODEC).forGetter(ResourcePower.Cfg::maxAction),
            LoggedOptionalField.of("on_change", Codec.list(ResourcePower.OnChange.CODEC), List.of()).forGetter(ResourcePower.Cfg::onChange),
            Codec.BOOL.optionalFieldOf("persistent", true).forGetter(ResourcePower.Cfg::persistent),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("size", 1).forGetter(ResourcePower.Cfg::size)
         )
         .apply(i, ResourcePower.Cfg::new)
   );
   private static final MapCodec<ResourcePower.Cfg> CFG_CODEC = AliasingMapCodec.wrap(
      INNER, Map.of("default", "start_value", "positions", "size", "slots", "size")
   );

   @Override
   public MapCodec<ResourcePower.Cfg> configCodec() {
      return CFG_CODEC;
   }

   public void onAdded(class_2960 powerId, ResourcePower.Cfg cfg, PowerContainer holder, class_2960 source) {
      if (holder instanceof PowerContainerImpl impl) {
         if (cfg.size > 65536 && WARNED.add(powerId)) {
            Apoli.LOGGER
               .warn(
                  "[Apoli] {} declares a resource of {} slots. Slots are only allocated once they are written, so this costs nothing until used, but writing near the top of that range will allocate roughly {} MB per holder.",
                  new Object[]{powerId, cfg.size, cfg.size * 4L / 1048576L}
               );
         }

         if (cfg.size > 1) {
            impl.trimAuxInts(powerId, cfg.size);
         }

         if (!impl.getAuxInt(powerId).isPresent()) {
            impl.setAuxInt(powerId, this.clampedStart(cfg, holder, powerId));
         }
      }
   }

   public void onRemoved(class_2960 powerId, ResourcePower.Cfg cfg, PowerContainer holder, class_2960 source) {
      if (holder instanceof PowerContainerImpl impl) {
         if (!holder.hasPower(powerId)) {
            impl.removeAux(powerId);
         }
      }
   }

   public void tick(class_2960 powerId, ResourcePower.Cfg cfg, PowerContainer holder) {
      if (cfg.enforceLimits) {
         if (holder instanceof PowerContainerImpl impl) {
            if (cfg.size > 1) {
               int[] table = impl.getAuxInts(powerId);
               if (table != null) {
                  int min = this.currentMin(cfg, holder, powerId);
                  int max = this.currentMax(cfg, holder, powerId);
                  int limit = Math.min(table.length, cfg.size);
                  boolean changed = false;

                  for (int slot = 1; slot < limit; slot++) {
                     int value = clamp(table[slot], min, max, cfg);
                     if (value != table[slot]) {
                        table[slot] = value;
                        changed = true;
                     }
                  }

                  if (changed) {
                     impl.markDirty();
                  }
               }
            }

            OptionalInt cur = impl.getAuxInt(powerId);
            if (!cur.isEmpty()) {
               int curVal = cur.getAsInt();
               int min = this.currentMin(cfg, holder, powerId);
               int max = this.currentMax(cfg, holder, powerId);
               int clamped = clamp(curVal, min, max, cfg);
               if (clamped != curVal) {
                  impl.setAuxInt(powerId, clamped);
                  this.fireBoundaryActions(powerId, cfg, holder.rawOwner(), curVal, clamped, min, max);
               }
            }
         }
      }
   }

   public OptionalInt readResource(class_2960 powerId, ResourcePower.Cfg cfg, PowerContainer holder) {
      return readValue(holder, powerId);
   }

   public OptionalInt writeResource(class_2960 powerId, ResourcePower.Cfg cfg, PowerContainer holder, int value) {
      return writeValue(holder, powerId, value);
   }

   public OptionalInt resourceBound(class_2960 powerId, ResourcePower.Cfg cfg, PowerContainer holder, boolean max) {
      return boundOf(holder, powerId, max);
   }

   public int resourceSize(class_2960 powerId, ResourcePower.Cfg cfg, PowerContainer holder) {
      return cfg.size;
   }

   public OptionalInt readResourceAt(class_2960 powerId, ResourcePower.Cfg cfg, PowerContainer holder, int slot) {
      if (!holder.hasPower(powerId)) {
         return OptionalInt.empty();
      }

      if (slot < 0 || slot >= cfg.size) {
         return OptionalInt.empty();
      }

      if (slot == 0) {
         return holder.getAuxInt(powerId);
      }

      int[] table = holder.getAuxInts(powerId);
      return table != null && slot < table.length ? OptionalInt.of(table[slot]) : OptionalInt.of(this.clampedStart(cfg, holder, powerId));
   }

   public OptionalInt writeResourceAt(class_2960 powerId, ResourcePower.Cfg cfg, PowerContainer holder, int slot, int value) {
      if (slot == 0) {
         return writeValue(holder, powerId, value);
      }

      if (holder instanceof PowerContainerImpl impl) {
         if (!holder.hasPower(powerId)) {
            return OptionalInt.empty();
         }

         if (slot >= 0 && slot < cfg.size) {
            int min = this.currentMin(cfg, holder, powerId);
            int max = this.currentMax(cfg, holder, powerId);
            int[] table = impl.auxIntsAtLeast(powerId, slot + 1, this.clampedStart(cfg, holder, powerId));
            table[0] = holder.getAuxIntOr(powerId, table[0]);
            int prev = table[slot];
            int target;
            if (!cfg.retainValue || !cfg.enforceLimits || value >= min && value <= max) {
               target = clamp(value, min, max, cfg);
            } else {
               target = prev;
            }

            if (target != prev) {
               table[slot] = target;
               impl.markDirty();
               this.fireBoundaryActions(powerId, cfg, holder.rawOwner(), prev, target, min, max);
            }

            return OptionalInt.of(target);
         } else {
            return OptionalInt.empty();
         }
      } else {
         return OptionalInt.empty();
      }
   }

   public int resourceIndexOf(class_2960 powerId, ResourcePower.Cfg cfg, PowerContainer holder, int value) {
      if (!holder.hasPower(powerId)) {
         return -1;
      }

      if (holder.getAuxIntOr(powerId, value + 1) == value) {
         return 0;
      }

      int[] table = holder.getAuxInts(powerId);
      int limit = table == null ? 1 : Math.min(table.length, cfg.size);

      for (int slot = 1; slot < limit; slot++) {
         if (table[slot] == value) {
            return slot;
         }
      }

      return limit < cfg.size && this.clampedStart(cfg, holder, powerId) == value ? limit : -1;
   }

   public int evalStartValue(ResourcePower.Cfg cfg, PowerContainer holder) {
      Expression start = (Expression)cfg.startValue.orElseGet(() -> (Expression)cfg.min.orElse(null));
      return start == null ? 0 : start.evalIntWith(holder.rawOwner(), holder, 0.0);
   }

   public int currentMin(ResourcePower.Cfg cfg, PowerContainer holder, class_2960 powerId) {
      if (cfg.min.isEmpty()) {
         return Integer.MIN_VALUE;
      }

      Expression min = (Expression)cfg.min.get();
      if (min.constantValue().isPresent()) {
         return (int)Math.round(min.constantValue().getAsDouble());
      }

      int cur = holder.getAuxIntOr(powerId, 0);
      return min.evalIntWith(holder.rawOwner(), holder, cur);
   }

   public int currentMax(ResourcePower.Cfg cfg, PowerContainer holder, class_2960 powerId) {
      if (cfg.max.isEmpty()) {
         return Integer.MAX_VALUE;
      }

      Expression max = (Expression)cfg.max.get();
      if (max.constantValue().isPresent()) {
         return (int)Math.round(max.constantValue().getAsDouble());
      }

      int cur = holder.getAuxIntOr(powerId, 0);
      return max.evalIntWith(holder.rawOwner(), holder, cur);
   }

   public static int clamp(int value, int min, int max, ResourcePower.Cfg cfg) {
      if (!cfg.enforceLimits) {
         return value;
      }

      int hi = Math.max(min, max);
      int lo = Math.min(min, max);
      return Math.min(hi, Math.max(lo, value));
   }

   public static OptionalInt boundOf(@Nullable PowerContainer holder, class_2960 powerId, boolean max) {
      Power loaded = ApoliPowers.get(powerId);
      if (loaded == null) {
         return OptionalInt.empty();
      }

      if (PowerTypeRegistry.get(loaded.typeId()) instanceof ResourcePower rp) {
         if (loaded.config() instanceof ResourcePower.Cfg cfg) {
            Expression bound = (Expression)(max ? cfg.max() : cfg.min()).orElse(null);
            if (bound == null) {
               return OptionalInt.of(max ? Integer.MAX_VALUE : Integer.MIN_VALUE);
            } else {
               OptionalDouble constant = bound.constantValue();
               if (constant.isPresent()) {
                  return OptionalInt.of((int)Math.round(constant.getAsDouble()));
               } else {
                  return holder == null ? OptionalInt.empty() : OptionalInt.of(max ? rp.currentMax(cfg, holder, powerId) : rp.currentMin(cfg, holder, powerId));
               }
            }
         } else {
            return OptionalInt.empty();
         }
      } else {
         return OptionalInt.empty();
      }
   }

   public static OptionalInt readValue(PowerContainer holder, class_2960 powerId) {
      return !holder.hasPower(powerId) ? OptionalInt.empty() : holder.getAuxInt(powerId);
   }

   public static void onEntityLoad(PowerContainer holder, class_2960 powerId) {
      Power loaded = ApoliPowers.get(powerId);
      if (loaded != null) {
         if (PowerTypeRegistry.get(loaded.typeId()) instanceof ResourcePower rp) {
            if (loaded.config() instanceof ResourcePower.Cfg cfg) {
               if (holder instanceof PowerContainerImpl impl) {
                  rp.resetOnLoad(powerId, cfg, impl);
               }
            }
         }
      }
   }

   protected void resetOnLoad(class_2960 powerId, ResourcePower.Cfg cfg, PowerContainerImpl impl) {
      if (cfg.size > 1) {
         impl.trimAuxInts(powerId, cfg.persistent ? cfg.size : 0);
      }

      boolean present = impl.getAuxInt(powerId).isPresent();
      if (!present || !cfg.persistent) {
         impl.setAuxInt(powerId, this.clampedStart(cfg, impl, powerId));
      }
   }

   protected int clampedStart(ResourcePower.Cfg cfg, PowerContainer holder, class_2960 powerId) {
      return clamp(this.evalStartValue(cfg, holder), this.currentMin(cfg, holder, powerId), this.currentMax(cfg, holder, powerId), cfg);
   }

   public static OptionalInt writeValue(PowerContainer holder, class_2960 powerId, int newValue) {
      if (!(holder instanceof PowerContainerImpl impl)) {
         return OptionalInt.empty();
      } else {
         if (!holder.hasPower(powerId)) {
            return OptionalInt.empty();
         }

         Power loaded = ApoliPowers.get(powerId);
         if (loaded == null) {
            return OptionalInt.empty();
         }

         if (PowerTypeRegistry.get(loaded.typeId()) instanceof ResourcePower rp) {
            if (!(loaded.config() instanceof ResourcePower.Cfg cfg)) {
               return OptionalInt.empty();
            } else {
               int var13 = impl.getAuxInt(powerId).orElse(rp.evalStartValue(cfg, holder));
               int min = rp.currentMin(cfg, holder, powerId);
               int max = rp.currentMax(cfg, holder, powerId);
               int target;
               if (!cfg.retainValue || !cfg.enforceLimits || newValue >= min && newValue <= max) {
                  target = clamp(newValue, min, max, cfg);
               } else {
                  target = var13;
               }

               if (target != var13) {
                  impl.setAuxInt(powerId, target);
                  int[] table = impl.getAuxInts(powerId);
                  if (table != null && table.length > 0) {
                     table[0] = target;
                  }

                  rp.fireBoundaryActions(powerId, cfg, holder.rawOwner(), var13, target, min, max);
               }

               return OptionalInt.of(target);
            }
         } else {
            return OptionalInt.empty();
         }
      }
   }

   protected void fireBoundaryActions(class_2960 powerId, ResourcePower.Cfg cfg, class_1297 owner, int prev, int newVal, int min, int max) {
      if (owner != null && newVal != prev) {
         if (!cfg.minAction.isEmpty() || !cfg.maxAction.isEmpty() || !cfg.onChange.isEmpty()) {
            if (owner.method_37908() instanceof class_3218 level) {
               int[] depth = (int[])ACTION_DEPTH.get();
               if (depth[0] >= 64) {
                  if (FunctionWarnings.first(powerId, "resource_actions")) {
                     Apoli.LOGGER
                        .warn(
                           "[Apoli] {} stopped running its min_action, max_action and on_change after {} changes in a row that each triggered another one. Check for an action that keeps changing the resource it was triggered by.",
                           powerId,
                           64
                        );
                  }

                  DevMode.reportEvery(
                     owner, "stopped " + powerId, 20, powerId + " stopped after 64 chained changes: an action keeps changing the resource that triggered it"
                  );
               } else {
                  StringBuilder fired = DevMode.any() ? new StringBuilder() : null;
                  depth[0]++;

                  try {
                     if (newVal == min && prev != min && cfg.minAction.isPresent()) {
                        ((EntityAction)cfg.minAction.get()).run(EntityCtx.of(owner, level));
                        if (fired != null) {
                           fired.append("min_action");
                        }
                     }

                     if (newVal == max && prev != max && cfg.maxAction.isPresent()) {
                        ((EntityAction)cfg.maxAction.get()).run(EntityCtx.of(owner, level));
                        if (fired != null) {
                           fired.append(fired.isEmpty() ? "" : ", ").append("max_action");
                        }
                     }

                     List<ResourcePower.OnChange> onChange = cfg.onChange;

                     for (int i = 0; i < onChange.size(); i++) {
                        ResourcePower.OnChange entry = (ResourcePower.OnChange)onChange.get(i);
                        if (entry.matches(newVal, owner)) {
                           ((EntityAction)entry.entityAction().get()).run(EntityCtx.of(owner, level));
                           if (fired != null) {
                              fired.append(fired.isEmpty() ? "" : ", ").append("on_change[").append(i).append(']');
                           }
                        }
                     }
                  } finally {
                     depth[0]--;
                  }

                  if (fired != null && !fired.isEmpty()) {
                     DevMode.reportEvery(owner, powerId, 20, powerId + " " + prev + " → " + newVal + " ran " + fired);
                  }
               }
            }
         }
      }
   }

   public record Cfg(
      Optional<Expression> min,
      Optional<Expression> max,
      Optional<Expression> startValue,
      HudRender hudRender,
      boolean enforceLimits,
      boolean retainValue,
      Optional<EntityAction> minAction,
      Optional<EntityAction> maxAction,
      List<ResourcePower.OnChange> onChange,
      boolean persistent,
      int size
   ) implements HudRendered {
   }

   public record OnChange(Optional<Expression> value, List<Expression> values, Comparison comparison, Optional<EntityAction> entityAction) {
      public static final Codec<ResourcePower.OnChange> CODEC = RecordCodecBuilder.create(
         i -> i.group(
               Expression.INT_OR_EXPR.optionalFieldOf("value").forGetter(ResourcePower.OnChange::value),
               Codec.list(Expression.INT_OR_EXPR).optionalFieldOf("values", List.of()).forGetter(ResourcePower.OnChange::values),
               Comparison.CODEC.optionalFieldOf("comparison", Comparison.EQUAL).forGetter(ResourcePower.OnChange::comparison),
               LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(ResourcePower.OnChange::entityAction)
            )
            .apply(i, ResourcePower.OnChange::new)
      );

      public boolean matches(int current, class_1297 owner) {
         if (this.entityAction.isEmpty()) {
            return false;
         }

         if (this.value.isEmpty() && this.values.isEmpty()) {
            return true;
         }

         if (this.value.isPresent() && this.comparison.compare(current, ((Expression)this.value.get()).evalInt(owner, current))) {
            return true;
         }

         for (int i = 0; i < this.values.size(); i++) {
            if (this.comparison.compare(current, ((Expression)this.values.get(i)).evalInt(owner, current))) {
               return true;
            }
         }

         return false;
      }
   }
}
