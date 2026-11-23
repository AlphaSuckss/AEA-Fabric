package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.data.Space;
import java.util.Optional;
import net.minecraft.class_2394;
import net.minecraft.class_2396;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public record AnchoredParticlesS2C(
   int entityId,
   String part,
   int space,
   float offsetX,
   float offsetY,
   float offsetZ,
   float velocityX,
   float velocityY,
   float velocityZ,
   int count,
   float spreadX,
   float spreadY,
   float spreadZ,
   float speed,
   boolean force,
   class_2394 particle
) {
   public static final class_2960 CHANNEL = Apoli.internal("anchored_particles");

   public static int spaceId(Optional<Space> space) {
      return (Integer)space.map(Enum::ordinal).orElse(-1);
   }

   public Optional<Space> spaceValue() {
      Space[] values = Space.values();
      return this.space >= 0 && this.space < values.length ? Optional.of(values[this.space]) : Optional.empty();
   }

   public void write(class_2540 buf) {
      buf.method_10804(this.entityId);
      buf.method_10814(this.part);
      buf.method_10804(this.space + 1);
      buf.writeFloat(this.offsetX);
      buf.writeFloat(this.offsetY);
      buf.writeFloat(this.offsetZ);
      buf.writeFloat(this.velocityX);
      buf.writeFloat(this.velocityY);
      buf.writeFloat(this.velocityZ);
      buf.method_10804(this.count);
      buf.writeFloat(this.spreadX);
      buf.writeFloat(this.spreadY);
      buf.writeFloat(this.spreadZ);
      buf.writeFloat(this.speed);
      buf.writeBoolean(this.force);
      buf.method_42065(class_7923.field_41180, this.particle.method_10295());
      this.particle.method_10294(buf);
   }

   public static AnchoredParticlesS2C read(class_2540 buf) {
      int entityId = buf.method_10816();
      String part = buf.method_19772();
      int space = buf.method_10816() - 1;
      float offsetX = buf.readFloat();
      float offsetY = buf.readFloat();
      float offsetZ = buf.readFloat();
      float velocityX = buf.readFloat();
      float velocityY = buf.readFloat();
      float velocityZ = buf.readFloat();
      int count = buf.method_10816();
      float spreadX = buf.readFloat();
      float spreadY = buf.readFloat();
      float spreadZ = buf.readFloat();
      float speed = buf.readFloat();
      boolean force = buf.readBoolean();
      class_2394 particle = readParticle(buf, buf.method_42064(class_7923.field_41180));
      return new AnchoredParticlesS2C(
         entityId, part, space, offsetX, offsetY, offsetZ, velocityX, velocityY, velocityZ, count, spreadX, spreadY, spreadZ, speed, force, particle
      );
   }

   private static <T extends class_2394> T readParticle(class_2540 buf, class_2396<T> type) {
      return type.method_10298().method_10297(type, buf);
   }
}
