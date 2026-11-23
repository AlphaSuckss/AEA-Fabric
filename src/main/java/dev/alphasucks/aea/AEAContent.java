package dev.alphasucks.aea;

import dev.alphasucks.aea.bridge.AeaContext;
import dev.alphasucks.aea.bridge.AeaPowerJson;
import dev.alphasucks.aea.bridge.AeaRegistrar;
import dev.alphasucks.aea.port.AeaPortServer;
import dev.alphasucks.aea.port.action.builtin.bientity.BiEntityActions;
import dev.alphasucks.aea.port.action.builtin.block.BlockActions;
import dev.alphasucks.aea.port.action.builtin.entity.EntityActions;
import dev.alphasucks.aea.port.action.builtin.item.ItemActions;
import dev.alphasucks.aea.port.action.builtin.meta.MetaActions;
import dev.alphasucks.aea.port.condition.builtin.bientity.BiEntityConditions;
import dev.alphasucks.aea.port.condition.builtin.block.BlockConditions;
import dev.alphasucks.aea.port.condition.builtin.damage.DamageConditions;
import dev.alphasucks.aea.port.condition.builtin.entity.EntityConditions;
import dev.alphasucks.aea.port.condition.builtin.item.ItemConditions;
import dev.alphasucks.aea.port.condition.builtin.meta.MetaConditions;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.PowerTypes;
import io.github.apace100.apoli.integration.PostPowerLoadCallback;
import io.github.apace100.apoli.integration.PostPowerReloadCallback;
import io.github.apace100.apoli.integration.PrePowerReloadCallback;

/** Registration hub for ported content; each batch hooks in here. */
public final class AEAContent {
    private static boolean initialised;
    private static boolean clientInitialised;

    private AEAContent() {
    }

    public static void init() {
        if (initialised) {
            return;
        }
        initialised = true;
        AeaContext.init();

        // Overgrown's registrations are collected first and pushed into Apoli 2.9.2 once both
        // Apoli and AEA have initialised (see AeaRegistrar).
        PowerTypes.bootstrap();
        EntityActions.register();
        BiEntityActions.register();
        BlockActions.register();
        ItemActions.register();
        MetaActions.registerEntity();
        MetaActions.registerBiEntity();
        MetaActions.registerBlock();
        MetaActions.registerItem();
        EntityConditions.register();
        BiEntityConditions.register();
        BlockConditions.register();
        ItemConditions.register();
        DamageConditions.register();
        MetaConditions.registerEntity();
        MetaConditions.registerBiEntity();
        MetaConditions.registerBlock();
        MetaConditions.registerItem();
        MetaConditions.registerDamage();
        MetaConditions.registerFluid();
        MetaConditions.registerBiome();

        PrePowerReloadCallback.EVENT.register(AeaPowerJson::clear);
        PostPowerLoadCallback.EVENT.register((powerId, factoryId, isSubPower, json, powerType) -> AeaPowerJson.record(powerId, json, isSubPower));
        PostPowerReloadCallback.EVENT.register(ApoliPowers::invalidate);

        dev.alphasucks.aea.power.VelocityImmunityPower.register();
        dev.alphasucks.aea.port.skill.SkillDataAttachment.init();
        AeaPortServer.init();
        dev.alphasucks.aea.mobai.MobAiContent.init();
        dev.alphasucks.aea.gui.AeaGui.init();
        dev.alphasucks.aea.port.power.PowerTypeRegistry.register(dev.alphasucks.aea.hitbox.HitboxPartPower.ID, new dev.alphasucks.aea.hitbox.HitboxPartPower());
        dev.alphasucks.aea.hitbox.HitboxParts.init();
        dev.alphasucks.aea.packassets.PackTextures.init();
        dev.alphasucks.aea.packassets.PackAssets.init();
        dev.alphasucks.aea.command.AeaCommands.init();
        dev.alphasucks.aea.port.power.PowerTypeRegistry.register(dev.alphasucks.aea.effect.MilkImmunityPower.ID, new dev.alphasucks.aea.effect.MilkImmunityPower());
        dev.alphasucks.aea.port.power.PowerTypeRegistry.register(dev.alphasucks.aea.seeinvisible.SeeInvisiblePower.ID, new dev.alphasucks.aea.seeinvisible.SeeInvisiblePower());
        dev.alphasucks.aea.port.power.PowerTypeRegistry.register(dev.alphasucks.aea.spawnpoint.PreventSpawnPointPower.ID, new dev.alphasucks.aea.spawnpoint.PreventSpawnPointPower());
        dev.alphasucks.aea.gamerule.GameRuleCondition.register();
        dev.alphasucks.aea.mindcontrol.MindControl.register();
        dev.alphasucks.aea.redstone.RedstonePulseTypes.register();
        if (dev.alphasucks.aea.compat.spell_engine.SpellEngineVersion.supported()) {
            dev.alphasucks.aea.compat.spell_engine.SpellEngineTypes.register();
        } else if (dev.alphasucks.aea.compat.spell_engine.SpellEngineVersion.installed()) {
            AEA.LOGGER.warn("[AEA] This Spell Engine version isn't supported on 1.20.1 (AEA supports 0.x); AEA's spell types are off.");
        }
        dev.alphasucks.aea.gamerule.DataGameRules.init();
        dev.alphasucks.aea.originsport.AeaOriginsPort.init();
        dev.alphasucks.aea.port.effects.AeaPortEffects.init();
        AeaRegistrar.onAeaInitialized();
    }

    public static void initClient() {
        if (clientInitialised) {
            return;
        }
        clientInitialised = true;
        dev.alphasucks.aea.client.AeaClientContent.init();
        dev.alphasucks.aea.gui.client.AeaGuiClient.init();
        dev.alphasucks.aea.hitbox.client.ClientHitboxParts.init();
        dev.alphasucks.aea.packassets.client.ClientPackTextures.init();
        dev.alphasucks.aea.packassets.client.ClientPackAssets.init();
        dev.alphasucks.aea.gamerule.client.ClientGameRules.init();
        dev.alphasucks.aea.mindcontrol.client.MindControlClient.init();
        dev.alphasucks.aea.originsport.AeaOriginsPort.initClient();
        dev.alphasucks.aea.port.effects.AeaPortEffects.initClient();
    }
}
