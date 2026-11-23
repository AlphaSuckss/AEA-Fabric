package dev.alphasucks.aea.port.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.alphasucks.aea.port.PowerContainerAttachment;
import dev.alphasucks.aea.port.action.builtin.entity.SuppressPowerAction;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerKeys;
import dev.alphasucks.aea.port.power.PowerSources;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import io.github.apace100.origins.origin.OriginRegistry;
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2172;
import net.minecraft.class_2186;
import net.minecraft.class_2232;
import net.minecraft.class_2561;
import net.minecraft.class_2960;

public final class ApoliPowerCommand {
   private static final SuggestionProvider<class_2168> LOADED_POWERS = (ctx, builder) -> class_2172.method_9270(ApoliPowers.view().keySet(), builder);
   private static final SuggestionProvider<class_2168> KNOWN_SOURCES = (ctx, builder) -> class_2172.method_9270(knownSources(), builder);

   private ApoliPowerCommand() {
   }

   public static void register(CommandDispatcher<class_2168> dispatcher) {
      LiteralArgumentBuilder<class_2168> root = (LiteralArgumentBuilder<class_2168>)class_2170.method_9247("apoli:power")
         .requires(ApoliPermissions.require("apoli.command.power", 2));
      root.then(
         class_2170.method_9247("grant")
            .then(
               class_2170.method_9244("targets", class_2186.method_9306())
                  .then(
                     ((RequiredArgumentBuilder)class_2170.method_9244("power", class_2232.method_9441())
                           .suggests(LOADED_POWERS)
                           .executes(ctx -> grant(ctx, null)))
                        .then(
                           class_2170.method_9244("source", class_2232.method_9441())
                              .suggests(KNOWN_SOURCES)
                              .executes(ctx -> grant(ctx, class_2232.method_9443(ctx, "source")))
                        )
                  )
            )
      );
      root.then(revokeTree("revoke"));
      root.then(revokeTree("remove"));
      root.then(
         class_2170.method_9247("grantall")
            .then(
               class_2170.method_9244("targets", class_2186.method_9306())
                  .then(class_2170.method_9244("source", class_2232.method_9441()).suggests(KNOWN_SOURCES).executes(ApoliPowerCommand::grantAll))
            )
      );
      root.then(revokeAllTree("revokeall"));
      root.then(revokeAllTree("removeall"));
      root.then(
         class_2170.method_9247("has")
            .then(
               ((RequiredArgumentBuilder)class_2170.method_9244("targets", class_2186.method_9306()).executes(ApoliPowerCommand::listAll))
                  .then(
                     class_2170.method_9244("power", class_2232.method_9441()).suggests(ApoliPowerCommand::suggestHeldPowers).executes(ApoliPowerCommand::has)
                  )
            )
      );
      root.then(
         class_2170.method_9247("suppress")
            .then(
               ((RequiredArgumentBuilder)class_2170.method_9244("targets", class_2186.method_9306())
                     .then(
                        class_2170.method_9247("power")
                           .then(
                              ((RequiredArgumentBuilder)class_2170.method_9244("power", class_2232.method_9441())
                                    .suggests(ApoliPowerCommand::suggestHeldPowers)
                                    .executes(ctx -> suppressPower(ctx, true, null)))
                                 .then(
                                    class_2170.method_9244("source", class_2232.method_9441())
                                       .executes(ctx -> suppressPower(ctx, true, class_2232.method_9443(ctx, "source")))
                                 )
                           )
                     ))
                  .then(
                     class_2170.method_9247("key")
                        .then(
                           ((RequiredArgumentBuilder)class_2170.method_9244("key", StringArgumentType.string())
                                 .suggests(ApoliPowerCommand::suggestHeldKeys)
                                 .executes(ctx -> suppressKey(ctx, true, null)))
                              .then(
                                 class_2170.method_9244("source", class_2232.method_9441())
                                    .executes(ctx -> suppressKey(ctx, true, class_2232.method_9443(ctx, "source")))
                              )
                        )
                  )
            )
      );
      root.then(
         class_2170.method_9247("unsuppress")
            .then(
               ((RequiredArgumentBuilder)((RequiredArgumentBuilder)class_2170.method_9244("targets", class_2186.method_9306())
                        .then(class_2170.method_9247("all").executes(ApoliPowerCommand::unsuppressAll)))
                     .then(
                        class_2170.method_9247("power")
                           .then(
                              ((RequiredArgumentBuilder)class_2170.method_9244("power", class_2232.method_9441())
                                    .suggests(ApoliPowerCommand::suggestSuppressedPowers)
                                    .executes(ctx -> suppressPower(ctx, false, null)))
                                 .then(
                                    class_2170.method_9244("source", class_2232.method_9441())
                                       .executes(ctx -> suppressPower(ctx, false, class_2232.method_9443(ctx, "source")))
                                 )
                           )
                     ))
                  .then(
                     class_2170.method_9247("key")
                        .then(
                           ((RequiredArgumentBuilder)class_2170.method_9244("key", StringArgumentType.string())
                                 .suggests(ApoliPowerCommand::suggestHeldKeys)
                                 .executes(ctx -> suppressKey(ctx, false, null)))
                              .then(
                                 class_2170.method_9244("source", class_2232.method_9441())
                                    .executes(ctx -> suppressKey(ctx, false, class_2232.method_9443(ctx, "source")))
                              )
                        )
                  )
            )
      );
      // AEA: Overgrown also redirected "/power" here; Apoli 2.9.2 owns that root, so only /apoli:power is registered.
      dispatcher.register(root);
   }

   private static LiteralArgumentBuilder<class_2168> revokeTree(String literal) {
      return (LiteralArgumentBuilder<class_2168>)class_2170.method_9247(literal)
         .then(
            class_2170.method_9244("targets", class_2186.method_9306())
               .then(
                  ((RequiredArgumentBuilder)class_2170.method_9244("power", class_2232.method_9441())
                        .suggests(ApoliPowerCommand::suggestHeldPowers)
                        .executes(ApoliPowerCommand::revoke))
                     .then(
                        class_2170.method_9247("from")
                           .then(
                              class_2170.method_9244("source", class_2232.method_9441())
                                 .suggests(ApoliPowerCommand::suggestSourcesOfPower)
                                 .executes(ApoliPowerCommand::revokeFromSource)
                           )
                     )
               )
         );
   }

   private static LiteralArgumentBuilder<class_2168> revokeAllTree(String literal) {
      return (LiteralArgumentBuilder<class_2168>)class_2170.method_9247(literal)
         .then(
            class_2170.method_9244("targets", class_2186.method_9306())
               .then(
                  class_2170.method_9244("source", class_2232.method_9441())
                     .suggests(ApoliPowerCommand::suggestRevokableSources)
                     .executes(ApoliPowerCommand::revokeAll)
               )
         );
   }

   private static int grant(CommandContext<class_2168> ctx, class_2960 explicitSource) throws CommandSyntaxException {
      class_2960 power = class_2232.method_9443(ctx, "power");
      if (ApoliPowers.get(power) == null) {
         ((class_2168)ctx.getSource())
            .method_9213(
               class_2561.method_43470(
                  "Unknown power: " + power + " (no JSON found at data/" + power.method_12836() + "/powers/" + power.method_12832() + ".json)"
               )
            );
         return 0;
      }

      class_2960 source = explicitSource != null ? explicitSource : power;
      int affected = 0;

      for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
         PowerContainer c = PowerContainerAttachment.getOrCreate(e);
         if (c != null && c.addPower(power, source)) {
            affected++;
         }
      }

      int result = affected;
      ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470("Granted " + power + " to " + entities(result) + "."), true);
      return affected;
   }

   private static int revoke(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2960 power = class_2232.method_9443(ctx, "power");
      int affected = 0;

      for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
         PowerContainer c = PowerContainer.of(e);
         if (c != null && c.removePowerCompletely(power)) {
            affected++;
         }
      }

      int result = affected;
      if (result == 0) {
         ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("No target had " + power + "."));
         return 0;
      } else {
         ((class_2168)ctx.getSource())
            .method_9226(() -> class_2561.method_43470("Revoked " + power + " (and any sub-powers) from " + entities(result) + "."), true);
         return affected;
      }
   }

   private static int revokeFromSource(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2960 power = class_2232.method_9443(ctx, "power");
      class_2960 source = class_2232.method_9443(ctx, "source");
      int affected = 0;

      for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
         PowerContainer c = PowerContainer.of(e);
         if (c != null && c.removePower(power, source)) {
            affected++;
         }
      }

      int result = affected;
      if (result == 0) {
         ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("No target had " + power + " from source " + source + "."));
         return 0;
      } else {
         ((class_2168)ctx.getSource())
            .method_9226(() -> class_2561.method_43470("Revoked " + power + " (source " + source + ") from " + entities(result) + "."), true);
         return affected;
      }
   }

   private static int grantAll(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2960 source = class_2232.method_9443(ctx, "source");
      Set<class_2960> powers = PowerSources.powersOf(source);
      if (powers != null && !powers.isEmpty()) {
         int granted = 0;
         int affected = 0;

         for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
            PowerContainer c = PowerContainerAttachment.getOrCreate(e);
            if (c != null) {
               for (class_2960 id : powers) {
                  if (c.addPower(id, source)) {
                     granted++;
                  }
               }

               affected++;
            }
         }

         int ents = affected;
         int pows = granted;
         int available = powers.size();
         ((class_2168)ctx.getSource())
            .method_9226(
               () -> class_2561.method_43470("Granted " + available + " power(s) from " + source + " (" + pows + " new) to " + entities(ents) + "."), true
            );
         return affected;
      } else {
         ((class_2168)ctx.getSource())
            .method_9213(class_2561.method_43470("'" + source + "' provides no powers. Use an origin, a skill tree or an apoli:multiple power id."));
         return 0;
      }
   }

   private static int revokeAll(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2960 source = class_2232.method_9443(ctx, "source");
      Set<class_2960> provided = PowerSources.powersOf(source);
      int affected = 0;

      for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
         PowerContainer c = PowerContainer.of(e);
         if (c != null) {
            boolean any = c.removeAllFromSource(source);
            if (provided != null) {
               for (class_2960 id : provided) {
                  if (c.removePowerCompletely(id)) {
                     any = true;
                  }
               }
            }

            if (any) {
               affected++;
            }
         }
      }

      int result = affected;
      if (result == 0) {
         ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("No target had any power from " + source + "."));
         return 0;
      } else {
         ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470("Revoked all powers from " + source + " on " + entities(result) + "."), true);
         return affected;
      }
   }

   private static int has(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2960 power = class_2232.method_9443(ctx, "power");
      int matches = 0;

      for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
         PowerContainer c = PowerContainer.of(e);
         boolean held = c != null && c.hasPower(power);
         if (held) {
            matches++;
         }

         boolean suppressed = held && c.isSuppressed(power);
         ((class_2168)ctx.getSource())
            .method_9226(
               () -> class_2561.method_43470(e.method_5477().getString() + ": ")
                  .method_10852(class_2561.method_43470(String.valueOf(held)).method_27692(held ? class_124.field_1060 : class_124.field_1061))
                  .method_10852(suppressed ? class_2561.method_43470(" (suppressed)").method_27692(class_124.field_1080) : class_2561.method_43473()),
               false
            );
      }

      return matches;
   }

   private static int listAll(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      int total = 0;

      for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
         PowerContainer c = PowerContainer.of(e);
         Set<class_2960> powers = c == null ? Set.of() : c.allPowers();
         List<class_2960> sorted = new ArrayList(powers);
         sorted.sort(class_2960::method_12833);
         ((class_2168)ctx.getSource())
            .method_9226(
               () -> class_2561.method_43470(e.method_5477().getString() + " — " + sorted.size() + " power(s):").method_27692(class_124.field_1065), false
            );

         for (class_2960 id : sorted) {
            boolean sub = ApoliPowers.isSubPower(id);
            boolean suppressed = c.isSuppressed(id);
            class_2561 line = class_2561.method_43470("  " + id)
               .method_27692(suppressed ? class_124.field_1063 : (sub ? class_124.field_1080 : class_124.field_1068))
               .method_27661()
               .method_10852(class_2561.method_43470(" [" + joinIds(c.sourcesOf(id)) + "]").method_27692(class_124.field_1063))
               .method_10852(suppressed ? class_2561.method_43470(" suppressed").method_27692(class_124.field_1061) : class_2561.method_43473());
            ((class_2168)ctx.getSource()).method_9226(() -> line, false);
         }

         total += sorted.size();
      }

      return total;
   }

   private static int suppressPower(CommandContext<class_2168> ctx, boolean suppress, class_2960 explicitSource) throws CommandSyntaxException {
      class_2960 power = class_2232.method_9443(ctx, "power");
      class_2960 source = explicitSource != null ? explicitSource : SuppressPowerAction.DEFAULT_SOURCE;
      int affected = 0;

      for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
         PowerContainer c = PowerContainer.of(e);
         if (c != null && (suppress ? c.suppressPower(power, source) : c.unsuppressPower(power, source))) {
            affected++;
         }
      }

      return report(
         ctx,
         affected,
         (suppress ? "Suppressed " : "Unsuppressed ") + power + " (source " + source + ")",
         suppress ? "already suppressed" : "was not suppressed"
      );
   }

   private static int suppressKey(CommandContext<class_2168> ctx, boolean suppress, class_2960 explicitSource) throws CommandSyntaxException {
      String key = StringArgumentType.getString(ctx, "key");
      class_2960 source = explicitSource != null ? explicitSource : SuppressPowerAction.DEFAULT_SOURCE;
      int affected = 0;

      for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
         PowerContainer c = PowerContainer.of(e);
         if (c != null) {
            for (class_2960 power : PowerKeys.heldPowersUsingKey(c, key)) {
               if (suppress ? c.suppressPower(power, source) : c.unsuppressPower(power, source)) {
                  affected++;
               }
            }
         }
      }

      return report(
         ctx,
         affected,
         (suppress ? "Suppressed " : "Unsuppressed ") + affected + " power(s) bound to " + key + " (source " + source + ")",
         "no held power uses that key"
      );
   }

   private static int unsuppressAll(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      int affected = 0;

      for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
         PowerContainer c = PowerContainer.of(e);
         if (c != null) {
            for (class_2960 power : c.directlySuppressedPowers()) {
               for (class_2960 source : c.suppressionSourcesOf(power)) {
                  if (c.unsuppressPower(power, source)) {
                     affected++;
                  }
               }
            }
         }
      }

      return report(ctx, affected, "Cleared " + affected + " suppression(s)", "nothing was suppressed");
   }

   private static int report(CommandContext<class_2168> ctx, int affected, String success, String noneReason) {
      if (affected == 0) {
         ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("No change (" + noneReason + ")."));
         return 0;
      } else {
         ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470(success + "."), true);
         return affected;
      }
   }

   private static CompletableFuture<Suggestions> suggestHeldPowers(CommandContext<class_2168> ctx, SuggestionsBuilder builder) {
      Set<class_2960> held = new LinkedHashSet();

      try {
         for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
            PowerContainer c = PowerContainer.of(e);
            if (c != null) {
               held.addAll(c.allPowers());
            }
         }
      } catch (Exception var6) {
      }

      return held.isEmpty() ? class_2172.method_9270(ApoliPowers.view().keySet(), builder) : class_2172.method_9270(held, builder);
   }

   private static CompletableFuture<Suggestions> suggestSourcesOfPower(CommandContext<class_2168> ctx, SuggestionsBuilder builder) {
      Set<class_2960> sources = new LinkedHashSet();

      try {
         class_2960 power = class_2232.method_9443(ctx, "power");

         for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
            PowerContainer c = PowerContainer.of(e);
            if (c != null) {
               sources.addAll(c.sourcesOf(power));
            }
         }
      } catch (Exception var7) {
      }

      return class_2172.method_9270(sources, builder);
   }

   private static CompletableFuture<Suggestions> suggestRevokableSources(CommandContext<class_2168> ctx, SuggestionsBuilder builder) {
      Set<class_2960> sources = new LinkedHashSet();

      try {
         for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
            PowerContainer c = PowerContainer.of(e);
            if (c != null) {
               sources.addAll(c.allSources());
            }
         }
      } catch (Exception var6) {
      }

      sources.addAll(knownSources());
      return class_2172.method_9270(sources, builder);
   }

   private static CompletableFuture<Suggestions> suggestSuppressedPowers(CommandContext<class_2168> ctx, SuggestionsBuilder builder) {
      Set<class_2960> suppressed = new LinkedHashSet();

      try {
         for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
            PowerContainer c = PowerContainer.of(e);
            if (c != null) {
               suppressed.addAll(c.directlySuppressedPowers());
            }
         }
      } catch (Exception var6) {
      }

      return class_2172.method_9270(suppressed, builder);
   }

   private static CompletableFuture<Suggestions> suggestHeldKeys(CommandContext<class_2168> ctx, SuggestionsBuilder builder) {
      Set<String> keys = new LinkedHashSet();

      try {
         for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
            PowerContainer c = PowerContainer.of(e);
            if (c != null) {
               keys.addAll(PowerKeys.keysHeldBy(c));
            }
         }
      } catch (Exception var6) {
      }

      if (keys.isEmpty()) {
         keys.addAll(PowerKeys.knownKeys());
      }

      List<String> quoted = new ArrayList(keys.size());

      for (String key : keys) {
         quoted.add("\"" + key + "\"");
      }

      return class_2172.method_9265(quoted, builder);
   }

   /** AEA: Overgrown's PowerSources.knownSources(); on Origins 1.10.2 the grantable sources are the origins. */
   private static List<class_2960> knownSources() {
      List<class_2960> out = new ArrayList<>();
      OriginRegistry.identifiers().forEach(out::add);
      return out;
   }

   private static String entities(int count) {
      return count + " entit" + (count == 1 ? "y" : "ies");
   }

   private static String joinIds(Collection<class_2960> ids) {
      if (ids.isEmpty()) {
         return "";
      }

      StringBuilder sb = new StringBuilder();

      for (class_2960 id : ids) {
         if (sb.length() > 0) {
            sb.append(", ");
         }

         sb.append(id);
      }

      return sb.toString();
   }
}
