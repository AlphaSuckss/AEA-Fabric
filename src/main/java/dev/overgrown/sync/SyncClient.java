package dev.overgrown.sync;

import dev.overgrown.sync.factory.action.bientity.suppress_power.utils.SuppressedPowerManager;
import dev.overgrown.sync.factory.action.bientity.suppress_power.utils.SuppressedPowerSync;
import dev.overgrown.sync.factory.action.entity.radial_menu.client.RadialMenuClient;
import dev.overgrown.sync.factory.data.disguise.DisguiseData;
import dev.overgrown.sync.factory.data.disguise.client.ClientDisguiseManager;
import dev.overgrown.sync.factory.data.keybind.DataDrivenKeybindDefinition;
import dev.overgrown.sync.factory.data.keybind.client.DynamicKeyBindingManager;
import dev.overgrown.sync.factory.data.rope.client.RopeClientInit;
import dev.overgrown.sync.factory.power.type.action_on_sending_message.utils.TranslationKeyResolver;
import dev.overgrown.sync.networking.ModPackets;
import dev.overgrown.sync.registry.entities.SyncEntityModelLayerRegistry;
import dev.overgrown.sync.registry.entities.SyncEntiyRendererRegistry;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents.Unload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.StartTick;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3264;
import net.minecraft.class_3300;
import net.minecraft.class_5498;

public class SyncClient implements ClientModInitializer {
   private static final Map<String, Boolean> LAST_KEY_STATES = new HashMap();
   private static String lastModelType = null;
   private static String lastPerspective = null;

   public void onInitializeClient() {
      ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
         public class_2960 getFabricId() {
            return Sync.identifier("translation_key_resolver");
         }

         public void method_14491(class_3300 manager) {
            TranslationKeyResolver.loadFromResourceManager(manager);
         }
      });
      RopeClientInit.init();
      RadialMenuClient.register();
      SyncEntityModelLayerRegistry.register();
      SyncEntiyRendererRegistry.register();
      ClientPlayNetworking.registerGlobalReceiver(ModPackets.KEYBIND_SYNC, (client, handler, buf, responseSender) -> {
         int count = buf.readInt();
         List<DataDrivenKeybindDefinition> definitions = new ArrayList(count);

         for (int i = 0; i < count; i++) {
            class_2960 id = buf.method_10810();
            String key = buf.method_19772();
            String category = buf.method_19772();
            String name = buf.readBoolean() ? buf.method_19772() : null;
            definitions.add(new DataDrivenKeybindDefinition(id, key, category, name));
         }

         client.execute(() -> {
            DynamicKeyBindingManager.applyKeybinds(definitions);
            dev.alphasucks.aea.port.client.ApoliKeyCache.refresh(); // AEA: keep Apoli's key cache on the new objects
         });
      });
      ClientPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, client) -> {
         client.execute(() -> {
            DynamicKeyBindingManager.unregisterAll();
            dev.alphasucks.aea.port.client.ApoliKeyCache.refresh();
         });
         // reset so the model type / key states are sent again to the next server
         lastPerspective = null;
         lastModelType = null;
         LAST_KEY_STATES.clear();
         SuppressedPowerManager.clearClient();
      });
      ClientPlayNetworking.registerGlobalReceiver(
         SuppressedPowerSync.SUPPRESSED_POWERS_UPDATE,
         (client, handler, buf, responseSender) -> SuppressedPowerSync.readPacket(
            buf, (entityId, powers) -> client.execute(() -> SuppressedPowerManager.setClientSuppressed(entityId, powers))
         )
      );
      ClientPlayNetworking.registerGlobalReceiver(ModPackets.DISGUISE_UPDATE, (client, handler, buf, responseSender) -> {
         int entityNetId = buf.readInt();
         boolean hasDisguise = buf.readBoolean();
         if (hasDisguise) {
            DisguiseData data = DisguiseData.read(buf);
            client.execute(() -> ClientDisguiseManager.setDisguise(entityNetId, data));
         } else {
            client.execute(() -> ClientDisguiseManager.removeDisguise(entityNetId));
         }
      });
      ClientEntityEvents.ENTITY_UNLOAD.register((Unload)(entity, world) -> {
         class_310 client = class_310.method_1551();
         if (client.field_1724 != null && entity == client.field_1724) {
            ClientDisguiseManager.clear();
         } else {
            // the server resends disguises when the entity is tracked again (EntityTrackingEvents.START_TRACKING)
            ClientDisguiseManager.removeDisguise(entity.method_5628());
         }

         SuppressedPowerManager.clearClient(entity.method_5667());
      });
      ClientTickEvents.START_CLIENT_TICK.register((StartTick)SyncClient::clientTick);
   }

   /**
    * Sends model type / perspective / key state changes. Also called by ClientTickEventMixin (Connector support);
    * both callers share this state, so whichever runs second in a tick finds nothing changed and sends nothing.
    */
   public static void clientTick(class_310 client) {
      if (client.field_1724 != null) {
         String currentModelType = client.field_1724.method_3121();
         if (currentModelType.equals("default")) {
            currentModelType = "wide";
         }

         if (lastModelType == null || !lastModelType.equals(currentModelType)) {
            class_2540 buf = new class_2540(Unpooled.buffer());
            buf.method_10814(currentModelType);
            ClientPlayNetworking.send(ModPackets.PLAYER_MODEL_TYPE_UPDATE, buf);
            lastModelType = currentModelType;
         }

         String currentPerspective = perspectiveToString(client.field_1690.method_31044());
         if (!currentPerspective.equals(lastPerspective)) {
            class_2540 buf = new class_2540(Unpooled.buffer());
            buf.method_10814(currentPerspective);
            ClientPlayNetworking.send(ModPackets.PERSPECTIVE_UPDATE, buf);
            lastPerspective = currentPerspective;
         }

         for (class_304 keyBinding : client.field_1690.field_1839) {
            String key = keyBinding.method_1431();
            boolean pressed = keyBinding.method_1434();
            Boolean lastState = (Boolean)LAST_KEY_STATES.get(key);
            if (lastState == null || lastState != pressed) {
               class_2540 buf = new class_2540(Unpooled.buffer());
               buf.method_10814(key);
               buf.writeBoolean(pressed);
               ClientPlayNetworking.send(ModPackets.KEY_PRESS_UPDATE, buf);
               LAST_KEY_STATES.put(key, pressed);
            }
         }
      }
   }

   private static String perspectiveToString(class_5498 perspective) {
      return switch (perspective) {
         case field_26664 -> "first_person";
         case field_26665 -> "third_person_back";
         case field_26666 -> "third_person_front";
      };
   }
}
