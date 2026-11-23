package dev.alphasucks.aea.port.data;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.particle.CustomParticleOptions;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2394;
import net.minecraft.class_2396;
import net.minecraft.class_2398;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import org.jetbrains.annotations.Nullable;

public final class ParticleEffect {
   public static final ParticleEffect EMPTY = new ParticleEffect(new Dynamic(JsonOps.INSTANCE).createString("minecraft:poof"));
   public static final Codec<ParticleEffect> CODEC = Codec.PASSTHROUGH.xmap(ParticleEffect::new, ParticleEffect::raw);
   private final Dynamic<?> raw;
   @Nullable
   private volatile ParticleEffect.Resolved resolved;

   public ParticleEffect(Dynamic<?> raw) {
      this.raw = raw;
   }

   public Dynamic<?> raw() {
      return this.raw;
   }

   @Nullable
   public class_2394 resolve(class_1937 level, @Nullable class_1297 actor) {
      class_2394 options = this.resolve(level);
      return options instanceof CustomParticleOptions custom && custom.dynamic() ? custom.bake(actor) : options;
   }

   @Nullable
   public class_2394 resolve(class_1937 level) {
      ParticleEffect.Resolved hit = this.resolved;
      if (hit != null) {
         return hit.options();
      }

      class_2394 built = this.parse();
      if (built == null) {
         Apoli.LOGGER.warn("[Apoli] Could not read the particle {} — no particle will be spawned where it is used.", this.raw);
      }

      this.resolved = new ParticleEffect.Resolved(built);
      return built;
   }

   @Nullable
   private class_2394 parse() {
      Dynamic<JsonElement> data = this.raw.convert(JsonOps.INSTANCE);
      String simple = (String)data.asString().result().orElse(null);
      if (simple != null) {
         return fromCommand(simple, "");
      }

      if (data.getMapValues().result().isEmpty()) {
         return null;
      }

      String params = (String)data.get("params").asString().result().orElse(null);
      return params != null
         ? fromCommand((String)data.get("type").asString().result().orElse(""), params)
         : (class_2394)class_2398.field_25125.parse(data).result().orElse(null);
   }

   @Nullable
   private static class_2394 fromCommand(String rawType, String params) {
      class_2960 id = class_2960.method_12829(rawType);
      if (id == null) {
         return null;
      }

      class_2396<?> particleType = class_7923.field_41180.method_10223(id);
      return particleType == null ? null : readUnchecked(particleType, params);
   }

   @Nullable
   private static class_2394 readUnchecked(class_2396<?> rawType, String params) {
      try {
         class_2396 type = rawType;
         String command = !params.isEmpty() && !Character.isWhitespace(params.charAt(0)) ? " " + params : params;
         StringReader reader = new StringReader(command);
         return type.method_10298().method_10296(type, reader);
      } catch (Exception e) {
         return null;
      }
   }

   public boolean equals(Object other) {
      return this == other || other instanceof ParticleEffect effect && this.raw.equals(effect.raw);
   }

   public int hashCode() {
      return this.raw.hashCode();
   }

   public String toString() {
      return "ParticleEffect[" + this.raw + "]";
   }

   private record Resolved(@Nullable class_2394 options) {
   }
}
