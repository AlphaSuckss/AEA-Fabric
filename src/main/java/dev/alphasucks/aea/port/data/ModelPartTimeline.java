package dev.alphasucks.aea.port.data;

import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.builtin.ModifyModelPartsPower;
import dev.alphasucks.aea.port.power.builtin.ModifyModelPartsPower.Config;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import net.minecraft.class_1309;
import org.jetbrains.annotations.Nullable;

public final class ModelPartTimeline {
   private final Map<class_1309, ModelPartTimeline.State> states = new WeakHashMap();

   public List<ModelPartTimeline.Slot> update(@Nullable class_1309 entity, double now) {
      if (entity == null) {
         return List.of();
      }

      ModelPartTimeline.State state = this.states.isEmpty() ? null : (ModelPartTimeline.State)this.states.get(entity);
      if (state == null && !ModifyModelPartsPower.has(entity)) {
         return List.of();
      }

      if (state == null) {
         state = new ModelPartTimeline.State();
         this.states.put(entity, state);
      } else if (state.lastSample == now) {
         return state.slots;
      }

      state.lastSample = now;
      state.now = now;
      state.poseMask = 0;
      List<ModelPartTimeline.Slot> slots = state.slots;

      for (int i = 0; i < slots.size(); i++) {
         ((ModelPartTimeline.Slot)slots.get(i)).seen = false;
      }

      PowerLookup.forEach(entity, ModifyModelPartsPower.CANONICAL, Config.class, state);

      for (int i = slots.size() - 1; i >= 0; i--) {
         ModelPartTimeline.Slot slot = (ModelPartTimeline.Slot)slots.get(i);
         if (!slot.seen && slot.active) {
            slot.active = false;
            slot.since = now;
            slot.weightAtEdge = slot.weight;
         }

         float weight = weight(slot, now);
         if (!slot.active && weight <= 0.0F) {
            slots.remove(i);
         } else {
            slot.weight = weight;
            float elapsed = (float)(now - slot.startedAt);
            slot.value = slot.transformation.sample(elapsed, entity);
            if (slot.transformation.type() == ModelPartTransformation.Type.REACH) {
               slot.hasTarget = slot.transformation.sampleTarget(elapsed, slot.target);
            }
         }
      }

      if (slots.isEmpty() && state.poseMask == 0 && !ModifyModelPartsPower.has(entity)) {
         this.states.remove(entity);
      }

      return slots;
   }

   public boolean overridesPose(@Nullable class_1309 entity, double now) {
      if (entity == null) {
         return false;
      }

      if (this.states.isEmpty() && !ModifyModelPartsPower.has(entity)) {
         return false;
      }

      this.update(entity, now);
      ModelPartTimeline.State state = (ModelPartTimeline.State)this.states.get(entity);
      return state != null && ModifyModelPartsPower.masked(state.poseMask, entity.method_18376());
   }

   private static float weight(ModelPartTimeline.Slot slot, double now) {
      ModelPartTransformation transformation = slot.transformation;
      float elapsed = (float)(now - slot.since);
      if (slot.active) {
         float duration = transformation.duration();
         if (!(duration <= 0.0F) && !(elapsed >= duration)) {
            float eased = transformation.easing().apply(elapsed / duration);
            return slot.weightAtEdge + (1.0F - slot.weightAtEdge) * eased;
         } else {
            return 1.0F;
         }
      } else {
         float duration = transformation.fadeOutDuration();
         return !(duration <= 0.0F) && !(elapsed >= duration) ? slot.weightAtEdge * (1.0F - transformation.easing().apply(elapsed / duration)) : 0.0F;
      }
   }

   public static final class Slot {
      private final ModelPartTransformation transformation;
      private int perspectiveMask;
      private boolean seen;
      private boolean active;
      private double since;
      private double startedAt;
      private float weightAtEdge;
      private float weight;
      private float value;
      private final float[] target = new float[3];
      private boolean hasTarget;

      private Slot(ModelPartTransformation transformation) {
         this.transformation = transformation;
      }

      public ModelPartTransformation transformation() {
         return this.transformation;
      }

      public float weight() {
         return this.weight;
      }

      public float value() {
         return this.value;
      }

      public boolean hasTarget() {
         return this.hasTarget;
      }

      public float targetX() {
         return this.target[0];
      }

      public float targetY() {
         return this.target[1];
      }

      public float targetZ() {
         return this.target[2];
      }

      public boolean rendersIn(boolean firstPerson) {
         return Perspective.masked(this.perspectiveMask, firstPerson);
      }
   }

   private static final class State implements Consumer<Config> {
      private final List<ModelPartTimeline.Slot> slots = new ArrayList(4);
      private double lastSample = Double.NaN;
      private double now;
      private int poseMask;

      public void accept(Config config) {
         this.poseMask = this.poseMask | config.overridePoseMask();
         List<ModelPartTransformation> transformations = config.transformations();

         for (int i = 0; i < transformations.size(); i++) {
            this.mark((ModelPartTransformation)transformations.get(i), config.perspectiveMask());
         }
      }

      private void mark(ModelPartTransformation transformation, int inheritedMask) {
         int mask = transformation.perspectiveMask();
         if (mask == -1) {
            mask = inheritedMask;
         }

         for (int i = 0; i < this.slots.size(); i++) {
            ModelPartTimeline.Slot slot = (ModelPartTimeline.Slot)this.slots.get(i);
            if (slot.transformation == transformation) {
               slot.seen = true;
               slot.perspectiveMask = mask;
               if (!slot.active) {
                  slot.active = true;
                  slot.since = this.now;
                  slot.startedAt = this.now;
                  slot.weightAtEdge = slot.weight;
               }

               return;
            }
         }

         ModelPartTimeline.Slot slot = new ModelPartTimeline.Slot(transformation);
         slot.perspectiveMask = mask;
         slot.seen = true;
         slot.active = true;
         slot.since = this.now;
         slot.startedAt = this.now;
         this.slots.add(slot);
      }
   }
}
