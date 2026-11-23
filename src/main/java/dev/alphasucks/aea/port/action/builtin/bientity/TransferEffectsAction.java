package dev.alphasucks.aea.port.action.builtin.bientity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.AttributeModifierHelper;
import dev.alphasucks.aea.port.data.IdOrTag;
import dev.alphasucks.aea.port.effects.CustomEffect;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1309;
import net.minecraft.class_4081;
import net.minecraft.class_6880;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import org.jetbrains.annotations.Nullable;

public final class TransferEffectsAction implements ActionType<BiEntityCtx, TransferEffectsAction.Cfg> {
   @Override
   public MapCodec<TransferEffectsAction.Cfg> codec() {
      return AliasingMapCodec.wrap(
         RecordCodecBuilder.mapCodec(
            i -> i.group(
                  TransferAction.Mode.CODEC.optionalFieldOf("mode", TransferAction.Mode.STEAL).forGetter(TransferEffectsAction.Cfg::mode),
                  Codec.BOOL.optionalFieldOf("copy", false).forGetter(TransferEffectsAction.Cfg::copy),
                  SingleOrList.of(IdOrTag.codec(class_7924.field_41208)).optionalFieldOf("effects").forGetter(TransferEffectsAction.Cfg::effects),
                  SingleOrList.of(CustomEffect.CATEGORY_CODEC).optionalFieldOf("category").forGetter(TransferEffectsAction.Cfg::categories),
                  AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("duration_modifier", List.of()).forGetter(TransferEffectsAction.Cfg::durationModifiers),
                  AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("amplifier_modifier", List.of()).forGetter(TransferEffectsAction.Cfg::amplifierModifiers),
                  AttributeModifier.LIST_OR_SINGLE
                     .optionalFieldOf("donor_duration_modifier", List.of())
                     .forGetter(TransferEffectsAction.Cfg::donorDurationModifiers)
               )
               .apply(i, TransferEffectsAction.Cfg::new)
         ),
         Map.of("effect", "effects")
      );
   }

   public void run(TransferEffectsAction.Cfg cfg, BiEntityCtx ctx) {
      boolean steal = cfg.mode == TransferAction.Mode.STEAL;
      class_1309 donor = steal ? ctx.livingTarget() : ctx.livingActor();
      class_1309 recipient = steal ? ctx.livingActor() : ctx.livingTarget();
      if (donor != null && recipient != null && donor != recipient && !donor.method_37908().method_8608()) {
         Collection<class_1293> active = donor.method_6026();
         if (!active.isEmpty()) {
            List<class_1293> moving = new ArrayList(active.size());

            for (class_1293 instance : active) {
               if (cfg.matches(instance)) {
                  moving.add(instance);
               }
            }

            for (int i = 0; i < moving.size(); i++) {
               class_1293 original = (class_1293)moving.get(i);
               class_1293 given = modified(original, cfg.durationModifiers, cfg.amplifierModifiers, recipient);
               if (given != null && recipient.method_6049(given)) {
                  recipient.method_37222(given, donor);
                  if (!cfg.copy) {
                     donor.method_6016(original.method_5579());
                  } else if (!cfg.donorDurationModifiers.isEmpty()) {
                     class_1293 kept = modified(original, cfg.donorDurationModifiers, List.of(), donor);
                     if (kept == null) {
                        donor.method_6016(original.method_5579());
                     } else {
                        donor.method_26082(kept, recipient);
                     }
                  }
               }
            }
         }
      }
   }

   @Nullable
   private static class_1293 modified(class_1293 original, List<AttributeModifier> duration, List<AttributeModifier> amplifier, class_1309 holder) {
      int ticks = original.method_5584();
      if (!duration.isEmpty() && !original.method_48559()) {
         ticks = (int)Math.round(AttributeModifierHelper.apply(ticks, duration, holder));
         if (ticks <= 0) {
            return null;
         }
      }

      int level = original.method_5578();
      if (!amplifier.isEmpty()) {
         level = Math.max(0, Math.min(255, (int)Math.round(AttributeModifierHelper.apply(level, amplifier, holder))));
      }

      return new class_1293(original.method_5579(), ticks, level, original.method_5591(), original.method_5581(), original.method_5592());
   }

   public record Cfg(
      TransferAction.Mode mode,
      boolean copy,
      Optional<List<IdOrTag<class_1291>>> effects,
      Optional<List<class_4081>> categories,
      List<AttributeModifier> durationModifiers,
      List<AttributeModifier> amplifierModifiers,
      List<AttributeModifier> donorDurationModifiers
   ) {
      boolean matches(class_1293 instance) {
         if (this.categories.isPresent() && !((List)this.categories.get()).contains(instance.method_5579().method_18792())) {
            return false;
         }

         if (this.effects.isEmpty()) {
            return true;
         }

         class_6880<class_1291> holder = class_7923.field_41174.method_47983(instance.method_5579());
         List<IdOrTag<class_1291>> wanted = (List<IdOrTag<class_1291>>)this.effects.get();

         for (int i = 0; i < wanted.size(); i++) {
            if (((IdOrTag)wanted.get(i)).matches(holder)) {
               return true;
            }
         }

         return false;
      }
   }
}
