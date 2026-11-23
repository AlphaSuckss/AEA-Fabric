package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.data.ModelParts;
import dev.alphasucks.aea.port.mixin.custom_model.AgeableListModelPartsInvoker;
import dev.alphasucks.aea.port.mixin.custom_model.ModelPartChildrenAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4592;
import net.minecraft.class_4595;
import net.minecraft.class_5597;
import net.minecraft.class_583;
import net.minecraft.class_630;

@Environment(EnvType.CLIENT)
public final class ModelPartNames {
   private static final class_630[] NO_CHAIN = new class_630[0];
   private static final ModelPartNames.Named EMPTY = new ModelPartNames.Named(Map.of(), Map.of());
   private static final Map<class_630, String> BAKED = Collections.synchronizedMap(new WeakHashMap());
   private static final Map<class_583<?>, ModelPartNames.Named> BY_MODEL = Collections.synchronizedMap(new WeakHashMap());

   private ModelPartNames() {
   }

   public static void recordBaked(class_630 parent) {
      Map<String, class_630> children = ((ModelPartChildrenAccessor)(Object)parent).apoli$children();
      if (!children.isEmpty()) {
         for (Entry<String, class_630> entry : children.entrySet()) {
            BAKED.put((class_630)entry.getValue(), (String)entry.getKey());
         }
      }
   }

   public static Map<String, class_630> of(class_583<?> model) {
      return named(model).parts();
   }

   public static ModelPartNames.Named named(class_583<?> model) {
      ModelPartNames.Named known = (ModelPartNames.Named)BY_MODEL.get(model);
      if (known != null) {
         return known;
      }

      ModelPartNames.Named built = build(model);
      BY_MODEL.put(model, built);
      return built;
   }

   private static ModelPartNames.Named build(class_583<?> model) {
      Map<String, class_630> parts = new LinkedHashMap();
      Map<class_630, class_630[]> chains = new IdentityHashMap();
      List<class_630> path = new ArrayList(8);
      if (model instanceof class_5597<?> hierarchical) {
         class_630 root = hierarchical.method_32008();
         path.add(root);
         walk(root, path, parts, chains);
      } else if (model instanceof class_4592<?> ageable) {
         AgeableListModelPartsInvoker invoker = (AgeableListModelPartsInvoker)ageable;
         addTopLevel(invoker.apoli$headParts(), path, parts, chains);
         addTopLevel(invoker.apoli$bodyParts(), path, parts, chains);
      } else if (model instanceof class_4595<?> list) {
         addTopLevel(list.method_22960(), path, parts, chains);
      }

      return parts.isEmpty() ? EMPTY : new ModelPartNames.Named(Collections.unmodifiableMap(parts), chains);
   }

   private static void addTopLevel(Iterable<class_630> top, List<class_630> path, Map<String, class_630> parts, Map<class_630, class_630[]> chains) {
      for (class_630 part : top) {
         String name = (String)BAKED.get(part);
         if (name != null && parts.putIfAbsent(ModelParts.normalize(name), part) == null) {
            chains.put(part, NO_CHAIN);
         }

         path.add(part);
         walk(part, path, parts, chains);
         path.remove(path.size() - 1);
      }
   }

   private static void walk(class_630 parent, List<class_630> path, Map<String, class_630> parts, Map<class_630, class_630[]> chains) {
      Map<String, class_630> children = ((ModelPartChildrenAccessor)(Object)parent).apoli$children();

      for (Entry<String, class_630> entry : children.entrySet()) {
         class_630 child = (class_630)entry.getValue();
         if (parts.putIfAbsent(ModelParts.normalize((String)entry.getKey()), child) == null) {
            chains.put(child, (class_630[])path.toArray(new class_630[0]));
         }

         path.add(child);
         walk(child, path, parts, chains);
         path.remove(path.size() - 1);
      }
   }

   public record Named(Map<String, class_630> parts, Map<class_630, class_630[]> chains) {
      public class_630[] chain(class_630 part) {
         class_630[] chain = (class_630[])this.chains.get(part);
         return chain == null ? ModelPartNames.NO_CHAIN : chain;
      }
   }
}
