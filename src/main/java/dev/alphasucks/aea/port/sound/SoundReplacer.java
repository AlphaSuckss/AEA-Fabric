package dev.alphasucks.aea.port.sound;

import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.sound.WeightedSound;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.builtin.ReplaceSoundPower.Config;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_5819;
import org.jetbrains.annotations.Nullable;

public final class SoundReplacer {
   private static final Comparator<Config> BY_PRIORITY = Comparator.comparingInt(Config::priority).reversed();

   private SoundReplacer() {
   }

   public static boolean hasEmission(@Nullable class_1297 entity) {
      return has(entity, ApoliIds.REPLACE_SOUND_EMISSION);
   }

   public static boolean hasReception(@Nullable class_1297 entity) {
      return has(entity, ApoliIds.REPLACE_SOUND_RECEPTION);
   }

   private static boolean has(@Nullable class_1297 entity, class_2960 typeId) {
      if (entity == null) {
         return false;
      }

      PowerContainer container = PowerContainer.of(entity);
      return container != null && !container.isEmpty() && !container.powersOfType(typeId).isEmpty();
   }

   public static void emit(@Nullable class_1297 source, class_3414 sound, float volume, float pitch, SoundReplacer.Emitter emitter) {
      apply(ApoliIds.REPLACE_SOUND_EMISSION, source, sound, volume, pitch, true, emitter);
   }

   public static void emitLocally(@Nullable class_1297 source, class_3414 sound, float volume, float pitch, SoundReplacer.Emitter emitter) {
      apply(ApoliIds.REPLACE_SOUND_EMISSION, source, sound, volume, pitch, false, emitter);
   }

   public static void receive(@Nullable class_1297 listener, class_3414 sound, float volume, float pitch, SoundReplacer.Emitter emitter) {
      apply(ApoliIds.REPLACE_SOUND_RECEPTION, listener, sound, volume, pitch, true, emitter);
   }

   private static void apply(
      class_2960 typeId, @Nullable class_1297 holder, class_3414 sound, float volume, float pitch, boolean runActions, SoundReplacer.Emitter emitter
   ) {
      List<Config> entries = collect(typeId, holder);
      if (entries.isEmpty()) {
         emitter.play(sound, volume, pitch);
      } else {
         class_5819 random = holder.method_37908().field_9229;
         class_2960 soundId = sound.method_14833();
         EntityCtx ctx = null;
         WeightedSound replacement = null;
         WeightedSound[] layers = null;
         int layerCount = 0;

         for (int i = 0; i < entries.size(); i++) {
            Config cfg = (Config)entries.get(i);
            WeightedSound picked = cfg.sounds().find(soundId, random);
            if (picked != null) {
               if (runActions && cfg.entityAction().isPresent()) {
                  if (ctx == null) {
                     ctx = new EntityCtx(holder, holder.method_37908());
                  }

                  ((EntityAction)cfg.entityAction().get()).run(ctx);
               }

               if (cfg.replace()) {
                  replacement = picked;
                  break;
               }

               if (layers == null) {
                  layers = new WeightedSound[entries.size() - i];
               }

               layers[layerCount++] = picked;
            }
         }

         if (replacement == null) {
            emitter.play(sound, volume, pitch);
         } else {
            emit(emitter, replacement, volume, pitch, false);
         }

         for (int i = 0; i < layerCount; i++) {
            emit(emitter, layers[i], volume, pitch, true);
         }
      }
   }

   private static void emit(SoundReplacer.Emitter emitter, WeightedSound picked, float volume, float pitch, boolean layered) {
      if (!picked.mutes()) {
         class_3414 resolved = resolve(picked.id());
         if (resolved != null) {
            if (layered) {
               emitter.layer(resolved, picked.volumeOr(volume), picked.pitchOr(pitch));
            } else {
               emitter.play(resolved, picked.volumeOr(volume), picked.pitchOr(pitch));
            }
         }
      }
   }

   private static List<Config> collect(class_2960 typeId, @Nullable class_1297 holder) {
      if (holder == null) {
         return List.of();
      }

      PowerContainer container = PowerContainer.of(holder);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(typeId);
         if (powers.isEmpty()) {
            return List.of();
         }

         List<Config> entries = null;
         EntityCtx ctx = null;

         for (int i = 0; i < powers.size(); i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof Config cfg) {
                  if (power.condition().isPresent()) {
                     if (ctx == null) {
                        ctx = new EntityCtx(holder, holder.method_37908());
                     }

                     if (!((EntityCondition)power.condition().get()).test(ctx)) {
                        continue;
                     }
                  }

                  if (entries == null) {
                     entries = new ArrayList(2);
                  }

                  entries.add(cfg);
               }
            }
         }

         if (entries == null) {
            return List.of();
         }

         if (entries.size() > 1) {
            entries.sort(BY_PRIORITY);
         }

         return entries;
      } else {
         return List.of();
      }
   }

   @Nullable
   private static class_3414 resolve(String id) {
      class_2960 location = class_2960.method_12829(id);
      return location == null ? null : Sounds.event(location);
   }

   @FunctionalInterface
   public interface Emitter {
      void play(class_3414 var1, float var2, float var3);

      default void layer(class_3414 sound, float volume, float pitch) {
         this.play(sound, volume, pitch);
      }
   }
}
