package dev.overgrown.sync.factory.action.entity.radial_menu;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.entity.radial_menu.packet.NetworkingConstants;
import dev.overgrown.sync.factory.action.entity.radial_menu.server.RadialMenuServer;
import dev.overgrown.sync.factory.action.entity.radial_menu.utils.RadialMenuEntry;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.data.SerializableData.Instance;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_1297;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_3222;

public class RadialMenuAction {
   public static void action(Instance data, class_1297 entity) {
      if (entity instanceof class_3222 player) {
         List<RadialMenuEntry> entries = data.get("entries");
         List<RadialMenuEntry> filteredEntries = (List<RadialMenuEntry>)entries.stream()
            .filter(entry -> entry.getCondition() == null || entry.getCondition().test(entity))
            .collect(Collectors.toList());
         if (!filteredEntries.isEmpty()) {
            class_2960 menuTexture = data.get("sprite_location");
            class_2540 buf = NetworkingConstants.createRadialMenuBuffer(filteredEntries, menuTexture);
            RadialMenuServer.openMenu(player, filteredEntries);
            ServerPlayNetworking.send(player, NetworkingConstants.RADIAL_MENU_SERVER_TO_CLIENT, buf);
         }
      }
   }

   public static ActionFactory<class_1297> getFactory() {
      return new ActionFactory<>(
         Sync.identifier("radial_menu"),
         new SerializableData().add("entries", RadialMenuEntry.RADIAL_MENU_ENTRIES).add("sprite_location", SerializableDataTypes.IDENTIFIER, null),
         RadialMenuAction::action
      );
   }
}
