package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.StorageTarget;
import net.minecraft.class_1297;
import net.minecraft.class_2487;
import net.minecraft.class_7923;
import net.minecraft.server.MinecraftServer;

public final class StoreDataAction implements ActionType<EntityCtx, StorageTarget> {
   @Override
   public MapCodec<StorageTarget> codec() {
      return StorageTarget.CODEC;
   }

   public void run(StorageTarget cfg, EntityCtx ctx) {
      class_1297 entity = ctx.raw();
      if (entity != null) {
         MinecraftServer server = ctx.level().method_8503();
         if (server != null) {
            class_2487 out = new class_2487();
            out.method_10582("id", class_7923.field_41177.method_10221(entity.method_5864()).toString());
            out.method_10582("uuid", entity.method_5845());
            out.method_10582("name", entity.method_5477().getString());
            out.method_10549("x", entity.method_23317());
            out.method_10549("y", entity.method_23318());
            out.method_10549("z", entity.method_23321());
            out.method_10582("pos", entity.method_23317() + " " + entity.method_23318() + " " + entity.method_23321());
            out.method_10582("dimension", entity.method_37908().method_27983().method_29177().toString());
            if (cfg.nbt()) {
               out.method_10566("nbt", entity.method_5647(new class_2487()));
            }

            cfg.write(server, out);
         }
      }
   }
}
