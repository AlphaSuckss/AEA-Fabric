package dev.alphasucks.aea.port.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import java.util.Optional;

public record FunctionalKey(Key key, Optional<EntityAction> action) {
   private static final Codec<FunctionalKey> RECORD_CODEC = RecordCodecBuilder.create(
      i -> i.group(Key.MAP_CODEC.forGetter(FunctionalKey::key), LoggedOptionalField.of("action", EntityAction.CODEC).forGetter(FunctionalKey::action))
         .apply(i, FunctionalKey::new)
   );
   public static final Codec<FunctionalKey> CODEC = Codec.either(Codec.STRING, RECORD_CODEC)
      .xmap(
         either -> (FunctionalKey)either.map(s -> new FunctionalKey(new Key(s, false), Optional.empty()), fk -> fk),
         fk -> fk.action.isEmpty() && !fk.key.continuous() ? Either.left(fk.key.key()) : Either.right(fk)
      );
}
