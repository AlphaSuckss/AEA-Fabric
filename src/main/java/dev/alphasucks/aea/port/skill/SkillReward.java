package dev.alphasucks.aea.port.skill;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.data.ItemStackData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_2960;

public record SkillReward(List<SkillPoints> points, List<SkillReward.RewardItem> items, boolean hidden) {
   public static final SkillReward NONE = new SkillReward(List.of(), List.of(), false);
   private static final int OFFHAND_SLOT = 40;
   private static final MapCodec<SkillReward> OBJECT = RecordCodecBuilder.mapCodec(
      i -> i.group(
            SkillPoints.LIST_CODEC.optionalFieldOf("points", List.of()).forGetter(SkillReward::points),
            SingleOrList.of(SkillReward.RewardItem.CODEC).optionalFieldOf("items", List.of()).forGetter(SkillReward::items),
            Codec.BOOL.optionalFieldOf("hidden", false).forGetter(SkillReward::hidden)
         )
         .apply(i, SkillReward::new)
   );
   private static final Codec<SkillReward> OBJECT_CODEC = new Codec<SkillReward>() {
      public <T> DataResult<Pair<SkillReward, T>> decode(DynamicOps<T> ops, T input) {
         Dynamic<T> reshaped = SkillPoints.liftTree(SkillCost.inlineItem(new Dynamic(ops, input)));
         return SkillReward.OBJECT.codec().decode(ops, reshaped.getValue());
      }

      public <T> DataResult<T> encode(SkillReward input, DynamicOps<T> ops, T prefix) {
         return SkillReward.OBJECT.codec().encode(input, ops, prefix);
      }
   };
   public static final Codec<SkillReward> CODEC = Codec.either(Codec.INT, OBJECT_CODEC)
      .xmap(
         either -> (SkillReward)either.map(SkillReward::ofPoints, reward -> reward),
         reward -> reward.isPlainPoints() ? Either.left(((SkillPoints)reward.points.get(0)).amount()) : Either.right(reward)
      );

   public SkillReward {
      points = List.copyOf(points);
      items = List.copyOf(items);
   }

   public static SkillReward ofPoints(int points) {
      return points == 0 ? NONE : new SkillReward(List.of(SkillPoints.own(points)), List.of(), false);
   }

   private boolean isPlainPoints() {
      return this.items.isEmpty()
         && !this.hidden
         && this.points.size() == 1
         && ((SkillPoints)this.points.get(0)).tree().isEmpty()
         && !((SkillPoints)this.points.get(0)).hidden();
   }

   public boolean isEmpty() {
      return this.points.isEmpty() && this.items.isEmpty();
   }

   public Map<class_2960, Integer> givePoints(SkillData data, class_2960 ownTree) {
      if (this.points.isEmpty()) {
         return Map.of();
      }

      Map<class_2960, Integer> given = SkillPoints.totals(this.points, ownTree);

      for (Entry<class_2960, Integer> entry : given.entrySet()) {
         data.addPoints((class_2960)entry.getKey(), (Integer)entry.getValue());
      }

      return given;
   }

   public List<class_1799> giveItems(class_1657 player) {
      if (this.items.isEmpty()) {
         return List.of();
      }

      List<class_1799> given = new ArrayList(this.items.size());

      for (SkillReward.RewardItem item : this.items) {
         given.add(item.stack.method_7972());
         player.method_31548().method_7398(item.stack.method_7972());
      }

      return given;
   }

   public static boolean canTakeBack(class_1657 player, SkillData data, SkillData.Receipt receipt) {
      for (Entry<class_2960, Integer> entry : receipt.rewardPoints().entrySet()) {
         if (data.getPoints((class_2960)entry.getKey()) < (Integer)entry.getValue()) {
            return false;
         }
      }

      if (receipt.rewardItems().isEmpty()) {
         return true;
      }

      class_1661 inventory = player.method_31548();
      int[] taken = new int[41];

      for (class_1799 reward : receipt.rewardItems()) {
         if (claim(inventory, reward, reward.method_7947(), taken) > 0) {
            return false;
         }
      }

      return true;
   }

   public static void takeBack(class_1657 player, SkillData data, SkillData.Receipt receipt) {
      for (Entry<class_2960, Integer> entry : receipt.rewardPoints().entrySet()) {
         data.addPoints((class_2960)entry.getKey(), -(Integer)entry.getValue());
      }

      if (!receipt.rewardItems().isEmpty()) {
         class_1661 inventory = player.method_31548();
         int[] taken = new int[41];

         for (class_1799 reward : receipt.rewardItems()) {
            claim(inventory, reward, reward.method_7947(), taken);
         }

         for (int slot = 0; slot < taken.length; slot++) {
            if (taken[slot] > 0) {
               inventory.method_5434(slot, taken[slot]);
            }
         }

         inventory.method_5431();
      }
   }

   private static int claim(class_1661 inventory, class_1799 wanted, int needed, int[] taken) {
      for (int slot = 0; slot <= 40 && needed > 0; slot++) {
         if (slot < inventory.field_7547.size() || slot == 40) {
            class_1799 stack = inventory.method_5438(slot);
            int available = stack.method_7947() - taken[slot];
            if (available > 0 && ItemStackData.sameData(stack, wanted)) {
               int use = Math.min(available, needed);
               taken[slot] += use;
               needed -= use;
            }
         }
      }

      return needed;
   }

   public record RewardItem(class_1799 stack, boolean hidden) {
      public static final Codec<SkillReward.RewardItem> CODEC = RecordCodecBuilder.<SkillReward.RewardItem>mapCodec(
            i -> i.group(
                  ItemStackData.MAP_CODEC.forGetter((SkillReward.RewardItem item) -> new ItemStackData(item.stack)),
                  Codec.BOOL.optionalFieldOf("hidden", false).forGetter(SkillReward.RewardItem::hidden)
               )
               .apply(i, (data, hidden) -> new SkillReward.RewardItem(data.stack(), hidden))
         )
         .codec()
         .flatXmap(SkillReward.RewardItem::requireCount, DataResult::success);

      private static DataResult<SkillReward.RewardItem> requireCount(SkillReward.RewardItem item) {
         return item.stack.method_7947() >= 1 ? DataResult.success(item) : DataResult.error(() -> "A skill reward item needs a count of at least 1");
      }
   }
}
