package dev.alphasucks.aea.port.action.builtin.block;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.action.ActionTypes;

/** AEA: filtered copy, registers only types Apoli 2.9.2 lacks. */
public final class BlockActions {
   private BlockActions() {
   }

   public static void register() {
      ActionTypes.BLOCK.register(Apoli.id("break_block"), new BreakBlockAction());
      ActionTypes.BLOCK.register(Apoli.id("store_data"), new StoreDataBlockAction());
      ActionTypes.BLOCK.register(Apoli.id("ghost_block"), new GhostBlockAction());
      ActionTypes.BLOCK.register(Apoli.id("spawn_entity"), new SpawnEntityBlockAction());
      ActionTypes.BLOCK.register(Apoli.id("spawn_particles"), new SpawnParticlesBlockAction());
      ActionTypes.BLOCK.register(Apoli.id("tick_rate"), new TickRateBlockAction());
      ActionTypes.BLOCK.register(Apoli.id("connected_blocks"), new ConnectedBlocksBlockMetaAction());
      ActionTypes.BLOCK.register(Apoli.id("script"), new ScriptBlockAction());
   }
}
