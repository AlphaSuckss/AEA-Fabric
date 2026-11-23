package dev.alphasucks.aea.port.client.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1001;
import net.minecraft.class_1002;
import net.minecraft.class_1004;
import net.minecraft.class_1005;
import net.minecraft.class_1006;
import net.minecraft.class_3684;
import net.minecraft.class_3885;
import net.minecraft.class_4004;
import net.minecraft.class_4043;
import net.minecraft.class_4073;
import net.minecraft.class_4506;
import net.minecraft.class_4507;
import net.minecraft.class_4606;
import net.minecraft.class_4607;
import net.minecraft.class_4691;
import net.minecraft.class_5167;
import net.minecraft.class_944;
import net.minecraft.class_970;
import net.minecraft.class_972;
import net.minecraft.class_973;
import net.minecraft.class_974;
import net.minecraft.class_975;
import net.minecraft.class_976;
import net.minecraft.class_977;
import net.minecraft.class_978;
import net.minecraft.class_979;
import net.minecraft.class_980;
import net.minecraft.class_983;
import net.minecraft.class_986;
import net.minecraft.class_988;
import net.minecraft.class_989;
import net.minecraft.class_990;
import net.minecraft.class_991;
import net.minecraft.class_992;
import net.minecraft.class_994;
import net.minecraft.class_996;
import net.minecraft.class_997;
import net.minecraft.class_998;

@Environment(EnvType.CLIENT)
public final class FeatureRenderers {
   private static final String[] NONE = new String[0];
   private static final Map<String, Class<?>> MAPPINGS = new LinkedHashMap();
   private static final ClassValue<String[]> KEYS = new ClassValue<String[]>() {
      protected String[] computeValue(Class<?> type) {
         List<String> out = new ArrayList(2);

         for (Entry<String, Class<?>> entry : FeatureRenderers.MAPPINGS.entrySet()) {
            if (((Class)entry.getValue()).isAssignableFrom(type)) {
               out.add((String)entry.getKey());
            }
         }

         return out.isEmpty() ? FeatureRenderers.NONE : (String[])out.toArray(String[]::new);
      }
   };

   private FeatureRenderers() {
   }

   private static void map(String name, Class<?> type) {
      MAPPINGS.put(name, type);
   }

   public static String[] keysFor(Class<?> layerClass) {
      return (String[])KEYS.get(layerClass);
   }

   public static Set<String> names() {
      return Collections.unmodifiableSet(MAPPINGS.keySet());
   }

   static {
      map("armor", class_970.class);
      map("cape", class_972.class);
      map("cat_collar", class_3684.class);
      map("creeper_power", class_974.class);
      map("deadmau5", class_978.class);
      map("dolphin_held_item", class_977.class);
      map("drowned_overlay", class_980.class);
      map("elytra", class_979.class);
      map("enderman_block", class_975.class);
      map("energy_swirl_overlay", class_4607.class);
      map("eyes", class_4606.class);
      map("fox_held_item", class_4043.class);
      map("head", class_976.class);
      map("held_item", class_989.class);
      map("horse_armor", class_4073.class);
      map("horse_marking", class_5167.class);
      map("iron_golem_crack", class_4691.class);
      map("iron_golem_flower", class_986.class);
      map("llama_decor", class_988.class);
      map("mooshroom_mushroom", class_991.class);
      map("panda_held_item", class_990.class);
      map("saddle", class_992.class);
      map("sheep_wool", class_994.class);
      map("shoulder_parrot", class_983.class);
      map("shulker_head", class_944.class);
      map("slime_overlay", class_997.class);
      map("snowman_pumpkin", class_996.class);
      map("stray_overlay", class_1002.class);
      map("stuck_arrows", class_973.class);
      map("stuck_objects", class_4507.class);
      map("stuck_stingers", class_4506.class);
      map("trident_riptide", class_998.class);
      map("tropical_fish_color", class_1001.class);
      map("villager_clothing", class_3885.class);
      map("villager_held_item", class_4004.class);
      map("witch_item", class_1005.class);
      map("wither_armor", class_1004.class);
      map("wolf_collar", class_1006.class);
   }
}
