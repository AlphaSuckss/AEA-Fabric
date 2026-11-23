package dev.alphasucks.aea.port.compat.pehkui;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.compat.ModCompat;
import dev.alphasucks.aea.port.scale.ScaleState;
import dev.alphasucks.aea.port.scale.ScaleType;
import dev.alphasucks.aea.port.scale.ScaleTypes;
import dev.alphasucks.aea.port.scale.Scales;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_1297;

public final class PehkuiBridge {
   public static final boolean PRESENT = ModCompat.classPresent("virtuoel.pehkui.api.ScaleTypes");
   private static boolean ready;
   private static boolean failed;
   private static Method getScaleData;
   private static Method setScale;
   private static Method getBaseScale;
   private static final Map<ScaleType, Object> MAPPED = new HashMap();
   private static boolean[] handled = new boolean[0];

   private PehkuiBridge() {
   }

   public static boolean ownsGeometry() {
      return PRESENT && wire();
   }

   private static boolean wire() {
      if (ready) {
         return !failed;
      }

      ready = true;

      try {
         Class<?> typesClass = Class.forName("virtuoel.pehkui.api.ScaleTypes");
         Class<?> typeClass = Class.forName("virtuoel.pehkui.api.ScaleType");
         Class<?> dataClass = Class.forName("virtuoel.pehkui.api.ScaleData");
         getScaleData = typeClass.getMethod("getScaleData", new Class[]{class_1297.class});
         setScale = dataClass.getMethod("setScale", new Class[]{float.class});
         getBaseScale = dataClass.getMethod("getBaseScale", new Class[0]);
         List<ScaleType> all = ScaleTypes.all();
         int i = 0;

         for (int n = all.size(); i < n; i++) {
            ScaleType type = (ScaleType)all.get(i);

            try {
               Field field = typesClass.getField(type.id().method_12832().toUpperCase(Locale.ROOT));
               Object value = field.get(null);
               if (typeClass.isInstance(value)) {
                  MAPPED.put(type, value);
               }
            } catch (NoSuchFieldException var9) {
            }
         }

         boolean[] owned = new boolean[all.size()];

         for (ScaleType type : MAPPED.keySet()) {
            owned[type.index()] = true;
         }

         handled = owned;
         failed = MAPPED.isEmpty();
      } catch (ReflectiveOperationException | RuntimeException e) {
         failed = true;
         Apoli.LOGGER.warn("[Apoli] Pehkui is installed but its scale API could not be bound; Apoli will apply its own scaling instead ({})", e.toString());
      }

      return !failed;
   }

   public static boolean handles(ScaleType type) {
      boolean[] owned = handled;
      int index = type.index();
      return index < owned.length && owned[index];
   }

   public static void push(class_1297 entity, ScaleState state) {
      if (ownsGeometry()) {
         if (!entity.method_37908().method_8608()) {
            float[] own = Scales.ownAll(entity, state);
            if (state.pushedScalesChanged(own)) {
               for (Entry<ScaleType, Object> entry : MAPPED.entrySet()) {
                  float value = own[((ScaleType)entry.getKey()).index()];

                  try {
                     Object data = getScaleData.invoke(entry.getValue(), new Object[]{entity});
                     if (data != null) {
                        float current = (Float)getBaseScale.invoke(data, new Object[0]);
                        if (!(Math.abs(current - value) < 1.0E-5F)) {
                           setScale.invoke(data, new Object[]{value});
                        }
                     }
                  } catch (ReflectiveOperationException | RuntimeException e) {
                     failed = true;
                     state.forgetDimensions();
                     Apoli.LOGGER.warn("[Apoli] Pehkui scale bridge failed, falling back to Apoli scaling ({})", e.toString());
                     return;
                  }
               }
            }
         }
      }
   }
}
