package dev.alphasucks.aea.port.script;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.data.Nbt;
import java.util.Optional;
import net.minecraft.class_2487;
import net.minecraft.class_2960;

public record ScriptRef(class_2960 script, class_2487 params) {
   public static final MapCodec<ScriptRef> CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            IdCodecs.ID.fieldOf("script").forGetter(ScriptRef::script),
            Nbt.CODEC.optionalFieldOf("params").forGetter(r -> r.params.method_33133() ? Optional.empty() : Optional.of(new Nbt(r.params)))
         )
         .apply(i, (script, params) -> new ScriptRef(script, (class_2487)params.map(Nbt::tag).orElseGet(class_2487::new)))
   );
}
