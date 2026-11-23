package dev.alphasucks.aea.port;

import dev.alphasucks.aea.port.client.ApoliKeyHandler;
import dev.alphasucks.aea.port.client.ApoliKeyMappings;
import dev.alphasucks.aea.port.client.BlockedKeys;
import dev.alphasucks.aea.port.client.CameraPerspectiveReporter;
import dev.alphasucks.aea.port.client.ClientEntitySets;
import dev.alphasucks.aea.port.client.ClientLabelState;
import dev.alphasucks.aea.port.client.ClientPowerState;
import dev.alphasucks.aea.port.client.ClientTickRates;
import dev.alphasucks.aea.port.client.CursorSpeedState;
import dev.alphasucks.aea.port.client.CustomProjectileRenderer;
import dev.alphasucks.aea.port.client.DynamicKeyMappingManager;
import dev.alphasucks.aea.port.client.ForcedKeys;
import dev.alphasucks.aea.port.client.KeyPressWatcher;
import dev.alphasucks.aea.port.client.KeyReleases;
import dev.alphasucks.aea.port.client.MouseMovementWatcher;
import dev.alphasucks.aea.port.client.PlayerModelTypeReporter;
import dev.alphasucks.aea.port.client.ScreenCloseKeys;
import dev.alphasucks.aea.port.client.TextOverlayRenderer;
import dev.alphasucks.aea.port.client.TextureFallbacks;
import dev.alphasucks.aea.port.client.camera.CameraController;
import dev.alphasucks.aea.port.client.disguise.ClientDisguiseManager;
import dev.alphasucks.aea.port.client.particle.AnchoredParticleClient;
import dev.alphasucks.aea.port.client.particle.CustomParticle;
import dev.alphasucks.aea.port.client.particle.ParticleSheet;
import dev.alphasucks.aea.port.client.particle.ParticleTextures;
import dev.alphasucks.aea.port.client.radial.RadialMenuScreen;
import dev.alphasucks.aea.port.client.render.AnimatedTextures;
import dev.alphasucks.aea.port.client.render.AnimationManager;
import dev.alphasucks.aea.port.client.render.CustomModelManager;
import dev.alphasucks.aea.port.client.rope.RopeClientManager;
import dev.alphasucks.aea.port.client.rope.RopeRenderer;
import dev.alphasucks.aea.port.client.rope.VerletRopeState;
import dev.alphasucks.aea.port.client.skill.ClientSkillState;
import dev.alphasucks.aea.port.client.skill.SkillTreeScreen;
import dev.alphasucks.aea.port.client.summon.CloneRenderer;
import dev.alphasucks.aea.port.client.summon.MinionModel;
import dev.alphasucks.aea.port.client.summon.MinionRenderer;
import dev.alphasucks.aea.port.client.summon.SummonModelLayers;
import dev.alphasucks.aea.port.entity.ApoliEntities;
import dev.alphasucks.aea.port.keybind.HeldKeys;
import dev.alphasucks.aea.port.network.payload.AnchoredParticlesS2C;
import dev.alphasucks.aea.port.network.payload.ApplyVelocityS2C;
import dev.alphasucks.aea.port.network.payload.DisguiseUpdateS2C;
import dev.alphasucks.aea.port.network.payload.ForceKeyS2C;
import dev.alphasucks.aea.port.network.payload.KeyHeldC2S;
import dev.alphasucks.aea.port.network.payload.LabelUpdateS2C;
import dev.alphasucks.aea.port.network.payload.PowerActivatedS2C;
import dev.alphasucks.aea.port.network.payload.PowerReleaseC2S;
import dev.alphasucks.aea.port.network.payload.RadialMenuOpenS2C;
import dev.alphasucks.aea.port.network.payload.RequestSkillStateC2S;
import dev.alphasucks.aea.port.network.payload.RopeCreateS2C;
import dev.alphasucks.aea.port.network.payload.RopeDeleteS2C;
import dev.alphasucks.aea.port.network.payload.RopeVerletLengthS2C;
import dev.alphasucks.aea.port.network.payload.ScaleSyncS2C;
import dev.alphasucks.aea.port.network.payload.SetPerspectiveS2C;
import dev.alphasucks.aea.port.network.payload.SkillDefsSyncS2C;
import dev.alphasucks.aea.port.network.payload.SkillStateSyncS2C;
import dev.alphasucks.aea.port.network.payload.SyncEntitySetsS2C;
import dev.alphasucks.aea.port.network.payload.SyncKeybindsS2C;
import dev.alphasucks.aea.port.network.payload.SyncTrackedEntitySetS2C;
import dev.alphasucks.aea.port.network.payload.TextDisplayS2C;
import dev.alphasucks.aea.port.network.payload.TickRateS2C;
import dev.alphasucks.aea.port.particle.ApoliParticles;
import dev.alphasucks.aea.port.power.PowerResources;
import dev.alphasucks.aea.port.power.builtin.EntitySetPower;
import dev.alphasucks.aea.port.power.builtin.ModifyEnchantmentLevelHandler;
import dev.alphasucks.aea.port.power.builtin.ModifyFogInterpolator;
import dev.alphasucks.aea.port.scale.ScaleSync;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_3675.class_307;
import net.minecraft.class_310;
import net.minecraft.class_3264;
import net.minecraft.class_3300;
import net.minecraft.class_5605;
import net.minecraft.class_5607;
import net.minecraft.class_591;

/**
 * AEA: the client half of Overgrown's Apoli initialiser, trimmed to the systems AEA ports.
 * Client power state comes from Apoli 2.9.2's synced component.
 */
public final class AeaPortClient {
    private static final class_304 SKILL_TREE_KEY = new class_304("key.apoli.skill_tree", class_307.field_1668, 75, "key.categories.apoli");
    private static boolean initialised;

    private AeaPortClient() {
    }

    public static void init() {
        if (initialised) {
            return;
        }
        initialised = true;
        KeyBindingHelper.registerKeyBinding(SKILL_TREE_KEY);
        EntityRendererRegistry.register(ApoliEntities.CUSTOM_PROJECTILE, CustomProjectileRenderer::new);
        ParticleFactoryRegistry.getInstance().register(ApoliParticles.CUSTOM, new CustomParticle.Provider());
        EntityModelLayerRegistry.registerModelLayer(SummonModelLayers.MINION, MinionModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(SummonModelLayers.CLONE, () -> class_5607.method_32110(class_591.method_32028(class_5605.field_27715, false), 64, 64));
        EntityModelLayerRegistry.registerModelLayer(SummonModelLayers.CLONE_SLIM, () -> class_5607.method_32110(class_591.method_32028(class_5605.field_27715, true), 64, 64));
        EntityRendererRegistry.register(ApoliEntities.MINION, MinionRenderer::new);
        EntityRendererRegistry.register(ApoliEntities.CLONE, CloneRenderer::new);
        EntitySetPower.setClientView(ClientEntitySets.VIEW);

        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> ClientLabelState.removeEntity(entity.method_5628()));
        PowerResources.setClientCooldownLookup((owner, powerId) -> owner == class_310.method_1551().field_1724 ? ClientPowerState.getCooldown(powerId) : 0);
        HeldKeys.setClientLookup((entity, key, grace) -> entity == class_310.method_1551().field_1724 && KeyPressWatcher.isLocalHeld(key, grace));
        KeyPressWatcher.setSender(keys -> {
            class_2540 buf = new class_2540(Unpooled.buffer());
            new KeyHeldC2S(keys).write(buf);
            ClientPlayNetworking.send(KeyHeldC2S.CHANNEL, buf);
        });
        KeyReleases.setSender(power -> {
            if (ClientPlayNetworking.canSend(PowerReleaseC2S.CHANNEL)) {
                class_2540 buf = new class_2540(Unpooled.buffer());
                new PowerReleaseC2S(power).write(buf);
                ClientPlayNetworking.send(PowerReleaseC2S.CHANNEL, buf);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(SyncEntitySetsS2C.CHANNEL, (mc, handler, buf, sender) -> {
            SyncEntitySetsS2C sets = SyncEntitySetsS2C.read(buf);
            mc.execute(() -> ClientEntitySets.apply(sets.powerId(), sets.members()));
        });
        ClientPlayNetworking.registerGlobalReceiver(SyncTrackedEntitySetS2C.CHANNEL, (mc, handler, buf, sender) -> {
            SyncTrackedEntitySetS2C sets = SyncTrackedEntitySetS2C.read(buf);
            mc.execute(() -> ClientEntitySets.applyTracked(sets.owner(), sets.powerId(), sets.members()));
        });
        ClientPlayNetworking.registerGlobalReceiver(PowerActivatedS2C.CHANNEL, (mc, handler, buf, sender) -> {
            PowerActivatedS2C payload = PowerActivatedS2C.read(buf);
            mc.execute(() -> ClientPowerState.setCooldown(payload.power(), payload.cooldown()));
        });
        ClientPlayNetworking.registerGlobalReceiver(SyncKeybindsS2C.CHANNEL, (mc, handler, buf, sender) -> {
            SyncKeybindsS2C payload = SyncKeybindsS2C.read(buf);
            mc.execute(() -> DynamicKeyMappingManager.applyKeybinds(payload.keybinds()));
        });
        ClientPlayNetworking.registerGlobalReceiver(AnchoredParticlesS2C.CHANNEL, (mc, handler, buf, sender) -> {
            AnchoredParticlesS2C payload = AnchoredParticlesS2C.read(buf);
            mc.execute(() -> AnchoredParticleClient.enqueue(payload));
        });
        ClientPlayNetworking.registerGlobalReceiver(ScaleSyncS2C.CHANNEL, (client, handler, buf, sender) -> {
            ScaleSyncS2C payload = ScaleSyncS2C.read(buf);
            client.execute(() -> {
                if (client.field_1687 != null) {
                    class_1297 entity = client.field_1687.method_8469(payload.entityId());
                    if (entity != null) {
                        ScaleSync.decode(entity, payload.data());
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(TickRateS2C.CHANNEL, (client, handler, buf, sender) -> {
            TickRateS2C payload = TickRateS2C.read(buf);
            client.execute(() -> ClientTickRates.set(payload.entityId(), payload.rate(), payload.baseRate()));
        });
        ClientPlayNetworking.registerGlobalReceiver(ApplyVelocityS2C.CHANNEL, (mc, handler, buf, sender) -> {
            ApplyVelocityS2C payload = ApplyVelocityS2C.read(buf);
            mc.execute(() -> {
                if (mc.field_1687 != null) {
                    class_1297 e = mc.field_1687.method_8469(payload.entityId());
                    if (e != null) {
                        class_243 delta = new class_243(payload.x(), payload.y(), payload.z());
                        e.method_18799(payload.set() ? delta : e.method_18798().method_1019(delta));
                    }
                }
            });
        });
        ClientDisguiseManager.install();
        ClientPlayNetworking.registerGlobalReceiver(DisguiseUpdateS2C.CHANNEL, (mc, handler, buf, sender) -> {
            DisguiseUpdateS2C payload = DisguiseUpdateS2C.read(buf);
            mc.execute(() -> payload.data().ifPresentOrElse(data -> ClientDisguiseManager.apply(payload.entityId(), data), () -> ClientDisguiseManager.remove(payload.entityId())));
        });
        ClientPlayNetworking.registerGlobalReceiver(TextDisplayS2C.CHANNEL, (mc, handler, buf, sender) -> {
            TextDisplayS2C payload = TextDisplayS2C.read(buf);
            mc.execute(() -> TextOverlayRenderer.apply(payload));
        });
        ClientPlayNetworking.registerGlobalReceiver(LabelUpdateS2C.CHANNEL, (mc, handler, buf, sender) -> {
            LabelUpdateS2C payload = LabelUpdateS2C.read(buf);
            mc.execute(() -> ClientLabelState.apply(payload.entityId(), payload.texts()));
        });
        ClientPlayNetworking.registerGlobalReceiver(ForceKeyS2C.CHANNEL, (mc, handler, buf, sender) -> {
            ForceKeyS2C payload = ForceKeyS2C.read(buf);
            mc.execute(() -> ForcedKeys.force(payload.key(), payload.duration(), payload.release()));
        });
        ClientPlayNetworking.registerGlobalReceiver(SetPerspectiveS2C.CHANNEL, (mc, handler, buf, sender) -> {
            SetPerspectiveS2C payload = SetPerspectiveS2C.read(buf);
            mc.execute(() -> CameraController.setPerspective(mc, payload.perspective()));
        });
        ClientPlayNetworking.registerGlobalReceiver(SkillDefsSyncS2C.CHANNEL, (mc, handler, buf, sender) -> {
            SkillDefsSyncS2C payload = SkillDefsSyncS2C.read(buf);
            mc.execute(() -> ClientSkillState.applyDefs(payload));
        });
        ClientPlayNetworking.registerGlobalReceiver(SkillStateSyncS2C.CHANNEL, (mc, handler, buf, sender) -> {
            SkillStateSyncS2C payload = SkillStateSyncS2C.read(buf);
            mc.execute(() -> ClientSkillState.applyState(payload));
        });
        ClientPlayNetworking.registerGlobalReceiver(RadialMenuOpenS2C.CHANNEL, (mc, handler, buf, sender) -> {
            RadialMenuOpenS2C payload = RadialMenuOpenS2C.read(buf);
            mc.execute(() -> mc.method_1507(new RadialMenuScreen(payload)));
        });
        ClientPlayNetworking.registerGlobalReceiver(RopeCreateS2C.CHANNEL, (mc, handler, buf, sender) -> {
            RopeCreateS2C payload = RopeCreateS2C.read(buf);
            mc.execute(() -> {
                if (mc.field_1687 != null) {
                    RopeClientManager.attach(payload, mc.field_1687);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(RopeDeleteS2C.CHANNEL, (mc, handler, buf, sender) -> {
            RopeDeleteS2C payload = RopeDeleteS2C.read(buf);
            mc.execute(() -> RopeClientManager.detach(payload.id()));
        });
        ClientPlayNetworking.registerGlobalReceiver(RopeVerletLengthS2C.CHANNEL, (mc, handler, buf, sender) -> {
            RopeVerletLengthS2C payload = RopeVerletLengthS2C.read(buf);
            mc.execute(() -> {
                VerletRopeState rope = RopeClientManager.get(payload.id());
                if (rope != null) {
                    rope.targetLength = payload.length();
                }
            });
        });

        ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public class_2960 getFabricId() {
                return Apoli.internal("client_caches");
            }

            @Override
            public void method_14491(class_3300 manager) {
                ParticleSheet.clearCache();
                ParticleTextures.clearCache();
                AnimatedTextures.clearCache();
                TextureFallbacks.clear();
            }
        });
        ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(CustomModelManager.INSTANCE);
        ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(AnimationManager.INSTANCE);

        ClientTickEvents.START_CLIENT_TICK.register(mc -> {
            if (mc.field_1724 != null && !mc.method_1493()) {
                ApoliKeyHandler.onClientTick();
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            ModifyEnchantmentLevelHandler.clientTick(mc.field_1724);
            CursorSpeedState.tick(mc);
            MouseMovementWatcher.clientTick(mc);
            if (mc.field_1724 != null && !mc.method_1493()) {
                while (SKILL_TREE_KEY.method_1436()) {
                    if (mc.field_1755 == null && ClientSkillState.hasAnyTree()) {
                        if (ClientPlayNetworking.canSend(RequestSkillStateC2S.CHANNEL)) {
                            ClientPlayNetworking.send(RequestSkillStateC2S.CHANNEL, PacketByteBufs.create());
                        }
                        mc.method_1507(new SkillTreeScreen());
                    }
                }
            }
            RopeClientManager.tick();
            TextOverlayRenderer.tick();
            ClientDisguiseManager.tick(mc);
            PlayerModelTypeReporter.tick(mc);
            CameraPerspectiveReporter.tick(mc);
            CameraController.clientTick(mc);
            ForcedKeys.tick();
            ModifyFogInterpolator.tick(mc.field_1724);
            dev.alphasucks.aea.port.client.ClientResolvedPowers.tick(mc);
        });
        WorldRenderEvents.AFTER_ENTITIES.register(RopeRenderer::render);
        HudRenderCallback.EVENT.register(TextOverlayRenderer::render);
        HudRenderCallback.EVENT.register(dev.alphasucks.aea.port.client.DevHudRenderer::render);
        ClientPlayNetworking.registerGlobalReceiver(dev.alphasucks.aea.port.network.payload.DevModeS2C.CHANNEL, (mc, handler, buf, sender) -> {
            dev.alphasucks.aea.port.network.payload.DevModeS2C mode = dev.alphasucks.aea.port.network.payload.DevModeS2C.read(buf);
            mc.execute(() -> dev.alphasucks.aea.port.client.ClientDevMode.set(mode.enabled()));
        });
        ClientPlayNetworking.registerGlobalReceiver(dev.alphasucks.aea.port.network.payload.SyncResolvedPowersS2C.CHANNEL, (mc, handler, buf, sender) -> {
            dev.alphasucks.aea.port.network.payload.SyncResolvedPowersS2C resolved = dev.alphasucks.aea.port.network.payload.SyncResolvedPowersS2C.read(buf);
            mc.execute(() -> dev.alphasucks.aea.port.client.ClientResolvedPowers.apply(resolved));
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(AeaPortClient::clearSession));
    }

    public static void clearSession() {
        DynamicKeyMappingManager.unregisterAll();
        ClientPowerState.clear();
        dev.alphasucks.aea.port.client.ClientResolvedPowers.clear();
        dev.alphasucks.aea.port.client.ClientDevMode.clear();
        KeyReleases.clear();
        ClientEntitySets.clear();
        ScreenCloseKeys.clear();
        CameraController.reset();
        CameraPerspectiveReporter.reset();
        TextOverlayRenderer.clear();
        ClientLabelState.clear();
        RopeClientManager.clear();
        KeyPressWatcher.reset();
        ApoliKeyMappings.reset();
        ForcedKeys.clear();
        BlockedKeys.clear();
        CursorSpeedState.reset();
        ClientDisguiseManager.clear();
        ClientSkillState.clear();
        ClientTickRates.clear();
    }
}
