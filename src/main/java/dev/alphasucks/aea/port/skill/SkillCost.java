package dev.alphasucks.aea.port.skill;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.ItemStackData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public record SkillCost(List<SkillPoints> points, List<SkillCost.ItemCost> items, boolean hidden) {
   public static final SkillCost FREE = new SkillCost(List.of(), List.of(), false);
   private static final int OFFHAND_SLOT = 40;
   private static final String[] BUNDLE_KEYS = new String[]{"points", "items", "hidden", "skill_tree"};
   private static final MapCodec<SkillCost> OBJECT = RecordCodecBuilder.mapCodec(
      i -> i.group(
            SkillPoints.LIST_CODEC.optionalFieldOf("points", List.of()).forGetter(SkillCost::points),
            SingleOrList.of(SkillCost.ItemCost.CODEC).optionalFieldOf("items", List.of()).forGetter(SkillCost::items),
            Codec.BOOL.optionalFieldOf("hidden", false).forGetter(SkillCost::hidden)
         )
         .apply(i, SkillCost::new)
   );
   private static final Codec<SkillCost> OBJECT_CODEC = new Codec<SkillCost>() {
      public <T> DataResult<Pair<SkillCost, T>> decode(DynamicOps<T> ops, T input) {
         Dynamic<T> reshaped = SkillPoints.liftTree(SkillCost.inlineItem(new Dynamic(ops, input)));
         return SkillCost.OBJECT.codec().decode(ops, reshaped.getValue());
      }

      public <T> DataResult<T> encode(SkillCost input, DynamicOps<T> ops, T prefix) {
         return SkillCost.OBJECT.codec().encode(input, ops, prefix);
      }
   };
   public static final Codec<SkillCost> CODEC = Codec.either(Codec.INT, OBJECT_CODEC)
      .xmap(
         either -> (SkillCost)either.map(SkillCost::ofPoints, cost -> cost),
         cost -> cost.isPlainPoints() ? Either.left(cost.points.isEmpty() ? 0 : ((SkillPoints)cost.points.get(0)).amount()) : Either.right(cost)
      );

   public SkillCost {
      points = List.copyOf(points);
      items = List.copyOf(items);
   }

   public static SkillCost ofPoints(int points) {
      return points == 0 ? FREE : new SkillCost(List.of(SkillPoints.own(points)), List.of(), false);
   }

   private boolean isPlainPoints() {
      if (!this.items.isEmpty() || this.hidden) {
         return false;
      } else {
         return this.points.isEmpty()
            ? true
            : this.points.size() == 1 && ((SkillPoints)this.points.get(0)).tree().isEmpty() && !((SkillPoints)this.points.get(0)).hidden();
      }
   }

   static <T> Dynamic<T> inlineItem(Dynamic<T> bundle) {
      if (bundle.get("item").result().isEmpty() && bundle.get("id").result().isEmpty()) {
         return bundle;
      }

      List<Dynamic<T>> items = new ArrayList();
      Optional<Dynamic<T>> listed = bundle.get("items").result();
      if (listed.isPresent()) {
         Optional<Stream<Dynamic<T>>> stream = ((Dynamic)listed.get()).asStreamOpt().result();
         if (stream.isPresent()) {
            ((Stream<Dynamic<T>>)stream.get()).forEach(items::add);
         } else {
            items.add((Dynamic)listed.get());
         }
      }

      Dynamic<T> item = bundle;

      for (String key : BUNDLE_KEYS) {
         item = item.remove(key);
      }

      items.add(item);
      Dynamic<T> reshaped = bundle.emptyMap().set("items", bundle.createList(items.stream()));

      for (String key : BUNDLE_KEYS) {
         if (!key.equals("items")) {
            Optional<Dynamic<T>> value = bundle.get(key).result();
            if (value.isPresent()) {
               reshaped = reshaped.set(key, (Dynamic)value.get());
            }
         }
      }

      return reshaped;
   }

   public boolean isFree() {
      return this.points.isEmpty() && this.items.isEmpty();
   }

   public boolean anyHidden() {
      if (!this.hidden && !SkillPoints.anyHidden(this.points)) {
         for (SkillCost.ItemCost item : this.items) {
            if (item.hidden) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public int pointsIn(class_2960 tree, class_2960 ownTree) {
      int total = 0;

      for (SkillPoints entry : this.points) {
         if (entry.treeOr(ownTree).equals(tree)) {
            total += entry.amount();
         }
      }

      return total;
   }

   public boolean affordable(SkillData data, class_2960 ownTree) {
      if (this.points.isEmpty()) {
         return true;
      }

      for (Entry<class_2960, Integer> entry : SkillPoints.totals(this.points, ownTree).entrySet()) {
         if (data.getPoints((class_2960)entry.getKey()) < (Integer)entry.getValue()) {
            return false;
         }
      }

      return true;
   }

   public Map<class_2960, Integer> pay(SkillData data, class_2960 ownTree) {
      if (this.points.isEmpty()) {
         return Map.of();
      }

      Map<class_2960, Integer> paid = SkillPoints.totals(this.points, ownTree);

      for (Entry<class_2960, Integer> entry : paid.entrySet()) {
         data.addPoints((class_2960)entry.getKey(), -(Integer)entry.getValue());
      }

      return paid;
   }

   @Nullable
   public List<class_1799> takeItems(class_1657 player) {
      if (this.items.isEmpty()) {
         return List.of();
      }

      int[] taken = this.allocate(player);
      if (taken == null) {
         return null;
      }

      class_1661 inventory = player.method_31548();
      List<class_1799> paid = new ArrayList();

      for (int slot = 0; slot < taken.length; slot++) {
         if (taken[slot] > 0) {
            merge(paid, inventory.method_5434(slot, taken[slot]));
         }
      }

      inventory.method_5431();
      return paid;
   }

   private int @Nullable [] allocate(class_1657 player) {
      class_1661 inventory = player.method_31548();
      int[] taken = new int[41];

      for (int i = 0; i < this.items.size(); i++) {
         SkillCost.ItemCost cost = (SkillCost.ItemCost)this.items.get(i);
         int needed = cost.count();

         for (int slot = 0; slot <= 40 && needed > 0; slot++) {
            if (payable(slot, inventory)) {
               class_1799 stack = inventory.method_5438(slot);
               int available = stack.method_7947() - taken[slot];
               if (available > 0 && cost.matches(stack, player)) {
                  int use = Math.min(available, needed);
                  taken[slot] += use;
                  needed -= use;
               }
            }
         }

         if (needed > 0) {
            return null;
         }
      }

      return taken;
   }

   private static boolean payable(int slot, class_1661 inventory) {
      return slot < inventory.field_7547.size() || slot == 40;
   }

   private static void merge(List<class_1799> paid, class_1799 stack) {
      if (!stack.method_7960()) {
         for (int i = 0; i < paid.size(); i++) {
            class_1799 existing = (class_1799)paid.get(i);
            if (ItemStackData.sameData(existing, stack)) {
               int room = existing.method_7914() - existing.method_7947();
               if (room > 0) {
                  int moved = Math.min(room, stack.method_7947());
                  existing.method_7933(moved);
                  stack.method_7934(moved);
                  if (stack.method_7960()) {
                     return;
                  }
               }
            }
         }

         paid.add(stack);
      }
   }

   public static void refund(class_1657 player, List<class_1799> paid) {
      for (int i = 0; i < paid.size(); i++) {
         class_1799 stack = (class_1799)paid.get(i);
         if (!stack.method_7960()) {
            player.method_31548().method_7398(stack.method_7972());
         }
      }
   }

   public record ItemCost(class_1799 stack, Optional<ItemCondition> condition, boolean hidden) {
      public static final Codec<SkillCost.ItemCost> CODEC = RecordCodecBuilder.<SkillCost.ItemCost>mapCodec(
            i -> i.group(
                  ItemStackData.MAP_CODEC.forGetter((SkillCost.ItemCost cost) -> new ItemStackData(cost.stack)),
                  LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(SkillCost.ItemCost::condition),
                  Codec.BOOL.optionalFieldOf("hidden", false).forGetter(SkillCost.ItemCost::hidden)
               )
               .apply(i, (data, condition, hidden) -> new SkillCost.ItemCost(data.stack(), condition, hidden))
         )
         .codec()
         .flatXmap(SkillCost.ItemCost::requireCount, DataResult::success);

      private static DataResult<SkillCost.ItemCost> requireCount(SkillCost.ItemCost cost) {
         return cost.stack.method_7947() >= 1 ? DataResult.success(cost) : DataResult.error(() -> "A skill cost item needs a count of at least 1");
      }

      public int count() {
         return this.stack.method_7947();
      }

      public boolean matches(class_1799 candidate, class_1657 player) {
         if (candidate.method_7960() || !candidate.method_31574(this.stack.method_7909())) {
            return false;
         } else {
            return ItemStackData.hasData(this.stack) && !ItemStackData.sameData(candidate, this.stack)
               ? false
               : this.condition.isEmpty() || ((ItemCondition)this.condition.get()).test(new ItemCtx(candidate, player.method_37908(), player));
         }
      }
   }
}
