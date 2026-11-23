package dev.alphasucks.aea.port.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LazyCodec;
import dev.alphasucks.aea.port.data.expr.ExprVars;
import dev.alphasucks.aea.port.power.PowerContainer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public record AttributeModifier(
   AttributeModifierOperation operation,
   Expression value,
   Optional<class_2960> attribute,
   Optional<String> name,
   Optional<class_2960> resource,
   Optional<AttributeModifier> nested,
   Optional<Expression> position
) {
   public static final Codec<AttributeModifier> CODEC;
   public static final Codec<List<AttributeModifier>> LIST_OR_SINGLE;

   public AttributeModifier(
      AttributeModifierOperation operation,
      Expression value,
      Optional<class_2960> attribute,
      Optional<String> name,
      Optional<class_2960> resource,
      Optional<AttributeModifier> nested
   ) {
      this(operation, value, attribute, name, resource, nested, Optional.empty());
   }

   public boolean needsContainer() {
      return this.resource.isPresent()
         || this.position.isPresent()
         || this.value.needsContainer()
         || this.nested.isPresent() && ((AttributeModifier)this.nested.get()).needsContainer();
   }

   public double resolveInput(@Nullable class_1297 entity, @Nullable PowerContainer container, double contextValue) {
      double base;
      if (this.resource.isPresent()) {
         base = this.position.isPresent()
            ? ExprVars.readResourceAt(
               container, (class_2960)this.resource.get(), ((Expression)this.position.get()).evalIntWith(entity, container, contextValue)
            )
            : ExprVars.readResource(container, (class_2960)this.resource.get());
      } else {
         base = this.value.evalWith(entity, container, contextValue);
      }

      if (this.nested.isPresent()) {
         double nestedInput = ((AttributeModifier)this.nested.get()).resolveInput(entity, container, contextValue);
         return ((AttributeModifier)this.nested.get()).operation.applySingle(base, nestedInput);
      } else {
         return base;
      }
   }

   public double applyToValue(double currentValue, @Nullable class_1297 entity, @Nullable PowerContainer container) {
      double input = this.resolveInput(entity, container, currentValue);
      return this.operation.applySingle(currentValue, input);
   }

   static {
      Codec<AttributeModifier>[] ref = new Codec[1];
      MapCodec<AttributeModifier> rawMap = RecordCodecBuilder.mapCodec(
         i -> i.group(
               AttributeModifierOperation.CODEC.fieldOf("operation").forGetter(AttributeModifier::operation),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("value", Expression.constant(0.0)).forGetter(AttributeModifier::value),
               IdCodecs.ID.optionalFieldOf("attribute").forGetter(AttributeModifier::attribute),
               Codec.STRING.optionalFieldOf("name").forGetter(AttributeModifier::name),
               IdCodecs.ID.optionalFieldOf("resource").forGetter(AttributeModifier::resource),
               new LazyCodec<AttributeModifier>(() -> ref[0]).optionalFieldOf("modifier").forGetter(AttributeModifier::nested),
               Expression.INT_OR_EXPR.optionalFieldOf("position").forGetter(AttributeModifier::position)
            )
            .apply(i, AttributeModifier::new)
      );
      Codec<AttributeModifier> built = AliasingMapCodec.wrap(rawMap, Map.of("amount", "value", "index", "position", "slot", "position")).codec();
      ref[0] = built;
      CODEC = built;
      LIST_OR_SINGLE = Codec.either(CODEC, Codec.list(CODEC))
         .xmap(
            either -> (List)either.map(List::of, AttributeModifierHelper::ensureSorted),
            list -> list.size() == 1 ? Either.left((AttributeModifier)list.get(0)) : Either.right(list)
         );
   }
}
