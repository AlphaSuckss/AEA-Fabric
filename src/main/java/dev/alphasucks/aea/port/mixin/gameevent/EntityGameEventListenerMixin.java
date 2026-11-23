package dev.alphasucks.aea.port.mixin.gameevent;

import dev.alphasucks.aea.port.power.builtin.EntityGameEventListenerAccess;
import dev.alphasucks.aea.port.power.builtin.GameEventListenerPower.State;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_5715;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1297.class)
public abstract class EntityGameEventListenerMixin implements EntityGameEventListenerAccess {
   @Unique
   @Nullable
   private Map<class_2960, State> apoli$gameEventStates;

   @Nullable
   @Override
   public Map<class_2960, State> apoli$getGameEventStates() {
      return this.apoli$gameEventStates;
   }

   @Override
   public void apoli$setGameEventStates(@Nullable Map<class_2960, State> map) {
      this.apoli$gameEventStates = map;
   }

   @Inject(method = "method_42147", at = @At("HEAD"))
   private void apoli$updateGameEventListeners(BiConsumer<class_5715<?>, class_3218> callback, CallbackInfo ci) {
      if (this.apoli$gameEventStates != null && !this.apoli$gameEventStates.isEmpty()) {
         class_1297 self = (class_1297)(Object)this;
         if (self.method_37908() instanceof class_3218 serverLevel) {
            for (State state : this.apoli$gameEventStates.values()) {
               callback.accept(state.getEventHandler(), serverLevel);
            }
         }
      }
   }
}
