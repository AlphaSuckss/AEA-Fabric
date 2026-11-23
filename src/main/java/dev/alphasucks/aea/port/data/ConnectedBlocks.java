package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2338.class_2339;
import org.jetbrains.annotations.Nullable;

public record ConnectedBlocks(Optional<BlockCondition> blockCondition, boolean diagonal, boolean downward, int limit, Optional<BlockCondition> neighborCondition) {
   public static final MapCodec<ConnectedBlocks> MAP_CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            LoggedOptionalField.strict("block_condition", BlockCondition.CODEC).forGetter(ConnectedBlocks::blockCondition),
            Codec.BOOL.optionalFieldOf("diagonal", true).forGetter(ConnectedBlocks::diagonal),
            Codec.BOOL.optionalFieldOf("downward", true).forGetter(ConnectedBlocks::downward),
            Codec.intRange(1, 1024).optionalFieldOf("limit", 64).forGetter(ConnectedBlocks::limit),
            LoggedOptionalField.strict("neighbor_condition", BlockCondition.CODEC).forGetter(ConnectedBlocks::neighborCondition)
         )
         .apply(i, ConnectedBlocks::new)
   );
   public static final Codec<ConnectedBlocks> CODEC = MAP_CODEC.codec();
   private static final int[][] ALL = offsets(true, true);
   private static final int[][] LEVEL_AND_UP = offsets(true, false);
   private static final int[][] FACES = offsets(false, true);
   private static final int[][] FACES_NOT_DOWN = offsets(false, false);
   private static final ThreadLocal<ConnectedBlocks.Scratch> SCRATCH = ThreadLocal.withInitial(ConnectedBlocks.Scratch::new);

   private static int[][] offsets(boolean diagonal, boolean downward) {
      List<int[]> out = new ArrayList(26);

      for (int dy = downward ? -1 : 0; dy <= 1; dy++) {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               int axes = (dx != 0 ? 1 : 0) + (dy != 0 ? 1 : 0) + (dz != 0 ? 1 : 0);
               if (axes != 0 && (diagonal || axes <= 1)) {
                  out.add(new int[]{dx, dy, dz});
               }
            }
         }
      }

      return (int[][])out.toArray(new int[0][]);
   }

   public List<class_2338> find(class_1937 level, class_2338 start, class_2680 startState, @Nullable class_1297 actor) {
      List<class_2338> found = new ArrayList();
      int size = this.walk(level, start, startState, actor, new LongOpenHashSet(), new LongArrayFIFOQueue(), found);
      return size == 0 ? List.of() : found;
   }

   public int count(class_1937 level, class_2338 start, class_2680 startState, @Nullable class_1297 actor) {
      ConnectedBlocks.Scratch scratch = (ConnectedBlocks.Scratch)SCRATCH.get();
      scratch.checked.clear();
      scratch.queue.clear();
      return this.walk(level, start, startState, actor, scratch.checked, scratch.queue, null);
   }

   private int walk(
      class_1937 level,
      class_2338 start,
      class_2680 startState,
      @Nullable class_1297 actor,
      LongOpenHashSet checked,
      LongArrayFIFOQueue queue,
      @Nullable List<class_2338> out
   ) {
      int[][] neighbors = this.diagonal ? (this.downward ? ALL : LEVEL_AND_UP) : (this.downward ? FACES : FACES_NOT_DOWN);
      class_2248 startBlock = startState.method_26204();
      boolean needNeighbor = this.neighborCondition.isPresent();
      class_2339 cursor = new class_2339();
      long origin = start.method_10063();
      checked.add(origin);
      queue.enqueue(origin);
      int size = 0;

      while (!queue.isEmpty()) {
         long current = queue.dequeueLong();
         int x = class_2338.method_10061(current);
         int y = class_2338.method_10071(current);
         int z = class_2338.method_10083(current);

         for (int[] offset : neighbors) {
            cursor.method_10103(x + offset[0], y + offset[1], z + offset[2]);
            long key = cursor.method_10063();
            if (checked.add(key) && level.method_8477(cursor)) {
               class_2680 state = level.method_8320(cursor);
               boolean member = this.blockCondition.isPresent()
                  ? ((BlockCondition)this.blockCondition.get()).test(new BlockCtx(cursor.method_10062(), state, level, actor))
                  : state.method_27852(startBlock);
               if (member) {
                  size++;
                  if (out != null) {
                     out.add(cursor.method_10062());
                  }

                  if (size >= this.limit) {
                     return needNeighbor ? 0 : size;
                  }

                  queue.enqueue(key);
               } else if (needNeighbor && ((BlockCondition)this.neighborCondition.get()).test(new BlockCtx(cursor.method_10062(), state, level, actor))) {
                  needNeighbor = false;
               }
            }
         }
      }

      return needNeighbor ? 0 : size;
   }

   private static final class Scratch {
      final LongOpenHashSet checked = new LongOpenHashSet();
      final LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
   }
}
