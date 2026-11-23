package dev.alphasucks.aea.port.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import java.util.function.Function;
import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_3445;
import net.minecraft.class_3448;
import net.minecraft.class_7923;
import org.jetbrains.annotations.Nullable;

public final class Stat {
   private static final class_2960 CUSTOM = new class_2960("custom");
   private final class_2960 type;
   private final class_2960 id;
   @Nullable
   private class_3445<?> resolved;
   private boolean resolveAttempted;
   private static final Codec<Stat> OBJECT_CODEC = RecordCodecBuilder.create(
      i -> i.group(IdCodecs.ID.fieldOf("type").forGetter(Stat::type), IdCodecs.ID.fieldOf("id").forGetter(Stat::id)).apply(i, Stat::new)
   );
   public static final Codec<Stat> CODEC = Codec.either(IdCodecs.ID.xmap(id -> new Stat(CUSTOM, id), Stat::id), OBJECT_CODEC)
      .xmap(either -> (Stat)either.map(Function.identity(), Function.identity()), stat -> CUSTOM.equals(stat.type()) ? Either.left(stat) : Either.right(stat));

   public Stat(class_2960 type, class_2960 id) {
      this.type = type;
      this.id = id;
   }

   public class_2960 type() {
      return this.type;
   }

   public class_2960 id() {
      return this.id;
   }

   @Nullable
   public class_3445<?> resolve() {
      if (!this.resolveAttempted) {
         this.resolveAttempted = true;
         class_3448 statType = class_7923.field_41193.method_10223(this.type);
         if (statType != null) {
            class_2378<?> reg = statType.method_14959();
            Object value = reg.method_10223(this.id);
            if (value != null) {
               this.resolved = statType.method_14956(value);
            }
         }
      }

      return this.resolved;
   }

   public static void setValue(class_3222 player, class_3445<?> stat, int value) {
      player.method_14248().method_15023(player, stat, value);
      player.method_37908().method_8428().method_1162(stat, player.method_5820(), score -> score.method_1128(value));
   }
}
