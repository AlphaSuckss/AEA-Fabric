package dev.alphasucks.aea.port.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class SkillData {
   private final Map<class_2960, Integer> points;
   private final Set<class_2960> purchased;
   private final Set<class_2960> grantedTrees;
   private final Map<class_2960, List<class_1799>> paidItems;
   private final Map<class_2960, Map<class_2960, Integer>> paidPoints;
   private final Map<class_2960, Map<class_2960, Integer>> rewardPoints;
   private final Map<class_2960, List<class_1799>> rewardItems;
   private static final Codec<Map<class_2960, Map<class_2960, Integer>>> POINTS_BY_SKILL = Codec.unboundedMap(
      class_2960.field_25139, Codec.unboundedMap(class_2960.field_25139, Codec.INT)
   );
   public static final Codec<SkillData> CODEC = RecordCodecBuilder.create(
      i -> i.group(
            Codec.unboundedMap(class_2960.field_25139, Codec.INT).optionalFieldOf("points", Map.of()).forGetter(d -> d.points),
            class_2960.field_25139.listOf().optionalFieldOf("purchased", List.of()).forGetter(d -> new ArrayList(d.purchased)),
            class_2960.field_25139.listOf().optionalFieldOf("granted_trees", List.of()).forGetter(d -> new ArrayList(d.grantedTrees)),
            Codec.unboundedMap(class_2960.field_25139, class_1799.field_24671.listOf()).optionalFieldOf("paid_items", Map.of()).forGetter(d -> d.paidItems),
            POINTS_BY_SKILL.optionalFieldOf("paid_points", Map.of()).forGetter(d -> d.paidPoints),
            POINTS_BY_SKILL.optionalFieldOf("reward_points", Map.of()).forGetter(d -> d.rewardPoints),
            Codec.unboundedMap(class_2960.field_25139, class_1799.field_24671.listOf()).optionalFieldOf("reward_items", Map.of()).forGetter(d -> d.rewardItems)
         )
         .apply(
            i,
            (points, purchased, grantedTrees, paidItems, paidPoints, rewardPoints, rewardItems) -> new SkillData(
               points, new HashSet(purchased), new HashSet(grantedTrees), paidItems, paidPoints, rewardPoints, rewardItems
            )
         )
   );

   public SkillData() {
      this(new HashMap(), new HashSet(), new HashSet(), new HashMap(), new HashMap(), new HashMap(), new HashMap());
   }

   private SkillData(
      Map<class_2960, Integer> points,
      Set<class_2960> purchased,
      Set<class_2960> grantedTrees,
      Map<class_2960, List<class_1799>> paidItems,
      Map<class_2960, Map<class_2960, Integer>> paidPoints,
      Map<class_2960, Map<class_2960, Integer>> rewardPoints,
      Map<class_2960, List<class_1799>> rewardItems
   ) {
      this.points = new HashMap(points);
      this.purchased = new HashSet(purchased);
      this.grantedTrees = new HashSet(grantedTrees);
      this.paidItems = new HashMap(paidItems);
      this.paidPoints = new HashMap(paidPoints);
      this.rewardPoints = new HashMap(rewardPoints);
      this.rewardItems = new HashMap(rewardItems);
   }

   public int getPoints(class_2960 root) {
      return (Integer)this.points.getOrDefault(root, 0);
   }

   public void setPoints(class_2960 root, int value) {
      this.points.put(root, Math.max(0, value));
   }

   public void addPoints(class_2960 root, int delta) {
      this.setPoints(root, this.getPoints(root) + delta);
   }

   public boolean isPurchased(class_2960 skill) {
      return this.purchased.contains(skill);
   }

   public void purchase(class_2960 skill, SkillData.Receipt receipt) {
      this.purchased.add(skill);
      store(this.paidItems, skill, receipt.paidItems());
      store(this.rewardItems, skill, receipt.rewardItems());
      if (receipt.paidPoints() == null) {
         this.paidPoints.remove(skill);
      } else {
         this.paidPoints.put(skill, Map.copyOf(receipt.paidPoints()));
      }

      if (receipt.rewardPoints().isEmpty()) {
         this.rewardPoints.remove(skill);
      } else {
         this.rewardPoints.put(skill, Map.copyOf(receipt.rewardPoints()));
      }
   }

   private static void store(Map<class_2960, List<class_1799>> stacks, class_2960 skill, List<class_1799> recorded) {
      if (recorded.isEmpty()) {
         stacks.remove(skill);
      } else {
         stacks.put(skill, List.copyOf(recorded));
      }
   }

   public SkillData.Receipt receipt(class_2960 skill) {
      return new SkillData.Receipt(
         (Map<class_2960, Integer>)this.paidPoints.get(skill),
         (List<class_1799>)this.paidItems.getOrDefault(skill, List.of()),
         (Map<class_2960, Integer>)this.rewardPoints.getOrDefault(skill, Map.of()),
         (List<class_1799>)this.rewardItems.getOrDefault(skill, List.of())
      );
   }

   public SkillData.Receipt unpurchase(class_2960 skill) {
      SkillData.Receipt receipt = this.receipt(skill);
      this.purchased.remove(skill);
      this.paidItems.remove(skill);
      this.paidPoints.remove(skill);
      this.rewardPoints.remove(skill);
      this.rewardItems.remove(skill);
      return receipt;
   }

   public boolean hasTree(class_2960 tree) {
      return this.grantedTrees.contains(tree);
   }

   public boolean grantTree(class_2960 tree) {
      return this.grantedTrees.add(tree);
   }

   public boolean revokeTree(class_2960 tree) {
      return this.grantedTrees.remove(tree);
   }

   public Map<class_2960, Integer> pointsView() {
      return this.points;
   }

   public Set<class_2960> purchasedView() {
      return this.purchased;
   }

   public Set<class_2960> grantedTreesView() {
      return this.grantedTrees;
   }

   public void copyFrom(SkillData other) {
      this.points.clear();
      this.points.putAll(other.points);
      this.purchased.clear();
      this.purchased.addAll(other.purchased);
      this.grantedTrees.clear();
      this.grantedTrees.addAll(other.grantedTrees);
      copyStacks(other.paidItems, this.paidItems);
      copyStacks(other.rewardItems, this.rewardItems);
      this.paidPoints.clear();
      this.paidPoints.putAll(other.paidPoints);
      this.rewardPoints.clear();
      this.rewardPoints.putAll(other.rewardPoints);
   }

   private static void copyStacks(Map<class_2960, List<class_1799>> from, Map<class_2960, List<class_1799>> to) {
      to.clear();
      from.forEach((skill, stacks) -> {
         List<class_1799> copies = new ArrayList(stacks.size());

         for (class_1799 stack : stacks) {
            copies.add(stack.method_7972());
         }

         to.put(skill, copies);
      });
   }

   public record Receipt(
      @Nullable Map<class_2960, Integer> paidPoints, List<class_1799> paidItems, Map<class_2960, Integer> rewardPoints, List<class_1799> rewardItems
   ) {
      public static SkillData.Receipt reward(Map<class_2960, Integer> rewardPoints, List<class_1799> rewardItems) {
         return new SkillData.Receipt(Map.of(), List.of(), rewardPoints, rewardItems);
      }
   }
}
