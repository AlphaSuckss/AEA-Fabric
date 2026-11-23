package dev.alphasucks.aea.port.condition.builtin.block;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.alias.AliasingOptions;
import dev.alphasucks.aea.port.condition.ConditionTypes;

/** AEA: filtered copy, registers only types Apoli 2.9.2 lacks. */
public final class BlockConditions {
   private BlockConditions() {
   }

   public static void register() {
      ConditionTypes.BLOCK.register(Apoli.id("tick_rate"), new TickRateBlockCondition());
      ConditionTypes.BLOCK.register(Apoli.id("command"), new CommandBlockCondition());
      ConditionTypes.BLOCK.register(Apoli.id("distance_from_coordinates"), new DistanceFromCoordinatesBlockCondition());
      ConditionTypes.BLOCK
         .register(Apoli.id("replacable"), new ReplacableBlockCondition(), AliasingOptions.builder().addTypeAlias(Apoli.id("replaceable")).build());
      ConditionTypes.BLOCK
         .register(Apoli.id("slipperiness"), new SlipperinessBlockCondition(), AliasingOptions.builder().addTypeAlias(Apoli.id("slipperness")).build());
      ConditionTypes.BLOCK.register(Apoli.id("script"), new ScriptBlockCondition());
   }
}
