package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import io.netty.buffer.Unpooled;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record SyncPowersChunkS2C(int index, int total, byte[] slice) {
   public static final class_2960 CHANNEL = Apoli.internal("sync_powers_chunk");

   public void write(class_2540 buf) {
      buf.method_10804(this.index);
      buf.method_10804(this.total);
      buf.method_10813(this.slice);
   }

   public static SyncPowersChunkS2C read(class_2540 buf) {
      return new SyncPowersChunkS2C(buf.method_10816(), buf.method_10816(), buf.method_10795());
   }

   public static byte[] encodeBlob(Map<class_2960, String> rawPowers) {
      class_2540 inner = new class_2540(Unpooled.buffer());
      inner.method_10804(rawPowers.size());

      for (Entry<class_2960, String> e : rawPowers.entrySet()) {
         inner.method_10812((class_2960)e.getKey());
         inner.method_10813(((String)e.getValue()).getBytes(StandardCharsets.UTF_8));
      }

      try {
         ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(64, inner.readableBytes() / 8));
         GZIPOutputStream gz = new GZIPOutputStream(out);

         try {
            inner.readBytes(gz, inner.readableBytes());
         } catch (Throwable var12) {
            try {
               gz.close();
            } catch (Throwable var11) {
               var12.addSuppressed(var11);
            }

            throw var12;
         }

         gz.close();
         return out.toByteArray();
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      } finally {
         inner.release();
      }
   }

   public static Map<class_2960, String> decodeBlob(byte[] blob) throws IOException {
      GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(blob));

      byte[] raw;
      try {
         raw = gz.readAllBytes();
      } catch (Throwable var8) {
         try {
            gz.close();
         } catch (Throwable var7) {
            var8.addSuppressed(var7);
         }

         throw var8;
      }

      gz.close();
      class_2540 buf = new class_2540(Unpooled.wrappedBuffer(raw));
      int n = buf.method_10816();
      HashMap out = new HashMap(n);

      for (int i = 0; i < n; i++) {
         class_2960 id = buf.method_10810();
         out.put(id, new String(buf.method_10795(), StandardCharsets.UTF_8));
      }

      return out;
   }
}
