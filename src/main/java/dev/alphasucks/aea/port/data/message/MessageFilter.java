package dev.alphasucks.aea.port.data.message;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.minecraft.class_3542;

public final class MessageFilter {
   private final String filter;
   private final MessageFilter.MatchMode matchMode;
   private final boolean literal;
   private final boolean caseInsensitive;
   private final boolean inverted;
   private final Optional<String> replacement;
   private final Optional<EntityAction> beforeAction;
   private final Optional<EntityAction> afterAction;
   private final boolean prevent;
   private final boolean deferred;
   private volatile Pattern compiled;
   private static final MapCodec<MessageFilter> RECORD_MAP_CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            Codec.STRING.fieldOf("filter").forGetter(f -> f.filter),
            MessageFilter.MatchMode.CODEC.optionalFieldOf("match_mode", MessageFilter.MatchMode.CONTAINS).forGetter(f -> f.matchMode),
            Codec.BOOL.optionalFieldOf("literal", false).forGetter(f -> f.literal),
            Codec.BOOL.optionalFieldOf("case_insensitive", false).forGetter(f -> f.caseInsensitive),
            Codec.BOOL.optionalFieldOf("inverted", false).forGetter(f -> f.inverted),
            Codec.STRING.optionalFieldOf("replacement").forGetter(f -> f.replacement),
            LoggedOptionalField.of("before_action", EntityAction.CODEC).forGetter(f -> f.beforeAction),
            LoggedOptionalField.of("after_action", EntityAction.CODEC).forGetter(f -> f.afterAction),
            Codec.BOOL.optionalFieldOf("prevent", false).forGetter(f -> f.prevent)
         )
         .apply(i, MessageFilter::new)
   );
   public static final MapCodec<MessageFilter> MAP_CODEC = RECORD_MAP_CODEC;
   private static final Codec<MessageFilter> STRING_CODEC = Codec.STRING.xmap(MessageFilter::of, f -> f.filter);
   public static final Codec<MessageFilter> CODEC = Codec.either(STRING_CODEC, RECORD_MAP_CODEC.codec())
      .xmap(either -> (MessageFilter)either.map(Function.identity(), Function.identity()), Either::right);

   public MessageFilter(
      String filter,
      MessageFilter.MatchMode matchMode,
      boolean literal,
      boolean caseInsensitive,
      boolean inverted,
      Optional<String> replacement,
      Optional<EntityAction> beforeAction,
      Optional<EntityAction> afterAction,
      boolean prevent
   ) {
      this.filter = filter;
      this.matchMode = matchMode;
      this.literal = literal;
      this.caseInsensitive = caseInsensitive;
      this.inverted = inverted;
      this.replacement = replacement;
      this.beforeAction = beforeAction;
      this.afterAction = afterAction;
      this.prevent = prevent;
      this.deferred = !literal && TranslationKeyResolver.hasPlaceholder(filter);
      if (this.deferred) {
         TranslationKeyResolver.require(filter);
      } else {
         this.compiled = compile(filter, matchMode, literal, caseInsensitive, false);
      }
   }

   private Pattern pattern() {
      Pattern current = this.compiled;
      if (current != null) {
         return current;
      }

      synchronized (this) {
         if (this.compiled == null) {
            this.compiled = compile(this.filter, this.matchMode, this.literal, this.caseInsensitive, true);
         }

         return this.compiled;
      }
   }

   private static Pattern compile(String filter, MessageFilter.MatchMode matchMode, boolean literal, boolean caseInsensitive, boolean expandPlaceholders) {
      int flags = caseInsensitive ? 66 : 0;
      if (literal) {
         return Pattern.compile(decorate(Pattern.quote(filter), matchMode), flags);
      }

      String expanded = expandPlaceholders ? TranslationKeyResolver.expandPattern(filter) : filter;

      try {
         return Pattern.compile(decorate(expanded, matchMode), flags);
      } catch (PatternSyntaxException e) {
         Apoli.LOGGER
            .warn(
               "[Apoli] Message filter \"{}\" is not valid regex ({}); matching it as literal text. Set \"literal\": true to silence this.",
               filter,
               e.getDescription()
            );
         return Pattern.compile(decorate(Pattern.quote(expanded), matchMode), flags);
      }
   }

   private static String decorate(String pattern, MessageFilter.MatchMode matchMode) {
      return switch (matchMode) {
         case CONTAINS -> pattern;
         case FULL -> "^(?:" + pattern + ")$";
         case WORD -> "\\b(?:" + pattern + ")\\b";
         case STARTS_WITH -> "^(?:" + pattern + ")";
         case ENDS_WITH -> "(?:" + pattern + ")$";
      };
   }

   public static MessageFilter of(String filter) {
      return new MessageFilter(filter, MessageFilter.MatchMode.CONTAINS, false, false, false, Optional.empty(), Optional.empty(), Optional.empty(), false);
   }

   public boolean matches(String message) {
      Matcher matcher = this.pattern().matcher(message);
      return this.matchMode == MessageFilter.MatchMode.FULL ? matcher.matches() : matcher.find();
   }

   public boolean inverted() {
      return this.inverted;
   }

   public String applyReplacement(String message) {
      if (this.replacement.isEmpty()) {
         return message;
      }

      try {
         return this.pattern().matcher(message).replaceAll((String)this.replacement.get());
      } catch (RuntimeException e) {
         Apoli.LOGGER
            .warn("[Apoli] Message filter replacement \"{}\" for pattern \"{}\" failed: {}", new Object[]{this.replacement.get(), this.filter, e.getMessage()});
         return message;
      }
   }

   public boolean hasReplacement() {
      return this.replacement.isPresent();
   }

   public String filter() {
      return this.filter;
   }

   public MessageFilter.MatchMode matchMode() {
      return this.matchMode;
   }

   public Optional<EntityAction> beforeAction() {
      return this.beforeAction;
   }

   public Optional<EntityAction> afterAction() {
      return this.afterAction;
   }

   public boolean prevent() {
      return this.prevent;
   }

   public enum MatchMode implements class_3542 {
      CONTAINS("contains"),
      FULL("full"),
      WORD("word"),
      STARTS_WITH("starts_with"),
      ENDS_WITH("ends_with");

      public static final Codec<MessageFilter.MatchMode> CODEC = class_3542.method_28140(MessageFilter.MatchMode::values);
      private final String name;

      MatchMode(String name) {
         this.name = name;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }
}
