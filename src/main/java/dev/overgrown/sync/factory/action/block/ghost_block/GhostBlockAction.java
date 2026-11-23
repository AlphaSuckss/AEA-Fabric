package dev.overgrown.sync.factory.action.block.ghost_block;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.data.SerializableData.Instance;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.class_1937;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2487;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_5321;
import net.minecraft.class_7923;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.tuple.Triple;

public class GhostBlockAction {
   private static final Map<UUID, GhostBlockAction.GhostBlockData> ACTIVE_GHOST_BLOCKS = new HashMap();
   private static final Map<GhostBlockAction.BlockPosKey, UUID> POSITION_TO_GHOST_ID = new HashMap();

   public static void action(Instance data, Triple<class_1937, class_2338, class_2350> triple) {
      class_1937 world = (class_1937)triple.getLeft();
      class_2338 originalPos = (class_2338)triple.getMiddle();
      class_2350 direction = (class_2350)triple.getRight();
      if (!world.method_8608()) {
         if (world instanceof class_3218 serverWorld) {
            class_2960 blockId = data.get("block");
            class_2248 block = class_7923.field_41175.method_10223(blockId);
            class_2487 nbt = data.get("nbt");
            int ticks = data.getInt("tick");
            boolean addBlock = data.getBoolean("add_block");
            class_2338 targetPos = originalPos;
            if (addBlock) {
               targetPos = originalPos.method_10093(direction);
            }

            GhostBlockAction.BlockPosKey posKey = new GhostBlockAction.BlockPosKey(serverWorld.method_27983(), targetPos);
            UUID existingGhostId = (UUID)POSITION_TO_GHOST_ID.get(posKey);
            if (existingGhostId != null) {
               GhostBlockAction.GhostBlockData existingData = (GhostBlockAction.GhostBlockData)ACTIVE_GHOST_BLOCKS.remove(existingGhostId);
               if (existingData != null) {
                  POSITION_TO_GHOST_ID.remove(new GhostBlockAction.BlockPosKey(existingData.worldKey(), existingData.pos()));
                  class_2680 originalState = existingData.originalState();
                  UUID newGhostId = UUID.randomUUID();
                  GhostBlockAction.GhostBlockData newGhostData = new GhostBlockAction.GhostBlockData(
                     serverWorld.method_27983(), targetPos, originalState, newGhostId, direction, data.get("end_action")
                  );
                  world.method_8652(targetPos, block.method_9564(), 3);
                  if (nbt != null) {
                     class_2586 blockEntity = world.method_8321(targetPos);
                     if (blockEntity != null) {
                        blockEntity.method_11014(nbt);
                        blockEntity.method_5431();
                     }
                  }

                  if (data.isPresent("block_action")) {
                     ActionFactory<Triple<class_1937, class_2338, class_2350>>.Instance blockAction = data.get("block_action");
                     blockAction.accept(Triple.of(world, targetPos, direction));
                  }

                  ACTIVE_GHOST_BLOCKS.put(newGhostId, newGhostData);
                  POSITION_TO_GHOST_ID.put(posKey, newGhostId);
                  Apoli.SCHEDULER.queue(server -> removeGhostBlock(newGhostId, server), ticks);
                  world.method_8413(targetPos, block.method_9564(), block.method_9564(), 3);
                  return;
               }
            }

            class_2680 originalState = world.method_8320(targetPos);
            world.method_8652(targetPos, block.method_9564(), 3);
            if (nbt != null) {
               class_2586 blockEntity = world.method_8321(targetPos);
               if (blockEntity != null) {
                  blockEntity.method_11014(nbt);
                  blockEntity.method_5431();
               }
            }

            if (data.isPresent("block_action")) {
               ActionFactory<Triple<class_1937, class_2338, class_2350>>.Instance blockAction = data.get("block_action");
               blockAction.accept(Triple.of(world, targetPos, direction));
            }

            UUID ghostId = UUID.randomUUID();
            GhostBlockAction.GhostBlockData ghostData = new GhostBlockAction.GhostBlockData(
               serverWorld.method_27983(), targetPos, originalState, ghostId, direction, data.get("end_action")
            );
            ACTIVE_GHOST_BLOCKS.put(ghostId, ghostData);
            POSITION_TO_GHOST_ID.put(posKey, ghostId);
            Apoli.SCHEDULER.queue(server -> removeGhostBlock(ghostId, server), ticks);
            world.method_8413(targetPos, block.method_9564(), block.method_9564(), 3);
         }
      }
   }

   private static void removeGhostBlock(UUID ghostId, MinecraftServer server) {
      GhostBlockAction.GhostBlockData data = (GhostBlockAction.GhostBlockData)ACTIVE_GHOST_BLOCKS.remove(ghostId);
      if (data != null) {
         POSITION_TO_GHOST_ID.remove(new GhostBlockAction.BlockPosKey(data.worldKey(), data.pos()));
         class_3218 world = server.method_3847(data.worldKey());
         if (world != null) {
            class_2338 pos = data.pos();
            boolean chunkLoaded = world.method_14178().method_12123(pos.method_10263() >> 4, pos.method_10260() >> 4);
            if (!chunkLoaded) {
               // load it: skipping the restore here would leave the ghost block in place forever
               world.method_8497(pos.method_10263() >> 4, pos.method_10260() >> 4);
               chunkLoaded = world.method_14178().method_12123(pos.method_10263() >> 4, pos.method_10260() >> 4);
            }

            if (chunkLoaded) {
               world.method_8652(pos, data.originalState(), 3);
               world.method_20290(2001, pos, class_2248.method_9507(data.originalState()));
               if (data.endAction() != null) {
                  data.endAction().accept(Triple.of(world, pos, data.direction()));
               }

               world.method_8413(pos, data.originalState(), data.originalState(), 3);
            }
         }
      }
   }

   public static void registerEvents() {
      // restore every pending ghost block before the worlds are saved; their scheduled restores won't run anymore
      ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
         for (UUID ghostId : new ArrayList<>(ACTIVE_GHOST_BLOCKS.keySet())) {
            removeGhostBlock(ghostId, server);
         }

         ACTIVE_GHOST_BLOCKS.clear();
         POSITION_TO_GHOST_ID.clear();
      });
   }

   public static ActionFactory<Triple<class_1937, class_2338, class_2350>> getFactory() {
      return new ActionFactory<>(
         Sync.identifier("ghost_block"),
         new SerializableData()
            .add("block", SerializableDataTypes.IDENTIFIER)
            .add("nbt", SerializableDataTypes.NBT, null)
            .add("tick", SerializableDataTypes.INT, 20)
            .add("block_action", ApoliDataTypes.BLOCK_ACTION, null)
            .add("add_block", SerializableDataTypes.BOOLEAN, false)
            .add("end_action", ApoliDataTypes.BLOCK_ACTION, null),
         GhostBlockAction::action
      );
   }

   private record BlockPosKey(class_5321<class_1937> worldKey, class_2338 pos) {
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else {
            return !(obj instanceof GhostBlockAction.BlockPosKey other) ? false : this.worldKey.equals(other.worldKey) && this.pos.equals(other.pos);
         }
      }

      public int hashCode() {
         return 31 * this.worldKey.hashCode() + this.pos.hashCode();
      }
   }

   private record GhostBlockData(
      class_5321<class_1937> worldKey,
      class_2338 pos,
      class_2680 originalState,
      UUID id,
      class_2350 direction,
      ActionFactory<Triple<class_1937, class_2338, class_2350>>.Instance endAction
   ) {
   }
}
