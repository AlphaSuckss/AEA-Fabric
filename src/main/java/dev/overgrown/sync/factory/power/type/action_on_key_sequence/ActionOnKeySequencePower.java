package dev.overgrown.sync.factory.power.type.action_on_key_sequence;

import com.google.gson.JsonSyntaxException;
import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.condition.entity.key_pressed.utils.KeyPressManager;
import dev.overgrown.sync.factory.power.type.action_on_key_sequence.data.FunctionalKey;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.HudRendered;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.Active.Key;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.data.SerializableData.Instance;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_2487;
import net.minecraft.class_2520;
import net.minecraft.class_3222;

public class ActionOnKeySequencePower extends Power implements HudRendered {
   public static final SerializableDataType<FunctionalKey> FUNCTIONAL_KEY = new SerializableDataType<>(
      FunctionalKey.class,
      (buf, fk) -> {
         Key ak = new Key();
         ak.key = fk.key();
         ak.continuous = fk.continuous();
         ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY.send(buf, ak);
         boolean hasAction = fk.action() != null;
         buf.writeBoolean(hasAction);
         if (hasAction) {
            ApoliDataTypes.ENTITY_ACTION.send(buf, fk.action());
         }
      },
      buf -> {
         Key ak = ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY.receive(buf);
         boolean hasAction = buf.readBoolean();
         Consumer<class_1297> action = hasAction ? (Consumer)ApoliDataTypes.ENTITY_ACTION.receive(buf) : null;
         return new FunctionalKey(ak.key, ak.continuous, action);
      },
      json -> {
         if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            return new FunctionalKey(json.getAsString(), false, null);
         } else if (json.isJsonObject()) {
            SerializableData sd = new SerializableData()
               .add("key", SerializableDataTypes.STRING)
               .add("continuous", SerializableDataTypes.BOOLEAN, false)
               .add("action", ApoliDataTypes.ENTITY_ACTION, null);
            Instance inst = sd.read(json.getAsJsonObject());
            return new FunctionalKey(inst.getString("key"), inst.getBoolean("continuous"), inst.get("action"));
         } else {
            throw new JsonSyntaxException("A FunctionalKey must be a string (key name) or an object with 'key', optional 'continuous', and optional 'action'.");
         }
      }
   );
   private final Consumer<class_1297> successAction;
   private final Consumer<class_1297> failAction;
   private final int cooldownDuration;
   private final HudRender hudRender;
   private final List<FunctionalKey> keys;
   private final List<String> keySequence;
   private int currentCooldown = 0;
   private int sequenceProgress = 0;
   private Map<String, Boolean> prevKeyStates = null;

   public ActionOnKeySequencePower(
      PowerType<?> type,
      class_1309 entity,
      Consumer<class_1297> successAction,
      Consumer<class_1297> failAction,
      int cooldownDuration,
      HudRender hudRender,
      List<FunctionalKey> keys,
      List<String> keySequence
   ) {
      super(type, entity);
      this.successAction = successAction;
      this.failAction = failAction;
      this.cooldownDuration = cooldownDuration;
      this.hudRender = hudRender;
      this.keys = keys;
      this.keySequence = keySequence;
      this.setTicking();
   }

   @Override
   public void tick() {
      if (!this.entity.method_37908().field_9236) {
         if (this.entity instanceof class_3222 player) {
            if (this.prevKeyStates == null) {
               this.prevKeyStates = new HashMap();

               for (FunctionalKey fk : this.keys) {
                  this.prevKeyStates.put(fk.key(), KeyPressManager.getKeyState(player.method_5667(), fk.key(), true));
               }
            } else if (this.currentCooldown > 0) {
               this.currentCooldown--;

               for (FunctionalKey fk : this.keys) {
                  this.prevKeyStates.put(fk.key(), KeyPressManager.getKeyState(player.method_5667(), fk.key(), true));
               }
            } else {
               List<String> firedKeyNames = new ArrayList();

               for (FunctionalKey fk : this.keys) {
                  boolean isNowPressed = KeyPressManager.getKeyState(player.method_5667(), fk.key(), true);
                  boolean wasPrevPressed = (Boolean)this.prevKeyStates.getOrDefault(fk.key(), false);
                  this.prevKeyStates.put(fk.key(), isNowPressed);
                  boolean fires = fk.continuous() ? isNowPressed : isNowPressed && !wasPrevPressed;
                  if (fires) {
                     if (fk.action() != null) {
                        fk.action().accept(this.entity);
                     }

                     firedKeyNames.add(fk.key());
                  }
               }

               if (!this.keySequence.isEmpty() && !firedKeyNames.isEmpty()) {
                  String expectedKey = (String)this.keySequence.get(this.sequenceProgress);
                  boolean correctKeyFired = firedKeyNames.contains(expectedKey);
                  if (correctKeyFired) {
                     this.sequenceProgress++;
                     if (this.sequenceProgress >= this.keySequence.size()) {
                        if (this.successAction != null) {
                           this.successAction.accept(this.entity);
                        }

                        this.currentCooldown = this.cooldownDuration;
                        this.sequenceProgress = 0;
                     }
                  } else if (this.sequenceProgress > 0) {
                     if (this.failAction != null) {
                        this.failAction.accept(this.entity);
                     }

                     String firstKey = (String)this.keySequence.get(0);
                     this.sequenceProgress = firedKeyNames.contains(firstKey) ? 1 : 0;
                  }
               }
            }
         }
      }
   }

   @Override
   public HudRender getRenderSettings() {
      return this.hudRender;
   }

   @Override
   public float getFill() {
      return this.cooldownDuration <= 0 ? 1.0F : 1.0F - (float)this.currentCooldown / this.cooldownDuration;
   }

   @Override
   public boolean shouldRender() {
      return this.hudRender.shouldRender();
   }

   @Override
   public class_2520 toTag() {
      class_2487 nbt = new class_2487();
      nbt.method_10569("cooldown", this.currentCooldown);
      nbt.method_10569("sequenceProgress", this.sequenceProgress);
      return nbt;
   }

   @Override
   public void fromTag(class_2520 tag) {
      if (tag instanceof class_2487 nbt) {
         this.currentCooldown = nbt.method_10550("cooldown");
         this.sequenceProgress = nbt.method_10550("sequenceProgress");
         // the sequence may have been shortened by a datapack change since this was saved
         if (this.sequenceProgress < 0 || this.sequenceProgress >= this.keySequence.size()) {
            this.sequenceProgress = 0;
         }
      }

      this.prevKeyStates = null;
   }

   public static PowerFactory<?> getFactory() {
      return new PowerFactory<ActionOnKeySequencePower>(
            Sync.identifier("action_on_key_sequence"),
            new SerializableData()
               .add("success_action", ApoliDataTypes.ENTITY_ACTION, null)
               .add("fail_action", ApoliDataTypes.ENTITY_ACTION, null)
               .add("cooldown", SerializableDataTypes.INT, 0)
               .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
               .add("keys", SerializableDataType.list(FUNCTIONAL_KEY))
               .add("key_sequence", SerializableDataTypes.STRINGS),
            data -> (type, entity) -> new ActionOnKeySequencePower(
               type,
               entity,
               data.get("success_action"),
               data.get("fail_action"),
               data.getInt("cooldown"),
               data.get("hud_render"),
               data.get("keys"),
               data.get("key_sequence")
            )
         )
         .allowCondition();
   }
}
