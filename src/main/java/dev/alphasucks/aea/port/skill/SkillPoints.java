package dev.alphasucks.aea.port.skill;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.SingleOrList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_2960;

public record SkillPoints(Optional<class_2960> tree, int amount, boolean hidden) {
   private static final Codec<SkillPoints> OBJECT = AliasingMapCodec.wrap(
         RecordCodecBuilder.<SkillPoints>mapCodec(
            i -> i.group(
                  IdCodecs.ID.optionalFieldOf("skill_tree").forGetter(SkillPoints::tree),
                  Codec.INT.fieldOf("amount").forGetter(SkillPoints::amount),
                  Codec.BOOL.optionalFieldOf("hidden", false).forGetter(SkillPoints::hidden)
               )
               .apply(i, SkillPoints::new)
         ),
         Map.of("points", "amount")
      )
      .codec();
   public static final Codec<SkillPoints> CODEC = Codec.either(Codec.INT, OBJECT)
      .xmap(
         either -> (SkillPoints)either.map(SkillPoints::own, points -> points),
         points -> points.isPlainOwn() ? Either.left(points.amount) : Either.right(points)
      );
   public static final Codec<List<SkillPoints>> LIST_CODEC = SingleOrList.of(CODEC).xmap(SkillPoints::withoutZeroes, list -> list);

   public static SkillPoints own(int amount) {
      return new SkillPoints(Optional.empty(), amount, false);
   }

   private boolean isPlainOwn() {
      return this.tree.isEmpty() && !this.hidden;
   }

   public class_2960 treeOr(class_2960 own) {
      return (class_2960)this.tree.orElse(own);
   }

   private static List<SkillPoints> withoutZeroes(List<SkillPoints> points) {
      List<SkillPoints> kept = new ArrayList(points.size());

      for (SkillPoints entry : points) {
         if (entry.amount != 0) {
            kept.add(entry);
         }
      }

      return List.copyOf(kept);
   }

   public static Map<class_2960, Integer> totals(List<SkillPoints> points, class_2960 own) {
      Map<class_2960, Integer> totals = new LinkedHashMap();

      for (SkillPoints entry : points) {
         totals.merge(entry.treeOr(own), entry.amount, Integer::sum);
      }

      return totals;
   }

   public static boolean anyHidden(List<SkillPoints> points) {
      for (SkillPoints entry : points) {
         if (entry.hidden) {
            return true;
         }
      }

      return false;
   }

   static <T> Dynamic<T> liftTree(Dynamic<T> bundle) {
      Optional<Dynamic<T>> tree = bundle.get("skill_tree").result();
      if (tree.isEmpty()) {
         return bundle;
      }

      Optional<Number> amount = bundle.get("points").asNumber().result();
      if (amount.isEmpty()) {
         return bundle;
      }

      Dynamic<T> entry = bundle.emptyMap().set("skill_tree", (Dynamic)tree.get()).set("amount", bundle.createInt(((Number)amount.get()).intValue()));
      return bundle.remove("skill_tree").set("points", entry);
   }
}
