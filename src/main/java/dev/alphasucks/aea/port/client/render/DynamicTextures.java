package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.data.TextureRef;
import dev.alphasucks.aea.port.data.TextureRef.Kind;
import dev.alphasucks.aea.port.data.TextureRef.Subject;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_640;
import net.minecraft.class_742;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class DynamicTextures {
   private DynamicTextures() {
   }

   public static class_2960 resolve(@Nullable class_2960 id, @Nullable class_1297 subject) {
      if (id != null && "apoli".equals(id.method_12836())) {
         Kind kind = TextureRef.kindOf(id);
         if (kind == null) {
            return id;
         }

         class_2960 resolved = lookup(kind, subject);
         return resolved != null ? resolved : id;
      } else {
         return id;
      }
   }

   public static class_1799 stack(@Nullable class_2960 id, @Nullable class_1297 subject) {
      Kind kind = TextureRef.kindOf(id);
      if (kind != null && kind.isItem() && subject instanceof class_1309 living) {
         return kind == Kind.HELD_ITEM ? living.method_6047() : living.method_6079();
      } else {
         return class_1799.field_8037;
      }
   }

   @Nullable
   public static class_1297 subject(TextureRef ref, @Nullable class_1297 self) {
      return (class_1297)(ref.subject() == Subject.VIEWER ? class_310.method_1551().field_1724 : self);
   }

   @Nullable
   private static class_2960 lookup(Kind kind, @Nullable class_1297 subject) {
      if (subject == null) {
         return null;
      }

      return switch (kind) {
         case PLAYER, PLAYER_FACE -> skin(subject);
         case PLAYER_CAPE -> cape(subject);
         case ENTITY -> entityTexture(subject);
         case HELD_ITEM, OFFHAND_ITEM -> null;
      };
   }

   @Nullable
   private static class_2960 entityTexture(class_1297 subject) {
      try {
         return class_310.method_1551().method_1561().method_3953(subject).method_3931(subject);
      } catch (RuntimeException e) {
         return null;
      }
   }

   @Nullable
   private static class_2960 skin(class_1297 subject) {
      return subject instanceof class_742 player ? player.method_3117() : entityTexture(subject);
   }

   @Nullable
   private static class_2960 cape(class_1297 subject) {
      return subject instanceof class_742 player ? player.method_3119() : null;
   }

   public static class_2960 resolve(@Nullable class_2960 id, @Nullable class_1297 subject, @Nullable UUID fallbackPlayer) {
      if (id != null && "apoli".equals(id.method_12836())) {
         Kind kind = TextureRef.kindOf(id);
         if (kind == null) {
            return id;
         }

         class_2960 resolved = lookup(kind, subject);
         if (resolved == null && fallbackPlayer != null) {
            resolved = fromPlayerInfo(kind, fallbackPlayer);
         }

         return resolved != null ? resolved : id;
      } else {
         return id;
      }
   }

   @Nullable
   private static class_640 playerInfo(UUID uuid) {
      class_634 connection = class_310.method_1551().method_1562();
      return connection == null ? null : connection.method_2871(uuid);
   }

   @Nullable
   private static class_2960 fromPlayerInfo(Kind kind, UUID uuid) {
      class_640 info = playerInfo(uuid);
      if (info == null) {
         return null;
      }

      return switch (kind) {
         case PLAYER, PLAYER_FACE, ENTITY -> info.method_2968();
         case PLAYER_CAPE -> info.method_2979();
         default -> null;
      };
   }
}
