package dev.alphasucks.aea.port.client.skill;

import dev.alphasucks.aea.port.data.ItemStackData;
import dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C;
import dev.alphasucks.aea.port.network.payload.SkillStateSyncS2C;
import dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.CostItem;
import dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.PointEntry;
import dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.RewardItem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.jetbrains.annotations.Nullable;

public final class ClientSkillState {
   private static final Map<class_2960, dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry> DEFS = new LinkedHashMap();
   private static final Map<class_2960, List<class_2960>> CHILDREN = new HashMap();
   private static final List<class_2960> ROOTS = new ArrayList();
   private static final Map<class_2960, class_2960> ROOT_OF = new HashMap();
   private static final Map<class_2960, Integer> POINTS = new HashMap();
   private static final Set<class_2960> PURCHASED = new HashSet();
   private static final Set<class_2960> HIDDEN = new HashSet();
   private static final Set<class_2960> LOCKED = new HashSet();
   private static final Set<class_2960> NON_REFUNDABLE = new HashSet();

   private ClientSkillState() {
   }

   public static void applyDefs(SkillDefsSyncS2C payload) {
      DEFS.clear();
      CHILDREN.clear();
      ROOTS.clear();
      ROOT_OF.clear();

      for (dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e : payload.entries()) {
         DEFS.put(e.id(), e);
      }

      for (dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e : DEFS.values()) {
         if (e.parent().isPresent() && DEFS.containsKey(e.parent().get())) {
            ((List)CHILDREN.computeIfAbsent((class_2960)e.parent().get(), k -> new ArrayList())).add(e.id());
         } else {
            ROOTS.add(e.id());
         }
      }

      Comparator<class_2960> byOrder = Comparator.comparingInt((class_2960 idx) -> ((dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry)DEFS.get(idx)).order())
         .thenComparing(class_2960::toString);

      for (List<class_2960> siblings : CHILDREN.values()) {
         siblings.sort(byOrder);
      }

      ROOTS.sort(byOrder);

      for (class_2960 id : DEFS.keySet()) {
         class_2960 cur = id;
         int guard = 0;

         while (true) {
            dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e = (dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry)DEFS.get(cur);
            if (e == null || e.parent().isEmpty() || !DEFS.containsKey(e.parent().get()) || guard++ > 256) {
               ROOT_OF.put(id, cur);
               break;
            }

            cur = (class_2960)e.parent().get();
         }
      }
   }

   public static void applyState(SkillStateSyncS2C payload) {
      POINTS.clear();
      POINTS.putAll(payload.points());
      PURCHASED.clear();
      PURCHASED.addAll(payload.purchased());
      HIDDEN.clear();
      HIDDEN.addAll(payload.hidden());
      LOCKED.clear();
      LOCKED.addAll(payload.locked());
      NON_REFUNDABLE.clear();
      NON_REFUNDABLE.addAll(payload.nonRefundable());
      if (class_310.method_1551().field_1755 instanceof SkillTreeScreen screen) {
         screen.refreshFromState();
      }
   }

   public static void clear() {
      DEFS.clear();
      CHILDREN.clear();
      ROOTS.clear();
      ROOT_OF.clear();
      POINTS.clear();
      PURCHASED.clear();
      HIDDEN.clear();
      LOCKED.clear();
   }

   public static boolean isHidden(class_2960 id) {
      return HIDDEN.contains(id);
   }

   public static boolean canRefund(class_2960 id) {
      return refundAllowed(id) && rewardsReturnable(id);
   }

   public static boolean refundBlockedByRewards(class_2960 id) {
      return refundAllowed(id) && !rewardsReturnable(id);
   }

   private static boolean refundAllowed(class_2960 id) {
      if (isPurchased(id) && !isHidden(id)) {
         if (NON_REFUNDABLE.contains(rootOf(id))) {
            return false;
         }

         for (class_2960 child : childrenOf(id)) {
            if (isPurchased(child)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean rewardsReturnable(class_2960 id) {
      dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e = (dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry)DEFS.get(id);
      if (e == null) {
         return true;
      }

      if (!enoughPoints(e.rewardPoints(), false)) {
         return false;
      }

      for (RewardItem item : e.rewardItems()) {
         if (heldExactly(item.display()) < item.count()) {
            return false;
         }
      }

      return true;
   }

   public static boolean isLocked(class_2960 id) {
      return LOCKED.contains(id);
   }

   public static boolean hasAnyTree() {
      return !ROOTS.isEmpty();
   }

   public static List<class_2960> roots() {
      return ROOTS;
   }

   public static List<class_2960> childrenOf(class_2960 id) {
      return (List<class_2960>)CHILDREN.getOrDefault(id, List.of());
   }

   @Nullable
   public static dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry def(class_2960 id) {
      return (dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry)DEFS.get(id);
   }

   public static class_2960 rootOf(class_2960 id) {
      return (class_2960)ROOT_OF.getOrDefault(id, id);
   }

   public static int points(class_2960 root) {
      return (Integer)POINTS.getOrDefault(root, 0);
   }

   public static boolean isPurchased(class_2960 id) {
      return PURCHASED.contains(id);
   }

   public static boolean isObtained(class_2960 id) {
      if (isPurchased(id)) {
         return true;
      }

      dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e = (dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry)DEFS.get(id);
      return e != null && isGate(e);
   }

   public static boolean isGate(dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e) {
      return e.powers().isEmpty() && e.rewardPoints().isEmpty() && e.rewardItems().isEmpty();
   }

   public static class_2561 treeName(class_2960 tree) {
      class_2960 root = rootOf(tree);
      dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry def = (dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry)DEFS.get(root);
      return def != null && !def.name().getString().isEmpty() ? def.name() : class_2561.method_43470(root.toString());
   }

   public static boolean isOwnTree(dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e, PointEntry point) {
      return rootOf(point.tree()).equals(rootOf(e.id()));
   }

   public static boolean canAfford(dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e, boolean visibleOnly) {
      return visibleOnly && e.costHidden() ? true : enoughPoints(e.costPoints(), visibleOnly);
   }

   private static boolean enoughPoints(List<PointEntry> points, boolean visibleOnly) {
      if (points.isEmpty()) {
         return true;
      }

      Map<class_2960, Integer> totals = new HashMap();

      for (PointEntry point : points) {
         if (!visibleOnly || !point.hidden()) {
            totals.merge(rootOf(point.tree()), point.amount(), Integer::sum);
         }
      }

      for (Entry<class_2960, Integer> total : totals.entrySet()) {
         if (points((class_2960)total.getKey()) < (Integer)total.getValue()) {
            return false;
         }
      }

      return true;
   }

   public static boolean parentSatisfied(dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e) {
      return e.parent().isEmpty() || isObtained((class_2960)e.parent().get());
   }

   @Nullable
   public static class_2960 excludedBy(class_2960 id) {
      dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry self = (dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry)DEFS.get(id);
      if (self != null) {
         for (class_2960 other : self.excludes()) {
            if (isPurchased(other)) {
               return other;
            }
         }
      }

      for (class_2960 purchased : PURCHASED) {
         dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry other = (dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry)DEFS.get(purchased);
         if (other != null && other.excludes().contains(id)) {
            return purchased;
         }
      }

      return null;
   }

   public static boolean canBuy(dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e) {
      return !isGate(e)
         && !isPurchased(e.id())
         && !isHidden(e.id())
         && !isLocked(e.id())
         && parentSatisfied(e)
         && canAfford(e, false)
         && hasCostItems(e, false);
   }

   public static boolean hasCostItems(dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e, boolean visibleOnly) {
      if (!e.costItems().isEmpty() && (!visibleOnly || !e.costHidden())) {
         for (CostItem item : e.costItems()) {
            if ((!visibleOnly || !item.hidden()) && heldCount(item) < item.count()) {
               return false;
            }
         }

         return true;
      } else {
         return true;
      }
   }

   public static int heldCount(CostItem item) {
      class_1657 player = class_310.method_1551().field_1724;
      if (player == null) {
         return 0;
      }

      class_1661 inventory = player.method_31548();
      int count = 0;

      for (int slot = 0; slot < inventory.field_7547.size(); slot++) {
         count += matching(inventory.method_5438(slot), item.display());
      }

      return count + matching(inventory.field_7544.get(0), item.display());
   }

   private static int heldExactly(class_1799 template) {
      class_1657 player = class_310.method_1551().field_1724;
      if (player == null) {
         return 0;
      }

      class_1661 inventory = player.method_31548();
      int count = 0;

      for (int slot = 0; slot < inventory.field_7547.size(); slot++) {
         class_1799 stack = inventory.method_5438(slot);
         if (ItemStackData.sameData(stack, template)) {
            count += stack.method_7947();
         }
      }

      class_1799 offhand = inventory.field_7544.get(0);
      return ItemStackData.sameData(offhand, template) ? count + offhand.method_7947() : count;
   }

   private static int matching(class_1799 stack, class_1799 template) {
      if (stack.method_7960() || !stack.method_31574(template.method_7909())) {
         return 0;
      } else {
         return ItemStackData.hasData(template) && !ItemStackData.sameData(stack, template) ? 0 : stack.method_7947();
      }
   }
}
