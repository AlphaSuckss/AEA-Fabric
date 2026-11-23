package dev.alphasucks.aea.port.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DataResult.PartialResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.class_2960;
import net.minecraft.class_3542;
import org.jetbrains.annotations.Nullable;

public record TextureRef(class_2960 texture, TextureRef.Subject subject, Optional<class_2960> set) {
   private static final String SENTINEL_PREFIX = "dynamic_texture/";
   public static final Codec<class_2960> ID_CODEC = Codec.STRING.comapFlatMap(TextureRef::parseId, class_2960::toString);
   private static final MapCodec<TextureRef.Raw> RAW = RecordCodecBuilder.mapCodec(
      i -> i.group(
            Codec.STRING.fieldOf("texture").forGetter(TextureRef.Raw::texture),
            Codec.STRING.optionalFieldOf("selector", "@s").forGetter(TextureRef.Raw::selector),
            LoggedOptionalField.strict("set", IdCodecs.ID).forGetter(TextureRef.Raw::set)
         )
         .apply(i, TextureRef.Raw::new)
   );
   public static final MapCodec<TextureRef> MAP_CODEC = RAW.flatXmap(TextureRef::fromRaw, TextureRef::toRaw);
   public static final Codec<TextureRef> CODEC = Codec.either(Codec.STRING, MAP_CODEC.codec())
      .comapFlatMap(
         either -> either.<DataResult<TextureRef>>map(
            string -> parseId(string).map(id -> new TextureRef(id, TextureRef.Subject.SELF, Optional.empty())), DataResult::success
         ),
         (TextureRef ref) -> ref.subject == TextureRef.Subject.SELF && ref.set.isEmpty()
            ? Either.<String, TextureRef>left(ref.texture.toString())
            : Either.<String, TextureRef>right(ref)
      );

   @Nullable
   public static TextureRef.Kind kindOf(@Nullable class_2960 id) {
      if (id != null && "apoli".equals(id.method_12836())) {
         String path = id.method_12832();
         if (!path.startsWith("dynamic_texture/")) {
            return null;
         }

         String keyword = path.substring("dynamic_texture/".length());

         for (TextureRef.Kind kind : TextureRef.Kind.values()) {
            if (kind.keyword.equals(keyword)) {
               return kind;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static boolean isDynamic(@Nullable class_2960 id) {
      return kindOf(id) != null;
   }

   @Nullable
   public TextureRef.Kind kind() {
      return kindOf(this.texture);
   }

   private static DataResult<class_2960> parseId(String raw) {
      TextureRef.Kind keyword = keyword(raw);
      if (keyword != null) {
         return DataResult.success(keyword.sentinel());
      }

      class_2960 parsed = class_2960.method_12829(raw);
      return parsed != null ? DataResult.success(parsed) : DataResult.error(() -> "Not a texture id or a dynamic texture keyword: " + raw);
   }

   @Nullable
   private static TextureRef.Kind keyword(String raw) {
      String normalised = raw.trim().toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');

      return switch (normalised) {
         case "player", "skin", "player_skin" -> TextureRef.Kind.PLAYER;
         case "player_face", "face", "head" -> TextureRef.Kind.PLAYER_FACE;
         case "player_cape", "cape", "cloak" -> TextureRef.Kind.PLAYER_CAPE;
         case "entity", "entity_texture" -> TextureRef.Kind.ENTITY;
         case "held_item", "item", "main_hand", "mainhand" -> TextureRef.Kind.HELD_ITEM;
         case "offhand_item", "off_hand", "offhand" -> TextureRef.Kind.OFFHAND_ITEM;
         default -> null;
      };
   }

   private static DataResult<TextureRef> fromRaw(TextureRef.Raw raw) {
      DataResult<class_2960> id = parseId(raw.texture());
      if (id.error().isPresent()) {
         return DataResult.error(() -> ((PartialResult)id.error().get()).message());
      }

      TextureRef.Subject subject = subject(raw.selector());
      return subject == null
         ? DataResult.error(
            () -> "Unsupported selector '"
               + raw.selector()
               + "'; textures resolve on the client, so only @s (the power holder) and @p (the viewing player) work"
         )
         : DataResult.success(new TextureRef((class_2960)id.result().get(), subject, raw.set()));
   }

   private static DataResult<TextureRef.Raw> toRaw(TextureRef ref) {
      return DataResult.success(new TextureRef.Raw(ref.texture.toString(), ref.subject.method_15434(), ref.set));
   }

   @Nullable
   private static TextureRef.Subject subject(String selector) {
      return switch (selector.trim().toLowerCase(Locale.ROOT)) {
         case "@s", "self", "holder" -> TextureRef.Subject.SELF;
         case "@p", "viewer", "client", "@a" -> TextureRef.Subject.VIEWER;
         default -> null;
      };
   }

   public enum Kind {
      PLAYER("player"),
      PLAYER_FACE("player_face"),
      PLAYER_CAPE("player_cape"),
      ENTITY("entity"),
      HELD_ITEM("held_item"),
      OFFHAND_ITEM("offhand_item");

      private final String keyword;
      private final class_2960 sentinel;

      Kind(String keyword) {
         this.keyword = keyword;
         this.sentinel = Apoli.id("dynamic_texture/" + keyword);
      }

      public String keyword() {
         return this.keyword;
      }

      public class_2960 sentinel() {
         return this.sentinel;
      }

      public boolean isItem() {
         return this == HELD_ITEM || this == OFFHAND_ITEM;
      }
   }

   private record Raw(String texture, String selector, Optional<class_2960> set) {
   }

   public enum Subject implements class_3542 {
      SELF("@s"),
      VIEWER("@p");

      private final String name;

      Subject(String name) {
         this.name = name;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }
}
