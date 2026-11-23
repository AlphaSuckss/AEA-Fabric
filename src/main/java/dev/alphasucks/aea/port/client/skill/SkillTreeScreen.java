package dev.alphasucks.aea.port.client.skill;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.alphasucks.aea.port.client.IconRenderer;
import dev.alphasucks.aea.port.network.payload.BuySkillC2S;
import dev.alphasucks.aea.port.network.payload.RefundSkillC2S;
import dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.CostItem;
import dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.PointEntry;
import dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.RewardItem;
import dev.alphasucks.aea.port.skill.SkillFrame;
import dev.alphasucks.aea.port.skill.SkillPlacement;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.class_124;
import net.minecraft.class_2540;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import net.minecraft.class_5481;
import org.jetbrains.annotations.Nullable;

public class SkillTreeScreen extends class_437 {
   private static final class_2960 WINDOW = new class_2960("textures/gui/advancements/window.png");
   private static final class_2960 WIDGETS = new class_2960("textures/gui/advancements/widgets.png");
   private static final class_2960 DEFAULT_BACKGROUND = new class_2960("textures/block/andesite.png");
   private static final int WINDOW_WIDTH = 252;
   private static final int WINDOW_HEIGHT = 140;
   private static final int INSIDE_X = 9;
   private static final int INSIDE_Y = 18;
   private static final int INSIDE_W = 234;
   private static final int INSIDE_H = 113;
   private final List<class_2960> roots;
   private class_2960 currentRoot;
   private static final float MIN_ZOOM = 0.25F;
   private static final float MAX_ZOOM = 1.0F;
   private final Map<class_2960, int[]> positions = new HashMap();
   private List<class_2960> treeSkills = new ArrayList();
   private double scrollX;
   private double scrollY;
   private float zoom = 1.0F;
   private boolean centered;
   private int minX;
   private int minY;
   private int maxX;
   private int maxY;
   private boolean isScrolling;

   public SkillTreeScreen() {
      super(class_2561.method_43471("screen.apoli.skill_tree"));
      this.roots = new ArrayList();

      for (class_2960 r : ClientSkillState.roots()) {
         if (!ClientSkillState.isHidden(r)) {
            this.roots.add(r);
         }
      }

      this.currentRoot = this.roots.isEmpty() ? null : (class_2960)this.roots.get(0);
   }

   @Override
   protected void method_25426() {
      this.layout();
   }

   public void refreshFromState() {
      class_2960 previousRoot = this.currentRoot;
      double previousScrollX = this.scrollX;
      double previousScrollY = this.scrollY;
      boolean hadView = this.centered;
      this.roots.clear();

      for (class_2960 r : ClientSkillState.roots()) {
         if (!ClientSkillState.isHidden(r)) {
            this.roots.add(r);
         }
      }

      if (this.currentRoot == null || !this.roots.contains(this.currentRoot)) {
         this.currentRoot = this.roots.isEmpty() ? null : (class_2960)this.roots.get(0);
      }

      this.layout();
      if (hadView && this.currentRoot != null && this.currentRoot.equals(previousRoot)) {
         this.scrollX = previousScrollX;
         this.scrollY = previousScrollY;
         this.centered = true;
         this.clampScroll();
      }
   }

   private void layout() {
      this.positions.clear();
      this.treeSkills.clear();
      this.centered = false;
      this.minX = this.minY = Integer.MAX_VALUE;
      this.maxX = this.maxY = Integer.MIN_VALUE;
      if (this.currentRoot != null) {
         this.gather(this.currentRoot, this.treeSkills);
         Map<class_2960, float[]> grid = new HashMap();
         this.placeRow(this.currentRoot, 0.0F, new float[]{0.0F}, grid, null);

         for (class_2960 id : this.treeSkills) {
            dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e = ClientSkillState.def(id);
            if (e != null && !e.placement().isEmpty()) {
               List<class_2960> branch = new ArrayList();
               float row = this.placeRow(id, ((SkillPlacement)e.placement().get()).x(), new float[]{0.0F}, grid, branch);
               float shift = ((SkillPlacement)e.placement().get()).y() - row;

               for (class_2960 member : branch) {
                  ((float[])grid.get(member))[1] += shift;
               }
            }
         }

         for (Entry<class_2960, float[]> cell : grid.entrySet()) {
            this.positions
               .put(
                  (class_2960)cell.getKey(),
                  new int[]{class_3532.method_15375(((float[])cell.getValue())[0] * 28.0F), class_3532.method_15375(((float[])cell.getValue())[1] * 27.0F)}
               );
         }

         for (class_2960 id : this.treeSkills) {
            int[] p = (int[])this.positions.get(id);
            if (p != null) {
               this.minX = Math.min(this.minX, p[0]);
               this.maxX = Math.max(this.maxX, p[0] + 28);
               this.minY = Math.min(this.minY, p[1]);
               this.maxY = Math.max(this.maxY, p[1] + 27);
            }
         }
      }
   }

   private void gather(class_2960 id, List<class_2960> out) {
      if (ClientSkillState.def(id) != null && !ClientSkillState.isHidden(id)) {
         out.add(id);

         for (class_2960 child : ClientSkillState.childrenOf(id)) {
            this.gather(child, out);
         }
      }
   }

   private float placeRow(class_2960 id, float column, float[] leaf, Map<class_2960, float[]> grid, @Nullable List<class_2960> branch) {
      List<class_2960> children = flowChildren(id);
      float row;
      if (children.isEmpty()) {
         row = (float)(leaf[0]++);
      } else {
         float first = 0.0F;
         float last = 0.0F;

         for (int k = 0; k < children.size(); k++) {
            float r = this.placeRow((class_2960)children.get(k), column + 1.0F, leaf, grid, branch);
            if (k == 0) {
               first = r;
            }

            if (k == children.size() - 1) {
               last = r;
            }
         }

         row = (first + last) / 2.0F;
      }

      grid.put(id, new float[]{column, row});
      if (branch != null) {
         branch.add(id);
      }

      return row;
   }

   private static List<class_2960> flowChildren(class_2960 id) {
      List<class_2960> children = ClientSkillState.childrenOf(id);
      List<class_2960> flow = new ArrayList(children.size());

      for (class_2960 child : children) {
         dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e = ClientSkillState.def(child);
         if (e == null || e.placement().isEmpty()) {
            flow.add(child);
         }
      }

      return flow;
   }

   @Override
   public void method_25394(class_332 g, int mouseX, int mouseY, float partialTick) {
      this.method_25420(g);
      int x = (this.field_22789 - 252) / 2;
      int y = (this.field_22790 - 140) / 2;
      this.renderInside(g, mouseX, mouseY, x, y);
      this.renderWindow(g, x, y);
      this.renderTooltips(g, mouseX, mouseY, x, y);
   }

   private void renderInside(class_332 g, int mouseX, int mouseY, int x, int y) {
      if (this.currentRoot == null) {
         g.method_25294(x + 9, y + 18, x + 9 + 234, y + 18 + 113, -16777216);
         g.method_27534(this.field_22793, class_2561.method_43471("screen.apoli.skill_tree.empty"), x + 9 + 117, y + 18 + 56 - 4, 16777215);
      } else {
         if (!this.centered) {
            this.scrollX = 234.0 / (2.0 * this.zoom) - (this.maxX + this.minX) / 2.0;
            this.scrollY = 113.0 / (2.0 * this.zoom) - (this.maxY + this.minY) / 2.0;
            this.centered = true;
         }

         int ox = x + 9;
         int oy = y + 18;
         g.method_44379(ox, oy, ox + 234, oy + 113);
         g.method_51448().method_22903();
         g.method_51448().method_46416(ox, oy, 0.0F);
         g.method_51448().method_22905(this.zoom, this.zoom, 1.0F);
         int k = class_3532.method_15357(this.scrollX);
         int l = class_3532.method_15357(this.scrollY);
         class_2960 bg = this.resolveBackground();
         int m = k % 16;
         int n = l % 16;
         int tilesX = class_3532.method_15386(234.0F / this.zoom / 16.0F) + 1;
         int tilesY = class_3532.method_15386(113.0F / this.zoom / 16.0F) + 1;

         for (int a = -1; a <= tilesX; a++) {
            for (int b = -1; b <= tilesY; b++) {
               g.method_25290(bg, m + 16 * a, n + 16 * b, 0.0F, 0.0F, 16, 16, 16, 16);
            }
         }

         this.drawConnectivity(g, k, l, true);
         this.drawConnectivity(g, k, l, false);
         this.drawWidgets(g, k, l);
         g.method_51448().method_22909();
         g.method_44380();
      }
   }

   private class_2960 resolveBackground() {
      dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry root = ClientSkillState.def(this.currentRoot);
      return root != null && root.background().isPresent() ? (class_2960)root.background().get() : DEFAULT_BACKGROUND;
   }

   private void drawConnectivity(class_332 g, int ox, int oy, boolean border) {
      for (class_2960 id : this.treeSkills) {
         dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e = ClientSkillState.def(id);
         if (e != null && !e.parent().isEmpty() && !e.placement().isPresent()) {
            int[] parent = (int[])this.positions.get(e.parent().get());
            int[] self = (int[])this.positions.get(id);
            if (parent != null && self != null) {
               int k = ox + parent[0] + 13;
               int l = ox + parent[0] + 26 + 4;
               int m = oy + parent[1] + 13;
               int nn = ox + self[0] + 13;
               int o = oy + self[1] + 13;
               int color = border ? -16777216 : -1;
               if (border) {
                  g.method_25292(l, k, m - 1, color);
                  g.method_25292(l + 1, k, m, color);
                  g.method_25292(l, k, m + 1, color);
                  g.method_25292(nn, l - 1, o - 1, color);
                  g.method_25292(nn, l - 1, o, color);
                  g.method_25292(nn, l - 1, o + 1, color);
                  g.method_25301(l - 1, o, m, color);
                  g.method_25301(l + 1, o, m, color);
               } else {
                  g.method_25292(l, k, m, color);
                  g.method_25292(nn, l, o, color);
                  g.method_25301(l, o, m, color);
               }
            }
         }
      }
   }

   private void drawWidgets(class_332 g, int ox, int oy) {
      for (class_2960 id : this.treeSkills) {
         dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e = ClientSkillState.def(id);
         int[] pos = (int[])this.positions.get(id);
         if (e != null && pos != null) {
            drawFrame(g, ox + pos[0] + 3, oy + pos[1], e.frame(), isObtained(e));
            IconRenderer.renderFake(g, e.icon(), ox + pos[0] + 8, oy + pos[1] + 5);
            if (ClientSkillState.isLocked(id)) {
               g.method_25294(ox + pos[0] + 3, oy + pos[1], ox + pos[0] + 29, oy + pos[1] + 26, -1342177280);
            }
         }
      }
   }

   private static void drawFrame(class_332 g, int x, int y, SkillFrame frame, boolean obtained) {
      g.method_25302(WIDGETS, x, y, frameU(frame), 128 + (obtained ? 0 : 26), 26, 26);
   }

   private static int frameU(SkillFrame frame) {
      return switch (frame) {
         case TASK -> 0;
         case CHALLENGE -> 26;
         case GOAL -> 52;
      };
   }

   private static boolean isObtained(dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e) {
      return ClientSkillState.isObtained(e.id());
   }

   private void renderWindow(class_332 g, int x, int y) {
      RenderSystem.enableBlend();
      g.method_25302(WINDOW, x, y, 0, 0, 252, 140);
      if (this.roots.size() > 1) {
         this.drawTabs(g, x, y);
      }

      dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry rootDef = this.currentRoot == null ? null : ClientSkillState.def(this.currentRoot);
      class_2561 title = rootDef != null ? rootDef.name() : this.method_25440();
      g.method_51439(this.field_22793, title, x + 8, y + 6, 4210752, false);
      if (this.currentRoot != null) {
         class_2561 points = class_2561.method_43469("screen.apoli.skill_tree.points", ClientSkillState.points(this.currentRoot));
         int w = this.field_22793.method_27525(points);
         g.method_51439(this.field_22793, points, x + 252 - w - 8, y + 6, 4210752, false);
      }
   }

   private void drawTabs(class_332 g, int x, int y) {
      for (int i = 0; i < this.roots.size(); i++) {
         class_2960 r = (class_2960)this.roots.get(i);
         dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry d = ClientSkillState.def(r);
         int tx = x + 4 + i * 29;
         int ty = y - 28 + 4;
         drawFrame(g, tx, ty, d != null ? d.frame() : SkillFrame.TASK, r.equals(this.currentRoot));
         if (d != null) {
            IconRenderer.renderFake(g, d.icon(), tx + 5, ty + 5);
         }
      }
   }

   private void renderTooltips(class_332 g, int mouseX, int mouseY, int x, int y) {
      if (this.currentRoot != null) {
         int screenX = mouseX - x - 9;
         int screenY = mouseY - y - 18;
         if (screenX > 0 && screenX < 234 && screenY > 0 && screenY < 113) {
            double localX = screenX / this.zoom;
            double localY = screenY / this.zoom;
            int k = class_3532.method_15357(this.scrollX);
            int l = class_3532.method_15357(this.scrollY);

            for (class_2960 id : this.treeSkills) {
               dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e = ClientSkillState.def(id);
               int[] pos = (int[])this.positions.get(id);
               if (e != null && pos != null) {
                  int wx = k + pos[0];
                  int wy = l + pos[1];
                  if (localX >= wx && localX <= wx + 26 && localY >= wy && localY <= wy + 26) {
                     g.method_51448().method_22903();
                     g.method_51448().method_46416(x + 9, y + 18, 400.0F);
                     this.drawHover(g, e, class_3532.method_15375(wx * this.zoom), class_3532.method_15375(wy * this.zoom));
                     g.method_51448().method_22909();
                     break;
                  }
               }
            }
         }
      }
   }

   private void drawHover(class_332 g, dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e, int wx, int wy) {
      List<class_5481> lines = this.tooltipLines(e);
      int boxWidth = Math.max(this.field_22793.method_27525(e.name()) + 32 + 5, 120);

      for (class_5481 line : lines) {
         boxWidth = Math.max(boxWidth, this.field_22793.method_30880(line) + 10);
      }

      boolean obtained = isObtained(e);
      int boxV = obtained ? 0 : 26;
      boolean flipRight = wx + 26 + boxWidth >= 234;
      int bx = flipRight ? wx + 26 - boxWidth : wx;
      int by = wy;
      int descPanelH = 32 + lines.size() * 9;
      boolean flipUp = 113 - by - 26 <= 6 + lines.size() * 9;
      if (!lines.isEmpty()) {
         g.method_48586(WIDGETS, bx, flipUp ? by + 26 - descPanelH : by, boxWidth, descPanelH, 10, 200, 26, 0, 52);
      }

      int half = boxWidth / 2;
      g.method_25302(WIDGETS, bx, by, 0, boxV, half, 26);
      g.method_25302(WIDGETS, bx + half, by, 200 - (boxWidth - half), boxV, boxWidth - half, 26);
      drawFrame(g, wx + 3, wy, e.frame(), obtained);
      g.method_27535(this.field_22793, e.name(), bx + 32, by + 9, 16777215);
      if (!lines.isEmpty()) {
         int textY = flipUp ? by + 26 - descPanelH + 7 : by + 9 + 17;

         for (int i = 0; i < lines.size(); i++) {
            g.method_51430(this.field_22793, (class_5481)lines.get(i), bx + 5, textY + i * 9, 11184810, false);
         }
      }

      IconRenderer.renderFake(g, e.icon(), wx + 8, wy + 5);
   }

   private List<class_5481> tooltipLines(dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e) {
      List<class_2561> parts = new ArrayList();
      if (!e.description().getString().isEmpty()) {
         parts.add(e.description().method_27661().method_27692(class_124.field_1080));
      }

      if (isObtained(e)) {
         if (!ClientSkillState.isGate(e)) {
            parts.add(class_2561.method_43471("screen.apoli.skill_tree.purchased").method_27692(class_124.field_1060));
            if (ClientSkillState.canRefund(e.id())) {
               parts.add(refundLine(e).method_27692(class_124.field_1054));
            } else if (ClientSkillState.refundBlockedByRewards(e.id())) {
               parts.add(class_2561.method_43471("screen.apoli.skill_tree.refund_blocked").method_27692(class_124.field_1061));
            }
         }
      } else {
         addCostLines(parts, e);
         addRewardLines(parts, e);
         parts.add(statusLine(e));
      }

      List<class_5481> out = new ArrayList();

      for (class_2561 c : parts) {
         out.addAll(this.field_22793.method_1728(c, 200));
      }

      return out;
   }

   private static void addCostLines(List<class_2561> parts, dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e) {
      if (e.costPoints().isEmpty() && e.costItems().isEmpty()) {
         parts.add(class_2561.method_43469("screen.apoli.skill_tree.cost", 0).method_27692(class_124.field_1065));
      } else if (e.costHidden()) {
         parts.add(class_2561.method_43471("screen.apoli.skill_tree.cost_hidden").method_27692(class_124.field_1065));
      } else {
         List<class_2561> lines = new ArrayList();
         boolean hiddenEntry = false;

         for (PointEntry point : e.costPoints()) {
            if (point.hidden()) {
               hiddenEntry = true;
            } else {
               lines.add(
                  pointLine(e, point, "screen.apoli.skill_tree.cost_points", "screen.apoli.skill_tree.cost_tree_points").method_27692(class_124.field_1065)
               );
            }
         }

         for (CostItem item : e.costItems()) {
            if (item.hidden()) {
               hiddenEntry = true;
            } else {
               boolean enough = ClientSkillState.heldCount(item) >= item.count();
               lines.add(
                  class_2561.method_43469("screen.apoli.skill_tree.cost_item", item.count(), item.display().method_7964())
                     .method_27692(enough ? class_124.field_1065 : class_124.field_1061)
               );
            }
         }

         if (lines.isEmpty()) {
            parts.add(class_2561.method_43471("screen.apoli.skill_tree.cost_hidden").method_27692(class_124.field_1065));
         } else if (!hiddenEntry && e.costItems().isEmpty() && e.costPoints().size() == 1 && ClientSkillState.isOwnTree(e, (PointEntry)e.costPoints().get(0))) {
            parts.add(class_2561.method_43469("screen.apoli.skill_tree.cost", ((PointEntry)e.costPoints().get(0)).amount()).method_27692(class_124.field_1065));
         } else {
            parts.add(class_2561.method_43471("screen.apoli.skill_tree.cost_items").method_27692(class_124.field_1065));
            parts.addAll(lines);
            if (hiddenEntry) {
               parts.add(class_2561.method_43471("screen.apoli.skill_tree.hidden_entry").method_27692(class_124.field_1065));
            }
         }
      }
   }

   private static void addRewardLines(List<class_2561> parts, dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e) {
      boolean grantsOtherPowers = false;

      for (class_2960 power : e.powers()) {
         if (!power.equals(e.id())) {
            grantsOtherPowers = true;
            break;
         }
      }

      if (e.rewardHidden()) {
         if (grantsOtherPowers || !e.rewardPoints().isEmpty() || !e.rewardItems().isEmpty()) {
            parts.add(class_2561.method_43471("screen.apoli.skill_tree.rewards_hidden").method_27692(class_124.field_1075));
         }
      } else {
         List<class_2561> lines = new ArrayList();
         boolean hiddenEntry = false;

         for (class_2561 power : e.rewardPowers()) {
            lines.add(class_2561.method_43469("screen.apoli.skill_tree.reward_power", power).method_27692(class_124.field_1075));
         }

         for (RewardItem item : e.rewardItems()) {
            if (item.hidden()) {
               hiddenEntry = true;
            } else {
               lines.add(
                  class_2561.method_43469("screen.apoli.skill_tree.reward_item", item.count(), item.display().method_7964()).method_27692(class_124.field_1075)
               );
            }
         }

         for (PointEntry point : e.rewardPoints()) {
            if (point.hidden()) {
               hiddenEntry = true;
            } else {
               lines.add(
                  pointLine(e, point, "screen.apoli.skill_tree.reward_points", "screen.apoli.skill_tree.reward_tree_points").method_27692(class_124.field_1075)
               );
            }
         }

         if (lines.isEmpty()) {
            if (hiddenEntry) {
               parts.add(class_2561.method_43471("screen.apoli.skill_tree.rewards_hidden").method_27692(class_124.field_1075));
            }
         } else {
            parts.add(class_2561.method_43471("screen.apoli.skill_tree.rewards").method_27692(class_124.field_1075));
            parts.addAll(lines);
            if (hiddenEntry) {
               parts.add(class_2561.method_43471("screen.apoli.skill_tree.hidden_entry").method_27692(class_124.field_1075));
            }
         }
      }
   }

   private static class_5250 pointLine(dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e, PointEntry point, String ownKey, String treeKey) {
      return ClientSkillState.isOwnTree(e, point)
         ? class_2561.method_43469(ownKey, point.amount())
         : class_2561.method_43469(treeKey, point.amount(), ClientSkillState.treeName(point.tree()));
   }

   private static class_2561 statusLine(dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e) {
      class_2960 blocker = ClientSkillState.excludedBy(e.id());
      if (blocker != null) {
         dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry blockerDef = ClientSkillState.def(blocker);
         return class_2561.method_43469(
               "screen.apoli.skill_tree.excluded", blockerDef != null ? blockerDef.name() : class_2561.method_43470(blocker.toString())
            )
            .method_27692(class_124.field_1061);
      }

      if (!ClientSkillState.parentSatisfied(e)) {
         if (e.placement().isPresent() && e.parent().isPresent()) {
            dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry parentDef = ClientSkillState.def((class_2960)e.parent().get());
            class_2561 parentName = parentDef != null ? parentDef.name() : class_2561.method_43470(((class_2960)e.parent().get()).toString());
            return class_2561.method_43469("screen.apoli.skill_tree.locked_named", parentName).method_27692(class_124.field_1061);
         } else {
            return class_2561.method_43471("screen.apoli.skill_tree.locked").method_27692(class_124.field_1061);
         }
      } else if (ClientSkillState.isLocked(e.id())) {
         return class_2561.method_43471("screen.apoli.skill_tree.unavailable").method_27692(class_124.field_1061);
      } else if (!ClientSkillState.canAfford(e, true)) {
         return class_2561.method_43471("screen.apoli.skill_tree.not_enough").method_27692(class_124.field_1061);
      } else if (!ClientSkillState.hasCostItems(e, true)) {
         return class_2561.method_43471("screen.apoli.skill_tree.missing_items").method_27692(class_124.field_1061);
      } else {
         return ClientSkillState.canAfford(e, false) && ClientSkillState.hasCostItems(e, false)
            ? class_2561.method_43471("screen.apoli.skill_tree.click_buy").method_27692(class_124.field_1054)
            : class_2561.method_43471("screen.apoli.skill_tree.cost_unmet").method_27692(class_124.field_1061);
      }
   }

   private static class_5250 refundLine(dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e) {
      if (e.rewardPoints().isEmpty() && e.rewardItems().isEmpty()) {
         boolean hiddenItem = false;

         for (CostItem item : e.costItems()) {
            hiddenItem |= item.hidden();
         }

         boolean simplePoints = e.costPoints().isEmpty()
            || e.costPoints().size() == 1 && !((PointEntry)e.costPoints().get(0)).hidden() && ClientSkillState.isOwnTree(e, (PointEntry)e.costPoints().get(0));
         if (!e.costHidden() && !hiddenItem && simplePoints) {
            int points = e.costPoints().isEmpty() ? 0 : ((PointEntry)e.costPoints().get(0)).amount();
            return class_2561.method_43469(e.costItems().isEmpty() ? "screen.apoli.skill_tree.refund" : "screen.apoli.skill_tree.refund_items", points);
         } else {
            return class_2561.method_43471("screen.apoli.skill_tree.refund_paid");
         }
      } else {
         return class_2561.method_43471("screen.apoli.skill_tree.refund_rewards");
      }
   }

   public boolean method_25402(double mouseX, double mouseY, int button) {
      int x = (this.field_22789 - 252) / 2;
      int y = (this.field_22790 - 140) / 2;
      if (button == 0 && this.roots.size() > 1) {
         for (int i = 0; i < this.roots.size(); i++) {
            int tx = x + 4 + i * 29;
            int ty = y - 28 + 4;
            if (mouseX >= tx && mouseX <= tx + 26 && mouseY >= ty && mouseY <= ty + 26) {
               this.currentRoot = (class_2960)this.roots.get(i);
               this.layout();
               return true;
            }
         }
      }

      if (button == 0 && this.currentRoot != null) {
         int screenX = (int)(mouseX - x - 9.0);
         int screenY = (int)(mouseY - y - 18.0);
         if (screenX > 0 && screenX < 234 && screenY > 0 && screenY < 113) {
            double localX = screenX / this.zoom;
            double localY = screenY / this.zoom;
            int k = class_3532.method_15357(this.scrollX);
            int l = class_3532.method_15357(this.scrollY);

            for (class_2960 id : this.treeSkills) {
               dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C.Entry e = ClientSkillState.def(id);
               int[] pos = (int[])this.positions.get(id);
               if (e != null && pos != null) {
                  int wx = k + pos[0];
                  int wy = l + pos[1];
                  if (localX >= wx && localX <= wx + 26 && localY >= wy && localY <= wy + 26) {
                     if (method_25442() && ClientSkillState.canRefund(id)) {
                        if (ClientPlayNetworking.canSend(RefundSkillC2S.CHANNEL)) {
                           class_2540 refundBuf = new class_2540(Unpooled.buffer());
                           new RefundSkillC2S(id).write(refundBuf);
                           ClientPlayNetworking.send(RefundSkillC2S.CHANNEL, refundBuf);
                        }
                     } else if (ClientSkillState.canBuy(e)) {
                        class_2540 buf = new class_2540(Unpooled.buffer());
                        new BuySkillC2S(id).write(buf);
                        ClientPlayNetworking.send(BuySkillC2S.CHANNEL, buf);
                     }

                     return true;
                  }
               }
            }
         }
      }

      return super.method_25402(mouseX, mouseY, button);
   }

   public boolean method_25403(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (button != 0) {
         this.isScrolling = false;
         return false;
      }

      if (!this.isScrolling) {
         this.isScrolling = true;
      } else {
         this.scroll(dragX, dragY);
      }

      return true;
   }

   public boolean method_25401(double mouseX, double mouseY, double amount) {
      this.setZoom(this.zoom * (float)Math.pow(1.2, amount));
      return true;
   }

   private void setZoom(float newZoom) {
      newZoom = class_3532.method_15363(newZoom, 0.25F, 1.0F);
      if (newZoom != this.zoom) {
         double centerLogicalX = 234.0 / (2.0 * this.zoom) - this.scrollX;
         double centerLogicalY = 113.0 / (2.0 * this.zoom) - this.scrollY;
         this.zoom = newZoom;
         this.scrollX = 234.0 / (2.0 * this.zoom) - centerLogicalX;
         this.scrollY = 113.0 / (2.0 * this.zoom) - centerLogicalY;
         this.clampScroll();
      }
   }

   private void scroll(double dx, double dy) {
      this.scrollX = this.scrollX + dx / this.zoom;
      this.scrollY = this.scrollY + dy / this.zoom;
      this.clampScroll();
   }

   private void clampScroll() {
      double visW = 234.0F / this.zoom;
      double visH = 113.0F / this.zoom;
      if (this.maxX - this.minX > visW) {
         this.scrollX = class_3532.method_15350(this.scrollX, visW - this.maxX, -this.minX);
      } else {
         this.scrollX = visW / 2.0 - (this.maxX + this.minX) / 2.0;
      }

      if (this.maxY - this.minY > visH) {
         this.scrollY = class_3532.method_15350(this.scrollY, visH - this.maxY, -this.minY);
      } else {
         this.scrollY = visH / 2.0 - (this.maxY + this.minY) / 2.0;
      }
   }

   @Override
   public boolean method_25421() {
      return false;
   }
}
