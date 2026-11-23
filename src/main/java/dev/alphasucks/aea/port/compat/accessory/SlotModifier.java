package dev.alphasucks.aea.port.compat.accessory;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.data.AttributeModifierOperation;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import net.minecraft.class_1322;
import net.minecraft.class_2960;

public record SlotModifier(String slot, class_2960 id, double amount, AttributeModifierOperation operation) {
   public static final Codec<SlotModifier> CODEC = RecordCodecBuilder.create(
      i -> i.group(
            Codec.STRING.fieldOf("slot").forGetter(SlotModifier::slot),
            IdCodecs.ID.fieldOf("id").forGetter(SlotModifier::id),
            Codec.DOUBLE.optionalFieldOf("amount", 0.0).forGetter(SlotModifier::amount),
            AttributeModifierOperation.CODEC.optionalFieldOf("operation", AttributeModifierOperation.ADD_BASE_EARLY).forGetter(SlotModifier::operation)
         )
         .apply(i, SlotModifier::new)
   );
   public static final Codec<List<SlotModifier>> LIST = Codec.either(CODEC, CODEC.listOf())
      .xmap(e -> (List)e.map(List::of, l -> l), l -> l.size() == 1 ? Either.left((SlotModifier)l.get(0)) : Either.right(l));

   public class_1322 toVanilla() {
      return new class_1322(
         UUID.nameUUIDFromBytes(this.id.toString().getBytes(StandardCharsets.UTF_8)), this.id.toString(), this.amount, this.operation.vanillaOperation()
      );
   }
}
