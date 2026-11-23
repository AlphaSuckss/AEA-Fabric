package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.sound.SoundVariants;
import dev.alphasucks.aea.port.sound.Sounds;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_3542;

public final class PlaySoundAction implements ActionType<EntityCtx, PlaySoundAction.Cfg> {
   @Override
   public MapCodec<PlaySoundAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.fieldOf("sound").forGetter(PlaySoundAction.Cfg::sound),
               PlaySoundAction.Category.CODEC.optionalFieldOf("category").forGetter(PlaySoundAction.Cfg::category),
               Codec.FLOAT.optionalFieldOf("volume", 1.0F).forGetter(PlaySoundAction.Cfg::volume),
               Codec.FLOAT.optionalFieldOf("pitch", 1.0F).forGetter(PlaySoundAction.Cfg::pitch),
               Codec.BOOL.optionalFieldOf("follow_entity", false).forGetter(PlaySoundAction.Cfg::followEntity),
               Codec.BOOL.optionalFieldOf("global").forGetter(PlaySoundAction.Cfg::global),
               Codec.BOOL.optionalFieldOf("internal", false).forGetter(PlaySoundAction.Cfg::internal),
               Expression.INT_OR_EXPR.optionalFieldOf("index").forGetter(PlaySoundAction.Cfg::index)
            )
            .apply(i, PlaySoundAction.Cfg::new)
      );
   }

   public void run(PlaySoundAction.Cfg cfg, EntityCtx ctx) {
      class_1297 e = ctx.raw();
      int index = cfg.index.isPresent() ? ((Expression)cfg.index.get()).evalInt(e) : -1;
      class_3414 event;
      if (cfg.isGlobal()) {
         event = class_3414.method_47909(index >= 0 ? SoundVariants.encode(cfg.sound, index) : cfg.sound, Float.MAX_VALUE);
      } else {
         event = index >= 0 ? Sounds.variant(cfg.sound, index) : Sounds.event(cfg.sound);
      }

      class_3419 source = (class_3419)cfg.category.map(PlaySoundAction.Category::vanilla).orElse(e.method_5634());
      class_1657 except = cfg.internal && e instanceof class_1657 player ? player : null;
      if (cfg.followEntity) {
         ctx.level().method_43129(except, e, event, source, cfg.volume, cfg.pitch);
      } else {
         ctx.level().method_43128(except, e.method_23317(), e.method_23318(), e.method_23321(), event, source, cfg.volume, cfg.pitch);
      }
   }

   public enum Category implements class_3542 {
      MASTER("master", class_3419.field_15250),
      MUSIC("music", class_3419.field_15253),
      RECORD("record", class_3419.field_15247),
      WEATHER("weather", class_3419.field_15252),
      BLOCK("block", class_3419.field_15245),
      HOSTILE("hostile", class_3419.field_15251),
      NEUTRAL("neutral", class_3419.field_15254),
      PLAYERS("players", class_3419.field_15248),
      AMBIENT("ambient", class_3419.field_15256),
      VOICE("voice", class_3419.field_15246);

      public static final Codec<PlaySoundAction.Category> CODEC = class_3542.method_28140(PlaySoundAction.Category::values);
      private final String name;
      private final class_3419 vanilla;

      Category(String n, class_3419 v) {
         this.name = n;
         this.vanilla = v;
      }

      @Override
      public String method_15434() {
         return this.name;
      }

      public class_3419 vanilla() {
         return this.vanilla;
      }
   }

   public record Cfg(
      class_2960 sound,
      Optional<PlaySoundAction.Category> category,
      float volume,
      float pitch,
      boolean followEntity,
      Optional<Boolean> global,
      boolean internal,
      Optional<Expression> index
   ) {
      public boolean isGlobal() {
         return (Boolean)this.global.orElse(this.followEntity);
      }
   }
}
