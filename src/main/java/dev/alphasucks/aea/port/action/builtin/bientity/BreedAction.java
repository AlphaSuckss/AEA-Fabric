package dev.alphasucks.aea.port.action.builtin.bientity;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.shared.EmptyCfg;
import net.minecraft.class_1429;
import net.minecraft.class_1495;
import net.minecraft.class_1498;
import net.minecraft.class_3218;

public final class BreedAction implements ActionType<BiEntityCtx, EmptyCfg> {
   @Override
   public MapCodec<EmptyCfg> codec() {
      return MapCodec.unit(EmptyCfg.INSTANCE);
   }

   public void run(EmptyCfg cfg, BiEntityCtx ctx) {
      if (ctx.actor() instanceof class_1429 parent && ctx.target() instanceof class_1429 mate && parent != mate) {
         if (parent.method_37908() instanceof class_3218 level && !parent.method_6109() && !mate.method_6109()) {
            if (parent.getClass() == mate.getClass() || horseOrDonkey(parent) && horseOrDonkey(mate)) {
               parent.method_24650(level, mate);
            }
         }
      }
   }

   private static boolean horseOrDonkey(class_1429 animal) {
      return animal instanceof class_1498 || animal instanceof class_1495;
   }
}
