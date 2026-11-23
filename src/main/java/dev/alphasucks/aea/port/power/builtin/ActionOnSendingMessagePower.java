package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.message.MessageFilter;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class ActionOnSendingMessagePower extends PowerType<ActionOnSendingMessagePower.Config> {
   @Override
   public MapCodec<ActionOnSendingMessagePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.optionalFieldOf("message_type").forGetter(ActionOnSendingMessagePower.Config::messageType),
               LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(ActionOnSendingMessagePower.Config::entityAction),
               MessageFilter.CODEC.optionalFieldOf("filter").forGetter(c -> Optional.empty()),
               MessageFilter.CODEC.listOf().optionalFieldOf("filters", List.of()).forGetter(ActionOnSendingMessagePower.Config::filters),
               Codec.INT.optionalFieldOf("priority", 0).forGetter(ActionOnSendingMessagePower.Config::priority)
            )
            .apply(i, (messageType, entityAction, filter, filters, priority) -> {
               List<MessageFilter> merged = new ArrayList(filters);
               filter.ifPresent(merged::add);
               return new ActionOnSendingMessagePower.Config(messageType, entityAction, List.copyOf(merged), priority);
            })
      );
   }

   public static ActionOnSendingMessagePower.Result fire(@Nullable class_1297 entity, String text, @Nullable class_2960 typeId) {
      if (entity == null) {
         return ActionOnSendingMessagePower.Result.ALLOW;
      }

      List<ActionOnSendingMessagePower.Config> configs = PowerLookup.active(
         entity, ApoliIds.ACTION_ON_SENDING_MESSAGE, ActionOnSendingMessagePower.Config.class
      );
      if (configs.isEmpty()) {
         return ActionOnSendingMessagePower.Result.ALLOW;
      }

      if (configs.size() > 1) {
         configs = new ArrayList(configs);
         configs.sort(Comparator.comparingInt(ActionOnSendingMessagePower.Config::priority).reversed());
      }

      String working = text;
      boolean prevented = false;

      for (int i = 0; i < configs.size(); i++) {
         ActionOnSendingMessagePower.Result result = process((ActionOnSendingMessagePower.Config)configs.get(i), entity, text, working, typeId);
         if (result.prevented()) {
            prevented = true;
         }

         if (result.message() != null) {
            working = result.message();
         }
      }

      if (prevented) {
         return ActionOnSendingMessagePower.Result.prevent();
      } else {
         return working.equals(text) ? ActionOnSendingMessagePower.Result.ALLOW : ActionOnSendingMessagePower.Result.modified(working);
      }
   }

   public static ActionOnSendingMessagePower.Report diagnose(class_1297 entity, String text, @Nullable class_2960 typeId) {
      List<String> lines = new ArrayList();
      List<ActionOnSendingMessagePower.Config> configs = PowerLookup.active(
         entity, ApoliIds.ACTION_ON_SENDING_MESSAGE, ActionOnSendingMessagePower.Config.class
      );
      if (configs.isEmpty()) {
         lines.add(
            "[Apoli] "
               + entity.method_5477().getString()
               + " has no active apoli:action_on_sending_message power (missing, suppressed, or its condition failed)."
         );
         return new ActionOnSendingMessagePower.Report(false, lines);
      }

      lines.add("[Apoli] " + configs.size() + " active action_on_sending_message power(s); testing \"" + text + "\".");
      boolean matched = false;
      boolean prevented = false;
      String working = text;

      for (ActionOnSendingMessagePower.Config config : configs) {
         if (config.messageType().isPresent() && !((class_2960)config.messageType().get()).equals(typeId)) {
            lines.add("  - skipped: message_type " + config.messageType().get() + " != " + typeId);
         } else if (config.filters().isEmpty()) {
            lines.add("  - no filters, so every message fires entity_action");
            matched = true;
         } else {
            for (MessageFilter filter : config.filters()) {
               boolean hit = filter.matches(text);
               lines.add(
                  "  - "
                     + (hit ? "MATCH" : "no match")
                     + ": \""
                     + filter.filter()
                     + "\" ("
                     + filter.matchMode().method_15434()
                     + (filter.inverted() ? ", inverted" : "")
                     + ")"
               );
            }

            List<MessageFilter> selected = select(config.filters(), text);
            if (selected == null) {
               lines.add("  => inert: an inverted filter matched, or no plain filter did");
            } else {
               matched = true;
               lines.add("  => fires entity_action once (" + selected.size() + " filter(s) applied)");

               for (MessageFilter filter : selected) {
                  if (filter.hasReplacement()) {
                     working = filter.applyReplacement(working);
                  }

                  if (filter.prevent()) {
                     prevented = true;
                  }
               }
            }
         }
      }

      if (prevented) {
         lines.add("[Apoli] Result: message would be prevented.");
      } else if (!working.equals(text)) {
         lines.add("[Apoli] Result: message would be rewritten to \"" + working + "\".");
      } else {
         lines.add("[Apoli] Result: message would be sent unchanged.");
      }

      lines.add("[Apoli] (dry run — no entity_action was executed)");
      return new ActionOnSendingMessagePower.Report(matched, lines);
   }

   public static ActionOnSendingMessagePower.Result process(
      ActionOnSendingMessagePower.Config config, class_1297 entity, String text, @Nullable class_2960 typeId
   ) {
      return process(config, entity, text, text, typeId);
   }

   public static ActionOnSendingMessagePower.Result process(
      ActionOnSendingMessagePower.Config config, class_1297 entity, String text, String working, @Nullable class_2960 typeId
   ) {
      if (config.messageType().isPresent() && !((class_2960)config.messageType().get()).equals(typeId)) {
         return ActionOnSendingMessagePower.Result.ALLOW;
      }

      EntityCtx ctx = new EntityCtx(entity, entity.method_37908());
      if (config.filters().isEmpty()) {
         config.entityAction().ifPresent(action -> action.run(ctx));
         return ActionOnSendingMessagePower.Result.ALLOW;
      }

      List<MessageFilter> matched = select(config.filters(), text);
      if (matched == null) {
         return ActionOnSendingMessagePower.Result.ALLOW;
      }

      for (MessageFilter filter : matched) {
         filter.beforeAction().ifPresent(action -> action.run(ctx));
      }

      config.entityAction().ifPresent(action -> action.run(ctx));
      String current = working;
      boolean prevented = false;

      for (MessageFilter filter : matched) {
         if (filter.hasReplacement()) {
            current = filter.applyReplacement(current);
         }

         filter.afterAction().ifPresent(action -> action.run(ctx));
         if (filter.prevent()) {
            prevented = true;
         }
      }

      if (prevented) {
         return ActionOnSendingMessagePower.Result.prevent();
      } else {
         return current.equals(working) ? ActionOnSendingMessagePower.Result.ALLOW : ActionOnSendingMessagePower.Result.modified(current);
      }
   }

   @Nullable
   public static List<MessageFilter> select(List<MessageFilter> filters, String text) {
      List<MessageFilter> matched = null;
      boolean hasPositive = false;

      for (int i = 0; i < filters.size(); i++) {
         MessageFilter filter = (MessageFilter)filters.get(i);
         boolean hit = filter.matches(text);
         if (filter.inverted()) {
            if (hit) {
               return null;
            }
         } else {
            hasPositive = true;
            if (hit) {
               if (matched == null) {
                  matched = new ArrayList(2);
               }

               matched.add(filter);
            }
         }
      }

      return hasPositive ? matched : List.of();
   }

   public record Config(Optional<class_2960> messageType, Optional<EntityAction> entityAction, List<MessageFilter> filters, int priority) {
   }

   public record Report(boolean matched, List<String> lines) {
   }

   public record Result(boolean prevented, @Nullable String message) {
      public static final ActionOnSendingMessagePower.Result ALLOW = new ActionOnSendingMessagePower.Result(false, null);

      public static ActionOnSendingMessagePower.Result prevent() {
         return new ActionOnSendingMessagePower.Result(true, null);
      }

      public static ActionOnSendingMessagePower.Result modified(String message) {
         return new ActionOnSendingMessagePower.Result(false, message);
      }
   }
}
