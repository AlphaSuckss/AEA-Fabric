package dev.alphasucks.aea.port.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import java.util.Map;

public record Key(String key, boolean continuous) {
   public static final String PRIMARY_ACTIVE = "key.apoli.primary_active";
   public static final Key DEFAULT_PRIMARY = new Key("key.apoli.primary_active", false);
   private static final Codec<String> WRAPPED_NAME = RecordCodecBuilder.create(
      i -> i.group(Codec.STRING.fieldOf("key").forGetter(name -> name)).apply(i, name -> name)
   );
   public static final Codec<String> NAME_CODEC = Codec.either(Codec.STRING, WRAPPED_NAME)
      .xmap(either -> (String)either.map(name -> name, name -> name), Either::left);
   public static final MapCodec<Key> MAP_CODEC = AliasingMapCodec.wrap(
      RecordCodecBuilder.mapCodec(
         i -> i.group(NAME_CODEC.fieldOf("key").forGetter(Key::key), Codec.BOOL.optionalFieldOf("continuous", false).forGetter(Key::continuous))
            .apply(i, Key::new)
      ),
      Map.of("continous", "continuous")
   );
   public static final Codec<Key> CODEC = Codec.either(Codec.STRING, MAP_CODEC.codec())
      .xmap(either -> (Key)either.map(s -> new Key(s, false), k -> k), Either::right);
}
