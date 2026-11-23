package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.TextComponent;
import dev.alphasucks.aea.port.entity.LabelManager;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_2561;
import net.minecraft.class_2564;
import net.minecraft.class_2703;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3542;
import net.minecraft.class_2703.class_5893;

public final class ModifyLabelRenderPower extends PowerType<ModifyLabelRenderPower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("modify_label_render");

   @Override
   public MapCodec<ModifyLabelRenderPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               TextComponent.CODEC.optionalFieldOf("text").forGetter(ModifyLabelRenderPower.Config::text),
               ModifyLabelRenderPower.LabelMode.CODEC
                  .optionalFieldOf("render_mode", ModifyLabelRenderPower.LabelMode.DEFAULT)
                  .forGetter(ModifyLabelRenderPower.Config::renderMode),
               Codec.INT.optionalFieldOf("tick_rate", 20).forGetter(ModifyLabelRenderPower.Config::tickRate),
               Codec.INT.optionalFieldOf("priority", 0).forGetter(ModifyLabelRenderPower.Config::priority),
               LoggedOptionalField.strict("entity_condition", EntityCondition.CODEC).forGetter(ModifyLabelRenderPower.Config::entityCondition),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ModifyLabelRenderPower.Config::bientityCondition),
               LoggedOptionalField.of("before_parse_action", EntityAction.CODEC).forGetter(ModifyLabelRenderPower.Config::beforeParseAction),
               LoggedOptionalField.of("after_parse_action", EntityAction.CODEC).forGetter(ModifyLabelRenderPower.Config::afterParseAction),
               Codec.BOOL.optionalFieldOf("override_chat_name", false).forGetter(ModifyLabelRenderPower.Config::overrideChatName),
               Codec.BOOL.optionalFieldOf("override_tab_name", false).forGetter(ModifyLabelRenderPower.Config::overrideTabName)
            )
            .apply(i, ModifyLabelRenderPower.Config::new)
      );
   }

   public void tick(class_2960 powerId, ModifyLabelRenderPower.Config cfg, PowerContainer holder) {
      class_1309 owner = holder.owner();
      if (owner != null && owner.method_37908() instanceof class_3218 level) {
         int var12 = Math.max(1, cfg.tickRate);
         if (owner.field_6012 % var12 == Math.floorMod(powerId.hashCode() + owner.method_5628(), var12)) {
            Power power = ApoliPowers.get(powerId);
            boolean active = power != null && (power.condition().isEmpty() || ((EntityCondition)power.condition().get()).test(new EntityCtx(owner, level)));
            boolean changed;
            if (!active) {
               changed = LabelManager.setText(owner, powerId, null);
            } else {
               EntityCtx ctx = new EntityCtx(owner, level);
               cfg.beforeParseAction.ifPresent(a -> a.run(ctx));
               class_2561 resolved = resolve(cfg, owner);
               changed = LabelManager.setText(owner, powerId, resolved);
               if (changed) {
                  cfg.afterParseAction.ifPresent(a -> a.run(ctx));
               }
            }

            if (changed) {
               boolean namesChanged = recomputeNameOverrides(owner);
               LabelManager.broadcast(owner);
               if (namesChanged && owner instanceof class_3222 player) {
                  refreshTabName(player);
               }
            }
         }
      }
   }

   public void onRemoved(class_2960 powerId, ModifyLabelRenderPower.Config cfg, PowerContainer holder, class_2960 source) {
      class_1309 owner = holder.owner();
      if (owner != null && !owner.method_37908().method_8608() && !holder.hasPower(powerId)) {
         if (LabelManager.setText(owner, powerId, null)) {
            boolean namesChanged = recomputeNameOverrides(owner);
            LabelManager.broadcast(owner);
            if (namesChanged && owner instanceof class_3222 player) {
               refreshTabName(player);
            }
         }
      }
   }

   private static class_2561 resolve(ModifyLabelRenderPower.Config cfg, class_1309 owner) {
      if (cfg.text.isEmpty()) {
         return class_2561.method_43473();
      }

      try {
         return class_2564.method_10881(owner.method_5671().method_9206(2).method_9217(), (class_2561)cfg.text.get(), owner, 0);
      } catch (Exception e) {
         return (class_2561)cfg.text.get();
      }
   }

   private static boolean recomputeNameOverrides(class_1309 owner) {
      if (!(owner instanceof class_3222)) {
         return false;
      }

      class_2561 chatName = null;
      class_2561 tabName = null;
      int chatPriority = Integer.MIN_VALUE;
      int tabPriority = Integer.MIN_VALUE;
      Map<class_2960, class_2561> texts = LabelManager.textsFor(owner.method_5667());

      for (Entry<class_2960, class_2561> entry : texts.entrySet()) {
         Power power = ApoliPowers.get((class_2960)entry.getKey());
         if (power != null && power.config() instanceof ModifyLabelRenderPower.Config cfg) {
            class_2561 text = (class_2561)entry.getValue();
            if (!text.getString().isEmpty()) {
               if (cfg.overrideChatName() && cfg.priority() > chatPriority) {
                  chatName = text;
                  chatPriority = cfg.priority();
               }

               if (cfg.overrideTabName() && cfg.priority() > tabPriority) {
                  tabName = text;
                  tabPriority = cfg.priority();
               }
            }
         }
      }

      return LabelManager.setNameOverrides(owner, chatName, chatPriority, tabName, tabPriority);
   }

   public static void refreshTabName(class_3222 player) {
      player.method_51469().method_8503().method_3760().method_14581(new class_2703(EnumSet.of(class_5893.field_29139), List.of(player)));
   }

   public static void onEntityGone(class_1297 entity) {
      LabelManager.onEntityGone(entity.method_5667());
   }

   public record Config(
      Optional<class_2561> text,
      ModifyLabelRenderPower.LabelMode renderMode,
      int tickRate,
      int priority,
      Optional<EntityCondition> entityCondition,
      Optional<BiEntityCondition> bientityCondition,
      Optional<EntityAction> beforeParseAction,
      Optional<EntityAction> afterParseAction,
      boolean overrideChatName,
      boolean overrideTabName
   ) {
   }

   public enum LabelMode implements class_3542 {
      DEFAULT("default"),
      HIDE_PARTIALLY("hide_partially"),
      HIDE_COMPLETELY("hide_completely");

      public static final Codec<ModifyLabelRenderPower.LabelMode> CODEC = class_3542.method_28140(ModifyLabelRenderPower.LabelMode::values);
      private final String name;

      LabelMode(String name) {
         this.name = name;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }
}
