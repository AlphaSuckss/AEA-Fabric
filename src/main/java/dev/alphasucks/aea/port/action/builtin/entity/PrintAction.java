package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.TextComponent;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PrintAction implements ActionType<EntityCtx, PrintAction.Cfg> {
   private static final String DEFAULT_MESSAGE_ID = "apoli/print";

   @Override
   public MapCodec<PrintAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("text", TextComponent.CODEC).forGetter(PrintAction.Cfg::text),
               Codec.STRING.optionalFieldOf("message_id", "apoli/print").forGetter(PrintAction.Cfg::messageId),
               LoggedOptionalField.of("number", Expression.DOUBLE_OR_EXPR).forGetter(PrintAction.Cfg::number),
               Codec.BOOL.optionalFieldOf("show_in_chat", false).forGetter(PrintAction.Cfg::showInChat)
            )
            .apply(i, PrintAction.Cfg::new)
      );
   }

   public void run(PrintAction.Cfg cfg, EntityCtx ctx) {
      class_1297 entity = ctx.raw();
      class_3222 viewer = cfg.showInChat && entity instanceof class_3222 player ? player : null;
      boolean toLog = cfg.logger.isInfoEnabled();
      if (viewer != null || toLog) {
         class_2561 text = (class_2561)cfg.text.orElse(null);
         String number = cfg.number.isPresent() ? format(((Expression)cfg.number.get()).eval(entity)) : null;
         if (toLog) {
            if (text != null && number != null) {
               cfg.logger.info("Text: {} Number: {}", text.getString(), number);
            } else if (text != null) {
               cfg.logger.info("{}", text.getString());
            } else if (number != null) {
               cfg.logger.info("{}", number);
            } else {
               cfg.logger.info("{}", cfg.messageId);
            }
         }

         if (viewer != null) {
            viewer.method_43496(chatMessage(cfg, text, number));
         }
      }
   }

   private static class_2561 chatMessage(PrintAction.Cfg cfg, @Nullable class_2561 text, @Nullable String number) {
      if (text != null && number != null) {
         return class_2561.method_43470("Text: ").method_10852(text).method_27693(" Number: " + number);
      } else if (text != null) {
         return text;
      } else {
         return number != null ? class_2561.method_43470(number) : class_2561.method_43470(cfg.messageId);
      }
   }

   private static String format(double value) {
      return value == (long)value ? Long.toString((long)value) : Double.toString(value);
   }

   public record Cfg(Optional<class_2561> text, String messageId, Optional<Expression> number, boolean showInChat, Logger logger) {
      public Cfg(Optional<class_2561> text, String messageId, Optional<Expression> number, boolean showInChat) {
         this(text, messageId, number, showInChat, LoggerFactory.getLogger(messageId));
      }
   }
}
