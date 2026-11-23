package dev.alphasucks.aea.port.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public record HudRender(List<HudRender.Entry> entries) {
   public static final class_2960 DEFAULT_SPRITE_LOCATION = new class_2960("apoli", "textures/gui/resource_bar.png");
   public static final HudRender.Entry HIDDEN = new HudRender.Entry(
      false, DEFAULT_SPRITE_LOCATION, 0, 0, Optional.empty(), false, Optional.empty(), Optional.empty(), List.of()
   );
   public static final HudRender DONT_RENDER = new HudRender(List.of(HIDDEN));
   public static final Codec<HudRender> CODEC = Codec.either(HudRender.Entry.CODEC, Codec.list(HudRender.Entry.CODEC))
      .xmap(
         either -> (HudRender)either.map(e -> new HudRender(List.of(e)), HudRender::new),
         hr -> hr.entries.size() == 1 ? Either.left((HudRender.Entry)hr.entries.get(0)) : Either.right(hr.entries)
      );

   @Nullable
   public HudRender.Entry selectEntry(EntityCtx ctx) {
      for (HudRender.Entry e : this.entries) {
         if (e.shouldRender && (!e.condition.isPresent() || ((EntityCondition)e.condition.get()).test(ctx))) {
            return e;
         }
      }

      return null;
   }

   public Optional<HudRender.Entry> select(EntityCtx ctx) {
      return Optional.ofNullable(this.selectEntry(ctx));
   }

   public boolean shouldRender() {
      for (HudRender.Entry e : this.entries) {
         if (e.shouldRender) {
            return true;
         }
      }

      return false;
   }

   public record Entry(
      boolean shouldRender,
      class_2960 spriteLocation,
      int barIndex,
      int iconIndex,
      Optional<EntityCondition> condition,
      boolean inverted,
      Optional<Integer> order,
      Optional<Expression> max,
      List<HudRender.Look> fallback
   ) {
      public static final Codec<HudRender.Entry> CODEC = RecordCodecBuilder.create(
         i -> i.group(
               Codec.BOOL.optionalFieldOf("should_render", true).forGetter(HudRender.Entry::shouldRender),
               IdCodecs.ID.optionalFieldOf("sprite_location", HudRender.DEFAULT_SPRITE_LOCATION).forGetter(HudRender.Entry::spriteLocation),
               Codec.INT.optionalFieldOf("bar_index", 0).forGetter(HudRender.Entry::barIndex),
               Codec.INT.optionalFieldOf("icon_index", 0).forGetter(HudRender.Entry::iconIndex),
               LoggedOptionalField.strict("condition", EntityCondition.CODEC).forGetter(HudRender.Entry::condition),
               Codec.BOOL.optionalFieldOf("inverted", false).forGetter(HudRender.Entry::inverted),
               Codec.INT.optionalFieldOf("order").forGetter(HudRender.Entry::order),
               Expression.INT_OR_EXPR.optionalFieldOf("max").forGetter(HudRender.Entry::max),
               SingleOrList.of(HudRender.Look.CODEC).optionalFieldOf("fallback", List.of()).forGetter(HudRender.Entry::fallback)
            )
            .apply(i, HudRender.Entry::new)
      );
   }

   public record Look(class_2960 spriteLocation, Optional<Integer> barIndex, Optional<Integer> iconIndex) {
      private static final Codec<HudRender.Look> OBJECT_CODEC = RecordCodecBuilder.create(
         i -> i.group(
               IdCodecs.ID.fieldOf("sprite_location").forGetter(HudRender.Look::spriteLocation),
               Codec.INT.optionalFieldOf("bar_index").forGetter(HudRender.Look::barIndex),
               Codec.INT.optionalFieldOf("icon_index").forGetter(HudRender.Look::iconIndex)
            )
            .apply(i, HudRender.Look::new)
      );
      public static final Codec<HudRender.Look> CODEC = Codec.either(IdCodecs.ID, OBJECT_CODEC)
         .xmap(
            either -> (HudRender.Look)either.map(id -> new HudRender.Look(id, Optional.empty(), Optional.empty()), look -> look),
            look -> look.barIndex.isEmpty() && look.iconIndex.isEmpty() ? Either.left(look.spriteLocation) : Either.right(look)
         );
   }
}
