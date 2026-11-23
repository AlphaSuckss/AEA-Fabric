package dev.alphasucks.aea.port.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_268;
import net.minecraft.class_270;
import net.minecraft.class_270.class_271;
import net.minecraft.class_270.class_272;
import org.jetbrains.annotations.Nullable;

public record TeamData(
   Optional<String> name,
   Optional<Boolean> friendlyFire,
   Optional<Boolean> seeFriendlyInvisibles,
   Optional<class_272> nametagVisibility,
   Optional<class_272> deathMessageVisibility,
   Optional<class_271> collisionRule,
   Optional<class_124> color
) {
   public static final Codec<class_272> VISIBILITY = enumCodec(class_272.values(), v -> v.field_1445, class_272::method_1213, "nametag visibility");
   public static final Codec<class_271> COLLISION_RULE = enumCodec(class_271.values(), r -> r.field_1436, class_271::method_1210, "collision rule");
   public static final Codec<class_124> COLOR = Codec.STRING.comapFlatMap(raw -> {
      class_124 formatting = class_124.method_533(raw);
      return formatting == null ? DataResult.error(() -> "Unknown team color: " + raw) : DataResult.success(formatting);
   }, class_124::method_537);
   private static final Map<String, String> LEGACY_FIELDS = Map.of(
      "friendlyFire",
      "friendly_fire",
      "showFriendlyInvisibles",
      "see_friendly_invisibles",
      "seeFriendlyInvisibles",
      "see_friendly_invisibles",
      "nametagVisibility",
      "nametag_visibility",
      "nameTagVisibility",
      "nametag_visibility",
      "deathMessageVisibility",
      "death_message_visibility",
      "collisionRule",
      "collision_rule"
   );
   private static final MapCodec<TeamData> MAP_CODEC = AliasingMapCodec.wrap(
      RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.STRING.optionalFieldOf("name").forGetter(TeamData::name),
               Codec.BOOL.optionalFieldOf("friendly_fire").forGetter(TeamData::friendlyFire),
               Codec.BOOL.optionalFieldOf("see_friendly_invisibles").forGetter(TeamData::seeFriendlyInvisibles),
               VISIBILITY.optionalFieldOf("nametag_visibility").forGetter(TeamData::nametagVisibility),
               VISIBILITY.optionalFieldOf("death_message_visibility").forGetter(TeamData::deathMessageVisibility),
               COLLISION_RULE.optionalFieldOf("collision_rule").forGetter(TeamData::collisionRule),
               COLOR.optionalFieldOf("color").forGetter(TeamData::color)
            )
            .apply(i, TeamData::new)
      ),
      LEGACY_FIELDS
   );
   public static final Codec<TeamData> CODEC = Codec.either(Codec.STRING, MAP_CODEC.codec())
      .xmap(
         either -> (TeamData)either.map(TeamData::named, data -> data),
         data -> data.isNameOnly() ? Either.left((String)data.name().orElseThrow()) : Either.right(data)
      );
   public static final Codec<List<TeamData>> LIST_OR_SINGLE = Codec.either(CODEC, Codec.list(CODEC))
      .xmap(either -> (List)either.map(List::of, list -> list), list -> list.size() == 1 ? Either.left((TeamData)list.get(0)) : Either.right(list));

   public static TeamData named(String name) {
      return new TeamData(Optional.of(name), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
   }

   public boolean isNameOnly() {
      return this.name.isPresent()
         && this.friendlyFire.isEmpty()
         && this.seeFriendlyInvisibles.isEmpty()
         && this.nametagVisibility.isEmpty()
         && this.deathMessageVisibility.isEmpty()
         && this.collisionRule.isEmpty()
         && this.color.isEmpty();
   }

   public boolean isEmpty() {
      return this.name.isEmpty()
         && this.friendlyFire.isEmpty()
         && this.seeFriendlyInvisibles.isEmpty()
         && this.nametagVisibility.isEmpty()
         && this.deathMessageVisibility.isEmpty()
         && this.collisionRule.isEmpty()
         && this.color.isEmpty();
   }

   public boolean matches(@Nullable class_270 team) {
      if (team == null) {
         return false;
      } else if (this.isEmpty()) {
         return true;
      } else if (this.name.isPresent() && !((String)this.name.get()).equals(team.method_1197())) {
         return false;
      } else if (this.friendlyFire.isPresent() && (Boolean)this.friendlyFire.get() != team.method_1205()) {
         return false;
      } else if (this.seeFriendlyInvisibles.isPresent() && (Boolean)this.seeFriendlyInvisibles.get() != team.method_1199()) {
         return false;
      } else if (this.nametagVisibility.isPresent() && this.nametagVisibility.get() != team.method_1201()) {
         return false;
      } else if (this.deathMessageVisibility.isPresent() && this.deathMessageVisibility.get() != team.method_1200()) {
         return false;
      } else {
         return this.collisionRule.isPresent() && this.collisionRule.get() != team.method_1203()
            ? false
            : this.color.isEmpty() || this.color.get() == team.method_1202();
      }
   }

   public boolean matches(@Nullable class_1297 entity) {
      return entity != null && this.matches(entity.method_5781());
   }

   public void applyTo(class_268 team) {
      this.friendlyFire.ifPresent(team::method_1135);
      this.seeFriendlyInvisibles.ifPresent(team::method_1143);
      this.nametagVisibility.ifPresent(team::method_1149);
      this.deathMessageVisibility.ifPresent(team::method_1133);
      this.collisionRule.ifPresent(team::method_1145);
      this.color.ifPresent(team::method_1141);
   }

   private static <E extends Enum<E>> Codec<E> enumCodec(E[] values, Function<E, String> naming, Function<String, E> byName, String label) {
      return Codec.STRING.comapFlatMap(raw -> {
         E direct = (E)byName.apply(raw);
         if (direct != null) {
            return DataResult.success(direct);
         }

         String normalised = raw.replace("_", "").toLowerCase(Locale.ROOT);

         for (E value : values) {
            if (((String)naming.apply(value)).toLowerCase(Locale.ROOT).equals(normalised)) {
               return DataResult.success(value);
            }
         }

         return DataResult.error(() -> "Unknown " + label + ": " + raw);
      }, naming);
   }
}
