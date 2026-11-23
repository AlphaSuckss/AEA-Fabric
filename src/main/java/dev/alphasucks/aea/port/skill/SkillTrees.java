package dev.alphasucks.aea.port.skill;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.PowerContainerAttachment;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.skill.SkillData.Receipt;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import org.jetbrains.annotations.Nullable;

public final class SkillTrees {
   public static final class_2960 POWER_SOURCE = Apoli.id("skill_tree");

   private SkillTrees() {
   }

   public static boolean treeAvailable(@Nullable class_3222 player, SkillData data, class_2960 treeId) {
      SkillTree tree = SkillRegistry.tree(treeId);
      if (tree == null) {
         return false;
      } else if (!tree.autoGrant() && !data.hasTree(treeId)) {
         return false;
      } else if (tree.condition().isEmpty()) {
         return true;
      } else {
         return player == null ? true : ((EntityCondition)tree.condition().get()).test(new EntityCtx(player, player.method_37908()));
      }
   }

   public static boolean grantTree(class_3222 player, class_2960 treeId) {
      if (SkillRegistry.tree(treeId) == null) {
         return false;
      }

      SkillData data = SkillDataAttachment.get(player);
      if (!data.grantTree(treeId)) {
         return false;
      }

      reconcilePowers(player);
      ApoliNetwork.sendSkillState(player);
      return true;
   }

   public static boolean revokeTree(class_3222 player, class_2960 treeId) {
      SkillData data = SkillDataAttachment.get(player);
      if (!data.revokeTree(treeId)) {
         return false;
      }

      reconcilePowers(player);
      ApoliNetwork.sendSkillState(player);
      return true;
   }

   public static void grantOnJoin(class_3222 player) {
      reconcilePowers(player);
   }

   private static Set<class_2960> desiredPowers(class_3222 player) {
      SkillData data = SkillDataAttachment.get(player);
      Set<class_2960> desired = new HashSet();

      for (SkillTree tree : SkillRegistry.trees()) {
         if (treeAvailable(player, data, tree.id())) {
            desired.addAll(tree.defaultPowers());
         }
      }

      for (class_2960 skillId : data.purchasedView()) {
         Skill skill = SkillRegistry.get(skillId);
         if (skill != null && treeAvailable(player, data, SkillRegistry.rootOf(skillId))) {
            desired.addAll(skill.powers());
         }
      }

      return desired;
   }

   public static void refresh(class_3222 player) {
      reconcilePowers(player);
      ApoliNetwork.sendSkillState(player);
   }

   private static void reconcilePowers(class_3222 player) {
      PowerContainer container = PowerContainerAttachment.getOrCreate(player);
      if (container != null) {
         Set<class_2960> desired = desiredPowers(player);
         Set<class_2960> current = new HashSet();

         for (class_2960 power : container.allPowers()) {
            if (container.sourcesOf(power).contains(POWER_SOURCE)) {
               current.add(power);
            }
         }

         for (class_2960 power : current) {
            if (!desired.contains(power)) {
               container.removePower(power, POWER_SOURCE);
            }
         }

         for (class_2960 power : desired) {
            if (!current.contains(power)) {
               container.addPower(power, POWER_SOURCE);
            }
         }
      }
   }

   public static boolean parentSatisfied(SkillData data, Skill skill) {
      class_2960 parentId = skill.parent();
      if (SkillRegistry.tree(parentId) != null) {
         return true;
      }

      if (data.isPurchased(parentId)) {
         return true;
      }

      Skill parent = SkillRegistry.get(parentId);
      return parent != null && parent.isGate();
   }

   public static boolean excluded(SkillData data, class_2960 skillId) {
      Skill skill = SkillRegistry.get(skillId);
      if (skill != null) {
         for (class_2960 ex : skill.excludes()) {
            if (data.isPurchased(ex)) {
               return true;
            }
         }
      }

      for (class_2960 purchasedId : data.purchasedView()) {
         Skill p = SkillRegistry.get(purchasedId);
         if (p != null && p.excludes().contains(skillId)) {
            return true;
         }
      }

      return false;
   }

   public static boolean tryPurchase(class_3222 player, class_2960 skillId) {
      Skill skill = SkillRegistry.get(skillId);
      if (skill != null && !skill.isGate()) {
         SkillData data = SkillDataAttachment.get(player);
         if (data.isPurchased(skillId)) {
            return false;
         }

         if (!parentSatisfied(data, skill)) {
            return false;
         }

         class_2960 root = SkillRegistry.rootOf(skillId);
         if (!treeAvailable(player, data, root)) {
            return false;
         }

         if (excluded(data, skillId)) {
            return false;
         }

         if (!skill.cost().affordable(data, root)) {
            return false;
         }

         EntityCtx ctx = new EntityCtx(player, player.method_37908());
         if (skill.condition().isPresent() && !((EntityCondition)skill.condition().get()).test(ctx)) {
            return false;
         }

         List<class_1799> paidItems = skill.cost().takeItems(player);
         if (paidItems == null) {
            return false;
         }

         Map<class_2960, Integer> paidPoints = skill.cost().pay(data, root);
         Map<class_2960, Integer> rewardPoints = skill.reward().givePoints(data, root);
         List<class_1799> rewardItems = skill.reward().giveItems(player);
         data.purchase(skillId, new Receipt(paidPoints, paidItems, rewardPoints, rewardItems));
         PowerContainer container = PowerContainerAttachment.getOrCreate(player);
         if (container != null) {
            for (class_2960 power : skill.powers()) {
               container.addPower(power, POWER_SOURCE);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static void returnPayment(class_3222 player, SkillData data, @Nullable Skill skill, class_2960 root, Receipt receipt) {
      if (receipt.paidPoints() != null) {
         for (Entry<class_2960, Integer> entry : receipt.paidPoints().entrySet()) {
            data.addPoints((class_2960)entry.getKey(), (Integer)entry.getValue());
         }
      } else if (skill != null) {
         for (Entry<class_2960, Integer> entry : SkillPoints.totals(skill.cost().points(), root).entrySet()) {
            data.addPoints((class_2960)entry.getKey(), (Integer)entry.getValue());
         }
      }

      SkillCost.refund(player, receipt.paidItems());
   }

   public static void resetSkills(class_3222 player, @Nullable class_2960 tree, boolean refund) {
      SkillData data = SkillDataAttachment.get(player);

      for (class_2960 skillId : new ArrayList<class_2960>(data.purchasedView())) {
         class_2960 root = SkillRegistry.rootOf(skillId);
         if (tree == null || tree.equals(root)) {
            Skill skill = SkillRegistry.get(skillId);
            Receipt receipt = data.unpurchase(skillId);
            if (refund) {
               SkillReward.takeBack(player, data, receipt);
               returnPayment(player, data, skill, root, receipt);
            }
         }
      }

      PowerContainer container = PowerContainerAttachment.getOrCreate(player);
      if (container != null) {
         container.removeAllFromSource(POWER_SOURCE);
      }

      grantOnJoin(player);
   }

   public static boolean canRefund(SkillData data, class_2960 skillId) {
      if (!data.isPurchased(skillId)) {
         return false;
      }

      Skill skill = SkillRegistry.get(skillId);
      if (skill == null) {
         return false;
      }

      SkillTree tree = SkillRegistry.tree(SkillRegistry.rootOf(skillId));
      if (tree != null && tree.refundable()) {
         for (class_2960 child : SkillRegistry.childrenOf(skillId)) {
            if (data.isPurchased(child)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean rewardsReturnable(class_3222 player, SkillData data, class_2960 skillId) {
      return SkillReward.canTakeBack(player, data, data.receipt(skillId));
   }

   public static boolean tryRefund(class_3222 player, class_2960 skillId, boolean force) {
      SkillData data = SkillDataAttachment.get(player);
      if (force ? data.isPurchased(skillId) : canRefund(data, skillId)) {
         if (!force && !rewardsReturnable(player, data, skillId)) {
            return false;
         }

         Skill skill = SkillRegistry.get(skillId);
         Receipt receipt = data.unpurchase(skillId);
         SkillReward.takeBack(player, data, receipt);
         returnPayment(player, data, skill, SkillRegistry.rootOf(skillId), receipt);
         reconcilePowers(player);
         return true;
      } else {
         return false;
      }
   }

   public static boolean forcePurchase(class_3222 player, class_2960 skillId) {
      Skill skill = SkillRegistry.get(skillId);
      if (skill != null && !skill.isGate()) {
         SkillData data = SkillDataAttachment.get(player);
         if (data.isPurchased(skillId)) {
            return false;
         }

         class_2960 root = SkillRegistry.rootOf(skillId);
         Map<class_2960, Integer> rewardPoints = skill.reward().givePoints(data, root);
         List<class_1799> rewardItems = skill.reward().giveItems(player);
         data.purchase(skillId, Receipt.reward(rewardPoints, rewardItems));
         reconcilePowers(player);
         return true;
      } else {
         return false;
      }
   }

   public static SkillTrees.Visibility computeVisibility(class_3222 player) {
      SkillData data = SkillDataAttachment.get(player);
      EntityCtx ctx = new EntityCtx(player, player.method_37908());
      Set<class_2960> hidden = new HashSet();
      Set<class_2960> locked = new HashSet();

      for (SkillTree tree : SkillRegistry.trees()) {
         if (!treeAvailable(player, data, tree.id())) {
            hidden.add(tree.id());
         }
      }

      for (Skill skill : SkillRegistry.all()) {
         class_2960 id = skill.id();
         boolean treeVisible = treeAvailable(player, data, SkillRegistry.rootOf(id));
         boolean selfVisible = skill.visibilityCondition().isEmpty() || ((EntityCondition)skill.visibilityCondition().get()).test(ctx);
         if (treeVisible && selfVisible) {
            boolean lock = skill.condition().isPresent() && !((EntityCondition)skill.condition().get()).test(ctx);
            if (!lock) {
               lock = excluded(data, id);
            }

            if (lock) {
               locked.add(id);
            }
         } else {
            hidden.add(id);
         }
      }

      return new SkillTrees.Visibility(hidden, locked);
   }

   public record Visibility(Set<class_2960> hidden, Set<class_2960> locked) {
   }
}
