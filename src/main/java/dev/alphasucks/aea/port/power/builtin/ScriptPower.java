package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.data.Nbt;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.script.ApoliScripts;
import dev.alphasucks.aea.port.script.ScriptCtx;
import dev.alphasucks.aea.port.script.ApoliScripts.Kind;
import java.util.Optional;
import net.minecraft.class_2487;
import net.minecraft.class_2960;

public final class ScriptPower extends PowerType<ScriptPower.Config> {
   @Override
   public MapCodec<ScriptPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.optionalFieldOf("on_added").forGetter(ScriptPower.Config::onAdded),
               IdCodecs.ID.optionalFieldOf("on_removed").forGetter(ScriptPower.Config::onRemoved),
               Nbt.CODEC.optionalFieldOf("params").forGetter(c -> c.params.method_33133() ? Optional.empty() : Optional.of(new Nbt(c.params)))
            )
            .apply(i, (added, removed, params) -> new ScriptPower.Config(added, removed, (class_2487)params.map(Nbt::tag).orElseGet(class_2487::new)))
      );
   }

   public void onAdded(class_2960 powerId, ScriptPower.Config cfg, PowerContainer holder, class_2960 source) {
      cfg.onAdded.ifPresent(id -> fire(Kind.POWER_ADDED, id, powerId, cfg, holder));
   }

   public void onRemoved(class_2960 powerId, ScriptPower.Config cfg, PowerContainer holder, class_2960 source) {
      cfg.onRemoved.ifPresent(id -> fire(Kind.POWER_REMOVED, id, powerId, cfg, holder));
   }

   @Override
   public boolean ticksNonLivingEntities() {
      return true;
   }

   private static void fire(Kind kind, class_2960 scriptId, class_2960 powerId, ScriptPower.Config cfg, PowerContainer holder) {
      if (holder.rawOwner() != null && !holder.rawOwner().method_37908().method_8608()) {
         ApoliScripts.run(kind, scriptId, ScriptCtx.ofPower(holder, powerId, cfg.params));
      }
   }

   public record Config(Optional<class_2960> onAdded, Optional<class_2960> onRemoved, class_2487 params) {
   }
}
