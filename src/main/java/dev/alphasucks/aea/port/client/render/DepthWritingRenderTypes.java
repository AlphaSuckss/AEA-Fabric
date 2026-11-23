package dev.alphasucks.aea.port.client.render;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.class_156;
import net.minecraft.class_1921;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_4668;
import net.minecraft.class_293.class_5596;

public final class DepthWritingRenderTypes extends class_1921 {
   private static final Function<class_2960, class_1921> EMISSIVE = class_156.method_34866(
      texture -> new DepthWritingRenderTypes(
         "apoli_entity_translucent_emissive_depth",
         true,
         Optional.of(class_1921.method_23287(texture)),
         new class_4683(texture, false, false),
         field_38344,
         field_21370,
         field_21345,
         field_21385
      )
   );
   private static final Function<class_2960, class_1921> EYES = class_156.method_34866(
      texture -> new DepthWritingRenderTypes(
         "apoli_eyes_depth", false, Optional.empty(), new class_4683(texture, false, false), field_29414, field_21366, field_21344, field_21386
      )
   );
   private final Optional<class_1921> outline;

   private DepthWritingRenderTypes(
      String name,
      boolean affectsCrumbling,
      Optional<class_1921> outline,
      class_4668 texture,
      class_4668 shader,
      class_4668 transparency,
      class_4668 cull,
      class_4668 overlay
   ) {
      this(
         name,
         affectsCrumbling,
         outline,
         new class_4668[]{
            texture,
            shader,
            transparency,
            field_21348,
            cull,
            field_21384,
            overlay,
            field_21352,
            field_21358,
            field_21379,
            field_21349,
            field_44815,
            field_21360
         }
      );
   }

   private DepthWritingRenderTypes(String name, boolean affectsCrumbling, Optional<class_1921> outline, class_4668[] shards) {
      super(name, class_290.field_1580, class_5596.field_27382, 1536, affectsCrumbling, true, () -> {
         for (class_4668 shard : shards) {
            shard.method_23516();
         }
      }, () -> {
         for (class_4668 shard : shards) {
            shard.method_23518();
         }
      });
      this.outline = outline;
   }

   public static class_1921 emissive(class_2960 texture) {
      return (class_1921)EMISSIVE.apply(texture);
   }

   public static class_1921 eyes(class_2960 texture) {
      return (class_1921)EYES.apply(texture);
   }

   @Override
   public Optional<class_1921> method_23289() {
      return this.outline;
   }
}
