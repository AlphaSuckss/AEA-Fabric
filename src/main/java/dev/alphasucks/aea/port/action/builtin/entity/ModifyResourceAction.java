package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.DataResult.PartialResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.AttributeModifierOperation;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerResources;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;
import net.minecraft.class_1297;
import net.minecraft.class_2960;

public final class ModifyResourceAction implements ActionType<EntityCtx, ModifyResourceAction.Cfg> {
   private static final MapCodec<ModifyResourceAction.Cfg> CANONICAL = AliasingMapCodec.wrap(
      RecordCodecBuilder.mapCodec(
         i -> i.group(
               AttributeModifier.CODEC
                  .optionalFieldOf(
                     "modifier",
                     new AttributeModifier(
                        AttributeModifierOperation.SET_BASE, Expression.constant(0.0), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
                     )
                  )
                  .forGetter(ModifyResourceAction.Cfg::modifier),
               IdCodecs.ID.fieldOf("resource").forGetter(ModifyResourceAction.Cfg::resource),
               Expression.INT_OR_EXPR.optionalFieldOf("position").forGetter(ModifyResourceAction.Cfg::position),
               IdCodecs.ID.optionalFieldOf("from").forGetter(ModifyResourceAction.Cfg::from),
               Expression.INT_OR_EXPR.optionalFieldOf("from_position").forGetter(ModifyResourceAction.Cfg::fromPosition)
            )
            .apply(i, ModifyResourceAction.Cfg::new)
      ),
      Map.of("index", "position", "slot", "position", "from_resource", "from", "from_index", "from_position", "from_slot", "from_position")
   );
   public static final MapCodec<ModifyResourceAction.Cfg> CONFIG_CODEC = new MapCodec<ModifyResourceAction.Cfg>() {
      public <T> Stream<T> keys(DynamicOps<T> ops) {
         return Stream.of(
               ops.createString("modifier"),
               ops.createString("resource"),
               ops.createString("position"),
               ops.createString("from"),
               ops.createString("from_position"),
               ops.createString("change"),
               ops.createString("operation")
         );
      }

      public <T> DataResult<ModifyResourceAction.Cfg> decode(DynamicOps<T> ops, MapLike<T> input) {
         if (input.get("modifier") != null) {
            return ModifyResourceAction.CANONICAL.decode(ops, input);
         }

         T changeRaw = (T)input.get("change");
         T operationRaw = (T)input.get("operation");
         T resourceRaw = (T)input.get("resource");
         if (changeRaw == null && operationRaw == null) {
            return ModifyResourceAction.CANONICAL.decode(ops, input);
         }

         if (resourceRaw == null) {
            return DataResult.error(() -> "Legacy change_resource requires a 'resource' field");
         }

         DataResult<class_2960> resourceR = IdCodecs.ID.parse(ops, resourceRaw);
         if (resourceR.error().isPresent()) {
            return DataResult.error(() -> "Invalid 'resource': " + ((PartialResult)resourceR.error().get()).message());
         }

         Expression change = Expression.constant(0.0);
         if (changeRaw != null) {
            DataResult<Expression> c = Expression.INT_OR_EXPR.parse(ops, changeRaw);
            if (c.error().isPresent()) {
               return DataResult.error(() -> "Invalid 'change': " + ((PartialResult)c.error().get()).message());
            }

            change = (Expression)c.result().get();
         }

         String operationStr = "add";
         if (operationRaw != null) {
            DataResult<String> s = ops.getStringValue(operationRaw);
            if (s.error().isPresent()) {
               return DataResult.error(() -> "Invalid 'operation': " + ((PartialResult)s.error().get()).message());
            }

            operationStr = (String)s.result().get();
         }
         AttributeModifierOperation op = switch (operationStr) {
            case "add" -> AttributeModifierOperation.ADD_BASE_EARLY;
            case "set" -> AttributeModifierOperation.SET_BASE;
            default -> null;
         };
         if (op == null) {
            String captured = operationStr;
            return DataResult.error(() -> "Legacy 'operation' must be 'add' or 'set', got: " + captured);
         } else {
            AttributeModifier synth = new AttributeModifier(op, change, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
            DataResult<Optional<Expression>> positionR = Expression.INT_OR_EXPR.optionalFieldOf("position").decode(ops, input);
            Optional<Expression> position = (Optional<Expression>)positionR.result().orElse(Optional.empty());
            return DataResult.success(new ModifyResourceAction.Cfg(synth, (class_2960)resourceR.result().get(), position, Optional.empty(), Optional.empty()));
         }
      }

      public <T> RecordBuilder<T> encode(ModifyResourceAction.Cfg input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
         return ModifyResourceAction.CANONICAL.encode(input, ops, prefix);
      }
   };

   @Override
   public MapCodec<ModifyResourceAction.Cfg> codec() {
      return CONFIG_CODEC;
   }

   public void run(ModifyResourceAction.Cfg cfg, EntityCtx ctx) {
      run(cfg, ctx.entity(), ctx.entity());
   }

   public static void run(ModifyResourceAction.Cfg cfg, class_1297 entity, class_1297 source) {
      PowerContainer container = PowerContainer.of(entity);
      if (container != null) {
         if (cfg.from.isPresent()) {
            PowerContainer sourceContainer = source == entity ? container : PowerContainer.of(source);
            if (sourceContainer != null) {
               copy(cfg, entity, container, sourceContainer);
            }
         } else {
            int size = PowerResources.size(container, cfg.resource);
            if (cfg.position.isEmpty() && size > 1) {
               for (int slot = 0; slot < size; slot++) {
                  applyAt(cfg, entity, container, slot);
               }
            } else if (cfg.position.isEmpty()) {
               OptionalInt cur = PowerResources.read(container, cfg.resource);
               if (!cur.isEmpty()) {
                  double next = cfg.modifier.applyToValue(cur.getAsInt(), entity, container);
                  PowerResources.write(container, cfg.resource, (int)Math.round(next));
               }
            } else {
               applyAt(cfg, entity, container, ((Expression)cfg.position.get()).evalIntWith(entity, container, 0.0));
            }
         }
      }
   }

   private static void applyAt(ModifyResourceAction.Cfg cfg, class_1297 entity, PowerContainer container, int slot) {
      OptionalInt cur = PowerResources.readAt(container, cfg.resource, slot);
      if (!cur.isEmpty()) {
         double next = cfg.modifier.applyToValue(cur.getAsInt(), entity, container);
         PowerResources.writeAt(container, cfg.resource, slot, (int)Math.round(next));
      }
   }

   private static void copy(ModifyResourceAction.Cfg cfg, class_1297 entity, PowerContainer container, PowerContainer sourceContainer) {
      class_2960 source = (class_2960)cfg.from.get();
      boolean wholeTable = cfg.position.isEmpty() && cfg.fromPosition.isEmpty();
      if (wholeTable) {
         int destination = Math.max(1, PowerResources.size(container, cfg.resource));
         int available = Math.max(1, PowerResources.size(sourceContainer, source));
         int slots = Math.min(destination, available);

         for (int slot = 0; slot < slots; slot++) {
            OptionalInt value = PowerResources.readAt(sourceContainer, source, slot);
            if (!value.isEmpty()) {
               writeInto(cfg, entity, container, slot, value.getAsInt());
            }
         }
      } else {
         int fromSlot = cfg.fromPosition.isPresent() ? ((Expression)cfg.fromPosition.get()).evalIntWith(entity, container, 0.0) : 0;
         OptionalInt value = PowerResources.readAt(sourceContainer, source, fromSlot);
         if (!value.isEmpty()) {
            int toSlot = cfg.position.isPresent() ? ((Expression)cfg.position.get()).evalIntWith(entity, container, 0.0) : fromSlot;
            writeInto(cfg, entity, container, toSlot, value.getAsInt());
         }
      }
   }

   private static void writeInto(ModifyResourceAction.Cfg cfg, class_1297 entity, PowerContainer container, int slot, int incoming) {
      OptionalInt cur = PowerResources.readAt(container, cfg.resource, slot);
      if (!cur.isEmpty()) {
         double next = cfg.modifier.operation().applySingle(cur.getAsInt(), incoming);
         PowerResources.writeAt(container, cfg.resource, slot, (int)Math.round(next));
      }
   }

   public record Cfg(
      AttributeModifier modifier, class_2960 resource, Optional<Expression> position, Optional<class_2960> from, Optional<Expression> fromPosition
   ) {
   }
}
