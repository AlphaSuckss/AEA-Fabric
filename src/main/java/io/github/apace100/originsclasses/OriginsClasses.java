package io.github.apace100.originsclasses;

import io.github.apace100.apoli.util.NamespaceAlias;
import io.github.apace100.originsclasses.effect.StealthEffect;
import io.github.apace100.originsclasses.power.ClassesPowerFactories;
import net.fabricmc.api.ModInitializer;
import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public class OriginsClasses implements ModInitializer {
   public static final String MODID = "origins-classes";
   public static boolean isClericEnchanting;

   public void onInitialize() {
      NamespaceAlias.addAlias("origins-classes", "apoli");
      ClassesPowerFactories.register();
      class_2378.method_10230(class_7923.field_41174, new class_2960("origins-classes", "stealth"), StealthEffect.INSTANCE);
      // AEA: the wandering-trader "obtainable items" cache used to survive /reload and world switches
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTING.register(server -> io.github.apace100.originsclasses.util.ItemUtil.invalidate());
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> io.github.apace100.originsclasses.util.ItemUtil.invalidate());
   }
}
