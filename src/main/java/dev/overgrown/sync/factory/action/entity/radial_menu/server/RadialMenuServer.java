package dev.overgrown.sync.factory.action.entity.radial_menu.server;

import dev.overgrown.sync.factory.action.entity.radial_menu.packet.NetworkingConstants;
import dev.overgrown.sync.factory.action.entity.radial_menu.utils.RadialMenuEntry;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_3222;

public class RadialMenuServer {
   // entries of the menu last opened for each player; the client only sends back the index of its choice
   private static final Map<UUID, List<RadialMenuEntry>> OPEN_MENUS = new ConcurrentHashMap<>();

   public static void openMenu(class_3222 player, List<RadialMenuEntry> entries) {
      OPEN_MENUS.put(player.method_5667(), List.copyOf(entries));
   }

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.RADIAL_MENU_CLIENT_TO_SERVER, (server, player, handler, buf, responseSender) -> {
         int index = buf.method_10816();
         server.execute(() -> {
            List<RadialMenuEntry> entries = OPEN_MENUS.remove(player.method_5667());
            player.method_7346();
            if (entries != null && index >= 0 && index < entries.size()) {
               RadialMenuEntry entry = entries.get(index);
               if (entry.getEntityAction() != null) {
                  entry.getEntityAction().accept(player);
               }
            }
         });
      });
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> OPEN_MENUS.remove(handler.field_14140.method_5667()));
   }
}
