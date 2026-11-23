package dev.alphasucks.aea.port.power.builtin;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.power.builtin.ActionOnSendingMessagePower.Result;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_7924;
import net.minecraft.class_2556.class_7602;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class MessagePowerHandler {
   private MessagePowerHandler() {
   }

   public static boolean handle(@Nullable class_1297 holder, @Nullable class_3222 speaker, String content, class_7602 params) {
      if (holder == null) {
         return true;
      }

      try {
         return run(holder, speaker, content, params);
      } catch (Throwable t) {
         Apoli.LOGGER
            .error(
               "[Apoli] action_on_sending_message failed while handling \"{}\" from {}; letting the message through.",
               new Object[]{content, holder.method_5477().getString(), t}
            );
         return true;
      }
   }

   private static boolean run(class_1297 holder, @Nullable class_3222 speaker, String content, class_7602 params) {
      class_2960 typeId = holder.method_37908().method_30349().method_30530(class_7924.field_41237).method_10221(params.comp_919());
      Result result = ActionOnSendingMessagePower.fire(holder, content, typeId);
      if (result.prevented()) {
         return false;
      }

      String replaced = result.message();
      if (replaced == null) {
         return true;
      }

      MinecraftServer server = holder.method_5682();
      if (server == null) {
         return true;
      }

      class_2561 decorated = params.method_44837(class_2561.method_43470(replaced));
      server.method_3760().method_43514(decorated, false);
      if (speaker != null && !server.method_3760().method_14571().contains(speaker)) {
         speaker.method_43496(decorated);
      }

      return false;
   }
}
