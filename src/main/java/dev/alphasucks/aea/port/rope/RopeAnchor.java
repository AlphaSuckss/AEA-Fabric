package dev.alphasucks.aea.port.rope;

import dev.alphasucks.aea.port.compat.sable.SableSubLevels;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_2540;
import org.jetbrains.annotations.Nullable;

public sealed interface RopeAnchor permits RopeAnchor.Position, RopeAnchor.OfEntity, RopeAnchor.OfSubLevel {
   byte KIND_POSITION = 0;
   byte KIND_ENTITY = 1;
   byte KIND_SUB_LEVEL = 2;

   @Nullable
   class_243 position(class_1937 var1);

   @Nullable
   class_1297 entity(class_1937 var1);

   boolean movable();

   void write(class_2540 var1);

   static RopeAnchor read(class_2540 buf) {
      return switch (buf.readByte()) {
         case 1 -> new RopeAnchor.OfEntity(buf.method_10816(), readVec(buf));
         case 2 -> new RopeAnchor.OfSubLevel(buf.method_10790(), readVec(buf));
         default -> new RopeAnchor.Position(readVec(buf));
      };
   }

   private static class_243 readVec(class_2540 buf) {
      return new class_243(buf.readDouble(), buf.readDouble(), buf.readDouble());
   }

   private static void writeVec(class_2540 buf, class_243 v) {
      buf.writeDouble(v.field_1352);
      buf.writeDouble(v.field_1351);
      buf.writeDouble(v.field_1350);
   }

   record OfEntity(int networkId, class_243 offset) implements RopeAnchor {
      @Override
      public class_243 position(class_1937 level) {
         class_1297 e = this.entity(level);
         return e == null ? null : e.method_5829().method_1005().method_1019(this.offset);
      }

      @Override
      public class_1297 entity(class_1937 level) {
         return level.method_8469(this.networkId);
      }

      @Override
      public boolean movable() {
         return true;
      }

      @Override
      public void write(class_2540 buf) {
         buf.writeByte(1);
         buf.method_10804(this.networkId);
         RopeAnchor.writeVec(buf, this.offset);
      }
   }

   record OfSubLevel(UUID subLevel, class_243 local) implements RopeAnchor {
      @Nullable
      @Override
      public class_243 position(class_1937 level) {
         return SableSubLevels.toWorld(level, this.subLevel, this.local);
      }

      @Override
      public class_1297 entity(class_1937 level) {
         return null;
      }

      @Override
      public boolean movable() {
         return true;
      }

      @Override
      public void write(class_2540 buf) {
         buf.writeByte(2);
         buf.method_10797(this.subLevel);
         RopeAnchor.writeVec(buf, this.local);
      }
   }

   record Position(class_243 pos) implements RopeAnchor {
      @Override
      public class_243 position(class_1937 level) {
         return this.pos;
      }

      @Override
      public class_1297 entity(class_1937 level) {
         return null;
      }

      @Override
      public boolean movable() {
         return false;
      }

      @Override
      public void write(class_2540 buf) {
         buf.writeByte(0);
         RopeAnchor.writeVec(buf, this.pos);
      }
   }
}
