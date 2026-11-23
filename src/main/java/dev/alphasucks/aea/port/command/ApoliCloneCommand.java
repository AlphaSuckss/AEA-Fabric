package dev.alphasucks.aea.port.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.alphasucks.aea.port.entity.ApoliEntities;
import dev.alphasucks.aea.port.entity.summon.CloneEntity;
import dev.alphasucks.aea.port.entity.summon.Summons;
import dev.alphasucks.aea.port.power.ApoliPowers;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2179;
import net.minecraft.class_2186;
import net.minecraft.class_2232;
import net.minecraft.class_2277;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import org.jetbrains.annotations.Nullable;

public final class ApoliCloneCommand {
   private static final class_2487 NO_OPTIONS = new class_2487();

   private ApoliCloneCommand() {
   }

   public static void register(CommandDispatcher<class_2168> dispatcher) {
      LiteralArgumentBuilder<class_2168> root = (LiteralArgumentBuilder<class_2168>)class_2170.method_9247("apoli:clone")
         .requires(ApoliPermissions.require("apoli.command.clone", 2));
      root.then(
         class_2170.method_9247("summon")
            .then(
               ((RequiredArgumentBuilder)((RequiredArgumentBuilder)class_2170.method_9244("owners", class_2186.method_9306())
                        .executes(ctx -> summon(ctx, null, NO_OPTIONS)))
                     .then(
                        class_2170.method_9244("options", class_2179.method_9284()).executes(ctx -> summon(ctx, null, class_2179.method_9285(ctx, "options")))
                     ))
                  .then(
                     ((RequiredArgumentBuilder)class_2170.method_9244("pos", class_2277.method_9737())
                           .executes(ctx -> summon(ctx, class_2277.method_9736(ctx, "pos"), NO_OPTIONS)))
                        .then(
                           class_2170.method_9244("options", class_2179.method_9284())
                              .executes(ctx -> summon(ctx, class_2277.method_9736(ctx, "pos"), class_2179.method_9285(ctx, "options")))
                        )
                  )
            )
      );
      root.then(
         class_2170.method_9247("remove")
            .then(
               ((RequiredArgumentBuilder)class_2170.method_9244("owners", class_2186.method_9306()).executes(ctx -> remove(ctx, null)))
                  .then(class_2170.method_9244("summon_id", class_2232.method_9441()).executes(ctx -> remove(ctx, class_2232.method_9443(ctx, "summon_id"))))
            )
      );
      root.then(class_2170.method_9247("list").then(class_2170.method_9244("owners", class_2186.method_9306()).executes(ApoliCloneCommand::list)));
      dispatcher.register(root);
   }

   private static int summon(CommandContext<class_2168> ctx, @Nullable class_243 pos, class_2487 optionsTag) throws CommandSyntaxException {
      class_3218 level = ((class_2168)ctx.getSource()).method_9225();
      ApoliCloneCommand.Options options = ApoliCloneCommand.Options.read(optionsTag);

      for (class_2960 power : options.powers()) {
         if (ApoliPowers.get(power) == null) {
            ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("Unknown power: " + power));
            return 0;
         }
      }

      int spawned = 0;

      for (class_1297 owner : class_2186.method_9317(ctx, "owners")) {
         if (owner instanceof class_1657 player) {
            class_243 at = pos != null ? pos : player.method_19538();

            for (int i = 0; i < options.count(); i++) {
               if (spawn(level, player, at, options) != null) {
                  spawned++;
               }
            }
         } else {
            ((class_2168)ctx.getSource())
               .method_9213(class_2561.method_43470(owner.method_5477().getString() + " is not a player — clones copy a player's skin and equipment."));
         }
      }

      int result = spawned;
      if (result == 0) {
         ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("No clones were summoned."));
         return 0;
      } else {
         ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470("Summoned " + result + " clone(s)."), true);
         return spawned;
      }
   }

   @Nullable
   private static CloneEntity spawn(class_3218 level, class_1657 owner, class_243 pos, ApoliCloneCommand.Options options) {
      CloneEntity clone = (CloneEntity)ApoliEntities.cloneType().method_5883(level);
      if (clone == null) {
         return null;
      }

      clone.setOwner(owner);
      clone.setCanAttack(options.canAttack());
      clone.setCanSit(options.canSit());
      clone.setFollowOwner(options.followOwner());
      clone.setSlim(options.slim());
      clone.setMaxLifeTime(options.lifetime());
      clone.method_5665(owner.method_5477());
      if (options.summonId() != null) {
         clone.setSummonId(options.summonId());
      }

      if (options.wideTexture() != null) {
         clone.setWideTexture(options.wideTexture());
      }

      if (options.slimTexture() != null) {
         clone.setSlimTexture(options.slimTexture());
      }

      clone.method_5808(pos.field_1352, pos.field_1351, pos.field_1350, owner.method_5791(), 0.0F);
      if (options.inheritEquipment()) {
         for (class_1304 slot : class_1304.values()) {
            class_1799 equipped = owner.method_6118(slot);
            if (!equipped.method_7960()) {
               clone.method_5946(slot, 0.0F);
               clone.method_5673(slot, equipped.method_7972());
            }
         }
      }

      level.method_8649(clone);
      clone.updateWeaponGoals();
      Summons.grantPowers(clone, options.powers());
      return clone;
   }

   private static int remove(CommandContext<class_2168> ctx, @Nullable class_2960 summonId) throws CommandSyntaxException {
      int removed = 0;

      for (CloneEntity clone : clonesOf(ctx)) {
         if (summonId == null || summonId.equals(clone.getSummonId())) {
            clone.method_31472();
            removed++;
         }
      }

      int result = removed;
      if (result == 0) {
         ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("No matching clones found."));
         return 0;
      } else {
         ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470("Removed " + result + " clone(s)."), true);
         return removed;
      }
   }

   private static int list(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      List<CloneEntity> clones = clonesOf(ctx);
      ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470(clones.size() + " clone(s):"), false);

      for (CloneEntity clone : clones) {
         class_2960 summonId = clone.getSummonId();
         ((class_2168)ctx.getSource())
            .method_9226(
               () -> class_2561.method_43470(
                  "  "
                     + clone.method_5845()
                     + " owner="
                     + (clone.method_35057() == null ? "none" : clone.method_35057().method_5477().getString())
                     + (summonId == null ? "" : " id=" + summonId)
                     + " lifetime="
                     + clone.getMaxLifeTime()
                     + " remaining="
                     + clone.getRemainingLifeTime()
               ),
               false
            );
      }

      return clones.size();
   }

   private static List<CloneEntity> clonesOf(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      List<CloneEntity> out = new ArrayList();
      class_3218 level = ((class_2168)ctx.getSource()).method_9225();

      for (class_1297 owner : class_2186.method_9317(ctx, "owners")) {
         for (CloneEntity clone : level.method_8390(CloneEntity.class, owner.method_5829().method_1014(256.0), c -> c.method_35057() == owner)) {
            out.add(clone);
         }
      }

      return out;
   }

   private record Options(
      int count,
      int lifetime,
      boolean canAttack,
      boolean canSit,
      boolean followOwner,
      boolean slim,
      boolean inheritEquipment,
      List<class_2960> powers,
      @Nullable class_2960 summonId,
      @Nullable class_2960 wideTexture,
      @Nullable class_2960 slimTexture
   ) {
      static ApoliCloneCommand.Options read(class_2487 tag) {
         return new ApoliCloneCommand.Options(
            clamp(tag.method_10545("count") ? tag.method_10550("count") : 1, 1, 64),
            tag.method_10545("lifetime") ? tag.method_10550("lifetime") : 1200,
            !tag.method_10545("can_attack") || tag.method_10577("can_attack"),
            !tag.method_10545("can_sit") || tag.method_10577("can_sit"),
            !tag.method_10545("follow_owner") || tag.method_10577("follow_owner"),
            tag.method_10577("slim"),
            !tag.method_10545("inherit_equipment") || tag.method_10577("inherit_equipment"),
            readIds(tag),
            readId(tag, "summon_id"),
            readId(tag, "wide_texture"),
            readId(tag, "slim_texture")
         );
      }

      private static int clamp(int value, int min, int max) {
         return value < min ? min : Math.min(value, max);
      }

      @Nullable
      private static class_2960 readId(class_2487 tag, String key) {
         return tag.method_10545(key) ? class_2960.method_12829(tag.method_10558(key)) : null;
      }

      private static List<class_2960> readIds(class_2487 tag) {
         if (tag.method_10573("powers", 8)) {
            class_2960 single = class_2960.method_12829(tag.method_10558("powers"));
            return single == null ? List.of() : List.of(single);
         }

         if (!tag.method_10573("powers", 9)) {
            return List.of();
         }

         class_2499 list = tag.method_10554("powers", 8);
         List<class_2960> out = new ArrayList(list.size());

         for (int i = 0; i < list.size(); i++) {
            class_2960 id = class_2960.method_12829(list.method_10608(i));
            if (id != null) {
               out.add(id);
            }
         }

         return out;
      }
   }
}
