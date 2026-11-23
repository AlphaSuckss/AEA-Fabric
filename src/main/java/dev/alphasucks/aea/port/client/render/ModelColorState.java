package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.client.ClientPowerState;
import dev.alphasucks.aea.port.client.ClientPowerState.TypeHolders;
import dev.alphasucks.aea.port.client.disguise.ClientDisguiseManager;
import dev.alphasucks.aea.port.power.builtin.ModelColorPower;
import dev.alphasucks.aea.port.power.builtin.ModelColorPower.PartColor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_572;
import net.minecraft.class_583;
import net.minecraft.class_630;
import org.jetbrains.annotations.Nullable;

public final class ModelColorState {
   private static final int MAX_DEPTH = 8;
   private static final TypeHolders HOLDERS = ClientPowerState.holders(ModelColorPower.CANONICAL);
   private static final ThreadLocal<ModelColorState.Frames> FRAMES = ThreadLocal.withInitial(ModelColorState.Frames::new);
   private static final ThreadLocal<List<PartColor>> SCRATCH = ThreadLocal.withInitial(() -> new ArrayList(4));
   private static volatile boolean active;

   private ModelColorState() {
   }

   public static void begin(class_1297 rendered, class_583<?> model) {
      ModelColorState.Frame frame = ((ModelColorState.Frames)FRAMES.get()).push();
      class_1297 source = ClientDisguiseManager.powerSource(rendered);
      frame.rendered = rendered;
      if (frame.resolve(source) && !frame.partColors.isEmpty() && model instanceof class_572<?> humanoid) {
         frame.map.fill(source, humanoid, frame.partColors, null, null);
      }

      active = frame.map.map() != null;
   }

   public static void beginHand(class_1297 player, class_572<?> model, class_630 arm, class_630 sleeve) {
      ModelColorState.Frame frame = ((ModelColorState.Frames)FRAMES.get()).push();
      frame.rendered = player;
      if (frame.resolve(player)) {
         if (!frame.partColors.isEmpty()) {
            frame.map.fill(player, model, frame.partColors, arm, sleeve);
         }

         if (frame.tinted) {
            frame.map.multiply(arm, frame.whole);
            frame.map.multiply(sleeve, frame.whole);
         }
      }

      active = frame.map.map() != null;
   }

   public static void end() {
      ModelColorState.Frame top = ((ModelColorState.Frames)FRAMES.get()).pop();
      active = top != null && top.map.map() != null;
   }

   public static boolean isActive() {
      return active;
   }

   @Nullable
   public static float[] colorFor(class_630 part) {
      ModelColorState.Frame top = ((ModelColorState.Frames)FRAMES.get()).top();
      if (top == null) {
         return null;
      }

      Map<class_630, float[]> map = top.map.map();
      return map == null ? null : (float[])map.get(part);
   }

   public static float alphaFor(@Nullable class_630 part) {
      if (!active) {
         return 1.0F;
      }

      float[] color = colorFor(part);
      return color == null ? 1.0F : color[3];
   }

   @Nullable
   public static float[] tint() {
      ModelColorState.Frame top = ((ModelColorState.Frames)FRAMES.get()).top();
      return top != null && top.tinted ? top.whole : null;
   }

   public static boolean whitens() {
      ModelColorState.Frame top = ((ModelColorState.Frames)FRAMES.get()).top();
      return top != null && top.tinted && top.whole[4] >= 1.0F;
   }

   public static float minAlpha(class_1297 rendered) {
      ModelColorState.Frame top = ((ModelColorState.Frames)FRAMES.get()).top();
      if (top != null && top.rendered == rendered) {
         return top.minAlpha;
      }

      class_1297 source = ClientDisguiseManager.powerSource(rendered);
      return held(source) ? ModelColorPower.minAlpha(source) : 1.0F;
   }

   public static float[] whole(@Nullable class_1297 source) {
      ModelColorState.Frame top = ((ModelColorState.Frames)FRAMES.get()).top();
      if (top != null && top.source == source) {
         return top.tinted ? top.whole : ModelColorPower.IDENTITY;
      } else {
         return held(source) ? ModelColorPower.colorFor(source) : ModelColorPower.IDENTITY;
      }
   }

   public static List<PartColor> partColors(@Nullable class_1297 source) {
      ModelColorState.Frame top = ((ModelColorState.Frames)FRAMES.get()).top();
      if (top != null && top.source == source) {
         return top.partColors;
      }

      List<PartColor> scratch = (List<PartColor>)SCRATCH.get();
      scratch.clear();
      if (held(source)) {
         ModelColorPower.collectPartColors(source, scratch);
      }

      return scratch;
   }

   public static void attach(class_630 part, float[] colours, int at) {
      ModelColorState.Frame top = ((ModelColorState.Frames)FRAMES.get()).top();
      if (top != null) {
         top.map.attach(part, colours, at);
         active = true;
      }
   }

   private static boolean held(@Nullable class_1297 source) {
      return source != null && HOLDERS.contains(source.method_5628());
   }

   private static final class Frame {
      private final float[] whole = new float[5];
      private final List<PartColor> partColors = new ArrayList(4);
      private final PartColorMap map = new PartColorMap();
      @Nullable
      private class_1297 rendered;
      @Nullable
      private class_1297 source;
      private boolean tinted;
      private float minAlpha = 1.0F;

      private boolean resolve(@Nullable class_1297 source) {
         this.source = source;
         this.tinted = false;
         this.minAlpha = 1.0F;
         this.partColors.clear();
         this.map.reset();
         if (ModelColorState.held(source) && ModelColorPower.resolve(source, this.whole, this.partColors)) {
            this.tinted = this.whole[0] != 1.0F || this.whole[1] != 1.0F || this.whole[2] != 1.0F || this.whole[3] != 1.0F || this.whole[4] != 0.0F;
            float min = this.whole[3];

            for (int i = 0; i < this.partColors.size(); i++) {
               min = Math.min(min, ((PartColor)this.partColors.get(i)).tint().alpha(source));
            }

            this.minAlpha = min;
            return true;
         } else {
            return false;
         }
      }
   }

   private static final class Frames {
      private ModelColorState.Frame[] frames = new ModelColorState.Frame[2];
      private int depth;

      private ModelColorState.Frame push() {
         if (this.depth >= 8) {
            this.depth = 0;
         }

         if (this.depth == this.frames.length) {
            this.frames = (ModelColorState.Frame[])Arrays.copyOf(this.frames, this.depth * 2);
         }

         ModelColorState.Frame frame = this.frames[this.depth];
         if (frame == null) {
            frame = new ModelColorState.Frame();
            this.frames[this.depth] = frame;
         }

         this.depth++;
         return frame;
      }

      @Nullable
      private ModelColorState.Frame pop() {
         if (this.depth > 0) {
            this.depth--;
         }

         return this.top();
      }

      @Nullable
      private ModelColorState.Frame top() {
         return this.depth == 0 ? null : this.frames[this.depth - 1];
      }
   }
}
