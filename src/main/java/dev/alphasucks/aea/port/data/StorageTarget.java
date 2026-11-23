package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import java.util.List;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import net.minecraft.class_4565;
import net.minecraft.server.MinecraftServer;

public record StorageTarget(class_2960 storage, String path, boolean nbt, boolean merge) {
   public static final MapCodec<StorageTarget> CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            IdCodecs.ID.fieldOf("storage").forGetter(StorageTarget::storage),
            Codec.STRING.optionalFieldOf("path", "").forGetter(StorageTarget::path),
            Codec.BOOL.optionalFieldOf("nbt", true).forGetter(StorageTarget::nbt),
            Codec.BOOL.optionalFieldOf("merge", false).forGetter(StorageTarget::merge)
         )
         .apply(i, StorageTarget::new)
   );

   public void write(MinecraftServer server, class_2487 value) {
      if (server != null) {
         class_4565 storage = server.method_22827();
         class_2487 root = storage.method_22546(this.storage);
         class_2487 holder = root;
         String[] keys = split(this.path);

         for (int k = 0; k < keys.length - 1; k++) {
            if (holder.method_10580(keys[k]) instanceof class_2487 compound) {
               holder = compound;
            } else {
               class_2487 created = new class_2487();
               holder.method_10566(keys[k], created);
               holder = created;
            }
         }

         if (keys.length == 0) {
            if (!this.merge) {
               for (String key : List.copyOf(root.method_10541())) {
                  root.method_10551(key);
               }
            }

            root.method_10543(value);
         } else {
            String leaf = keys[keys.length - 1];
            if (this.merge && holder.method_10580(leaf) instanceof class_2487 existing) {
               existing.method_10543(value);
            } else {
               holder.method_10566(leaf, value);
            }
         }

         storage.method_22547(this.storage, root);
      }
   }

   public static class_2487 read(MinecraftServer server, class_2960 storage, String path) {
      if (server == null) {
         return null;
      }

      class_2487 current = server.method_22827().method_22546(storage);

      for (String key : split(path)) {
         if (!(current.method_10580(key) instanceof class_2487 compound)) {
            return null;
         }

         current = compound;
      }

      return current;
   }

   private static String[] split(String path) {
      return path != null && !path.isEmpty() ? path.split("\\.") : new String[0];
   }
}
