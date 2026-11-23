package dev.alphasucks.aea.port.mixin.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.alphasucks.aea.port.command.ApoliResourceCommand;
import dev.alphasucks.aea.port.power.PowerContainer;
import java.util.Collection;
import java.util.function.BinaryOperator;
import net.minecraft.class_1297;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2186;
import net.minecraft.class_2232;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3050;
import net.minecraft.class_7157;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_3050.class)
public class ExecuteCommandMixin {
   @Shadow
   @Final
   private static BinaryOperator<ResultConsumer<class_2168>> field_13634;

   @Inject(method = "method_13271", at = @At("TAIL"))
   private static void apoli$registerExecuteStoreResource(CommandDispatcher<class_2168> commandDispatcher, class_7157 commandBuildContext, CallbackInfo ci) {
      // AEA: adds "execute store result|success resource <targets> <power>"; writes go through
      // ApoliResourceCommand.writeAll (PowerResources + Apoli 2.9.2 resource/cooldown fallback).
      if (!(commandDispatcher.getRoot().getChild("execute") instanceof LiteralCommandNode<class_2168> executeNode)) {
         return;
      }
      CommandNode<class_2168> storeNode = executeNode.getChild("store");
      if (storeNode == null) {
         return;
      }
      CommandNode<class_2168> result = storeNode.getChild("result");
      if (result != null) {
         result.addChild(apoli$storeResource(executeNode, false));
      }
      CommandNode<class_2168> success = storeNode.getChild("success");
      if (success != null) {
         success.addChild(apoli$storeResource(executeNode, true));
      }
   }

   @Unique
   private static CommandNode<class_2168> apoli$storeResource(LiteralCommandNode<class_2168> executeNode, boolean successOnly) {
      return class_2170.method_9247("resource")
            .then(
               class_2170.method_9244("targets", class_2186.method_9306())
                  .then(
                     class_2170.method_9244("power", class_2232.method_9441())
                        .suggests(ApoliResourceCommand.RESOURCE_POWERS)
                        .redirect(
                           executeNode,
                           ctx -> apoli$storeValue(
                              (class_2168)ctx.getSource(), class_2186.method_9317(ctx, "targets"), class_2232.method_9443(ctx, "power"), successOnly
                           )
                        )
                  )
            )
         .build();
   }

   @Unique
   private static class_2168 apoli$storeValue(class_2168 source, Collection<? extends class_1297> targets, class_2960 power, boolean successOnly) {
      return source.method_9209((context, success, result) -> {
         int value = successOnly ? (success ? 1 : 0) : result;
         int affected = 0;

         for (class_1297 target : targets) {
            if (target.method_5805()) {
               PowerContainer container = PowerContainer.of(target);
               if (container != null && ApoliResourceCommand.writeAll(container, power, value).isPresent()) {
                  affected++;
               }
            }
         }

         if (affected == 0) {
            source.method_9213(class_2561.method_43470("No target holds the resource power " + power));
         }
      }, field_13634);
   }
}
