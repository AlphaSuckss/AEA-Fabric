package dev.alphasucks.aea.port.action.builtin.block;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.data.StorageTarget;
import net.minecraft.class_2259;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_2769;
import net.minecraft.class_3218;
import net.minecraft.class_7923;
import net.minecraft.server.MinecraftServer;

public final class StoreDataBlockAction implements ActionType<BlockCtx, StorageTarget> {
   @Override
   public MapCodec<StorageTarget> codec() {
      return StorageTarget.CODEC;
   }

   public void run(StorageTarget cfg, BlockCtx ctx) {
      if (ctx.level() instanceof class_3218 level) {
         MinecraftServer server = level.method_8503();
         if (server != null) {
            class_2338 pos = ctx.pos();
            class_2680 state = ctx.state();
            class_2487 out = new class_2487();
            out.method_10582("id", class_7923.field_41175.method_10221(state.method_26204()).toString());
            out.method_10582("state", class_2259.method_9685(state));
            out.method_10569("x", pos.method_10263());
            out.method_10569("y", pos.method_10264());
            out.method_10569("z", pos.method_10260());
            out.method_10582("pos", pos.method_10263() + " " + pos.method_10264() + " " + pos.method_10260());
            out.method_10582("dimension", level.method_27983().method_29177().toString());
            class_2487 properties = new class_2487();

            for (class_2769<?> property : state.method_28501()) {
               properties.method_10582(property.method_11899(), name(state, property));
            }

            out.method_10566("properties", properties);
            if (cfg.nbt()) {
               class_2586 blockEntity = level.method_8321(pos);
               if (blockEntity != null) {
                  out.method_10566("nbt", saveBlockEntity(level, blockEntity));
               }
            }

            cfg.write(server, out);
         }
      }
   }

   private static <T extends Comparable<T>> String name(class_2680 state, class_2769<T> property) {
      return property.method_11901((T)state.method_11654(property));
   }

   private static class_2487 saveBlockEntity(class_3218 level, class_2586 blockEntity) {
      return blockEntity.method_38244();
   }
}
