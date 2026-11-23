package dev.alphasucks.aea.port.action.builtin.item;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.StorageTarget;
import net.minecraft.class_1799;
import net.minecraft.class_2487;
import net.minecraft.class_7923;
import net.minecraft.server.MinecraftServer;

public final class StoreDataItemAction implements ActionType<ItemCtx, StorageTarget> {
   @Override
   public MapCodec<StorageTarget> codec() {
      return StorageTarget.CODEC;
   }

   public void run(StorageTarget cfg, ItemCtx ctx) {
      MinecraftServer server = ctx.level().method_8503();
      if (server != null) {
         class_1799 stack = ctx.stack();
         class_2487 out = new class_2487();
         out.method_10582("id", class_7923.field_41178.method_10221(stack.method_7909()).toString());
         out.method_10569("count", stack.method_7947());
         out.method_10582("name", stack.method_7964().getString());
         if (cfg.nbt()) {
            out.method_10566("nbt", saveStack(ctx, stack));
         }

         cfg.write(server, out);
      }
   }

   private static class_2487 saveStack(ItemCtx ctx, class_1799 stack) {
      return stack.method_7953(new class_2487());
   }
}
