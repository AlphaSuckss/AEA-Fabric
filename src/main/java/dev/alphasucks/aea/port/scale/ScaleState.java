package dev.alphasucks.aea.port.scale;

import java.util.Arrays;
import net.minecraft.class_1297;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class ScaleState {
   public static final float DEFAULT = 1.0F;
   public static final float MIN = 1.0E-4F;
   public static final float MAX = 10000.0F;
   private final class_1297 owner;
   private float[] value;
   private float[] prev;
   private float[] from;
   private float[] to;
   private short[] ticks;
   private short[] total;
   private byte[] easing;
   private int animating;
   private boolean prevStale;
   private float[] cache;
   private float[] own;
   private long cacheTick = Long.MIN_VALUE;
   private int cacheGeneration = Integer.MIN_VALUE;
   private float cachePartial = Float.NaN;
   private boolean cacheVolatile = true;
   private float appliedWidth = Float.NaN;
   private float appliedHeight = Float.NaN;
   private float appliedEye = Float.NaN;
   private float[] pushed;

   public ScaleState(class_1297 owner) {
      this.owner = owner;
   }

   public class_1297 owner() {
      return this.owner;
   }

   public boolean isDefault() {
      if (this.value == null) {
         return true;
      }

      for (int i = 0; i < this.value.length; i++) {
         if (this.value[i] != 1.0F) {
            return false;
         }
      }

      return this.animating == 0;
   }

   public boolean isAnimating() {
      return this.animating > 0;
   }

   public boolean needsTick() {
      return this.animating > 0 || this.prevStale;
   }

   private void allocate() {
      if (this.value == null) {
         int n = ScaleTypes.count();
         this.value = new float[n];
         this.prev = new float[n];
         this.from = new float[n];
         this.to = new float[n];
         this.ticks = new short[n];
         this.total = new short[n];
         this.easing = new byte[n];
         Arrays.fill(this.value, 1.0F);
         Arrays.fill(this.prev, 1.0F);
         Arrays.fill(this.from, 1.0F);
         Arrays.fill(this.to, 1.0F);
      }
   }

   public float base(ScaleType type) {
      return this.value == null ? 1.0F : this.value[type.index()];
   }

   public float base(ScaleType type, float partial) {
      return this.baseAt(type.index(), partial);
   }

   public float baseAt(int i, float partial) {
      if (this.value == null) {
         return 1.0F;
      } else {
         return this.total[i] != 0 && !(partial >= 1.0F) ? this.prev[i] + (this.value[i] - this.prev[i]) * partial : this.value[i];
      }
   }

   public float target(ScaleType type) {
      if (this.value == null) {
         return 1.0F;
      }

      int i = type.index();
      return this.total[i] == 0 ? this.value[i] : this.to[i];
   }

   public void set(ScaleType type, float target, int overTicks, ScaleEasing ease) {
      this.allocate();
      int i = type.index();
      float clamped = clamp(target);
      boolean wasAnimating = this.total[i] != 0;
      if (overTicks <= 0) {
         if (wasAnimating) {
            this.animating--;
         }

         this.total[i] = 0;
         this.ticks[i] = 0;
         this.from[i] = clamped;
         this.to[i] = clamped;
         this.prev[i] = clamped;
         this.value[i] = clamped;
      } else {
         if (!wasAnimating) {
            this.animating++;
         }

         this.from[i] = this.value[i];
         this.to[i] = clamped;
         this.ticks[i] = 0;
         this.total[i] = (short)Math.min(overTicks, 32767);
         this.easing[i] = (byte)ease.ordinal();
      }

      this.invalidate();
   }

   public void reset() {
      if (this.value != null) {
         Arrays.fill(this.value, 1.0F);
         Arrays.fill(this.prev, 1.0F);
         Arrays.fill(this.from, 1.0F);
         Arrays.fill(this.to, 1.0F);
         Arrays.fill(this.ticks, (short)0);
         Arrays.fill(this.total, (short)0);
         Arrays.fill(this.easing, (byte)0);
         this.animating = 0;
         this.prevStale = false;
         this.invalidate();
      }
   }

   public void tick() {
      if (this.animating == 0) {
         if (this.prevStale) {
            if (this.value != null) {
               System.arraycopy(this.value, 0, this.prev, 0, this.value.length);
            }

            this.prevStale = false;
            this.invalidate();
         }
      } else {
         for (int i = 0; i < this.value.length; i++) {
            this.prev[i] = this.value[i];
            short span = this.total[i];
            if (span != 0) {
               short elapsed = (short)(this.ticks[i] + 1);
               if (elapsed >= span) {
                  this.value[i] = this.to[i];
                  this.ticks[i] = 0;
                  this.total[i] = 0;
                  this.animating--;
               } else {
                  this.ticks[i] = elapsed;
                  float t = ScaleEasing.byOrdinal(this.easing[i]).apply((float)elapsed / span);
                  this.value[i] = this.from[i] + (this.to[i] - this.from[i]) * t;
               }
            }
         }

         this.prevStale = true;
         this.invalidate();
      }
   }

   public void invalidate() {
      this.cacheGeneration = Integer.MIN_VALUE;
      this.cachePartial = Float.NaN;
   }

   float[] cached(long gameTime, int generation, float partial) {
      if (this.cache == null || this.cacheGeneration != generation || this.cachePartial != partial) {
         return null;
      } else {
         return this.cacheVolatile && this.cacheTick != gameTime ? null : this.cache;
      }
   }

   float[] cacheSlot() {
      if (this.cache == null) {
         this.cache = new float[ScaleTypes.count()];
      }

      return this.cache;
   }

   float[] ownSlot() {
      if (this.own == null) {
         this.own = new float[ScaleTypes.count()];
      }

      return this.own;
   }

   public float ownAt(int index) {
      return this.own == null ? 1.0F : this.own[index];
   }

   void storeCache(long gameTime, int generation, float partial, boolean perTick) {
      this.cacheTick = gameTime;
      this.cacheGeneration = generation;
      this.cachePartial = partial;
      this.cacheVolatile = perTick;
   }

   public boolean dimensionsChanged(float width, float height, float eye) {
      if (this.appliedWidth == width && this.appliedHeight == height && this.appliedEye == eye) {
         return false;
      }

      this.appliedWidth = width;
      this.appliedHeight = height;
      this.appliedEye = eye;
      return true;
   }

   public boolean pushedScalesChanged(float[] current) {
      float[] last = this.pushed;
      if (last == null || last.length != current.length) {
         last = new float[current.length];
         Arrays.fill(last, Float.NaN);
         this.pushed = last;
      }

      boolean changed = false;

      for (int i = 0; i < current.length; i++) {
         if (last[i] != current[i]) {
            last[i] = current[i];
            changed = true;
         }
      }

      return changed;
   }

   public void forgetDimensions() {
      this.appliedWidth = Float.NaN;
      this.appliedHeight = Float.NaN;
      this.appliedEye = Float.NaN;
      this.pushed = null;
   }

   public static float clamp(float raw) {
      if (Float.isNaN(raw)) {
         return 1.0F;
      } else if (raw < 1.0E-4F) {
         return 1.0E-4F;
      } else {
         return raw > 10000.0F ? 10000.0F : raw;
      }
   }

   public void save(class_2487 tag) {
      if (this.value != null) {
         class_2499 list = new class_2499();

         for (int i = 0; i < this.value.length; i++) {
            boolean animated = this.total[i] != 0;
            if (this.value[i] != 1.0F || animated) {
               class_2487 entry = new class_2487();
               entry.method_10582("id", ScaleTypes.byIndex(i).id().toString());
               entry.method_10548("value", this.value[i]);
               if (animated) {
                  entry.method_10548("from", this.from[i]);
                  entry.method_10548("to", this.to[i]);
                  entry.method_10575("ticks", this.ticks[i]);
                  entry.method_10575("total", this.total[i]);
                  entry.method_10567("easing", this.easing[i]);
               }

               list.add(entry);
            }
         }

         if (!list.isEmpty()) {
            tag.method_10566("scales", list);
         }
      }
   }

   public void load(class_2487 tag) {
      if (tag.method_10573("scales", 9)) {
         class_2499 list = tag.method_10554("scales", 10);
         if (!list.isEmpty()) {
            this.allocate();
            this.animating = 0;

            for (int i = 0; i < list.size(); i++) {
               class_2487 entry = list.method_10602(i);
               ScaleType type = ScaleTypes.get(class_2960.method_12829(entry.method_10558("id")));
               if (type != null) {
                  int index = type.index();
                  this.value[index] = clamp(entry.method_10583("value"));
                  this.prev[index] = this.value[index];
                  if (entry.method_10545("total")) {
                     this.from[index] = clamp(entry.method_10583("from"));
                     this.to[index] = clamp(entry.method_10583("to"));
                     this.ticks[index] = entry.method_10568("ticks");
                     this.total[index] = entry.method_10568("total");
                     this.easing[index] = entry.method_10571("easing");
                     if (this.total[index] != 0) {
                        this.animating++;
                     }
                  }
               }
            }

            this.prevStale = this.animating > 0;
            this.forgetDimensions();
            this.invalidate();
         }
      }
   }

   public void write(class_2540 buf) {
      int count = 0;
      if (this.value != null) {
         for (int i = 0; i < this.value.length; i++) {
            if (this.value[i] != 1.0F || this.total[i] != 0) {
               count++;
            }
         }
      }

      buf.method_10804(count);
      if (count != 0) {
         for (int i = 0; i < this.value.length; i++) {
            if (this.value[i] != 1.0F || this.total[i] != 0) {
               buf.method_10804(i);
               buf.writeFloat(this.value[i]);
               buf.writeFloat(this.prev[i]);
               buf.writeFloat(this.from[i]);
               buf.writeFloat(this.to[i]);
               buf.writeShort(this.ticks[i]);
               buf.writeShort(this.total[i]);
               buf.writeByte(this.easing[i]);
            }
         }
      }
   }

   public void read(class_2540 buf) {
      int count = buf.method_10816();
      if (count == 0) {
         this.reset();
         this.forgetDimensions();
      } else {
         this.allocate();
         Arrays.fill(this.value, 1.0F);
         Arrays.fill(this.prev, 1.0F);
         Arrays.fill(this.total, (short)0);
         Arrays.fill(this.ticks, (short)0);
         this.animating = 0;
         int types = ScaleTypes.count();

         for (int n = 0; n < count; n++) {
            int index = buf.method_10816();
            float v = buf.readFloat();
            float p = buf.readFloat();
            float f = buf.readFloat();
            float t = buf.readFloat();
            short tick = buf.readShort();
            short span = buf.readShort();
            byte ease = buf.readByte();
            if (index >= 0 && index < types) {
               this.value[index] = clamp(v);
               this.prev[index] = clamp(p);
               this.from[index] = clamp(f);
               this.to[index] = clamp(t);
               this.ticks[index] = tick;
               this.total[index] = span;
               this.easing[index] = ease;
               if (span != 0) {
                  this.animating++;
               }
            }
         }

         this.prevStale = this.animating > 0;
         this.forgetDimensions();
         this.invalidate();
      }
   }

   public void copyFrom(@Nullable ScaleState other) {
      if (other != null && other.value != null) {
         this.allocate();
         System.arraycopy(other.value, 0, this.value, 0, this.value.length);
         System.arraycopy(other.value, 0, this.prev, 0, this.value.length);
         Arrays.fill(this.total, (short)0);
         Arrays.fill(this.ticks, (short)0);
         this.animating = 0;
         this.prevStale = false;
         this.forgetDimensions();
         this.invalidate();
      }
   }
}
