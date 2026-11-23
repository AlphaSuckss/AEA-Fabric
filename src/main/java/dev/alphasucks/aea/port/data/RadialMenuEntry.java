package dev.alphasucks.aea.port.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.EntityCondition;
import java.util.Optional;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;

public record RadialMenuEntry(
   class_1799 item,
   Optional<class_2960> buttonTexture,
   Optional<class_2960> icon,
   Optional<class_2960> highlightIcon,
   Optional<class_2960> highlightButtonTexture,
   EntityAction entityAction,
   Optional<EntityCondition> condition,
   int distance,
   int velocity,
   Optional<class_2561> tooltip,
   int buttonWidth,
   int buttonHeight,
   int iconWidth,
   int iconHeight,
   int itemWidth,
   int itemHeight,
   int offsetX,
   int offsetY,
   Optional<Float> angle
) {
   private static final MapCodec<RadialMenuEntry> BODY_CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
            ItemStackData.CODEC.optionalFieldOf("item").forGetter(e -> e.item.method_7960() ? Optional.empty() : Optional.of(new ItemStackData(e.item))),
            IdCodecs.ID.optionalFieldOf("button_texture").forGetter(RadialMenuEntry::buttonTexture),
            IdCodecs.ID.optionalFieldOf("icon").forGetter(RadialMenuEntry::icon),
            IdCodecs.ID.optionalFieldOf("highlight_icon_texture").forGetter(RadialMenuEntry::highlightIcon),
            IdCodecs.ID.optionalFieldOf("highlight_button_texture").forGetter(RadialMenuEntry::highlightButtonTexture),
            EntityAction.CODEC.fieldOf("entity_action").forGetter(RadialMenuEntry::entityAction),
            LoggedOptionalField.strict("condition", EntityCondition.CODEC).forGetter(RadialMenuEntry::condition),
            Codec.INT.optionalFieldOf("distance", -1).forGetter(RadialMenuEntry::distance),
            Codec.INT.optionalFieldOf("velocity", -1).forGetter(RadialMenuEntry::velocity),
            TextComponent.CODEC.optionalFieldOf("tooltip").forGetter(RadialMenuEntry::tooltip),
            Codec.INT.optionalFieldOf("button_width", 16).forGetter(RadialMenuEntry::buttonWidth),
            Codec.INT.optionalFieldOf("button_height", 20).forGetter(RadialMenuEntry::buttonHeight),
            Codec.INT.optionalFieldOf("icon_width", 16).forGetter(RadialMenuEntry::iconWidth),
            Codec.INT.optionalFieldOf("icon_height", 16).forGetter(RadialMenuEntry::iconHeight),
            Codec.INT.optionalFieldOf("item_width", 16).forGetter(RadialMenuEntry::itemWidth),
            Codec.INT.optionalFieldOf("item_height", 16).forGetter(RadialMenuEntry::itemHeight)
         )
         .apply(instance, RadialMenuEntry::fromData)
   );
   private static final MapCodec<RadialMenuEntry.Placement> PLACEMENT_CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
            Codec.INT.optionalFieldOf("offset_x", 0).forGetter(RadialMenuEntry.Placement::offsetX),
            Codec.INT.optionalFieldOf("offset_y", 0).forGetter(RadialMenuEntry.Placement::offsetY),
            Codec.FLOAT.optionalFieldOf("angle").forGetter(RadialMenuEntry.Placement::angle)
         )
         .apply(instance, RadialMenuEntry.Placement::new)
   );
   public static final Codec<RadialMenuEntry> CODEC = Codec.mapPair(BODY_CODEC, PLACEMENT_CODEC)
      .xmap(
         pair -> ((RadialMenuEntry)pair.getFirst()).withPlacement((RadialMenuEntry.Placement)pair.getSecond()),
         entry -> Pair.of(entry, new RadialMenuEntry.Placement(entry.offsetX, entry.offsetY, entry.angle))
      )
      .codec();

   private RadialMenuEntry withPlacement(RadialMenuEntry.Placement placement) {
      return new RadialMenuEntry(
         this.item,
         this.buttonTexture,
         this.icon,
         this.highlightIcon,
         this.highlightButtonTexture,
         this.entityAction,
         this.condition,
         this.distance,
         this.velocity,
         this.tooltip,
         this.buttonWidth,
         this.buttonHeight,
         this.iconWidth,
         this.iconHeight,
         this.itemWidth,
         this.itemHeight,
         placement.offsetX(),
         placement.offsetY(),
         placement.angle()
      );
   }

   private static RadialMenuEntry fromData(
      Optional<ItemStackData> item,
      Optional<class_2960> buttonTexture,
      Optional<class_2960> icon,
      Optional<class_2960> highlightIcon,
      Optional<class_2960> highlightButtonTexture,
      EntityAction entityAction,
      Optional<EntityCondition> condition,
      int distance,
      int velocity,
      Optional<class_2561> tooltip,
      int buttonWidth,
      int buttonHeight,
      int iconWidth,
      int iconHeight,
      int itemWidth,
      int itemHeight
   ) {
      return new RadialMenuEntry(
         (class_1799)item.map(ItemStackData::stack).orElse(class_1799.field_8037),
         buttonTexture,
         icon,
         highlightIcon,
         highlightButtonTexture,
         entityAction,
         condition,
         distance,
         velocity,
         tooltip,
         buttonWidth,
         buttonHeight,
         iconWidth,
         iconHeight,
         itemWidth,
         itemHeight,
         0,
         0,
         Optional.empty()
      );
   }

   private record Placement(int offsetX, int offsetY, Optional<Float> angle) {
   }
}
