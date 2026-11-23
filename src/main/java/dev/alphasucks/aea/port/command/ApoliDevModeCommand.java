package dev.alphasucks.aea.port.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.alphasucks.aea.port.dev.DevMode;
import java.util.Collection;
import java.util.List;
import net.minecraft.class_124;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2186;
import net.minecraft.class_2561;
import net.minecraft.class_3222;

public final class ApoliDevModeCommand {
   private ApoliDevModeCommand() {
   }

   public static void register(CommandDispatcher<class_2168> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247("apoli:dev_mode")
                  .requires(ApoliPermissions.require("apoli.command.dev_mode", 2)))
               .executes(ctx -> toggle(ctx, List.of(((class_2168)ctx.getSource()).method_9207()))))
            .then(class_2170.method_9244("targets", class_2186.method_9308()).executes(ctx -> toggle(ctx, class_2186.method_9312(ctx, "targets"))))
      );
   }

   private static int toggle(CommandContext<class_2168> ctx, Collection<class_3222> targets) throws CommandSyntaxException {
      int changed = 0;

      for (class_3222 player : targets) {
         boolean enabled = DevMode.toggle(player);
         player.method_43496(
            class_2561.method_43470("Apoli developer mode " + (enabled ? "on" : "off")).method_27692(enabled ? class_124.field_1060 : class_124.field_1080)
         );
         // AEA: Overgrown also replayed its macro-expansion report here; AEA has no macro loader (Apoli 2.9.2 loads powers).
         changed++;
      }

      int total = changed;
      ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470("Toggled Apoli developer mode for " + total + " player(s)"), true);
      return changed;
   }
}
