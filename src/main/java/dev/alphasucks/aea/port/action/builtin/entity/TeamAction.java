package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.TeamData;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_270;
import net.minecraft.class_3542;
import net.minecraft.server.MinecraftServer;

public final class TeamAction implements ActionType<EntityCtx, TeamAction.Cfg> {
   @Override
   public MapCodec<TeamAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               class_3542.method_28140(TeamAction.Operation::values)
                  .optionalFieldOf("operation", TeamAction.Operation.JOIN)
                  .forGetter(TeamAction.Cfg::operation),
               TeamData.CODEC.optionalFieldOf("team").forGetter(TeamAction.Cfg::team),
               Codec.BOOL.optionalFieldOf("create_if_missing", true).forGetter(TeamAction.Cfg::createIfMissing)
            )
            .apply(i, TeamAction.Cfg::new)
      );
   }

   public void run(TeamAction.Cfg cfg, EntityCtx ctx) {
      MinecraftServer server = ctx.level().method_8503();
      if (server != null) {
         class_1297 entity = ctx.entity();
         class_269 scoreboard = server.method_3845();
         switch (cfg.operation) {
            case JOIN:
               class_268 joinTeam = resolve(scoreboard, cfg, entity);
               if (joinTeam == null) {
                  return;
               }

               cfg.team.ifPresent(data -> data.applyTo(joinTeam));
               scoreboard.method_1172(entity.method_5820(), joinTeam);
               break;
            case LEAVE:
               scoreboard.method_1195(entity.method_5820());
               break;
            case CREATE:
               class_268 createTeam = resolve(scoreboard, cfg, entity);
               if (createTeam != null) {
                  cfg.team.ifPresent(data -> data.applyTo(createTeam));
               }
               break;
            case MODIFY:
               class_268 modifyTeam = target(scoreboard, cfg, entity);
               if (modifyTeam != null) {
                  cfg.team.ifPresent(data -> data.applyTo(modifyTeam));
               }
               break;
            case DELETE:
               class_268 team = target(scoreboard, cfg, entity);
               if (team != null) {
                  scoreboard.method_1191(team);
               }
               break;
            case EMPTY:
               class_268 emptyTeam = target(scoreboard, cfg, entity);
               if (emptyTeam == null) {
                  return;
               }

               for (String member : List.copyOf(emptyTeam.method_1204())) {
                  scoreboard.method_1157(member, emptyTeam);
               }
         }
      }
   }

   private static class_268 resolve(class_269 scoreboard, TeamAction.Cfg cfg, class_1297 entity) {
      String name = teamName(cfg, entity);
      if (name == null) {
         return null;
      } else {
         class_268 existing = scoreboard.method_1153(name);
         if (existing != null) {
            return existing;
         } else {
            return cfg.createIfMissing ? scoreboard.method_1171(name) : null;
         }
      }
   }

   private static class_268 target(class_269 scoreboard, TeamAction.Cfg cfg, class_1297 entity) {
      String name = teamName(cfg, entity);
      return name == null ? null : scoreboard.method_1153(name);
   }

   private static String teamName(TeamAction.Cfg cfg, class_1297 entity) {
      if (cfg.team.isPresent() && ((TeamData)cfg.team.get()).name().isPresent()) {
         return (String)((TeamData)cfg.team.get()).name().get();
      }

      class_270 current = entity.method_5781();
      return current == null ? null : current.method_1197();
   }

   public record Cfg(TeamAction.Operation operation, Optional<TeamData> team, boolean createIfMissing) {
   }

   public enum Operation implements class_3542 {
      JOIN("join"),
      LEAVE("leave"),
      CREATE("create"),
      MODIFY("modify"),
      DELETE("delete"),
      EMPTY("empty");

      private final String id;

      Operation(String id) {
         this.id = id;
      }

      @Override
      public String method_15434() {
         return this.id;
      }
   }
}
