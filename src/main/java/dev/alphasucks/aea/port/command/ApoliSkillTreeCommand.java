package dev.alphasucks.aea.port.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.skill.Skill;
import dev.alphasucks.aea.port.skill.SkillData;
import dev.alphasucks.aea.port.skill.SkillDataAttachment;
import dev.alphasucks.aea.port.skill.SkillRegistry;
import dev.alphasucks.aea.port.skill.SkillTree;
import dev.alphasucks.aea.port.skill.SkillTrees;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2172;
import net.minecraft.class_2186;
import net.minecraft.class_2232;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3222;

public final class ApoliSkillTreeCommand {
   private static final SuggestionProvider<class_2168> TREES = (ctx, builder) -> class_2172.method_9270(SkillRegistry.roots(), builder);
   private static final SuggestionProvider<class_2168> SKILLS = (ctx, builder) -> {
      List<class_2960> ids = new ArrayList();

      for (Skill skill : SkillRegistry.all()) {
         ids.add(skill.id());
      }

      return class_2172.method_9270(ids, builder);
   };

   private ApoliSkillTreeCommand() {
   }

   public static void register(CommandDispatcher<class_2168> dispatcher) {
      LiteralArgumentBuilder<class_2168> root = (LiteralArgumentBuilder<class_2168>)class_2170.method_9247("apoli:skill_tree")
         .requires(ApoliPermissions.require("apoli.command.skill_tree", 2));
      root.then(
         class_2170.method_9247("buy")
            .then(
               class_2170.method_9244("targets", class_2186.method_9308())
                  .then(
                     ((RequiredArgumentBuilder)class_2170.method_9244("skill", class_2232.method_9441()).suggests(SKILLS).executes(ctx -> buy(ctx, false)))
                        .then(class_2170.method_9247("force").executes(ctx -> buy(ctx, true)))
                  )
            )
      );
      root.then(
         class_2170.method_9247("unbuy")
            .then(
               class_2170.method_9244("targets", class_2186.method_9308())
                  .then(class_2170.method_9244("skill", class_2232.method_9441()).suggests(SKILLS).executes(ApoliSkillTreeCommand::unbuy))
            )
      );
      root.then(
         ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247("points")
                     .then(
                        class_2170.method_9247("get")
                           .then(
                              class_2170.method_9244("target", class_2186.method_9305())
                                 .then(class_2170.method_9244("tree", class_2232.method_9441()).suggests(TREES).executes(ApoliSkillTreeCommand::pointsGet))
                           )
                     ))
                  .then(
                     class_2170.method_9247("set")
                        .then(
                           class_2170.method_9244("targets", class_2186.method_9308())
                              .then(
                                 class_2170.method_9244("tree", class_2232.method_9441())
                                    .suggests(TREES)
                                    .then(class_2170.method_9244("value", IntegerArgumentType.integer(0)).executes(ApoliSkillTreeCommand::pointsSet))
                              )
                        )
                  ))
               .then(
                  class_2170.method_9247("add")
                     .then(
                        class_2170.method_9244("targets", class_2186.method_9308())
                           .then(
                              class_2170.method_9244("tree", class_2232.method_9441())
                                 .suggests(TREES)
                                 .then(class_2170.method_9244("value", IntegerArgumentType.integer()).executes(ctx -> pointsAdd(ctx, 1)))
                           )
                     )
               ))
            .then(
               class_2170.method_9247("remove")
                  .then(
                     class_2170.method_9244("targets", class_2186.method_9308())
                        .then(
                           class_2170.method_9244("tree", class_2232.method_9441())
                              .suggests(TREES)
                              .then(class_2170.method_9244("value", IntegerArgumentType.integer()).executes(ctx -> pointsAdd(ctx, -1)))
                        )
                  )
            )
      );
      root.then(
         class_2170.method_9247("grant")
            .then(
               class_2170.method_9244("targets", class_2186.method_9308())
                  .then(class_2170.method_9244("tree", class_2232.method_9441()).suggests(TREES).executes(ApoliSkillTreeCommand::grant))
            )
      );
      root.then(
         class_2170.method_9247("revoke")
            .then(
               class_2170.method_9244("targets", class_2186.method_9308())
                  .then(class_2170.method_9244("tree", class_2232.method_9441()).suggests(TREES).executes(ApoliSkillTreeCommand::revoke))
            )
      );
      root.then(
         class_2170.method_9247("reset")
            .then(
               ((RequiredArgumentBuilder)((RequiredArgumentBuilder)class_2170.method_9244("targets", class_2186.method_9308())
                        .executes(ctx -> reset(ctx, null, true)))
                     .then(class_2170.method_9247("norefund").executes(ctx -> reset(ctx, null, false))))
                  .then(
                     ((RequiredArgumentBuilder)class_2170.method_9244("tree", class_2232.method_9441())
                           .suggests(TREES)
                           .executes(ctx -> reset(ctx, class_2232.method_9443(ctx, "tree"), true)))
                        .then(class_2170.method_9247("norefund").executes(ctx -> reset(ctx, class_2232.method_9443(ctx, "tree"), false)))
                  )
            )
      );
      root.then(class_2170.method_9247("list").then(class_2170.method_9244("target", class_2186.method_9305()).executes(ApoliSkillTreeCommand::list)));
      LiteralCommandNode<class_2168> node = dispatcher.register(root);
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247("skills").requires(ApoliPermissions.require("apoli.command.skill_tree", 2)))
            .redirect(node)
      );
   }

   private static int buy(CommandContext<class_2168> ctx, boolean force) throws CommandSyntaxException {
      class_2960 skill = class_2232.method_9443(ctx, "skill");
      if (!knownSkill(ctx, skill)) {
         return 0;
      }

      int done = 0;

      for (class_3222 player : class_2186.method_9312(ctx, "targets")) {
         boolean ok = force ? SkillTrees.forcePurchase(player, skill) : SkillTrees.tryPurchase(player, skill);
         if (ok) {
            ApoliNetwork.sendSkillState(player);
            done++;
         }
      }

      feedbackCount(
         ctx,
         done,
         (force ? "Force-bought " : "Bought ") + skill + " for %d player(s).",
         "No purchases made (already purchased, or requirements not met — try 'force')."
      );
      return done;
   }

   private static boolean knownSkill(CommandContext<class_2168> ctx, class_2960 skill) {
      if (SkillRegistry.get(skill) != null) {
         return true;
      }

      int loaded = SkillRegistry.all().size();
      ((class_2168)ctx.getSource())
         .method_9213(
            class_2561.method_43470(
               "Unknown skill: "
                  + skill
                  + (
                     loaded == 0
                        ? ". No skills are loaded — define them with a 'skill' block in a power file, or as skill_trees files carrying a 'parent'."
                        : ". " + loaded + " skill(s) are loaded; check the id and that its parent chain ends at a skill tree."
                  )
            )
         );
      return false;
   }

   private static int unbuy(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2960 skill = class_2232.method_9443(ctx, "skill");
      if (!knownSkill(ctx, skill)) {
         return 0;
      }

      int done = 0;

      for (class_3222 player : class_2186.method_9312(ctx, "targets")) {
         if (SkillTrees.tryRefund(player, skill, true)) {
            ApoliNetwork.sendSkillState(player);
            done++;
         }
      }

      feedbackCount(ctx, done, "Refunded " + skill + " for %d player(s).", "Nobody had that skill purchased.");
      return done;
   }

   private static int pointsGet(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_3222 player = class_2186.method_9315(ctx, "target");
      class_2960 tree = class_2232.method_9443(ctx, "tree");
      if (!knownTree(ctx, tree)) {
         return 0;
      }

      int points = SkillDataAttachment.get(player).getPoints(tree);
      ((class_2168)ctx.getSource())
         .method_9226(() -> class_2561.method_43470(player.method_7334().getName() + " has " + points + " point(s) in " + tree + "."), false);
      return points;
   }

   private static int pointsSet(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2960 tree = class_2232.method_9443(ctx, "tree");
      if (!knownTree(ctx, tree)) {
         return 0;
      }

      int value = IntegerArgumentType.getInteger(ctx, "value");
      int done = 0;

      for (class_3222 player : class_2186.method_9312(ctx, "targets")) {
         SkillDataAttachment.get(player).setPoints(tree, value);
         ApoliNetwork.sendSkillState(player);
         done++;
      }

      feedbackCount(ctx, done, "Set " + tree + " points to " + value + " for %d player(s).", "No players matched.");
      return done;
   }

   private static int pointsAdd(CommandContext<class_2168> ctx, int sign) throws CommandSyntaxException {
      class_2960 tree = class_2232.method_9443(ctx, "tree");
      if (!knownTree(ctx, tree)) {
         return 0;
      }

      int value = IntegerArgumentType.getInteger(ctx, "value") * sign;
      int done = 0;
      int last = 0;

      for (class_3222 player : class_2186.method_9312(ctx, "targets")) {
         SkillData data = SkillDataAttachment.get(player);
         data.addPoints(tree, value);
         last = data.getPoints(tree);
         ApoliNetwork.sendSkillState(player);
         done++;
      }

      int now = last;
      feedbackCount(
         ctx, done, (value < 0 ? "Removed " + -value : "Added " + value) + " " + tree + " point(s) for %d player(s); now " + now + ".", "No players matched."
      );
      return done;
   }

   private static boolean knownTree(CommandContext<class_2168> ctx, class_2960 tree) {
      if (SkillRegistry.tree(tree) != null) {
         return true;
      }

      StringBuilder known = new StringBuilder();

      for (class_2960 id : SkillRegistry.roots()) {
         if (known.length() > 0) {
            known.append(", ");
         }

         known.append(id);
      }

      ((class_2168)ctx.getSource())
         .method_9213(
            class_2561.method_43470(
               "Unknown skill tree: "
                  + tree
                  + (known.length() == 0 ? ". No skill trees are loaded — check data/<namespace>/skill_trees/." : ". Loaded trees: " + known)
            )
         );
      return false;
   }

   private static int grant(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2960 tree = class_2232.method_9443(ctx, "tree");
      if (SkillRegistry.tree(tree) == null) {
         ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("Unknown skill tree: " + tree));
         return 0;
      }

      int done = 0;

      for (class_3222 player : class_2186.method_9312(ctx, "targets")) {
         if (SkillTrees.grantTree(player, tree)) {
            done++;
         }
      }

      feedbackCount(ctx, done, "Granted " + tree + " to %d player(s).", "No changes (already granted; note auto_grant trees are always available).");
      return done;
   }

   private static int revoke(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2960 tree = class_2232.method_9443(ctx, "tree");
      int done = 0;

      for (class_3222 player : class_2186.method_9312(ctx, "targets")) {
         if (SkillTrees.revokeTree(player, tree)) {
            done++;
         }
      }

      feedbackCount(ctx, done, "Revoked " + tree + " from %d player(s).", "No changes (nobody had an explicit grant; auto_grant trees cannot be revoked).");
      return done;
   }

   private static int reset(CommandContext<class_2168> ctx, class_2960 tree, boolean refund) throws CommandSyntaxException {
      int done = 0;

      for (class_3222 player : class_2186.method_9312(ctx, "targets")) {
         SkillTrees.resetSkills(player, tree, refund);
         ApoliNetwork.sendSkillState(player);
         done++;
      }

      String scope = tree == null ? "all trees" : tree.toString();
      feedbackCount(ctx, done, "Reset " + scope + (refund ? " (points refunded)" : " (no refund)") + " for %d player(s).", "No players matched.");
      return done;
   }

   private static int list(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_3222 player = class_2186.method_9315(ctx, "target");
      SkillData data = SkillDataAttachment.get(player);
      StringBuilder trees = new StringBuilder();

      for (SkillTree tree : SkillRegistry.trees()) {
         boolean available = SkillTrees.treeAvailable(player, data, tree.id());
         if (trees.length() > 0) {
            trees.append(", ");
         }

         trees.append(tree.id()).append(tree.autoGrant() ? " (auto)" : (available ? " (granted)" : " (not granted)"));
      }

      String name = player.method_7334().getName();
      ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470(name + " — trees: " + (trees.length() == 0 ? "none" : trees)), false);
      StringBuilder points = new StringBuilder();

      for (Entry<class_2960, Integer> e : data.pointsView().entrySet()) {
         if (points.length() > 0) {
            points.append(", ");
         }

         points.append(e.getKey()).append(" = ").append(e.getValue());
      }

      ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470("  points: " + (points.length() == 0 ? "none" : points)), false);
      Collection<class_2960> purchased = data.purchasedView();
      StringBuilder skills = new StringBuilder();

      for (class_2960 id : purchased) {
         if (skills.length() > 0) {
            skills.append(", ");
         }

         skills.append(id);
      }

      ((class_2168)ctx.getSource())
         .method_9226(() -> class_2561.method_43470("  purchased (" + purchased.size() + "): " + (skills.length() == 0 ? "none" : skills)), false);
      return purchased.size();
   }

   private static void feedbackCount(CommandContext<class_2168> ctx, int count, String successFormat, String noneMessage) {
      if (count > 0) {
         ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470(String.format(successFormat, new Object[]{count})), true);
      } else {
         ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470(noneMessage));
      }
   }
}
