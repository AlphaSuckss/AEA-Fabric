package dev.alphasucks.aea.port.client;

import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.builtin.ModifyLabelRenderPower.Config;
import dev.alphasucks.aea.port.power.builtin.ModifyLabelRenderPower.LabelMode;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class ClientLabelState {
   private static final Map<Integer, Map<class_2960, class_2561>> LABELS = new ConcurrentHashMap();

   private ClientLabelState() {
   }

   public static void apply(int entityId, Map<class_2960, class_2561> texts) {
      if (texts.isEmpty()) {
         LABELS.remove(entityId);
      } else {
         LABELS.put(entityId, texts);
      }
   }

   public static void removeEntity(int entityId) {
      LABELS.remove(entityId);
   }

   public static void clear() {
      LABELS.clear();
   }

   @Nullable
   public static ClientLabelState.Pick pick(class_1297 entity, @Nullable class_1297 viewer) {
      Map<class_2960, class_2561> texts = (Map<class_2960, class_2561>)LABELS.get(entity.method_5628());
      if (texts == null) {
         return null;
      }

      class_2561 bestText = null;
      LabelMode bestMode = null;
      int bestPriority = Integer.MIN_VALUE;

      for (Entry<class_2960, class_2561> entry : texts.entrySet()) {
         Power power = ApoliPowers.get((class_2960)entry.getKey());
         if (power != null
            && power.config() instanceof Config cfg
            && (bestMode == null || cfg.priority() > bestPriority)
            && viewerConditionsPass(cfg, entity, viewer)) {
            bestPriority = cfg.priority();
            bestMode = cfg.renderMode();
            class_2561 text = (class_2561)entry.getValue();
            bestText = text.getString().isEmpty() && text.method_10855().isEmpty() ? null : text;
         }
      }

      return bestMode == null ? null : new ClientLabelState.Pick(bestText, bestMode);
   }

   private static boolean viewerConditionsPass(Config cfg, class_1297 entity, @Nullable class_1297 viewer) {
      if (cfg.entityCondition().isEmpty() && cfg.bientityCondition().isEmpty()) {
         return true;
      } else if (viewer == null) {
         return false;
      } else if (cfg.entityCondition().isPresent() && !((EntityCondition)cfg.entityCondition().get()).test(EntityCtx.of(viewer, viewer.method_37908()))) {
         return false;
      } else {
         return cfg.bientityCondition().isPresent()
            ? ((BiEntityCondition)cfg.bientityCondition().get()).test(new BiEntityCtx(viewer, entity, entity.method_37908()))
            : true;
      }
   }

   public record Pick(@Nullable class_2561 text, LabelMode mode) {
   }
}
