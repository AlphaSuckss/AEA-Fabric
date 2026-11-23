package dev.alphasucks.aea.port.compat.accessory.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.alphasucks.aea.port.command.ApoliPermissions;
import dev.alphasucks.aea.port.compat.accessory.Accessories;
import dev.alphasucks.aea.port.compat.accessory.AccessorySlotRef;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2186;
import net.minecraft.class_2561;

public final class AccessoryCommand {
   private AccessoryCommand() {
   }

   public static void register(CommandDispatcher<class_2168> dispatcher) {
      LiteralArgumentBuilder<class_2168> root = (LiteralArgumentBuilder<class_2168>)class_2170.method_9247("apoli:accessory")
         .requires(ApoliPermissions.require("apoli.command.accessory", 2));
      root.then(class_2170.method_9247("list").then(class_2170.method_9244("targets", class_2186.method_9306()).executes(ctx -> list(ctx, false))));
      root.then(class_2170.method_9247("slots").then(class_2170.method_9244("targets", class_2186.method_9306()).executes(ctx -> list(ctx, true))));
      LiteralCommandNode<class_2168> node = dispatcher.register(root);
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247("accessory").requires(ApoliPermissions.require("apoli.command.accessory", 2)))
            .redirect(node)
      );
   }

   private static int list(CommandContext<class_2168> ctx, boolean allSlots) throws CommandSyntaxException {
      int total = 0;

      for (class_1297 entity : class_2186.method_9317(ctx, "targets")) {
         if (entity instanceof class_1309 living) {
            List<AccessorySlotRef> refs = allSlots ? Accessories.slots(living) : Accessories.equipped(living);
            String header = entity.method_5477().getString() + " — " + (allSlots ? "slots" : "equipped") + ": " + refs.size();
            ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470(header), false);

            for (AccessorySlotRef ref : refs) {
               class_1799 stack = ref.getStack();
               String line = "  ["
                  + ref.provider()
                  + "] "
                  + ref.slotId()
                  + (stack.method_7960() ? "" : " = " + stack.method_7947() + "x " + stack.method_7964().getString());
               ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470(line), false);
            }

            total += refs.size();
         }
      }

      return total;
   }
}
