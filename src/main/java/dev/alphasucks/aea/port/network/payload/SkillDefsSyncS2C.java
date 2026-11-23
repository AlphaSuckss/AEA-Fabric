package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.data.IconData;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.skill.Skill;
import dev.alphasucks.aea.port.skill.SkillFrame;
import dev.alphasucks.aea.port.skill.SkillPlacement;
import dev.alphasucks.aea.port.skill.SkillPoints;
import dev.alphasucks.aea.port.skill.SkillRegistry;
import dev.alphasucks.aea.port.skill.SkillReward;
import dev.alphasucks.aea.port.skill.SkillTree;
import dev.alphasucks.aea.port.skill.SkillCost.ItemCost;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1799;
import net.minecraft.class_2540;
import net.minecraft.class_2561;
import net.minecraft.class_2960;

public record SkillDefsSyncS2C(List<SkillDefsSyncS2C.Entry> entries, boolean legacyFormat) {
   public static final class_2960 CHANNEL = Apoli.internal("skill_defs_sync");

   public SkillDefsSyncS2C(List<SkillDefsSyncS2C.Entry> entries) {
      this(entries, false);
   }

   public static SkillDefsSyncS2C fromCurrent() {
      return fromCurrent(false);
   }

   public static SkillDefsSyncS2C fromCurrent(boolean legacyFormat) {
      List<SkillDefsSyncS2C.Entry> entries = new ArrayList();

      for (SkillTree tree : SkillRegistry.trees()) {
         entries.add(SkillDefsSyncS2C.Entry.tree(tree.id(), tree.name(), tree.description(), tree.icon(), tree.background(), tree.order(), tree.frame()));
      }

      for (Skill skill : SkillRegistry.all()) {
         class_2960 root = SkillRegistry.rootOf(skill.id());
         List<SkillDefsSyncS2C.CostItem> costItems = new ArrayList(skill.cost().items().size());

         for (ItemCost item : skill.cost().items()) {
            costItems.add(SkillDefsSyncS2C.CostItem.of(item));
         }

         SkillReward reward = skill.reward();
         List<SkillDefsSyncS2C.RewardItem> rewardItems = new ArrayList(reward.items().size());

         for (SkillReward.RewardItem item : reward.items()) {
            rewardItems.add(new SkillDefsSyncS2C.RewardItem(item.stack().method_46651(1), item.stack().method_7947(), item.hidden()));
         }

         entries.add(
            new SkillDefsSyncS2C.Entry(
               skill.id(),
               Optional.of(skill.parent()),
               skill.name(),
               skill.description(),
               skill.icon(),
               skill.powers(),
               skill.cost().pointsIn(root, root),
               Optional.empty(),
               skill.order(),
               skill.frame(),
               skill.excludes(),
               costItems,
               pointEntries(skill.cost().points(), root),
               skill.cost().hidden(),
               pointEntries(reward.points(), root),
               rewardItems,
               reward.hidden() ? List.of() : powerNames(skill),
               reward.hidden(),
               skill.placement()
            )
         );
      }

      return new SkillDefsSyncS2C(entries, legacyFormat);
   }

   private static List<SkillDefsSyncS2C.PointEntry> pointEntries(List<SkillPoints> points, class_2960 root) {
      if (points.isEmpty()) {
         return List.of();
      }

      List<SkillDefsSyncS2C.PointEntry> entries = new ArrayList(points.size());

      for (SkillPoints point : points) {
         entries.add(new SkillDefsSyncS2C.PointEntry(point.treeOr(root), point.amount(), point.hidden()));
      }

      return entries;
   }

   private static List<class_2561> powerNames(Skill skill) {
      List<class_2561> names = new ArrayList();

      for (class_2960 powerId : skill.powers()) {
         if (!powerId.equals(skill.id())) {
            Power power = ApoliPowers.get(powerId);
            if (power != null && !power.hidden()) {
               names.add(power.displayName(powerId));
            }
         }
      }

      return names;
   }

   public void write(class_2540 buf) {
      buf.method_10804(this.entries.size());

      for (SkillDefsSyncS2C.Entry e : this.entries) {
         buf.method_10812(e.id);
         buf.method_37435(e.parent, (b, p) -> b.method_10812(p));
         buf.method_10805(e.name);
         buf.method_10805(e.description);
         if (this.legacyFormat) {
            buf.method_10793(e.icon.stack());
         } else {
            e.icon.write(buf);
         }

         buf.method_34062(e.powers, (b, p) -> b.method_10812(p));
         buf.method_10804(e.cost);
         buf.method_37435(e.background, (b, bg) -> b.method_10812(bg));
         if (!this.legacyFormat) {
            buf.method_10804(e.order);
            buf.method_10817(e.frame);
            buf.method_34062(e.excludes, (b, x) -> b.method_10812(x));
            buf.method_10804(e.costItems.size());

            for (SkillDefsSyncS2C.CostItem item : e.costItems) {
               buf.method_10793(item.display);
               buf.method_10804(item.count);
               buf.writeBoolean(item.conditional);
               buf.writeBoolean(item.hidden);
            }

            writePoints(buf, e.costPoints);
            buf.writeBoolean(e.costHidden);
            writePoints(buf, e.rewardPoints);
            buf.method_10804(e.rewardItems.size());

            for (SkillDefsSyncS2C.RewardItem item : e.rewardItems) {
               buf.method_10793(item.display);
               buf.method_10804(item.count);
               buf.writeBoolean(item.hidden);
            }

            buf.method_10804(e.rewardPowers.size());

            for (class_2561 name : e.rewardPowers) {
               buf.method_10805(name);
            }

            buf.writeBoolean(e.rewardHidden);
            buf.writeBoolean(e.placement.isPresent());
            if (e.placement.isPresent()) {
               buf.writeFloat(((SkillPlacement)e.placement.get()).x());
               buf.writeFloat(((SkillPlacement)e.placement.get()).y());
            }
         }
      }
   }

   private static void writePoints(class_2540 buf, List<SkillDefsSyncS2C.PointEntry> points) {
      buf.method_10804(points.size());

      for (SkillDefsSyncS2C.PointEntry point : points) {
         buf.method_10812(point.tree);
         buf.method_10804(point.amount);
         buf.writeBoolean(point.hidden);
      }
   }

   public static SkillDefsSyncS2C read(class_2540 buf) {
      int start = buf.readerIndex();

      try {
         SkillDefsSyncS2C parsed = readEntries(buf, true);
         if (!buf.isReadable()) {
            return parsed;
         }
      } catch (Exception var3) {
      }

      buf.readerIndex(start);
      return readEntries(buf, false);
   }

   private static SkillDefsSyncS2C readEntries(class_2540 buf, boolean modern) {
      int n = buf.method_10816();
      List<SkillDefsSyncS2C.Entry> entries = new ArrayList(Math.min(n, 1024));

      for (int i = 0; i < n; i++) {
         class_2960 id = buf.method_10810();
         Optional<class_2960> parent = buf.method_37436(b -> b.method_10810());
         class_2561 name = buf.method_10808();
         class_2561 description = buf.method_10808();
         IconData icon = modern ? IconData.read(buf) : IconData.ofItem(buf.method_10819());
         List<class_2960> powers = buf.method_34066(b -> b.method_10810());
         int cost = buf.method_10816();
         Optional<class_2960> background = buf.method_37436(b -> b.method_10810());
         if (!modern) {
            List<SkillDefsSyncS2C.PointEntry> legacyPoints = cost != 0 && !parent.isEmpty()
               ? List.of(new SkillDefsSyncS2C.PointEntry((class_2960)parent.get(), cost, false))
               : List.of();
            entries.add(
               new SkillDefsSyncS2C.Entry(
                  id,
                  parent,
                  name,
                  description,
                  icon,
                  powers,
                  cost,
                  background,
                  0,
                  SkillFrame.TASK,
                  List.of(),
                  List.of(),
                  legacyPoints,
                  false,
                  List.of(),
                  List.of(),
                  List.of(),
                  false,
                  Optional.empty()
               )
            );
         } else {
            int order = buf.method_10816();
            SkillFrame frame = buf.method_10818(SkillFrame.class);
            List<class_2960> excludes = buf.method_34066(b -> b.method_10810());
            List<SkillDefsSyncS2C.CostItem> costItems = readCostItems(buf);
            List<SkillDefsSyncS2C.PointEntry> costPoints = readPoints(buf);
            boolean costHidden = buf.readBoolean();
            List<SkillDefsSyncS2C.PointEntry> rewardPoints = readPoints(buf);
            List<SkillDefsSyncS2C.RewardItem> rewardItems = readRewardItems(buf);
            int powerNames = buf.method_10816();
            List<class_2561> rewardPowers = new ArrayList(Math.min(powerNames, 64));

            for (int k = 0; k < powerNames; k++) {
               rewardPowers.add(buf.method_10808());
            }

            boolean rewardHidden = buf.readBoolean();
            Optional<SkillPlacement> placement = buf.readBoolean() ? Optional.of(new SkillPlacement(buf.readFloat(), buf.readFloat())) : Optional.empty();
            entries.add(
               new SkillDefsSyncS2C.Entry(
                  id,
                  parent,
                  name,
                  description,
                  icon,
                  powers,
                  cost,
                  background,
                  order,
                  frame,
                  excludes,
                  costItems,
                  costPoints,
                  costHidden,
                  rewardPoints,
                  rewardItems,
                  rewardPowers,
                  rewardHidden,
                  placement
               )
            );
         }
      }

      return new SkillDefsSyncS2C(entries);
   }

   private static List<SkillDefsSyncS2C.PointEntry> readPoints(class_2540 buf) {
      int n = buf.method_10816();
      List<SkillDefsSyncS2C.PointEntry> points = new ArrayList(Math.min(n, 64));

      for (int i = 0; i < n; i++) {
         points.add(new SkillDefsSyncS2C.PointEntry(buf.method_10810(), buf.method_10816(), buf.readBoolean()));
      }

      return points;
   }

   private static List<SkillDefsSyncS2C.CostItem> readCostItems(class_2540 buf) {
      int n = buf.method_10816();
      List<SkillDefsSyncS2C.CostItem> items = new ArrayList(Math.min(n, 64));

      for (int i = 0; i < n; i++) {
         class_1799 display = buf.method_10819();
         items.add(new SkillDefsSyncS2C.CostItem(display, buf.method_10816(), buf.readBoolean(), buf.readBoolean()));
      }

      return items;
   }

   private static List<SkillDefsSyncS2C.RewardItem> readRewardItems(class_2540 buf) {
      int n = buf.method_10816();
      List<SkillDefsSyncS2C.RewardItem> items = new ArrayList(Math.min(n, 64));

      for (int i = 0; i < n; i++) {
         class_1799 display = buf.method_10819();
         items.add(new SkillDefsSyncS2C.RewardItem(display, buf.method_10816(), buf.readBoolean()));
      }

      return items;
   }

   public record CostItem(class_1799 display, int count, boolean conditional, boolean hidden) {
      static SkillDefsSyncS2C.CostItem of(ItemCost cost) {
         return new SkillDefsSyncS2C.CostItem(cost.stack().method_46651(1), cost.count(), cost.condition().isPresent(), cost.hidden());
      }
   }

   public record Entry(
      class_2960 id,
      Optional<class_2960> parent,
      class_2561 name,
      class_2561 description,
      IconData icon,
      List<class_2960> powers,
      int cost,
      Optional<class_2960> background,
      int order,
      SkillFrame frame,
      List<class_2960> excludes,
      List<SkillDefsSyncS2C.CostItem> costItems,
      List<SkillDefsSyncS2C.PointEntry> costPoints,
      boolean costHidden,
      List<SkillDefsSyncS2C.PointEntry> rewardPoints,
      List<SkillDefsSyncS2C.RewardItem> rewardItems,
      List<class_2561> rewardPowers,
      boolean rewardHidden,
      Optional<SkillPlacement> placement
   ) {
      static SkillDefsSyncS2C.Entry tree(
         class_2960 id, class_2561 name, class_2561 description, IconData icon, Optional<class_2960> background, int order, SkillFrame frame
      ) {
         return new SkillDefsSyncS2C.Entry(
            id,
            Optional.empty(),
            name,
            description,
            icon,
            List.of(),
            0,
            background,
            order,
            frame,
            List.of(),
            List.of(),
            List.of(),
            false,
            List.of(),
            List.of(),
            List.of(),
            false,
            Optional.empty()
         );
      }
   }

   public record PointEntry(class_2960 tree, int amount, boolean hidden) {
   }

   public record RewardItem(class_1799 display, int count, boolean hidden) {
   }
}
