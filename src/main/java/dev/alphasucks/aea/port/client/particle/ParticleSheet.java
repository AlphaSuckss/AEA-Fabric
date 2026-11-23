package dev.alphasucks.aea.port.client.particle;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.particle.ParticleFrameLayout;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1011;
import net.minecraft.class_1079;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_3532;
import net.minecraft.class_7771;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class ParticleSheet {
   private static final int[] SINGLE_ORDER = new int[]{0};
   public static final ParticleSheet SINGLE = new ParticleSheet(1, 1, SINGLE_ORDER, 0, null, 0, false);
   private static final Map<ParticleSheet.Key, ParticleSheet> CACHE = new HashMap();
   private static final Map<class_2960, ParticleSheet.Source> SOURCES = new HashMap();
   private final int columns;
   private final int rows;
   private final int[] order;
   private final int uniformTime;
   private final int @Nullable [] starts;
   private final int totalTicks;
   private final boolean metadataDriven;

   private ParticleSheet(int columns, int rows, int[] order, int uniformTime, int @Nullable [] starts, int totalTicks, boolean metadataDriven) {
      this.columns = Math.max(1, columns);
      this.rows = Math.max(1, rows);
      this.order = order;
      this.uniformTime = uniformTime;
      this.starts = starts;
      this.totalTicks = totalTicks;
      this.metadataDriven = metadataDriven;
   }

   public static synchronized ParticleSheet of(class_2960 texture, ParticleFrameLayout layout, int frames, int frameTime) {
      if (frames == 1) {
         return SINGLE;
      }

      ParticleSheet.Key key = new ParticleSheet.Key(texture, layout, frames, frameTime);
      ParticleSheet cached = (ParticleSheet)CACHE.get(key);
      if (cached != null) {
         return cached;
      }

      ParticleSheet built = build(texture, layout, frames, frameTime);
      CACHE.put(key, built);
      return built;
   }

   public static synchronized void clearCache() {
      CACHE.clear();
      SOURCES.clear();
   }

   public boolean animated() {
      return this.order.length > 1;
   }

   public boolean loopsByDefault() {
      return this.metadataDriven;
   }

   public int stepCount() {
      return this.order.length;
   }

   public int cellAt(int age, int lifetime, boolean loop) {
      int steps = this.order.length;
      if (steps <= 1) {
         return this.order[0];
      }

      int index;
      if (this.starts != null) {
         int time = loop ? Math.floorMod(age, this.totalTicks) : Math.min(age, this.totalTicks - 1);
         index = this.stepOf(time);
      } else if (this.uniformTime > 0) {
         index = age / this.uniformTime;
      } else {
         index = (int)((long)age * steps / Math.max(1, lifetime));
      }

      return this.order[loop ? Math.floorMod(index, steps) : class_3532.method_15340(index, 0, steps - 1)];
   }

   private int stepOf(int time) {
      int low = 0;
      int high = this.starts.length - 1;

      while (low < high) {
         int mid = low + high + 1 >>> 1;
         if (this.starts[mid] <= time) {
            low = mid;
         } else {
            high = mid - 1;
         }
      }

      return low;
   }

   public float u0(int cell) {
      return (float)(cell % this.columns) / this.columns;
   }

   public float u1(int cell) {
      return (float)(cell % this.columns + 1) / this.columns;
   }

   public float v0(int cell) {
      return (float)(cell / this.columns) / this.rows;
   }

   public float v1(int cell) {
      return (float)(cell / this.columns + 1) / this.rows;
   }

   private static ParticleSheet build(class_2960 texture, ParticleFrameLayout layout, int frames, int frameTime) {
      ParticleSheet.Source source = sourceOf(texture);
      if (source == null) {
         return declaredOnly(layout, frames, frameTime);
      }

      class_1079 animation = source.animation();
      int columns;
      int rows;
      if (animation != null) {
         class_7771 size = animation.method_24143(source.width(), source.height());
         columns = Math.max(1, source.width() / Math.max(1, size.comp_1049()));
         rows = Math.max(1, source.height() / Math.max(1, size.comp_1050()));
      } else {
         int[] grid = declaredGrid(layout, frames, source.width(), source.height());
         columns = grid[0];
         rows = grid[1];
      }

      int cells = columns * rows;
      if (cells <= 1) {
         return SINGLE;
      }

      if (animation == null) {
         int steps = frames > 0 ? Math.min(frames, cells) : cells;
         return new ParticleSheet(columns, rows, sequence(steps), Math.max(0, frameTime), null, 0, false);
      }

      List<int[]> steps = new ArrayList();
      animation.method_33460((index, timex) -> {
         if (index >= 0 && index < cells && timex > 0) {
            steps.add(new int[]{index, timex});
         }
      });
      if (steps.isEmpty()) {
         int[] order = sequence(cells);
         int time = frameTime > 0 ? frameTime : Math.max(1, animation.method_4684());
         return new ParticleSheet(columns, rows, order, time, null, 0, true);
      }

      int[] order = new int[steps.size()];

      for (int i = 0; i < order.length; i++) {
         order[i] = ((int[])steps.get(i))[0];
      }

      if (frameTime > 0) {
         return new ParticleSheet(columns, rows, order, frameTime, null, 0, true);
      }

      int[] starts = new int[steps.size()];
      int total = 0;
      boolean uniform = true;
      int first = ((int[])steps.get(0))[1];

      for (int i = 0; i < steps.size(); i++) {
         starts[i] = total;
         int time = ((int[])steps.get(i))[1];
         if (time != first) {
            uniform = false;
         }

         total += time;
      }

      return uniform
         ? new ParticleSheet(columns, rows, order, first, null, 0, true)
         : new ParticleSheet(columns, rows, order, 0, starts, Math.max(1, total), true);
   }

   private static ParticleSheet declaredOnly(ParticleFrameLayout layout, int frames, int frameTime) {
      if (frames <= 1) {
         return SINGLE;
      }

      boolean row = layout == ParticleFrameLayout.HORIZONTAL;
      return new ParticleSheet(row ? frames : 1, row ? 1 : frames, sequence(frames), Math.max(0, frameTime), null, 0, false);
   }

   private static int[] declaredGrid(ParticleFrameLayout layout, int frames, int width, int height) {
      if (frames <= 1 && layout == ParticleFrameLayout.AUTO) {
         return new int[]{1, 1};
      }

      return switch (layout) {
         case VERTICAL -> new int[]{1, frames > 0 ? frames : stripCount(height, width)};
         case HORIZONTAL -> new int[]{frames > 0 ? frames : stripCount(width, height), 1};
         case GRID -> {
            int cell = Math.max(1, Math.min(width, height));
            yield new int[]{Math.max(1, width / cell), Math.max(1, height / cell)};
         }
         case AUTO -> height >= width ? new int[]{1, frames} : new int[]{frames, 1};
      };
   }

   private static int stripCount(int along, int across) {
      return across > 0 && along > across && along % across == 0 ? along / across : 1;
   }

   private static int[] sequence(int count) {
      int[] order = new int[Math.max(1, count)];
      int i = 0;

      while (i < order.length) {
         order[i] = i++;
      }

      return order;
   }

   @Nullable
   private static ParticleSheet.Source sourceOf(class_2960 texture) {
      ParticleSheet.Source cached = (ParticleSheet.Source)SOURCES.get(texture);
      if (cached != null) {
         return cached;
      }

      ParticleSheet.Source built = readSource(texture);
      if (built != null) {
         SOURCES.put(texture, built);
      }

      return built;
   }

   @Nullable
   private static ParticleSheet.Source readSource(class_2960 texture) {
      Optional<class_3298> found = class_310.method_1551().method_1478().method_14486(texture);
      if (found.isEmpty()) {
         return null;
      }

      class_3298 resource = (class_3298)found.get();
      class_1079 animation = null;

      try {
         animation = (class_1079)resource.method_14481().method_43041(class_1079.field_5337).orElse(null);
      } catch (Exception e) {
         Apoli.LOGGER.warn("[Apoli] Could not read the animation metadata of particle texture {}: {}", texture, e.toString());
      }

      int[] size = readSize(resource, texture);
      return size == null ? null : new ParticleSheet.Source(size[0], size[1], animation);
   }

   private static int @Nullable [] readSize(class_3298 resource, class_2960 texture) {
      try {
         InputStream stream = resource.method_14482();

         label120: {
            int[] var6;
            try {
               byte[] header = stream.readNBytes(24);
               if (header.length != 24
                  || (header[0] & 255) != 137
                  || header[1] != 80
                  || header[2] != 78
                  || header[3] != 71
                  || header[12] != 73
                  || header[13] != 72
                  || header[14] != 68
                  || header[15] != 82) {
                  break label120;
               }

               int width = readInt(header, 16);
               int height = readInt(header, 20);
               if (width <= 0 || height <= 0) {
                  break label120;
               }

               var6 = new int[]{width, height};
            } catch (Throwable var13) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var9) {
                     var13.addSuppressed(var9);
                  }
               }

               throw var13;
            }

            if (stream != null) {
               stream.close();
            }

            return var6;
         }

         if (stream != null) {
            stream.close();
         }
      } catch (Exception var14) {
      }

      try {
         InputStream stream = resource.method_14482();

         int[] var17;
         try (class_1011 image = class_1011.method_4309(stream)) {
            var17 = new int[]{Math.max(1, image.method_4307()), Math.max(1, image.method_4323())};
         } catch (Throwable var11) {
            if (stream != null) {
               try {
                  stream.close();
               } catch (Throwable var7) {
                  var11.addSuppressed(var7);
               }
            }

            throw var11;
         }

         if (stream != null) {
            stream.close();
         }

         return var17;
      } catch (Exception e) {
         Apoli.LOGGER.warn("[Apoli] Could not read the size of particle texture {}: {}", texture, e.toString());
         return null;
      }
   }

   private static int readInt(byte[] bytes, int offset) {
      return (bytes[offset] & 0xFF) << 24 | (bytes[offset + 1] & 0xFF) << 16 | (bytes[offset + 2] & 0xFF) << 8 | bytes[offset + 3] & 0xFF;
   }

   private record Key(class_2960 texture, ParticleFrameLayout layout, int frames, int frameTime) {
   }

   private record Source(int width, int height, @Nullable class_1079 animation) {
   }
}
