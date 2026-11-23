package dev.alphasucks.aea.port.effects;

import java.util.Set;
import net.minecraft.class_2960;

public interface RuntimeMobEffectRegistry {
   /** Unregisters every {@link CustomMobEffect} whose id is not in {@code keep}. */
   void apoli$unregisterCustom(Set<class_2960> keep);

   CustomMobEffect apoli$register(CustomMobEffect effect);
}
