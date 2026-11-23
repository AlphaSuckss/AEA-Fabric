package dev.alphasucks.aea.port.data;

import dev.alphasucks.aea.port.condition.builtin.entity.EntityNbtSnapshot;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2520;
import net.minecraft.class_2586;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import org.jetbrains.annotations.Nullable;

public final class NbtSources {
   private static final ThreadLocal<NbtSources.TickCache> CACHE = ThreadLocal.withInitial(NbtSources.TickCache::new);

   private NbtSources() {
   }

   @Nullable
   public static class_2520 ofEntity(@Nullable class_1297 entity) {
      return entity == null ? null : EntityNbtSnapshot.cached(entity);
   }

   @Nullable
   public static class_2520 ofStack(class_1799 stack, @Nullable class_1937 level) {
      if (!stack.method_7960() && level != null) {
         NbtSources.TickCache cache = ((NbtSources.TickCache)CACHE.get()).at(level.method_8510());
         class_2520 tag = (class_2520)cache.stacks.get(stack);
         if (tag == null) {
            tag = stack.method_7953(new class_2487());
            cache.stacks.put(stack, tag);
         }

         return tag;
      } else {
         return null;
      }
   }

   @Nullable
   public static class_2520 ofBlock(@Nullable class_1937 level, class_2338 pos) {
      if (level != null && level.method_8477(pos)) {
         class_2586 blockEntity = level.method_8321(pos);
         if (blockEntity == null) {
            return null;
         }

         NbtSources.TickCache cache = ((NbtSources.TickCache)CACHE.get()).at(level.method_8510());
         class_2520 tag = (class_2520)cache.blocks.get(blockEntity);
         if (tag == null) {
            tag = blockEntity.method_38244();
            cache.blocks.put(blockEntity, tag);
         }

         return tag;
      } else {
         return null;
      }
   }

   @Nullable
   public static class_2487 ofStorage(@Nullable class_1937 level, class_2960 id) {
      return level instanceof class_3218 serverLevel ? serverLevel.method_8503().method_22827().method_22546(id) : null;
   }

   private static final class TickCache {
      long tick = Long.MIN_VALUE;
      final Map<class_1799, class_2520> stacks = new IdentityHashMap();
      final Map<class_2586, class_2520> blocks = new IdentityHashMap();

      NbtSources.TickCache at(long now) {
         if (this.tick != now) {
            this.stacks.clear();
            this.blocks.clear();
            this.tick = now;
         }

         return this;
      }
   }
}
