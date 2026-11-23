package dev.alphasucks.aea.port.compat.accessory;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;

public record AccessorySlot(Optional<String> provider, Optional<String> group, Optional<String> type, Optional<Integer> index) {
   private static final Codec<AccessorySlot> RECORD_CODEC = RecordCodecBuilder.create(
      i -> i.group(
            Codec.STRING.optionalFieldOf("provider").forGetter(AccessorySlot::provider),
            Codec.STRING.optionalFieldOf("group").forGetter(AccessorySlot::group),
            Codec.STRING.optionalFieldOf("slot").forGetter(AccessorySlot::type),
            Codec.INT.optionalFieldOf("index").forGetter(AccessorySlot::index)
         )
         .apply(i, AccessorySlot::new)
   );
   public static final Codec<AccessorySlot> CODEC = Codec.either(Codec.STRING, RECORD_CODEC)
      .xmap(e -> (AccessorySlot)e.map(AccessorySlot::parse, r -> r), Either::right);
   public static final Codec<List<AccessorySlot>> LIST = Codec.either(CODEC, CODEC.listOf())
      .xmap(e -> (List)e.map(List::of, l -> l), l -> l.size() == 1 ? Either.left((AccessorySlot)l.get(0)) : Either.right(l));

   private static AccessorySlot parse(String s) {
      String[] p = s.split("/");
      Optional<String> group = Optional.empty();
      Optional<Integer> index = Optional.empty();
      Optional<String> type;
      if (p.length <= 1) {
         type = Optional.of(s);
      } else {
         group = Optional.of(p[0]);
         type = Optional.of(p[1]);
         if (p.length >= 3) {
            try {
               index = Optional.of(Integer.parseInt(p[2]));
            } catch (NumberFormatException var6) {
            }
         }
      }

      return new AccessorySlot(Optional.empty(), group, type, index);
   }

   public boolean test(AccessorySlotRef ref) {
      return (this.provider.isEmpty() || ((String)this.provider.get()).equalsIgnoreCase(ref.provider()))
         && (this.group.isEmpty() || ((String)this.group.get()).equals(ref.group()))
         && (this.type.isEmpty() || ((String)this.type.get()).equals(ref.type()))
         && (this.index.isEmpty() || (Integer)this.index.get() == ref.index());
   }

   public static boolean matchesAny(List<AccessorySlot> filters, AccessorySlotRef ref) {
      if (filters.isEmpty()) {
         return true;
      }

      for (AccessorySlot f : filters) {
         if (f.test(ref)) {
            return true;
         }
      }

      return false;
   }
}
