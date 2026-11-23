package dev.overgrown.sync;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.overgrown.sync.factory.action.bientity.suppress_power.utils.SuppressedPowerManager;
import dev.overgrown.sync.factory.action.bientity.suppress_power.utils.SuppressedPowerSync;
import dev.overgrown.sync.factory.action.block.ghost_block.GhostBlockAction;
import dev.overgrown.sync.factory.compatibility.jade.power.type.modify_jade_tooltip.ModifyJadeTooltipPower;
import dev.overgrown.sync.factory.power.type.body_part_damage_modifier.utils.HitLocationTracker;
import dev.overgrown.sync.registry.factory.utils.ApoliRegistryHelper;
import dev.overgrown.sync.factory.action.bientity.transfer.StolenPowerSlotManager;
import dev.overgrown.sync.factory.action.entity.grant_all_powers.GrantAllPowersAction;
import dev.overgrown.sync.factory.action.entity.grant_all_powers.SourcePowerRegistry;
import dev.overgrown.sync.factory.action.entity.radial_menu.server.RadialMenuServer;
import dev.overgrown.sync.factory.action.entity.teleportation.events.EntityCleanupHandler;
import dev.overgrown.sync.factory.action.entity.toggle_transfer_mode.utils.TransferModeManager;
import dev.overgrown.sync.factory.condition.entity.key_pressed.utils.KeyPressManager;
import dev.overgrown.sync.factory.condition.entity.perspective.utils.PerspectiveManager;
import dev.overgrown.sync.factory.condition.entity.player_model_type.utils.PlayerModelTypeManager;
import dev.overgrown.sync.factory.data.disguise.DisguiseData;
import dev.overgrown.sync.factory.data.disguise.DisguiseManager;
import dev.overgrown.sync.factory.data.keybind.DataDrivenKeybindDefinition;
import dev.overgrown.sync.factory.data.keybind.DataDrivenKeybindLoader;
import dev.overgrown.sync.factory.data.rope.common.RopeInit;
import dev.overgrown.sync.factory.power.type.action_on_death.ActionOnDeathPower;
import dev.overgrown.sync.factory.power.type.action_on_sending_message.ActionOnSendingMessagePower;
import dev.overgrown.sync.factory.power.type.action_on_sending_message.ActionOnSendingMessagePower.Result;
import dev.overgrown.sync.factory.power.type.action_on_sending_message.utils.TranslationKeyResolver;
import dev.overgrown.sync.factory.power.type.entity_set.EntitySetPower;
import dev.overgrown.sync.networking.ModPackets;
import dev.overgrown.sync.registry.entities.SyncEntityRegistry;
import dev.overgrown.sync.registry.factory.SyncTypeRegistry;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Prioritized.CallInstance;
import io.github.apace100.apoli.util.NamespaceAlias;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AfterDeath;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AllowDeath;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents.AfterPlayerChange;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.AfterRespawn;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents.StartTracking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents.AllowChatMessage;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents.AllowCommandMessage;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Join;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2232;
import net.minecraft.class_2540;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_3264;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sync implements ModInitializer {
   public static final String MOD_ID = "sync";
   public static final Logger LOGGER = LoggerFactory.getLogger("sync");
   public static boolean HAS_ASPECTSLIB = false;
   public static boolean HAS_JADE = false;
   private static final Map<class_1309, Float> LAST_DEATH_DAMAGE = Collections.synchronizedMap(new WeakHashMap<>());

   public static class_2960 identifier(String path) {
      return new class_2960("sync", path);
   }

   public void onInitialize() {
      HAS_ASPECTSLIB = FabricLoader.getInstance().isModLoaded("aspectslib");
      if (HAS_ASPECTSLIB) {
         LOGGER.info("AspectsLib detected - compatibility enabled");
      }

      HAS_JADE = FabricLoader.getInstance().isModLoaded("jade");
      if (HAS_JADE) {
         LOGGER.info("Jade detected - tooltip compat enabled");
      }

      TranslationKeyResolver.load();
      NamespaceAlias.addAlias("apoli", "sync");
      SyncTypeRegistry.register();
      if (!HAS_JADE) {
         // AEA: ModifyJadeTooltipPower has no Jade dependency; register it even without Jade so a server and a client
         // that differ in having Jade still agree on the power factory list (Apoli's power sync packet needs that)
         ApoliRegistryHelper.registerPowerFactory(ModifyJadeTooltipPower.getFactory());
      }

      RadialMenuServer.register();
      SyncEntityRegistry.register();
      RopeInit.init();
      SourcePowerRegistry.registerClearHook();
      EntityCleanupHandler.register();
      SuppressedPowerSync.register();
      GhostBlockAction.registerEvents();
      // AEA: data/*/keybinds is handled by the ported Overgrown keybind loader
      ServerEntityEvents.ENTITY_LOAD.register(EntitySetPower::integrateLoadCallback);
      ServerEntityEvents.ENTITY_UNLOAD.register(EntitySetPower::integrateUnloadCallback);
      ResourceManagerHelper.get(class_3264.field_14190).registerReloadListener(new GrantAllPowersAction());
      ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((AllowChatMessage)(message, sender, params) -> {
         String content = message.method_44862();
         class_2960 typeId = sender.method_37908().method_30349().method_30530(class_7924.field_41237).method_10221(params.comp_919());
         return processMessagePowers(sender, content, typeId);
      });
      ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((AllowCommandMessage)(message, source, params) -> {
         if (source.method_9228() instanceof class_3222 sender) {
            String var6 = message.method_44862();
            class_2960 typeId = source.method_30497().method_30530(class_7924.field_41237).method_10221(params.comp_919());
            return processMessagePowers(sender, var6, typeId);
         } else {
            return true;
         }
      });
      ServerLivingEntityEvents.ALLOW_DEATH.register((AllowDeath)(entity, damageSource, damageAmount) -> {
         LAST_DEATH_DAMAGE.put(entity, damageAmount);
         return true;
      });
      ServerLivingEntityEvents.AFTER_DEATH.register((AfterDeath)(entity, damageSource) -> {
         Float recorded = LAST_DEATH_DAMAGE.remove(entity);
         float damageAmount = recorded != null ? recorded : entity.method_6063();
         PowerHolderComponent.getPowers(entity, ActionOnDeathPower.class).forEach(power -> power.onDeath(damageSource, damageAmount));
         if (entity instanceof class_3222 player) {
            // stolen power slots are kept: the transferred powers survive death, so their slots must too
            TransferModeManager.remove(player.method_5667());
         }

         DisguiseManager.removeDisguise(entity);
         SuppressedPowerManager.removeAll(entity.method_5667());
      });
      ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> HitLocationTracker.clearAll());
      EntityTrackingEvents.START_TRACKING.register((StartTracking)(trackedEntity, player) -> {
         DisguiseData data = DisguiseManager.getDisguise(trackedEntity.method_5667());
         if (data != null) {
            ServerPlayNetworking.send(player, ModPackets.DISGUISE_UPDATE, DisguiseManager.buildSetPacket(trackedEntity.method_5628(), data));
         }
      });
      ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((AfterPlayerChange)(player, origin, destination) -> resendOwnDisguise(player));
      ServerPlayerEvents.AFTER_RESPAWN.register((AfterRespawn)(oldPlayer, newPlayer, alive) -> resendOwnDisguise(newPlayer));
      ServerPlayNetworking.registerGlobalReceiver(ModPackets.PLAYER_MODEL_TYPE_UPDATE, (server, player, handler, buf, responseSender) -> {
         String modelType = buf.method_19772();
         server.execute(() -> PlayerModelTypeManager.setModelType(player, modelType));
      });
      ServerPlayNetworking.registerGlobalReceiver(ModPackets.KEY_PRESS_UPDATE, (server, player, handler, buf, responseSender) -> {
         String key = buf.method_19772();
         boolean pressed = buf.readBoolean();
         server.execute(() -> KeyPressManager.updateKeyState(player.method_5667(), key, pressed));
      });
      ServerPlayNetworking.registerGlobalReceiver(ModPackets.PERSPECTIVE_UPDATE, (server, player, handler, buf, responseSender) -> {
         String perspective = buf.method_19772();
         server.execute(() -> PerspectiveManager.setPerspective(player, perspective));
      });
      ServerPlayConnectionEvents.JOIN.register((Join)(handler, packetSender, server) -> {
         server.execute(() -> syncDisguisesToPlayer(handler.field_14140, server));
         // AEA: data/*/keybinds is handled by the ported Overgrown keybind loader
      });
      ServerPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, server) -> {
         KeyPressManager.removePlayer(handler.field_14140.method_5667());
         PlayerModelTypeManager.removePlayer(handler.field_14140.method_5667());
         PerspectiveManager.removePlayer(handler.field_14140.method_5667());
         SuppressedPowerManager.removeAll(handler.field_14140.method_5667());
         UUID id = handler.field_14140.method_5667();
         // stolen power slots are kept in memory so they survive a relog (not a server restart)
         TransferModeManager.remove(id);
      });
      CommandRegistrationCallback.EVENT
         .register(
            (CommandRegistrationCallback)(dispatcher, registryAccess, environment) -> dispatcher.register(
               (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247("disguise")
                        .requires(source -> source.method_9259(2)))
                     .then(class_2170.method_9244("entity_type", class_2232.method_9441()).suggests((ctx, builder) -> {
                        class_7923.field_41177.method_10235().forEach(id -> {
                           if (id.toString().startsWith(builder.getRemaining()) || id.method_12832().startsWith(builder.getRemaining())) {
                              builder.suggest(id.toString());
                           }
                        });
                        return builder.buildFuture();
                     }).executes(ctx -> {
                        class_3222 player = ((class_2168)ctx.getSource()).method_9207();
                        class_2960 typeId = class_2232.method_9443(ctx, "entity_type");
                        class_1299<?> entityType = class_7923.field_41177.method_10223(typeId);
                        if (!class_7923.field_41177.method_10250(typeId)) {
                           ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("Unknown entity type: " + typeId));
                           return 0;
                        } else {
                           class_1297 dummy = entityType.method_5883(player.method_37908());
                           if (dummy == null) {
                              ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("Cannot create entity of type: " + typeId));
                              return 0;
                           } else {
                              dummy.method_31472();
                              DisguiseData data = new DisguiseData(typeId, -1, null, null);
                              DisguiseManager.forceApplyDisguise(player, data);
                              ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470("Disguised as " + typeId), false);
                              return 1;
                           }
                        }
                     })))
                  .then(class_2170.method_9247("clear").executes(ctx -> {
                     class_3222 player = ((class_2168)ctx.getSource()).method_9207();
                     DisguiseManager.removeDisguise(player);
                     ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470("Disguise removed"), false);
                     return 1;
                  }))
            )
         );
   }

   private static void resendOwnDisguise(class_3222 player) {
      DisguiseData data = DisguiseManager.getDisguise(player.method_5667());
      if (data != null) {
         ServerPlayNetworking.send(player, ModPackets.DISGUISE_UPDATE, DisguiseManager.buildSetPacket(player.method_5628(), data));
      }
   }

   private static void syncDisguisesToPlayer(class_3222 recipient, MinecraftServer server) {
      Map<UUID, DisguiseData> all = DisguiseManager.getAllDisguises();
      if (!all.isEmpty()) {
         server.method_3738().forEach(world -> world.method_27909().forEach(entity -> {
            DisguiseData data = (DisguiseData)all.get(entity.method_5667());
            if (data != null) {
               class_2540 buf = DisguiseManager.buildSetPacket(entity.method_5628(), data);
               ServerPlayNetworking.send(recipient, ModPackets.DISGUISE_UPDATE, buf);
            }
         }));
      }
   }

   private static boolean processMessagePowers(class_3222 player, String content, class_2960 typeId) {
      CallInstance<ActionOnSendingMessagePower> call = new CallInstance<>();
      call.add(player, ActionOnSendingMessagePower.class);
      String currentMessage = content;
      boolean prevented = false;

      for (int p = call.getMaxPriority(); p >= call.getMinPriority(); p--) {
         for (ActionOnSendingMessagePower power : call.getPowers(p)) {
            Result result = power.processMessage(currentMessage, typeId);
            if (result.isPrevented()) {
               prevented = true;
               break;
            }

            if (result.getModifiedMessage() != null) {
               currentMessage = result.getModifiedMessage();
            }
         }

         if (prevented) {
            break;
         }
      }

      if (prevented) {
         return false;
      } else if (!currentMessage.equals(content)) {
         class_2561 formatted = class_2561.method_43469("chat.type.text", player.method_5476(), class_2561.method_43470(currentMessage));
         player.field_13995.method_3760().method_43514(formatted, false);
         return false;
      } else {
         return true;
      }
   }

   private static void syncKeybindsToPlayer(class_3222 player) {
      List<DataDrivenKeybindDefinition> defs = DataDrivenKeybindLoader.LOADED;
      class_2540 buf = PacketByteBufs.create();
      buf.writeInt(defs.size());

      for (DataDrivenKeybindDefinition def : defs) {
         buf.method_10812(def.id());
         buf.method_10814(def.key());
         buf.method_10814(def.category());
         buf.writeBoolean(def.name() != null);
         if (def.name() != null) {
            buf.method_10814(def.name());
         }
      }

      ServerPlayNetworking.send(player, ModPackets.KEYBIND_SYNC, buf);
      LOGGER.debug("[Sync/Keybinds] Sent {} keybind definition(s) to {}.", defs.size(), player.method_5477().getString());
   }
}
