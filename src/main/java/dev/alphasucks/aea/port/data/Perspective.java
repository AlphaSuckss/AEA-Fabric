package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.alphasucks.aea.port.codec.SingleOrList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public enum Perspective {
   FIRST_PERSON,
   THIRD_PERSON;

   public static final List<Perspective> BOTH = List.of(FIRST_PERSON, THIRD_PERSON);
   public static final List<Perspective> THIRD_ONLY = List.of(THIRD_PERSON);
   public static final int MASK_BOTH = maskOf(BOTH);
   public static final int MASK_THIRD_ONLY = maskOf(THIRD_ONLY);
   public static final int MASK_INHERIT = -1;
   private static final Map<String, Perspective> BY_NAME = new HashMap();
   public static final Codec<Perspective> CODEC;
   public static final Codec<List<Perspective>> LIST_CODEC;

   public static int maskOf(List<Perspective> perspectives) {
      int mask = 0;

      for (int i = 0; i < perspectives.size(); i++) {
         mask |= 1 << ((Perspective)perspectives.get(i)).ordinal();
      }

      return mask;
   }

   public static boolean masked(int mask, boolean firstPerson) {
      return (mask & 1 << (firstPerson ? FIRST_PERSON.ordinal() : THIRD_PERSON.ordinal())) != 0;
   }

   static {
      for (Perspective perspective : values()) {
         BY_NAME.put(ModelParts.normalize(perspective.name()), perspective);
      }

      BY_NAME.put("first", FIRST_PERSON);
      BY_NAME.put("third", THIRD_PERSON);
      CODEC = Codec.STRING
         .comapFlatMap(
            string -> {
               Perspective perspectivex = (Perspective)BY_NAME.get(ModelParts.normalize(string));
               return perspectivex != null
                  ? DataResult.success(perspectivex)
                  : DataResult.error(() -> "Unknown perspective: '" + string + "' (expected first_person or third_person)");
            },
            perspectivex -> perspectivex.name().toLowerCase(Locale.ROOT)
         );
      LIST_CODEC = SingleOrList.of(CODEC);
   }
}
