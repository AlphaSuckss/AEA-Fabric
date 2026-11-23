package dev.alphasucks.aea.port.scale;

import io.netty.buffer.Unpooled;
import net.minecraft.class_1297;
import net.minecraft.class_2540;

public final class ScaleSync {
   private ScaleSync() {
   }

   public static byte[] encode(class_1297 entity) {
      ScaleState state = Scales.stateOf(entity);
      class_2540 buf = new class_2540(Unpooled.buffer());

      try {
         if (state == null) {
            buf.method_10804(0);
         } else {
            state.write(buf);
         }

         byte[] out = new byte[buf.readableBytes()];
         buf.readBytes(out);
         return out;
      } finally {
         buf.release();
      }
   }

   public static void decode(class_1297 entity, byte[] data) {
      class_2540 buf = new class_2540(Unpooled.wrappedBuffer(data));

      try {
         ScaleState state = Scales.stateOrCreate(entity);
         state.read(buf);
         Scales.refreshDimensions(entity, state);
      } finally {
         buf.release();
      }
   }
}
