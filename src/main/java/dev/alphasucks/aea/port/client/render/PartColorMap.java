package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.power.builtin.ModelColorPower.PartColor;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_572;
import net.minecraft.class_630;
import org.jetbrains.annotations.Nullable;

public final class PartColorMap {
   private final Map<class_630, float[]> colors = new IdentityHashMap();
   private final List<float[]> pool = new ArrayList();
   private final List<class_630> parts = new ArrayList(8);
   private final float[] tint = new float[5];
   private int used;

   PartColorMap() {
   }

   @Nullable
   Map<class_630, float[]> map() {
      return this.colors.isEmpty() ? null : this.colors;
   }

   void reset() {
      if (!this.colors.isEmpty()) {
         this.colors.clear();
      }

      this.used = 0;
   }

   void fill(class_1297 entity, class_572<?> model, List<PartColor> partColors, @Nullable class_630 onlyA, @Nullable class_630 onlyB) {
      for (int i = 0; i < partColors.size(); i++) {
         PartColor partColor = (PartColor)partColors.get(i);
         this.parts.clear();
         ModelPartLookup.resolveInto(model, partColor.part().sided(entity), this.parts);
         if (!this.parts.isEmpty()) {
            partColor.tint().resolve(entity, this.tint);

            for (int p = 0; p < this.parts.size(); p++) {
               class_630 part = (class_630)this.parts.get(p);
               if (onlyA == null || part == onlyA || part == onlyB) {
                  this.multiply(part, this.tint);
               }
            }
         }
      }

      this.parts.clear();
   }

   void multiply(class_630 part, float[] whole) {
      float[] color = this.entry(part);
      color[0] *= whole[0];
      color[1] *= whole[1];
      color[2] *= whole[2];
      color[3] *= whole[3];
      color[4] = Math.max(color[4], whole[4]);
   }

   void attach(class_630 part, float[] colours, int at) {
      System.arraycopy(colours, at, this.entry(part), 0, 5);
   }

   private float[] entry(class_630 part) {
      float[] color = (float[])this.colors.get(part);
      if (color == null) {
         color = this.next();
         this.colors.put(part, color);
      }

      return color;
   }

   private float[] next() {
      float[] color;
      if (this.used < this.pool.size()) {
         color = (float[])this.pool.get(this.used);
      } else {
         color = new float[5];
         this.pool.add(color);
      }

      this.used++;
      color[0] = 1.0F;
      color[1] = 1.0F;
      color[2] = 1.0F;
      color[3] = 1.0F;
      color[4] = 0.0F;
      return color;
   }
}
