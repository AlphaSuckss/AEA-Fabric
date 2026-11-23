package dev.alphasucks.aea.port.block;

import dev.alphasucks.aea.port.action.BlockAction;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1937;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_2806;
import net.minecraft.class_3218;
import net.minecraft.class_5321;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class GhostBlocks {
   public static final int PLACE_FLAGS = 50;
   private static final Map<GhostBlocks.Key, GhostBlocks.Ghost> ACTIVE = new HashMap();
   private static final Map<Long, List<GhostBlocks.Key>> EXPIRY = new HashMap();
   private static long currentTick;

   private GhostBlocks() {
   }

   public static boolean isGhost(class_1937 level, class_2338 pos) {
      return ACTIVE.isEmpty() ? false : ACTIVE.containsKey(new GhostBlocks.Key(level.method_27983(), pos.method_10063()));
   }

   public static void place(
      class_3218 level,
      class_2338 pos,
      class_2680 state,
      @Nullable class_2487 blockEntityNbt,
      int duration,
      Optional<BlockAction> blockAction,
      Optional<BlockAction> endAction
   ) {
      GhostBlocks.Key key = new GhostBlocks.Key(level.method_27983(), pos.method_10063());
      GhostBlocks.Ghost existing = (GhostBlocks.Ghost)ACTIVE.remove(key);
      class_2680 original;
      class_2487 originalBlockEntity;
      if (existing != null) {
         original = existing.original;
         originalBlockEntity = existing.originalBlockEntity;
      } else {
         original = level.method_8320(pos);
         class_2586 be = level.method_8321(pos);
         originalBlockEntity = be == null ? null : be.method_38242();
      }

      level.method_8652(pos, state, 50);
      if (blockEntityNbt != null) {
         class_2586 be = level.method_8321(pos);
         if (be != null) {
            class_2487 tag = blockEntityNbt.method_10553();
            tag.method_10569("x", pos.method_10263());
            tag.method_10569("y", pos.method_10264());
            tag.method_10569("z", pos.method_10260());
            be.method_11014(tag);
            be.method_5431();
         }
      }

      ACTIVE.put(key, new GhostBlocks.Ghost(level.method_27983(), pos.method_10062(), original, originalBlockEntity, endAction));
      ((List)EXPIRY.computeIfAbsent(currentTick + Math.max(1, duration), k -> new ArrayList(1))).add(key);
      if (blockAction.isPresent()) {
         ((BlockAction)blockAction.get()).run(new BlockCtx(pos.method_10062(), state, level));
      }
   }

   public static void tick(MinecraftServer server) {
      List<GhostBlocks.Key> due = (List<GhostBlocks.Key>)EXPIRY.remove(++currentTick);
      if (due != null) {
         for (int i = 0; i < due.size(); i++) {
            restore(server, (GhostBlocks.Key)due.get(i));
         }
      }
   }

   public static void restoreAll(MinecraftServer server) {
      if (!ACTIVE.isEmpty()) {
         for (GhostBlocks.Key key : List.copyOf(ACTIVE.keySet())) {
            restore(server, key);
         }

         EXPIRY.clear();
      }
   }

   public static void clear() {
      ACTIVE.clear();
      EXPIRY.clear();
      currentTick = 0L;
   }

   private static void restore(MinecraftServer server, GhostBlocks.Key key) {
      GhostBlocks.Ghost ghost = (GhostBlocks.Ghost)ACTIVE.remove(key);
      if (ghost != null) {
         class_3218 level = server.method_3847(ghost.dimension);
         if (level != null) {
            class_2338 pos = ghost.pos;
            if (level.method_8402(pos.method_10263() >> 4, pos.method_10260() >> 4, class_2806.field_12803, true) != null) {
               level.method_8652(pos, ghost.original, 50);
               if (ghost.originalBlockEntity != null) {
                  class_2586 be = level.method_8321(pos);
                  if (be != null) {
                     be.method_11014(ghost.originalBlockEntity.method_10553());
                     be.method_5431();
                  }
               }

               level.method_20290(2001, pos, class_2248.method_9507(ghost.original));
               if (ghost.endAction.isPresent()) {
                  ((BlockAction)ghost.endAction.get()).run(new BlockCtx(pos, ghost.original, level));
               }
            }
         }
      }
   }

   private record Ghost(
      class_5321<class_1937> dimension, class_2338 pos, class_2680 original, @Nullable class_2487 originalBlockEntity, Optional<BlockAction> endAction
   ) {
   }

   private record Key(class_5321<class_1937> dimension, long pos) {
   }
}
