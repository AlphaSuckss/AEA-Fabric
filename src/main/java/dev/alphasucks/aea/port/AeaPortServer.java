package dev.alphasucks.aea.port;

import dev.alphasucks.aea.port.action.DelayedActionQueue;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.ScrollDirection;
import dev.alphasucks.aea.port.entity.ApoliEntities;
import dev.alphasucks.aea.port.entity.GrabManager;
import dev.alphasucks.aea.port.entity.disguise.DisguiseData;
import dev.alphasucks.aea.port.entity.disguise.DisguiseManager;
import dev.alphasucks.aea.port.keybind.HeldKeys;
import dev.alphasucks.aea.port.keybind.KeyDispatch;
import dev.alphasucks.aea.port.loader.ApoliKeybindLoader;
import dev.alphasucks.aea.port.network.payload.CameraPerspectiveC2S;
import dev.alphasucks.aea.port.network.payload.CameraTypeC2S;
import dev.alphasucks.aea.port.network.payload.DisguiseUpdateS2C;
import dev.alphasucks.aea.port.network.payload.KeyHeldC2S;
import dev.alphasucks.aea.port.network.payload.LabelUpdateS2C;
import dev.alphasucks.aea.port.network.payload.MouseMovementC2S;
import dev.alphasucks.aea.port.network.payload.PlayerModelTypeC2S;
import dev.alphasucks.aea.port.network.payload.PowerActivatedS2C;
import dev.alphasucks.aea.port.network.payload.PowerActivationC2S;
import dev.alphasucks.aea.port.network.payload.PowerReleaseC2S;
import dev.alphasucks.aea.port.network.payload.PowerToggleC2S;
import dev.alphasucks.aea.port.network.payload.RadialMenuSelectC2S;
import dev.alphasucks.aea.port.network.payload.RefundSkillC2S;
import dev.alphasucks.aea.port.network.payload.BuySkillC2S;
import dev.alphasucks.aea.port.network.payload.RequestSkillStateC2S;
import dev.alphasucks.aea.port.network.payload.RopeChangeLengthC2S;
import dev.alphasucks.aea.port.network.payload.RopeSwingC2S;
import dev.alphasucks.aea.port.network.payload.ScaleSyncS2C;
import dev.alphasucks.aea.port.network.payload.ScrollWheelC2S;
import dev.alphasucks.aea.port.network.payload.SyncKeybindsS2C;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerResources;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import dev.alphasucks.aea.port.power.builtin.ActionOnKeyPressPower;
import dev.alphasucks.aea.port.power.builtin.ActionOnKeySequencePower;
import dev.alphasucks.aea.port.power.builtin.ActionOnMouseMovementPower;
import dev.alphasucks.aea.port.power.builtin.ActionOnScrollWheelPower;
import dev.alphasucks.aea.port.power.builtin.ActionOnUseHandler;
import dev.alphasucks.aea.port.power.builtin.DeathHandler;
import dev.alphasucks.aea.port.power.builtin.EntitySetPower;
import dev.alphasucks.aea.port.power.builtin.EntitySetStorage;
import dev.alphasucks.aea.port.power.builtin.FireProjectilePower;
import dev.alphasucks.aea.port.power.builtin.InventoryPower;
import dev.alphasucks.aea.port.power.builtin.MessagePowerHandler;
import dev.alphasucks.aea.port.power.builtin.ModifyEnchantmentLevelHandler;
import dev.alphasucks.aea.port.power.builtin.PowerStoragePower;
import dev.alphasucks.aea.port.power.builtin.PreventUseHandler;
import dev.alphasucks.aea.port.power.builtin.TogglePower;
import dev.alphasucks.aea.port.power.PowerContainerImpl;
import dev.alphasucks.aea.port.rope.RopeManager;
import dev.alphasucks.aea.port.scale.ScaleState;
import dev.alphasucks.aea.port.scale.ScaleSync;
import dev.alphasucks.aea.port.scale.Scales;
import dev.alphasucks.aea.port.scale.ScaleTypes;
import dev.alphasucks.aea.port.script.ScriptLoader;
import dev.alphasucks.aea.port.skill.SkillTreeLoader;
import dev.alphasucks.aea.port.skill.SkillTrees;
import dev.alphasucks.aea.port.tick.TickRates;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_3264;
import net.minecraft.class_3300;
import net.minecraft.class_3302;
import net.minecraft.class_3695;
import net.minecraft.class_3302.class_4045;
import net.minecraft.server.MinecraftServer;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * AEA: the server half of Overgrown's Apoli initialiser, trimmed to the systems AEA ports.
 * Power loading, power syncing and power commands are Apoli 2.9.2's job here.
 */
public final class AeaPortServer {
    private static boolean initialised;
    private static ApoliKeybindLoader keybindLoader;

    private AeaPortServer() {
    }

    public static void init() {
        if (initialised) {
            return;
        }
        initialised = true;
        ScaleTypes.bootstrap();
        dev.alphasucks.aea.port.advancement.ApoliCriteria.register();
        dev.alphasucks.aea.port.attribution.PowerCause.bootstrap();
        dev.alphasucks.aea.port.particle.ApoliParticles.register();
        dev.alphasucks.aea.port.compat.accessory.AccessoryCompat.init();
        if (dev.alphasucks.aea.port.compat.ModCompat.HARDCORE_REVIVAL) {
            dev.alphasucks.aea.port.compat.hardcorerevival.HardcoreRevivalCompat.init();
        }
        ApoliEntities.register();
        dev.alphasucks.aea.port.command.AeaPortCommands.register();

        keybindLoader = new ApoliKeybindLoader();
        ResourceManagerHelper.get(class_3264.field_14190).registerReloadListener(new IdentifiedReloader(Apoli.internal("keybinds_reloader"), keybindLoader));
        ResourceManagerHelper.get(class_3264.field_14190).registerReloadListener(new IdentifiedReloader(Apoli.internal("skill_trees_reloader"), new SkillTreeLoader()));
        ResourceManagerHelper.get(class_3264.field_14190).registerReloadListener(new IdentifiedReloader(Apoli.internal("scripts_reloader"), new ScriptLoader()));

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            keybindLoader.attachServer(server);
            GrabManager.clearAll();
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            EntitySetStorage.attach(server);
            ApoliNetwork.broadcastKeybinds(server, SyncKeybindsS2C.fromCurrent());
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            ModifyEnchantmentLevelHandler.clearServer();
            for (class_3222 player : server.method_3760().method_14571()) {
                SkillTrees.grantOnJoin(player);
                ApoliNetwork.sendSkillDefs(player);
                ApoliNetwork.sendSkillState(player);
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            EntitySetPower.reset();
            ModifyEnchantmentLevelHandler.clearServer();
            TickRates.clear();
            DelayedActionQueue.clear();
            RopeManager.clear();
            dev.alphasucks.aea.port.power.ResolvedPowers.clear();
        });

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> MessagePowerHandler.handle(sender, sender, message.method_44862(), params));
        ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((message, source, params) -> MessagePowerHandler.handle(source.method_9228(), source.method_44023(), message.method_44862(), params));

        ServerTickEvents.END_SERVER_TICK.register(AeaPortServer::onServerTick);

        ServerPlayNetworking.registerGlobalReceiver(PowerActivationC2S.CHANNEL, (server, player, handler, buf, sender) -> {
            PowerActivationC2S payload = PowerActivationC2S.read(buf);
            server.execute(() -> handleActivation(player, payload));
        });
        ServerPlayNetworking.registerGlobalReceiver(PowerToggleC2S.CHANNEL, (server, player, handler, buf, sender) -> {
            PowerToggleC2S payload = PowerToggleC2S.read(buf);
            server.execute(() -> handleToggle(player, payload));
        });
        ServerPlayNetworking.registerGlobalReceiver(PowerReleaseC2S.CHANNEL, (server, player, handler, buf, sender) -> {
            PowerReleaseC2S payload = PowerReleaseC2S.read(buf);
            server.execute(() -> KeyDispatch.releasePower(player, payload.power()));
        });
        ServerPlayNetworking.registerGlobalReceiver(PlayerModelTypeC2S.CHANNEL, (server, player, handler, buf, sender) -> {
            PlayerModelTypeC2S payload = PlayerModelTypeC2S.read(buf);
            server.execute(() -> dev.alphasucks.aea.port.entity.PlayerModelTypes.set(player.method_5667(), payload.modelType()));
        });
        ServerPlayNetworking.registerGlobalReceiver(CameraTypeC2S.CHANNEL, (server, player, handler, buf, sender) -> {
            CameraTypeC2S payload = CameraTypeC2S.read(buf);
            server.execute(() -> dev.alphasucks.aea.port.entity.CameraPerspectives.setType(player.method_5667(), payload.cameraType()));
        });
        ServerPlayNetworking.registerGlobalReceiver(CameraPerspectiveC2S.CHANNEL, (server, player, handler, buf, sender) -> {
            CameraPerspectiveC2S payload = CameraPerspectiveC2S.read(buf);
            server.execute(() -> dev.alphasucks.aea.port.entity.CameraPerspectives.set(player.method_5667(), payload.firstPerson()));
        });
        ServerPlayNetworking.registerGlobalReceiver(KeyHeldC2S.CHANNEL, (server, player, handler, buf, sender) -> {
            KeyHeldC2S payload = KeyHeldC2S.read(buf);
            server.execute(() -> HeldKeys.setServerHeld(player, payload.keys()));
        });
        ServerPlayNetworking.registerGlobalReceiver(ScrollWheelC2S.CHANNEL, (server, player, handler, buf, sender) -> {
            ScrollWheelC2S payload = ScrollWheelC2S.read(buf);
            server.execute(() -> ActionOnScrollWheelPower.scroll(player, payload.up() ? ScrollDirection.UP : ScrollDirection.DOWN, payload.notches()));
        });
        ServerPlayNetworking.registerGlobalReceiver(MouseMovementC2S.CHANNEL, (server, player, handler, buf, sender) -> {
            MouseMovementC2S payload = MouseMovementC2S.read(buf);
            server.execute(() -> ActionOnMouseMovementPower.moved(player, payload.yaw(), payload.pitch()));
        });
        ServerPlayNetworking.registerGlobalReceiver(BuySkillC2S.CHANNEL, (server, player, handler, buf, sender) -> {
            BuySkillC2S payload = BuySkillC2S.read(buf);
            server.execute(() -> {
                if (SkillTrees.tryPurchase(player, payload.skill())) {
                    ApoliNetwork.sendSkillState(player);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(RequestSkillStateC2S.CHANNEL, (server, player, handler, buf, sender) -> server.execute(() -> ApoliNetwork.sendSkillState(player)));
        ServerPlayNetworking.registerGlobalReceiver(RefundSkillC2S.CHANNEL, (server, player, handler, buf, sender) -> {
            RefundSkillC2S payload = RefundSkillC2S.read(buf);
            server.execute(() -> {
                if (SkillTrees.tryRefund(player, payload.skill(), false)) {
                    ApoliNetwork.sendSkillState(player);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(RadialMenuSelectC2S.CHANNEL, (server, player, handler, buf, sender) -> {
            RadialMenuSelectC2S payload = RadialMenuSelectC2S.read(buf);
            server.execute(() -> dev.alphasucks.aea.port.radial.RadialMenuManager.select(player, payload.nonce(), payload.index()));
        });
        ServerPlayNetworking.registerGlobalReceiver(RopeChangeLengthC2S.CHANNEL, (server, player, handler, buf, sender) -> {
            RopeChangeLengthC2S p = RopeChangeLengthC2S.read(buf);
            server.execute(() -> RopeManager.handleLengthChange(player, p.ropeId(), p.delta()));
        });
        ServerPlayNetworking.registerGlobalReceiver(RopeSwingC2S.CHANNEL, (server, player, handler, buf, sender) -> {
            RopeSwingC2S p = RopeSwingC2S.read(buf);
            server.execute(() -> RopeManager.applySwingInput(player, p.ropeId(), p.inputDir()));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            var id = handler.field_14140.method_5667();
            HeldKeys.clearServer(id);
            if (PowerTypeRegistry.get(Apoli.id("action_on_key_sequence")) instanceof ActionOnKeySequencePower seq) {
                seq.forget(id);
            }
            if (PowerTypeRegistry.get(Apoli.id("action_on_key_press")) instanceof ActionOnKeyPressPower press) {
                press.forget(id);
            }
            dev.alphasucks.aea.port.radial.RadialMenuManager.forget(id);
            GrabManager.release(id);
            dev.alphasucks.aea.port.entity.CameraPerspectives.remove(id);
            DisguiseManager.onPlayerLeave(id);
            dev.alphasucks.aea.port.entity.LabelManager.onEntityGone(id);
            dev.alphasucks.aea.port.action.builtin.entity.TextAction.onPlayerLeave(id);
            dev.alphasucks.aea.port.entity.PlayerModelTypes.remove(id);
            InventoryPower.onPlayerLeave(id);
            dev.alphasucks.aea.bridge.AeaSuppression.forget(id);
            server.execute(() -> dev.alphasucks.aea.port.power.ResolvedPowers.forget(id));
        });

        UseEntityCallback.EVENT.register((player, level, hand, target, hitResult) -> {
            if (PreventUseHandler.isPrevented(player, target, hand)) {
                return class_1269.field_5814;
            }
            return level.method_8608() ? class_1269.field_5811 : ActionOnUseHandler.fireOncePerTick(player, target, hand);
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof class_3222 sp) {
                dev.alphasucks.aea.port.entity.PlayerModelTypes.resolveFrom(sp.method_5667(), sp.method_7334());
                level.method_8503().execute(() -> {
                    ApoliNetwork.sendKeybinds(sp, SyncKeybindsS2C.fromCurrent());
                    SkillTrees.grantOnJoin(sp);
                    ApoliNetwork.sendSkillDefs(sp);
                    ApoliNetwork.sendSkillState(sp);
                    EntitySetPower.syncAllTo(sp);
                    Scales.syncIfScaled(sp);
                    RopeManager.syncTo(sp);
                });
            }
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            RopeManager.onEntityGone(entity.method_5667());
            dev.alphasucks.aea.port.entity.LabelManager.onEntityGone(entity.method_5667());
            var reason = entity.method_35049();
            if (reason != null && reason.method_31486()) {
                EntitySetPower.onEntityGone(entity);
            }
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> Scales.transfer(oldPlayer, newPlayer));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> Scales.syncIfScaled(player));
        ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register((originalEntity, newEntity, origin, destination) -> Scales.syncIfScaled(newEntity));
        ServerLivingEntityEvents.AFTER_DEATH.register(DeathHandler::onDeath);

        Scales.setSyncer(entity -> ApoliNetwork.broadcastScale(entity, new ScaleSyncS2C(entity.method_5628(), ScaleSync.encode(entity))));
        EntityTrackingEvents.START_TRACKING.register((tracked, player) -> {
            ScaleState scales = Scales.stateOf(tracked);
            if (scales != null && !scales.isDefault()) {
                ApoliNetwork.sendScale(player, new ScaleSyncS2C(tracked.method_5628(), ScaleSync.encode(tracked)));
            }
            EntitySetPower.syncTracking(tracked, player, true);
            DisguiseData disguise = DisguiseManager.get(tracked.method_5667());
            if (disguise != null) {
                ApoliNetwork.sendDisguise(player, new DisguiseUpdateS2C(tracked.method_5628(), Optional.of(disguise)));
            }
            Map<class_2960, class_2561> labels = dev.alphasucks.aea.port.entity.LabelManager.textsFor(tracked.method_5667());
            if (!labels.isEmpty()) {
                ApoliNetwork.sendLabel(player, new LabelUpdateS2C(tracked.method_5628(), labels));
            }
        });
        EntityTrackingEvents.STOP_TRACKING.register((tracked, player) -> EntitySetPower.syncTracking(tracked, player, false));
        DisguiseManager.setBroadcaster((entity, data) -> ApoliNetwork.broadcastDisguise(entity, new DisguiseUpdateS2C(entity.method_5628(), data)));
        dev.alphasucks.aea.port.entity.LabelManager.setBroadcaster((entity, texts) -> ApoliNetwork.broadcastLabel(entity, new LabelUpdateS2C(entity.method_5628(), texts)));
    }

    private static void onServerTick(MinecraftServer server) {
        DelayedActionQueue.tick();
        HeldKeys.tickForced(server);
        RopeManager.tick(server);
        GrabManager.tick(server);
        TickRates.serverTick(server);
        dev.alphasucks.aea.port.power.ResolvedPowers.tick(server);
        boolean forcedKeys = HeldKeys.anyForced();
        ModifyEnchantmentLevelHandler.beginScan();
        dev.alphasucks.aea.port.power.PoweredEntities.forEach(entity -> {
            PowerContainerImpl impl = PowerContainerImpl.of(entity);
            if (impl != null) {
                ModifyEnchantmentLevelHandler.scan(entity, impl);
                if (forcedKeys) {
                    KeyDispatch.tickForcedNonPlayer(entity);
                }
            }
        });
        ModifyEnchantmentLevelHandler.endScan();
        EntitySetPower.flushPendingRemovals();
        EntitySetPower.flushSync(server);
    }

    private static void handleActivation(class_3222 player, PowerActivationC2S payload) {
        if (GrabManager.keybindsDisabled(player.method_5667())) {
            return;
        }
        PowerContainer c = PowerContainer.of(player);
        if (c == null || !c.hasPower(payload.power()) || c.isSuppressed(payload.power()) || PowerStoragePower.keyMuted(c, payload.power())) {
            return;
        }
        Power loaded = ApoliPowers.get(payload.power());
        if (loaded == null) {
            return;
        }
        PowerType<?> type = PowerTypeRegistry.get(loaded.typeId());
        if (loaded.condition().isPresent() && !loaded.condition().get().test(new EntityCtx(player, player.method_51469()))) {
            return;
        }
        if (type instanceof ActionOnKeyPressPower active && loaded.config() instanceof ActionOnKeyPressPower.Config cfg) {
            if (active.tryActivate(payload.power(), cfg, c)) {
                ApoliNetwork.sendActivated(player, new PowerActivatedS2C(payload.power(), PowerResources.cooldownTicks(cfg.cooldown(), c)));
            }
        } else if (type instanceof FireProjectilePower fpp && loaded.config() instanceof FireProjectilePower.Config cfg) {
            if (fpp.tryActivate(payload.power(), cfg, c)) {
                ApoliNetwork.sendActivated(player, new PowerActivatedS2C(payload.power(), PowerResources.cooldownTicks(cfg.params().cooldown(), c)));
            }
        } else if (type instanceof InventoryPower inv && loaded.config() instanceof InventoryPower.Config cfg && c instanceof PowerContainerImpl impl) {
            inv.open(payload.power(), cfg, player, impl);
        }
    }

    private static void handleToggle(class_3222 player, PowerToggleC2S payload) {
        if (GrabManager.keybindsDisabled(player.method_5667())) {
            return;
        }
        PowerContainer c = PowerContainer.of(player);
        if (c == null || !c.hasPower(payload.power()) || c.isSuppressed(payload.power()) || PowerStoragePower.keyMuted(c, payload.power())) {
            return;
        }
        Power loaded = ApoliPowers.get(payload.power());
        if (loaded != null && PowerTypeRegistry.get(loaded.typeId()) instanceof TogglePower && loaded.config() instanceof TogglePower.Config cfg) {
            if (loaded.condition().isEmpty() || loaded.condition().get().test(new EntityCtx(player, player.method_51469()))) {
                TogglePower.toggleByKey(c, payload.power(), cfg);
            }
        }
    }

    private static final class IdentifiedReloader implements IdentifiableResourceReloadListener {
        private final class_2960 id;
        private final class_3302 delegate;

        IdentifiedReloader(class_2960 id, class_3302 delegate) {
            this.id = id;
            this.delegate = delegate;
        }

        @Override
        public class_2960 getFabricId() {
            return id;
        }

        /** run after Apoli 2.9.2 has loaded powers (skill trees and scripts can point at them) */
        @Override
        public java.util.Collection<class_2960> getFabricDependencies() {
            return java.util.List.of(new class_2960("apoli", "powers"));
        }

        @Override
        public CompletableFuture<Void> method_25931(class_4045 barrier, class_3300 rm, class_3695 prep, class_3695 apply, Executor prepExec, Executor applyExec) {
            return delegate.method_25931(barrier, rm, prep, apply, prepExec, applyExec);
        }
    }
}
