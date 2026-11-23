package dev.alphasucks.aea.port.action.builtin.bientity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.data.MacroArguments;
import dev.alphasucks.aea.port.dev.DevMode;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2168;
import net.minecraft.server.MinecraftServer;

public final class ExecuteCommandBiEntityAction implements ActionType<BiEntityCtx, ExecuteCommandBiEntityAction.Cfg> {
   @Override
   public MapCodec<ExecuteCommandBiEntityAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.STRING.fieldOf("command").forGetter(ExecuteCommandBiEntityAction.Cfg::command),
               Codec.STRING.optionalFieldOf("actor_selector", "%a").forGetter(ExecuteCommandBiEntityAction.Cfg::actorSelector),
               Codec.STRING.optionalFieldOf("target_selector", "%t").forGetter(ExecuteCommandBiEntityAction.Cfg::targetSelector),
               MacroArguments.FIELD.forGetter(ExecuteCommandBiEntityAction.Cfg::arguments)
            )
            .apply(i, ExecuteCommandBiEntityAction.Cfg::new)
      );
   }

   public void run(ExecuteCommandBiEntityAction.Cfg cfg, BiEntityCtx ctx) {
      class_1297 actor = ctx.actor();
      class_1297 target = ctx.target();
      if (actor != null && target != null) {
         MinecraftServer server = ctx.level().method_8503();
         if (server != null) {
            String command = cfg.command();
            if (!cfg.actorSelector().isEmpty()) {
               command = command.replace(cfg.actorSelector(), actor.method_5845());
            }

            if (!cfg.targetSelector().isEmpty()) {
               command = command.replace(cfg.targetSelector(), target.method_5845());
            }

            if (cfg.arguments().isPresent()) {
               command = MacroArguments.expand(command, ((MacroArguments)cfg.arguments().get()).resolve(server, actor, target), actor);
               if (command == null) {
                  return;
               }
            }

            DevMode.echoCommand(actor, command);
            class_2168 source = actor.method_5671().method_9206(4).method_9217();
            server.method_3734().method_44252(source, command);
         }
      }
   }

   public record Cfg(String command, String actorSelector, String targetSelector, Optional<MacroArguments> arguments) {
   }
}
