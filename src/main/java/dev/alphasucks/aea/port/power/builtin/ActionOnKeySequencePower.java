package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.FunctionalKey;
import dev.alphasucks.aea.port.data.HudRender;
import dev.alphasucks.aea.port.data.SequenceStep;
import dev.alphasucks.aea.port.keybind.HeldKeys;
import dev.alphasucks.aea.port.network.payload.PowerActivatedS2C;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.HudRendered;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerResources;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;

public final class ActionOnKeySequencePower extends PowerType<ActionOnKeySequencePower.Config> {
   private final Map<UUID, ActionOnKeySequencePower.PlayerState> states = new HashMap();

   @Override
   public MapCodec<ActionOnKeySequencePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("success_action", EntityAction.CODEC).forGetter(ActionOnKeySequencePower.Config::successAction),
               LoggedOptionalField.of("fail_action", EntityAction.CODEC).forGetter(ActionOnKeySequencePower.Config::failAction),
               Expression.INT_OR_EXPR.optionalFieldOf("cooldown", Expression.constant(0.0)).forGetter(ActionOnKeySequencePower.Config::cooldown),
               Codec.INT.optionalFieldOf("timeout", 20).forGetter(ActionOnKeySequencePower.Config::timeout),
               HudRender.CODEC.optionalFieldOf("hud_render", HudRender.DONT_RENDER).forGetter(ActionOnKeySequencePower.Config::hudRender),
               Codec.list(FunctionalKey.CODEC).fieldOf("keys").forGetter(ActionOnKeySequencePower.Config::keys),
               Codec.list(SequenceStep.CODEC).fieldOf("key_sequence").forGetter(ActionOnKeySequencePower.Config::keySequence)
            )
            .apply(i, ActionOnKeySequencePower::assemble)
      );
   }

   private static ActionOnKeySequencePower.Config assemble(
      Optional<EntityAction> successAction,
      Optional<EntityAction> failAction,
      Expression cooldown,
      int timeout,
      HudRender hudRender,
      List<FunctionalKey> keys,
      List<SequenceStep> steps
   ) {
      int atoms = SequenceStep.atomCount(steps);
      List<String> atomKeys = new ArrayList(atoms);
      int[] hold = new int[atoms];
      int[] gap = new int[atoms];
      SequenceStep.expand(steps, atomKeys, hold, gap);
      List<String> frozen = List.copyOf(atomKeys);
      return new ActionOnKeySequencePower.Config(successAction, failAction, cooldown, timeout, hudRender, keys, steps, frozen, hold, gap, computePrefix(frozen));
   }

   public void onAdded(class_2960 powerId, ActionOnKeySequencePower.Config cfg, PowerContainer holder, class_2960 source) {
      ActionOnKeySequencePower.PlayerState ps = (ActionOnKeySequencePower.PlayerState)this.states.get(holder.rawOwner().method_5667());
      if (ps != null) {
         ps.seqs.remove(powerId);
      }
   }

   public void onRemoved(class_2960 powerId, ActionOnKeySequencePower.Config cfg, PowerContainer holder, class_2960 source) {
      if (!holder.hasPower(powerId)) {
         ActionOnKeySequencePower.PlayerState ps = (ActionOnKeySequencePower.PlayerState)this.states.get(holder.rawOwner().method_5667());
         if (ps != null) {
            ps.seqs.remove(powerId);
         }
      }
   }

   public void forget(UUID player) {
      this.states.remove(player);
   }

   @Override
   public boolean isCooldown() {
      return true;
   }

   public OptionalInt readResource(class_2960 powerId, ActionOnKeySequencePower.Config cfg, PowerContainer holder) {
      class_1309 owner = holder.owner();
      if (owner == null) {
         return OptionalInt.of(0);
      }

      if (owner.method_37908().method_8608()) {
         return OptionalInt.of(PowerResources.clientCooldown(holder, powerId));
      }

      ActionOnKeySequencePower.PlayerState ps = (ActionOnKeySequencePower.PlayerState)this.states.get(owner.method_5667());
      ActionOnKeySequencePower.SeqState st = ps == null ? null : (ActionOnKeySequencePower.SeqState)ps.seqs.get(powerId);
      return OptionalInt.of(st == null ? 0 : Math.max(0, st.cooldown));
   }

   public OptionalInt writeResource(class_2960 powerId, ActionOnKeySequencePower.Config cfg, PowerContainer holder, int value) {
      class_1309 owner = holder.owner();
      if (owner != null && !owner.method_37908().method_8608()) {
         int clamped = Math.max(0, Math.min(value, Math.max(PowerResources.cooldownTicks(cfg.cooldown, holder), 0)));
         ((ActionOnKeySequencePower.SeqState)((ActionOnKeySequencePower.PlayerState)this.states
                  .computeIfAbsent(owner.method_5667(), u -> new ActionOnKeySequencePower.PlayerState()))
               .seqs
               .computeIfAbsent(powerId, k -> new ActionOnKeySequencePower.SeqState()))
            .cooldown = clamped;
         if (owner instanceof class_3222 player) {
            ApoliNetwork.sendActivated(player, new PowerActivatedS2C(powerId, clamped));
         }

         return OptionalInt.of(clamped);
      } else {
         return OptionalInt.empty();
      }
   }

   public OptionalInt resourceBound(class_2960 powerId, ActionOnKeySequencePower.Config cfg, PowerContainer holder, boolean max) {
      return OptionalInt.of(max ? Math.max(PowerResources.cooldownTicks(cfg.cooldown, holder), 0) : 0);
   }

   public void tick(class_2960 powerId, ActionOnKeySequencePower.Config cfg, PowerContainer holder) {
      class_1309 owner = holder.owner();
      if (owner instanceof class_3222 player) {
         if (owner.method_37908() instanceof class_3218 level) {
            ActionOnKeySequencePower.PlayerState var10 = (ActionOnKeySequencePower.PlayerState)this.states
               .computeIfAbsent(player.method_5667(), u -> new ActionOnKeySequencePower.PlayerState());
            long now = level.method_8510();
            if (var10.lastTick != now) {
               var10.lastTick = now;
               this.process(player, level, var10, holder);
            }
         }
      }
   }

   private void process(class_3222 player, class_3218 level, ActionOnKeySequencePower.PlayerState ps, PowerContainer holder) {
      ps.ids.clear();
      ps.configs.clear();
      PowerLookup.forEachEntry(player, ApoliIds.ACTION_ON_KEY_SEQUENCE, ActionOnKeySequencePower.Config.class, ps.collector);
      if (!ps.ids.isEmpty()) {
         if (ps.ids.size() > 1) {
            Collections.sort(ps.ids);
         }

         boolean anyStorage = !holder.powersOfType(ApoliIds.POWER_STORAGE).isEmpty();
         EntityCtx ctx = new EntityCtx(player, level);
         Set<String> held = HeldKeys.serverHeldSet(player.method_5667());
         List<String> edges = edges(held, ps.heldLast);
         ps.heldLast.clear();
         ps.heldLast.addAll(held);

         for (int i = 0; i < ps.ids.size(); i++) {
            class_2960 id = (class_2960)ps.ids.get(i);
            ActionOnKeySequencePower.Config cfg = (ActionOnKeySequencePower.Config)ps.configs.get(id);
            if (!anyStorage || !PowerStoragePower.keyMuted(holder, id)) {
               ActionOnKeySequencePower.SeqState st = (ActionOnKeySequencePower.SeqState)ps.seqs
                  .computeIfAbsent(id, k -> new ActionOnKeySequencePower.SeqState());
               st.completedNow = false;
               if (st.cooldown > 0) {
                  st.cooldown--;
               } else if (conditionHolds(id, ctx)) {
                  for (int k = 0; k < cfg.keys.size(); k++) {
                     FunctionalKey fk = (FunctionalKey)cfg.keys.get(k);
                     String keyName = fk.key().key();
                     boolean down = held.contains(keyName);
                     boolean edge = edges != null && edges.contains(keyName);
                     if (fk.key().continuous() ? down : edge) {
                        fk.action().ifPresent(a -> a.run(ctx));
                     }
                  }

                  if (!cfg.atomKeys.isEmpty()) {
                     if (st.progress > 0 || st.holdLeft > 0) {
                        st.sinceAdvance++;
                     }

                     if (st.holdLeft > 0) {
                        if (!held.contains(cfg.atomKeys.get(st.progress - 1))) {
                           breakRun(cfg, st, ctx);
                        } else {
                           st.idle = 0;
                           if (--st.holdLeft == 0) {
                              st.sinceAdvance = 0;
                              if (st.progress == cfg.atomKeys.size()) {
                                 st.progress = 0;
                                 st.pending = true;
                                 st.completedNow = true;
                              }
                           }
                        }
                     } else {
                        if (st.progress > 0 && st.progress < cfg.atomKeys.size()) {
                           int gap = cfg.atomGap[st.progress];
                           if (gap > 0 && st.sinceAdvance > gap) {
                              breakRun(cfg, st, ctx);
                           }
                        }

                        if (edges == null) {
                           if (st.progress > 0 || st.pending) {
                              st.idle++;
                              if (cfg.timeout > 0 && st.idle >= cfg.timeout) {
                                 st.progress = 0;
                                 st.idle = 0;
                                 st.sinceAdvance = 0;
                                 if (st.pending) {
                                    this.fire(player, id, cfg, st, ctx);
                                 }
                              }
                           }
                        } else {
                           st.idle = 0;

                           for (int e = 0; e < edges.size() && st.holdLeft <= 0; e++) {
                              if (this.feed((String)edges.get(e), cfg, st, ctx)) {
                                 st.pending = true;
                                 st.completedNow = true;
                                 break;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         this.arbitrate(player, ps, ctx);
      }
   }

   private static void breakRun(ActionOnKeySequencePower.Config cfg, ActionOnKeySequencePower.SeqState st, EntityCtx ctx) {
      st.progress = 0;
      st.holdLeft = 0;
      st.idle = 0;
      st.sinceAdvance = 0;
      cfg.failAction.ifPresent(a -> a.run(ctx));
   }

   private void arbitrate(class_3222 player, ActionOnKeySequencePower.PlayerState ps, EntityCtx ctx) {
      for (int i = 0; i < ps.ids.size(); i++) {
         ActionOnKeySequencePower.SeqState st = (ActionOnKeySequencePower.SeqState)ps.seqs.get(ps.ids.get(i));
         if (st != null && st.pending) {
            class_2960 id = (class_2960)ps.ids.get(i);
            ActionOnKeySequencePower.Config cfg = (ActionOnKeySequencePower.Config)ps.configs.get(id);
            if (subsumedByLongerCompletion(ps, id, cfg)) {
               st.pending = false;
               st.idle = 0;
            } else if (!blockedByViableExtension(ps, id, cfg)) {
               this.fire(player, id, cfg, st, ctx);
            }
         }
      }
   }

   private static boolean subsumedByLongerCompletion(ActionOnKeySequencePower.PlayerState ps, class_2960 self, ActionOnKeySequencePower.Config cfg) {
      for (int i = 0; i < ps.ids.size(); i++) {
         class_2960 other = (class_2960)ps.ids.get(i);
         if (!other.equals(self)) {
            ActionOnKeySequencePower.SeqState st = (ActionOnKeySequencePower.SeqState)ps.seqs.get(other);
            if (st != null && st.completedNow && isProperInfix(cfg.atomKeys, ((ActionOnKeySequencePower.Config)ps.configs.get(other)).atomKeys)) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean blockedByViableExtension(ActionOnKeySequencePower.PlayerState ps, class_2960 self, ActionOnKeySequencePower.Config cfg) {
      int len = cfg.atomKeys.size();

      for (int i = 0; i < ps.ids.size(); i++) {
         class_2960 other = (class_2960)ps.ids.get(i);
         if (!other.equals(self)) {
            ActionOnKeySequencePower.SeqState st = (ActionOnKeySequencePower.SeqState)ps.seqs.get(other);
            if (st != null
               && st.cooldown <= 0
               && (st.progress >= len || st.holdLeft != 0)
               && isProperInfix(cfg.atomKeys, ((ActionOnKeySequencePower.Config)ps.configs.get(other)).atomKeys)) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean isProperInfix(List<String> shorter, List<String> longer) {
      int n = shorter.size();
      int m = longer.size();
      if (n >= m) {
         return false;
      }

      label28:
      for (int start = 0; start <= m - n; start++) {
         for (int i = 0; i < n; i++) {
            if (!((String)shorter.get(i)).equals(longer.get(start + i))) {
               continue label28;
            }
         }

         return true;
      }

      return false;
   }

   private void fire(class_3222 player, class_2960 powerId, ActionOnKeySequencePower.Config cfg, ActionOnKeySequencePower.SeqState st, EntityCtx ctx) {
      st.pending = false;
      st.progress = 0;
      st.holdLeft = 0;
      st.idle = 0;
      st.sinceAdvance = 0;
      int ticks = Math.max(PowerResources.cooldownTicks(cfg.cooldown, PowerContainer.of(player)), 0);
      st.cooldown = ticks;
      cfg.successAction.ifPresent(a -> a.run(ctx));
      ApoliNetwork.sendActivated(player, new PowerActivatedS2C(powerId, ticks));
   }

   private static List<String> edges(Set<String> held, Set<String> heldLast) {
      if (held.isEmpty()) {
         return null;
      }

      List<String> out = null;

      for (String key : held) {
         if (!heldLast.contains(key)) {
            if (out == null) {
               out = new ArrayList(2);
            }

            out.add(key);
         }
      }

      if (out != null && out.size() > 1) {
         Collections.sort(out);
      }

      return out;
   }

   private boolean feed(String key, ActionOnKeySequencePower.Config cfg, ActionOnKeySequencePower.SeqState state, EntityCtx ctx) {
      List<String> seq = cfg.atomKeys;
      int m = seq.size();
      int before = state.progress;
      int q = before;

      while (q > 0 && !((String)seq.get(q)).equals(key)) {
         q = cfg.prefixFunction[q - 1];
      }

      if (((String)seq.get(q)).equals(key)) {
         q++;
      }

      if (q < before) {
         cfg.failAction.ifPresent(a -> a.run(ctx));
      }

      if (q != before) {
         state.sinceAdvance = 0;
      }

      if (q > 0 && cfg.atomHold[q - 1] > 0) {
         state.progress = q;
         state.holdLeft = cfg.atomHold[q - 1];
         return false;
      } else if (q == m) {
         state.progress = 0;
         return true;
      } else {
         state.progress = q;
         return false;
      }
   }

   private static boolean conditionHolds(class_2960 powerId, EntityCtx ctx) {
      Power loaded = ApoliPowers.get(powerId);
      return loaded != null && !loaded.condition().isEmpty() ? ((EntityCondition)loaded.condition().get()).test(ctx) : true;
   }

   private static int[] computePrefix(List<String> pattern) {
      int m = pattern.size();
      int[] pi = new int[m];
      int k = 0;

      for (int i = 1; i < m; i++) {
         while (k > 0 && !((String)pattern.get(i)).equals(pattern.get(k))) {
            k = pi[k - 1];
         }

         if (((String)pattern.get(i)).equals(pattern.get(k))) {
            k++;
         }

         pi[i] = k;
      }

      return pi;
   }

   public record Config(
      Optional<EntityAction> successAction,
      Optional<EntityAction> failAction,
      Expression cooldown,
      int timeout,
      HudRender hudRender,
      List<FunctionalKey> keys,
      List<SequenceStep> keySequence,
      List<String> atomKeys,
      int[] atomHold,
      int[] atomGap,
      int[] prefixFunction
   ) implements HudRendered {
   }

   private static final class PlayerState {
      final Set<String> heldLast = new HashSet();
      final Map<class_2960, ActionOnKeySequencePower.SeqState> seqs = new HashMap();
      final List<class_2960> ids = new ArrayList(4);
      final Map<class_2960, ActionOnKeySequencePower.Config> configs = new HashMap();
      final BiConsumer<class_2960, ActionOnKeySequencePower.Config> collector = (id, cfg) -> {
         this.ids.add(id);
         this.configs.put(id, cfg);
      };
      long lastTick = Long.MIN_VALUE;
   }

   private static final class SeqState {
      int progress;
      int cooldown;
      int idle;
      int sinceAdvance;
      int holdLeft;
      boolean pending;
      boolean completedNow;
   }
}
