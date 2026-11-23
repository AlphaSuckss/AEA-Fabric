package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.entity.GrabManager;
import dev.alphasucks.aea.port.shared.EmptyCfg;
import net.minecraft.class_1297;

public final class ReleaseGrabAction implements ActionType<EntityCtx, EmptyCfg> {
   @Override
   public MapCodec<EmptyCfg> codec() {
      return MapCodec.unit(EmptyCfg.INSTANCE);
   }

   public void run(EmptyCfg cfg, EntityCtx ctx) {
      class_1297 entity = ctx.raw();
      if (entity != null && !entity.method_37908().method_8608()) {
         GrabManager.release(entity.method_5667());
      }
   }
}
