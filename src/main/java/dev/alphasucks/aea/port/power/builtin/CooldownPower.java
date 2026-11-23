package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.HudRender;
import dev.alphasucks.aea.port.dev.DevMode;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerContainerImpl;
import dev.alphasucks.aea.port.power.builtin.ResourcePower.Cfg;
import dev.alphasucks.aea.port.power.builtin.ResourcePower.OnChange;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.class_1297;
import net.minecraft.class_2960;

public final class CooldownPower extends ResourcePower {
   private static final MapCodec<Cfg> CFG_CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            Expression.INT_OR_EXPR.fieldOf("cooldown").forGetter(cfg -> (Expression)cfg.max().orElse(Expression.constant(0.0))),
            HudRender.CODEC.optionalFieldOf("hud_render", HudRender.DONT_RENDER).forGetter(Cfg::hudRender),
            Codec.BOOL.optionalFieldOf("persistent", true).forGetter(Cfg::persistent),
            Expression.INT_OR_EXPR
               .optionalFieldOf("start_value", Expression.constant(0.0))
               .forGetter(cfg -> (Expression)cfg.startValue().orElse(Expression.constant(0.0))),
            LoggedOptionalField.of("min_action", EntityAction.CODEC).forGetter(Cfg::minAction),
            LoggedOptionalField.of("max_action", EntityAction.CODEC).forGetter(Cfg::maxAction),
            LoggedOptionalField.of("on_change", Codec.list(OnChange.CODEC), List.of()).forGetter(Cfg::onChange)
         )
         .apply(
            i,
            (cooldown, hud, persistent, start, minAction, maxAction, onChange) -> new Cfg(
               Optional.of(Expression.constant(0.0)),
               Optional.of(cooldown),
               Optional.of(start),
               hud,
               true,
               false,
               minAction,
               maxAction,
               onChange,
               persistent,
               1
            )
         )
   );

   @Override
   public MapCodec<Cfg> configCodec() {
      return CFG_CODEC;
   }

   @Override
   public boolean isCooldown() {
      return true;
   }

   @Override
   public void onAdded(class_2960 powerId, Cfg cfg, PowerContainer holder, class_2960 source) {
      if (holder instanceof PowerContainerImpl impl) {
         if (!impl.getAuxInt(powerId).isPresent()) {
            impl.setAuxInt(powerId, deadline(holder, this.startTicks(cfg, holder, powerId)));
         }
      }
   }

   @Override
   protected void resetOnLoad(class_2960 powerId, Cfg cfg, PowerContainerImpl impl) {
      if (!impl.getAuxInt(powerId).isPresent() || !cfg.persistent()) {
         impl.setAuxInt(powerId, deadline(impl, this.startTicks(cfg, impl, powerId)));
      }
   }

   @Override
   public void tick(class_2960 powerId, Cfg cfg, PowerContainer holder) {
      if (holder instanceof PowerContainerImpl impl) {
         int stored = impl.getAuxIntOr(powerId, 0);
         if (stored != 0) {
            class_1297 owner = holder.rawOwner();
            int remaining = stored - now(holder);
            if (remaining > 0) {
               if (!cfg.onChange().isEmpty()) {
                  this.fireBoundaryActions(powerId, cfg, owner, remaining + 1, remaining, Integer.MIN_VALUE, Integer.MAX_VALUE);
               }
            } else {
               impl.setAuxInt(powerId, 0);
               this.fireBoundaryActions(powerId, cfg, owner, 1, 0, 0, this.length(cfg, holder, powerId));
            }
         }
      }
   }

   @Override
   public OptionalInt readResource(class_2960 powerId, Cfg cfg, PowerContainer holder) {
      if (!holder.hasPower(powerId)) {
         return OptionalInt.empty();
      }

      if (DevMode.isEnabled(holder.rawOwner())) {
         return OptionalInt.of(0);
      }

      int stored = holder.getAuxIntOr(powerId, 0);
      return stored == 0 ? OptionalInt.of(0) : OptionalInt.of(Math.max(0, stored - now(holder)));
   }

   @Override
   public OptionalInt writeResource(class_2960 powerId, Cfg cfg, PowerContainer holder, int value) {
      if (holder instanceof PowerContainerImpl impl) {
         if (!holder.hasPower(powerId)) {
            return OptionalInt.empty();
         }

         int length = this.length(cfg, holder, powerId);
         int target = Math.max(0, Math.min(value, Math.max(length, 0)));
         int prev = this.readResource(powerId, cfg, holder).orElse(0);
         impl.setAuxInt(powerId, deadline(holder, target));
         if (target != prev) {
            this.fireBoundaryActions(powerId, cfg, holder.rawOwner(), prev, target, 0, length);
         }

         return OptionalInt.of(target);
      } else {
         return OptionalInt.empty();
      }
   }

   @Override
   public OptionalInt readResourceAt(class_2960 powerId, Cfg cfg, PowerContainer holder, int slot) {
      return slot == 0 ? this.readResource(powerId, cfg, holder) : OptionalInt.empty();
   }

   @Override
   public OptionalInt writeResourceAt(class_2960 powerId, Cfg cfg, PowerContainer holder, int slot, int value) {
      return slot == 0 ? this.writeResource(powerId, cfg, holder, value) : OptionalInt.empty();
   }

   @Override
   public int resourceIndexOf(class_2960 powerId, Cfg cfg, PowerContainer holder, int value) {
      OptionalInt current = this.readResource(powerId, cfg, holder);
      return current.isPresent() && current.getAsInt() == value ? 0 : -1;
   }

   private int length(Cfg cfg, PowerContainer holder, class_2960 powerId) {
      return this.currentMax(cfg, holder, powerId);
   }

   private int startTicks(Cfg cfg, PowerContainer holder, class_2960 powerId) {
      int start = this.evalStartValue(cfg, holder);
      return Math.max(0, Math.min(start, Math.max(this.length(cfg, holder, powerId), 0)));
   }

   private static int deadline(PowerContainer holder, int ticks) {
      if (ticks <= 0) {
         return 0;
      }

      int at = now(holder) + ticks;
      return at == 0 ? 1 : at;
   }

   private static int now(PowerContainer holder) {
      return (int)holder.rawOwner().method_37908().method_8510();
   }
}
