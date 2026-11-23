package dev.alphasucks.aea.port.radial;

import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.class_3222;

public final class RadialMenuManager {
   private static final Map<UUID, RadialMenuManager.Pending> OPEN = new ConcurrentHashMap();
   private static final AtomicInteger NONCE = new AtomicInteger();

   private RadialMenuManager() {
   }

   public static int open(class_3222 player, List<EntityAction> actions) {
      int nonce = NONCE.incrementAndGet();
      OPEN.put(player.method_5667(), new RadialMenuManager.Pending(nonce, actions));
      return nonce;
   }

   public static void select(class_3222 player, int nonce, int index) {
      RadialMenuManager.Pending pending = (RadialMenuManager.Pending)OPEN.get(player.method_5667());
      if (pending != null && pending.nonce == nonce) {
         OPEN.remove(player.method_5667());
         if (index >= 0 && index < pending.actions.size()) {
            ((EntityAction)pending.actions.get(index)).run(new EntityCtx(player, player.method_37908()));
         }
      }
   }

   public static void forget(UUID player) {
      OPEN.remove(player);
   }

   private record Pending(int nonce, List<EntityAction> actions) {
   }
}
