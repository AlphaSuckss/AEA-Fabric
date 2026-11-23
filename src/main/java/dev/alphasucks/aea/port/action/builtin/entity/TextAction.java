package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.TextBar;
import dev.alphasucks.aea.port.data.TextComponent;
import dev.alphasucks.aea.port.network.payload.TextDisplayS2C;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_3222;
import net.minecraft.class_5250;

public final class TextAction implements ActionType<EntityCtx, TextAction.Cfg> {
   private static final Codec<TextAction.Segment> SEGMENT_CODEC = RecordCodecBuilder.create(
      i -> i.group(
            TextComponent.CODEC.fieldOf("text").forGetter(TextAction.Segment::text),
            LoggedOptionalField.strict("condition", EntityCondition.CODEC).forGetter(TextAction.Segment::condition)
         )
         .apply(i, TextAction.Segment::new)
   );
   private static final Codec<List<TextAction.Segment>> TEXT_CODEC = Codec.either(
         SEGMENT_CODEC.listOf(), TextComponent.CODEC.xmap(c -> new TextAction.Segment(c, Optional.empty()), TextAction.Segment::text)
      )
      .xmap(either -> (List)either.map(Function.identity(), List::of), list -> Either.left(list));
   private static final Map<UUID, TextAction.BarState[]> STATES = new ConcurrentHashMap();

   @Override
   public MapCodec<TextAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               TextBar.CODEC.optionalFieldOf("bar", TextBar.ACTIONBAR).forGetter(TextAction.Cfg::bar),
               TEXT_CODEC.fieldOf("text").forGetter(TextAction.Cfg::text),
               Codec.BOOL.optionalFieldOf("bold", false).forGetter(TextAction.Cfg::bold),
               Codec.BOOL.optionalFieldOf("italic", false).forGetter(TextAction.Cfg::italic),
               Codec.BOOL.optionalFieldOf("underlined", false).forGetter(TextAction.Cfg::underlined),
               Codec.BOOL.optionalFieldOf("strikethrough", false).forGetter(TextAction.Cfg::strikethrough),
               Codec.BOOL.optionalFieldOf("obfuscated", false).forGetter(TextAction.Cfg::obfuscated),
               Codec.INT.optionalFieldOf("fadein", 10).forGetter(TextAction.Cfg::fadeIn),
               Codec.INT.optionalFieldOf("duration", 70).forGetter(TextAction.Cfg::duration),
               Codec.INT.optionalFieldOf("fadeout", 20).forGetter(TextAction.Cfg::fadeOut)
            )
            .apply(i, TextAction.Cfg::new)
      );
   }

   public void run(TextAction.Cfg cfg, EntityCtx ctx) {
      if (ctx.raw() instanceof class_3222 player) {
         class_2561 var5 = compose(cfg, ctx);
         sendDeduped(player, cfg, var5);
      }
   }

   private static class_2561 compose(TextAction.Cfg cfg, EntityCtx ctx) {
      class_5250 out = class_2561.method_43473();
      class_2583 base = baseStyle(cfg);
      if (base != class_2583.field_24360) {
         out.method_10862(base);
      }

      for (int i = 0; i < cfg.text.size(); i++) {
         TextAction.Segment segment = (TextAction.Segment)cfg.text.get(i);
         if (!segment.condition().isPresent() || ((EntityCondition)segment.condition().get()).test(ctx)) {
            out.method_10852(segment.text());
         }
      }

      return out;
   }

   private static class_2583 baseStyle(TextAction.Cfg cfg) {
      class_2583 style = class_2583.field_24360;
      if (cfg.bold) {
         style = style.method_27706(class_124.field_1067);
      }

      if (cfg.italic) {
         style = style.method_27706(class_124.field_1056);
      }

      if (cfg.underlined) {
         style = style.method_27706(class_124.field_1073);
      }

      if (cfg.strikethrough) {
         style = style.method_27706(class_124.field_1055);
      }

      if (cfg.obfuscated) {
         style = style.method_27706(class_124.field_1051);
      }

      return style;
   }

   private static void sendDeduped(class_3222 player, TextAction.Cfg cfg, class_2561 composed) {
      TextAction.BarState[] states = (TextAction.BarState[])STATES.computeIfAbsent(player.method_5667(), k -> new TextAction.BarState[TextBar.VALUES.length]);
      int slot = cfg.bar.ordinal();
      TextAction.BarState state = states[slot];
      if (state == null) {
         states[slot] = state = new TextAction.BarState();
      }

      long now = player.method_51469().method_8510();
      boolean permanent = cfg.duration < 0;
      boolean changed = state.lastText == null
         || !composed.equals(state.lastText)
         || state.fadeIn != cfg.fadeIn
         || state.stay != cfg.duration
         || state.fadeOut != cfg.fadeOut;
      if (changed || !permanent && now >= state.refreshAt) {
         state.lastText = composed;
         state.fadeIn = cfg.fadeIn;
         state.stay = cfg.duration;
         state.fadeOut = cfg.fadeOut;
         state.refreshAt = permanent ? Long.MAX_VALUE : now + Math.max(1, cfg.duration / 2);
         ApoliNetwork.sendTextDisplay(player, new TextDisplayS2C(cfg.bar, composed, cfg.fadeIn, cfg.duration, cfg.fadeOut));
      }
   }

   public static void onPlayerLeave(UUID player) {
      STATES.remove(player);
   }

   private static final class BarState {
      class_2561 lastText;
      int fadeIn;
      int stay;
      int fadeOut;
      long refreshAt = Long.MAX_VALUE;
   }

   public record Cfg(
      TextBar bar,
      List<TextAction.Segment> text,
      boolean bold,
      boolean italic,
      boolean underlined,
      boolean strikethrough,
      boolean obfuscated,
      int fadeIn,
      int duration,
      int fadeOut
   ) {
   }

   public record Segment(class_2561 text, Optional<EntityCondition> condition) {
   }
}
