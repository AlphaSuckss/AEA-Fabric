package dev.alphasucks.aea.port.power;

import com.mojang.serialization.Codec;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.alias.AliasDefault;
import dev.alphasucks.aea.port.alias.AliasingOptions;
import dev.alphasucks.aea.port.compat.ModCompat;
import dev.alphasucks.aea.port.compat.accessory.power.ActionOnAccessoryChangePower;
import dev.alphasucks.aea.port.compat.accessory.power.ModifyAccessorySlotsPower;
import dev.alphasucks.aea.port.compat.accessory.power.PreventAccessoryEquipPower;
import dev.alphasucks.aea.port.compat.accessory.power.PreventAccessoryUnequipPower;
import dev.alphasucks.aea.port.compat.hardcorerevival.power.ActionOnKnockoutPower;
import dev.alphasucks.aea.port.compat.hardcorerevival.power.ActionOnRevivePower;
import dev.alphasucks.aea.port.compat.icarus.WingsPower;
import dev.alphasucks.aea.port.compat.walkers.power.ShapePowers.ActionOnShapeAbilityUse;
import dev.alphasucks.aea.port.compat.walkers.power.ShapePowers.ActionOnShapeChange;
import dev.alphasucks.aea.port.compat.walkers.power.ShapePowers.PreventShapeAbilityUse;
import dev.alphasucks.aea.port.compat.walkers.power.ShapePowers.PreventShapeChange;
import dev.alphasucks.aea.port.data.RenderMode;
import dev.alphasucks.aea.port.power.builtin.ActionOnBlockPlacePower;
import dev.alphasucks.aea.port.power.builtin.ActionOnBreedPower;
import dev.alphasucks.aea.port.power.builtin.ActionOnCollisionPower;
import dev.alphasucks.aea.port.power.builtin.ActionOnDeathPower;
import dev.alphasucks.aea.port.power.builtin.ActionOnItemPickupPower;
import dev.alphasucks.aea.port.power.builtin.ActionOnKeyPressPower;
import dev.alphasucks.aea.port.power.builtin.ActionOnKeySequencePower;
import dev.alphasucks.aea.port.power.builtin.ActionOnKillPower;
import dev.alphasucks.aea.port.power.builtin.ActionOnMouseMovementPower;
import dev.alphasucks.aea.port.power.builtin.ActionOnScrollWheelPower;
import dev.alphasucks.aea.port.power.builtin.ActionOnSendingMessagePower;
import dev.alphasucks.aea.port.power.builtin.ActionOnTamePower;
import dev.alphasucks.aea.port.power.builtin.ActionOnUsePower;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower;
import dev.alphasucks.aea.port.power.builtin.DisableSlotPower;
import dev.alphasucks.aea.port.power.builtin.EdibleItemPower;
import dev.alphasucks.aea.port.power.builtin.EmissivePower;
import dev.alphasucks.aea.port.power.builtin.EntitySetPower;
import dev.alphasucks.aea.port.power.builtin.ExtraTradesPower;
import dev.alphasucks.aea.port.power.builtin.FluidVisionPower;
import dev.alphasucks.aea.port.power.builtin.FunctionPower;
import dev.alphasucks.aea.port.power.builtin.GameEventListenerPower;
import dev.alphasucks.aea.port.power.builtin.IgnoreFluidPower;
import dev.alphasucks.aea.port.power.builtin.InfiniteTradesPower;
import dev.alphasucks.aea.port.power.builtin.ModifyAnvilPower;
import dev.alphasucks.aea.port.power.builtin.ModifyBlockStuckSpeedPower;
import dev.alphasucks.aea.port.power.builtin.ModifyBouncinessPower;
import dev.alphasucks.aea.port.power.builtin.ModifyCameraPower;
import dev.alphasucks.aea.port.power.builtin.ModifyCameraSubmersionPower;
import dev.alphasucks.aea.port.power.builtin.ModifyCursorSpeedPower;
import dev.alphasucks.aea.port.power.builtin.ModifyDamagePower;
import dev.alphasucks.aea.port.power.builtin.ModifyEnchantabilityPower;
import dev.alphasucks.aea.port.power.builtin.ModifyEnchantmentLevelPower;
import dev.alphasucks.aea.port.power.builtin.ModifyFogPower;
import dev.alphasucks.aea.port.power.builtin.ModifyFovPower;
import dev.alphasucks.aea.port.power.builtin.ModifyHearingRangePower;
import dev.alphasucks.aea.port.power.builtin.ModifyLabelRenderPower;
import dev.alphasucks.aea.port.power.builtin.ModifyModelPartsPower;
import dev.alphasucks.aea.port.power.builtin.ModifyPlayerModelPower;
import dev.alphasucks.aea.port.power.builtin.ModifyProjectileDivergencePower;
import dev.alphasucks.aea.port.power.builtin.ModifyResourceChangePower;
import dev.alphasucks.aea.port.power.builtin.ModifyTickRatePower;
import dev.alphasucks.aea.port.power.builtin.ModifyTypeTagPower;
import dev.alphasucks.aea.port.power.builtin.ModifyUseSlowdownPower;
import dev.alphasucks.aea.port.power.builtin.PosePower;
import dev.alphasucks.aea.port.power.builtin.PowerStoragePower;
import dev.alphasucks.aea.port.power.builtin.PreventBeeAngerPower;
import dev.alphasucks.aea.port.power.builtin.PreventBlockPlacePower;
import dev.alphasucks.aea.port.power.builtin.PreventEntitySelectionPower;
import dev.alphasucks.aea.port.power.builtin.PreventItemPickupPower;
import dev.alphasucks.aea.port.power.builtin.PreventKeyPressPower;
import dev.alphasucks.aea.port.power.builtin.PreventLevelEventPower;
import dev.alphasucks.aea.port.power.builtin.PreventPowersPower;
import dev.alphasucks.aea.port.power.builtin.PreventTeleportPower;
import dev.alphasucks.aea.port.power.builtin.PreventUsePower;
import dev.alphasucks.aea.port.power.builtin.ReceiveActionPower;
import dev.alphasucks.aea.port.power.builtin.ReceiveConditionPower;
import dev.alphasucks.aea.port.power.builtin.ReplaceLootTablePower;
import dev.alphasucks.aea.port.power.builtin.ReplaceSoundEmissionPower;
import dev.alphasucks.aea.port.power.builtin.ReplaceSoundReceptionPower;
import dev.alphasucks.aea.port.power.builtin.ScalePower;
import dev.alphasucks.aea.port.power.builtin.ScareMobsPower;
import dev.alphasucks.aea.port.power.builtin.ScriptPower;
import dev.alphasucks.aea.port.power.builtin.ShowBothArmsPower;
import dev.alphasucks.aea.port.power.builtin.SimplePower;
import dev.alphasucks.aea.port.power.builtin.SolidHitboxPower;
import dev.alphasucks.aea.port.power.builtin.SprintingPower;
import dev.alphasucks.aea.port.power.builtin.WaterBreathingPower;
import dev.alphasucks.aea.port.power.builtin.ZoomPower;
import java.util.Map;
import net.minecraft.class_8103;

/** AEA: filtered copy, registers only types Apoli 2.9.2 lacks. */
public final class PowerTypes {
   public static final ActionOnKeyPressPower ACTION_ON_KEY_PRESS = new ActionOnKeyPressPower();
   public static final ActionOnUsePower ACTION_ON_USE = new ActionOnUsePower();
   public static final PreventUsePower PREVENT_USE = new PreventUsePower();
   public static final ModifyDamagePower MODIFY_DAMAGE = new ModifyDamagePower();

   private PowerTypes() {
   }

   public static void bootstrap() {
      PowerTypeRegistry.register(Apoli.id("action_on_key_press"), ACTION_ON_KEY_PRESS, AliasingOptions.builder().addTypeAlias(Apoli.id("active_self")).build());
      PowerTypeRegistry.register(
         Apoli.id("action_on_key_sequence"), new ActionOnKeySequencePower(), AliasingOptions.builder().addTypeAlias("sync:action_on_key_sequence").build()
      );
      PowerTypeRegistry.register(
         Apoli.id("action_on_scroll_wheel"), new ActionOnScrollWheelPower(), AliasingOptions.builder().addTypeAlias(Apoli.id("action_on_scroll")).build()
      );
      PowerTypeRegistry.register(Apoli.id("entity_set"), new EntitySetPower());
      PowerTypeRegistry.register(
         Apoli.id("modify_type_tag"), new ModifyTypeTagPower(), AliasingOptions.builder().addTypeAlias(Apoli.id("entity_group")).build()
      );
      PowerTypeRegistry.register(Apoli.id("function"), new FunctionPower());
      PowerTypeRegistry.register(
         Apoli.id("action_on_use"),
         ACTION_ON_USE,
         AliasingOptions.builder().addTypeAlias(Apoli.id("action_on_entity_use")).addTypeAlias(Apoli.id("action_on_being_used")).build()
      );
      PowerTypeRegistry.register(
         Apoli.id("modify_damage"),
         MODIFY_DAMAGE,
         AliasingOptions.builder().addTypeAlias(Apoli.id("modify_damage_dealt")).addTypeAlias(Apoli.id("modify_damage_taken")).build()
      );
      PowerTypeRegistry.register(
         Apoli.id("simple"), new SimplePower(), AliasingOptions.builder().addTypeAlias(Apoli.id("dummy")).addTypeAlias(Apoli.id("nothing")).build()
      );
      PowerTypeRegistry.register(Apoli.id("prevent_teleport"), new PreventTeleportPower());
      PowerTypeRegistry.register(Apoli.id("disable_slot"), new DisableSlotPower(), AliasingOptions.builder().addTypeAlias(Apoli.id("restrict_slot")).build());
      PowerTypeRegistry.register(
         Apoli.id("modify_cursor_speed"),
         new ModifyCursorSpeedPower(),
         AliasingOptions.builder()
            .addTypeAlias(Apoli.id("modify_mouse_sensitivity"))
            .addTypeAlias(Apoli.id("modify_mouse_speed"))
            .addTypeAlias(Apoli.id("modify_look_sensitivity"))
            .build()
      );
      PowerTypeRegistry.register(Apoli.id("modify_enchantment_level"), new ModifyEnchantmentLevelPower());
      PowerTypeRegistry.register(Apoli.id("modify_block_stuck_speed"), new ModifyBlockStuckSpeedPower());
      PowerTypeRegistry.register(Apoli.id("scare_mobs"), new ScareMobsPower());
      PowerTypeRegistry.register(Apoli.id("water_breathing"), new WaterBreathingPower());
      PowerTypeRegistry.register(Apoli.id("modify_resource_change"), new ModifyResourceChangePower());
      PowerTypeRegistry.register(Apoli.id("modify_use_slowdown"), new ModifyUseSlowdownPower());
      PowerTypeRegistry.register(
         Apoli.id("prevent_use"),
         PREVENT_USE,
         AliasingOptions.builder().addTypeAlias(Apoli.id("prevent_entity_use")).addTypeAlias(Apoli.id("prevent_being_used")).build()
      );
      PowerTypeRegistry.register(Apoli.id("prevent_block_place"), new PreventBlockPlacePower());
      PowerTypeRegistry.register(Apoli.id("prevent_item_pickup"), new PreventItemPickupPower());
      PowerTypeRegistry.register(Apoli.id("prevent_key_press"), new PreventKeyPressPower());
      PowerTypeRegistry.register(Apoli.id("scale"), new ScalePower(), AliasingOptions.builder().renameField("scale_type", "scale_types").build());
      PowerTypeRegistry.register(Apoli.id("solid_hitbox"), new SolidHitboxPower());
      PowerTypeRegistry.register(
         Apoli.id("sprinting"),
         new SprintingPower(),
         AliasingOptions.builder().addTypeAlias(Apoli.id("force_sprint")).addTypeAlias(Apoli.id("forced_sprinting")).build()
      );
      PowerTypeRegistry.register(Apoli.id("action_on_block_place"), new ActionOnBlockPlacePower());
      PowerTypeRegistry.register(Apoli.id("action_on_death"), new ActionOnDeathPower());
      PowerTypeRegistry.register(
         Apoli.id("action_on_kill"), new ActionOnKillPower(), AliasingOptions.builder().addTypeAlias(Apoli.id("self_action_on_kill")).build()
      );
      PowerTypeRegistry.register(Apoli.id("action_on_item_pickup"), new ActionOnItemPickupPower());
      PowerTypeRegistry.register(Apoli.id("action_on_tame"), new ActionOnTamePower());
      PowerTypeRegistry.register(Apoli.id("action_on_breed"), new ActionOnBreedPower());
      PowerTypeRegistry.register(Apoli.id("modify_projectile_divergence"), new ModifyProjectileDivergencePower());
      PowerTypeRegistry.register(Apoli.id("modify_enchantability"), new ModifyEnchantabilityPower());
      PowerTypeRegistry.register(Apoli.id("infinite_trades"), new InfiniteTradesPower());
      PowerTypeRegistry.register(Apoli.id("extra_trades"), new ExtraTradesPower());
      PowerTypeRegistry.register(Apoli.id("modify_anvil"), new ModifyAnvilPower());
      PowerTypeRegistry.register(Apoli.id("prevent_level_event"), new PreventLevelEventPower());
      PowerTypeRegistry.register(Apoli.id("action_on_collision"), new ActionOnCollisionPower());
      PowerTypeRegistry.register(Apoli.id("replace_sound_emission"), new ReplaceSoundEmissionPower());
      PowerTypeRegistry.register(Apoli.id("replace_sound_reception"), new ReplaceSoundReceptionPower());
      PowerTypeRegistry.register(Apoli.id("ignore_fluid"), new IgnoreFluidPower(), AliasingOptions.builder().addTypeAlias(Apoli.id("ignore_water")).build());
      PowerTypeRegistry.register(Apoli.id("replace_loot_table"), new ReplaceLootTablePower());
      PowerTypeRegistry.register(Apoli.id("prevent_powers"), new PreventPowersPower());
      PowerTypeRegistry.register(Apoli.id("modify_model_parts"), new ModifyModelPartsPower());
      PowerTypeRegistry.register(
         Apoli.id("custom_model_render"), new CustomModelRenderPower(), AliasingOptions.builder().addTypeAlias(Apoli.id("energy_swirl")).build()
      );
      PowerTypeRegistry.registerAliasFieldRenames(Apoli.id("energy_swirl"), Map.of("size", "scale", "speed", "scroll_speed"));
      PowerTypeRegistry.registerAliasDefaults(
         Apoli.id("energy_swirl"),
         AliasDefault.of("render_as_overlay", Codec.BOOL, true),
         AliasDefault.of("render_type", RenderMode.CODEC, RenderMode.ENERGY_SWIRL),
         AliasDefault.of("texture_location", Codec.STRING, "minecraft:textures/entity/wither/wither_armor.png"),
         AliasDefault.of("scroll_speed", Codec.FLOAT, 0.01F),
         AliasDefault.of("red", Codec.FLOAT, 0.5F),
         AliasDefault.of("green", Codec.FLOAT, 0.5F),
         AliasDefault.of("blue", Codec.FLOAT, 0.5F)
      );
      PowerTypeRegistry.register(Apoli.id("modify_hearing_range"), new ModifyHearingRangePower());
      PowerTypeRegistry.register(Apoli.id("action_on_sending_message"), new ActionOnSendingMessagePower());
      PowerTypeRegistry.register(Apoli.id("pose"), new PosePower());
      PowerTypeRegistry.register(Apoli.id("modify_player_model"), new ModifyPlayerModelPower());
      PowerTypeRegistry.register(Apoli.id("show_both_arms"), new ShowBothArmsPower());
      PowerTypeRegistry.register(Apoli.id("emissive"), new EmissivePower());
      PowerTypeRegistry.register(
         Apoli.id("modify_label_render"), new ModifyLabelRenderPower(), AliasingOptions.builder().addTypeAlias("sync:modify_label_render").build()
      );
      PowerTypeRegistry.register(Apoli.id("power_storage"), new PowerStoragePower());
      PowerTypeRegistry.register(Apoli.id("edible_item"), new EdibleItemPower());
      PowerTypeRegistry.register(Apoli.id("modify_tick_rate"), new ModifyTickRatePower());
      PowerTypeRegistry.register(Apoli.id("action_on_mouse_movement"), new ActionOnMouseMovementPower());
      PowerTypeRegistry.register(Apoli.id("game_event_listener"), new GameEventListenerPower());
      if (ModCompat.ICARUS) {
         PowerTypeRegistry.register(Apoli.id("wings"), new WingsPower());
      }
      if (ModCompat.anyAccessory()) {
         PowerTypeRegistry.register(
            Apoli.id("action_on_accessory_change"),
            new ActionOnAccessoryChangePower(),
            AliasingOptions.builder().addTypeAlias(Apoli.id("action_on_trinket_change")).build()
         );
         PowerTypeRegistry.register(
            Apoli.id("prevent_accessory_equip"),
            new PreventAccessoryEquipPower(),
            AliasingOptions.builder().addTypeAlias(Apoli.id("prevent_trinket_equip")).build()
         );
         PowerTypeRegistry.register(
            Apoli.id("prevent_accessory_unequip"),
            new PreventAccessoryUnequipPower(),
            AliasingOptions.builder().addTypeAlias(Apoli.id("prevent_trinket_unequip")).build()
         );
         PowerTypeRegistry.register(
            Apoli.id("modify_accessory_slots"),
            new ModifyAccessorySlotsPower(),
            AliasingOptions.builder().addTypeAlias(Apoli.id("modify_trinket_slot")).addTypeAlias(Apoli.id("modify_trinket_slots")).build()
         );
      }
      if (ModCompat.HARDCORE_REVIVAL) {
         PowerTypeRegistry.register(Apoli.id("action_on_knockout"), new ActionOnKnockoutPower());
         PowerTypeRegistry.register(Apoli.id("action_on_revive"), new ActionOnRevivePower());
      }
      PowerTypeRegistry.register(Apoli.id("script"), new ScriptPower());
      PowerTypeRegistry.register(
         Apoli.id("receive_action"), new ReceiveActionPower(), AliasingOptions.builder().addTypeAlias("shappoli:receive_action").build()
      );
      PowerTypeRegistry.register(
         Apoli.id("receive_condition"), new ReceiveConditionPower(), AliasingOptions.builder().addTypeAlias("shappoli:receive_condition").build()
      );
      PowerTypeRegistry.register(
         Apoli.id("fluid_vision"),
         new FluidVisionPower(),
         AliasingOptions.builder().addTypeAlias(Apoli.id("lava_vision")).renameField("s", "start").renameField("v", "end").build()
      );
      PowerTypeRegistry.register(Apoli.id("modify_camera"), new ModifyCameraPower());
      PowerTypeRegistry.register(Apoli.id("modify_fov"), new ModifyFovPower());
      PowerTypeRegistry.register(Apoli.id("zoom"), new ZoomPower());
      PowerTypeRegistry.register(
         Apoli.id("modify_camera_submersion"),
         new ModifyCameraSubmersionPower(),
         AliasingOptions.builder().addTypeAlias(Apoli.id("modify_camera_submersion_type")).build()
      );
      if (ModCompat.WALKERS) {
         PowerTypeRegistry.register(
            Apoli.id("action_on_shape_change"),
            new ActionOnShapeChange(),
            AliasingOptions.builder()
               .addTypeAlias(Apoli.id("action_on_morph"))
               .addTypeAlias("shappoli:action_on_shape_change")
               .addTypeAlias("shappoli:action_on_morph")
               .build()
         );
         PowerTypeRegistry.register(
            Apoli.id("action_on_shape_ability_use"),
            new ActionOnShapeAbilityUse(),
            AliasingOptions.builder().addTypeAlias("shappoli:action_on_shape_ability_use").build()
         );
         PowerTypeRegistry.register(
            Apoli.id("prevent_shape_change"),
            new PreventShapeChange(),
            AliasingOptions.builder()
               .addTypeAlias(Apoli.id("prevent_morph"))
               .addTypeAlias("shappoli:prevent_shape_change")
               .addTypeAlias("shappoli:prevent_morph")
               .build()
         );
         PowerTypeRegistry.register(
            Apoli.id("prevent_shape_ability_use"),
            new PreventShapeAbilityUse(),
            AliasingOptions.builder().addTypeAlias("shappoli:prevent_shape_ability_use").build()
         );
      }
      PowerTypeRegistry.register(
         Apoli.id("prevent_entity_selection"),
         new PreventEntitySelectionPower(),
         AliasingOptions.builder().addTypeAlias("apugli:prevent_entity_selection").build()
      );
      PowerTypeRegistry.register(Apoli.id("prevent_bee_anger"), new PreventBeeAngerPower());
      PowerTypeRegistry.register(
         Apoli.id("modify_bounciness"), new ModifyBouncinessPower(), AliasingOptions.builder().addTypeAlias(Apoli.id("modify_block_bounciness")).build()
      );
      PowerTypeRegistry.register(Apoli.id("modify_fog"), new ModifyFogPower(), AliasingOptions.builder().addTypeAlias(Apoli.id("blindness")).build());
   }

   private static AliasDefault<Boolean> selfGlowTarget(boolean value) {
      return AliasDefault.of("self_glow_target", Codec.BOOL, value);
   }

   private static AliasDefault<Boolean> targetUsed(boolean value) {
      return AliasDefault.of("target_used", Codec.BOOL, value);
   }

   private static AliasDefault<String> entityActionTarget(String side) {
      return AliasDefault.of("entity_action_target", Codec.STRING, side);
   }
}
