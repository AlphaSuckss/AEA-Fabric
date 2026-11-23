package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.data.EntityPoses;
import dev.alphasucks.aea.port.data.ModelPartTransformation;
import dev.alphasucks.aea.port.data.Perspective;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import net.minecraft.class_4050;
import org.jetbrains.annotations.Nullable;

public final class ModifyModelPartsPower extends PowerType<ModifyModelPartsPower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("modify_model_parts");

   @Override
   public MapCodec<ModifyModelPartsPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         instance -> instance.group(
               ModelPartTransformation.CODEC.listOf().fieldOf("transformations").forGetter(ModifyModelPartsPower.Config::transformations),
               EntityPoses.CODEC.listOf().optionalFieldOf("override_pose", List.of()).forGetter(ModifyModelPartsPower.Config::overridePose),
               Perspective.LIST_CODEC.optionalFieldOf("perspectives", Perspective.THIRD_ONLY).forGetter(ModifyModelPartsPower.Config::perspectives)
            )
            .apply(
               instance,
               (transformations, poses, perspectives) -> new ModifyModelPartsPower.Config(
                  transformations, poses, maskOf(poses), perspectives, Perspective.maskOf(perspectives)
               )
            )
      );
   }

   public static int maskOf(List<class_4050> poses) {
      int mask = 0;

      for (int i = 0; i < poses.size(); i++) {
         mask |= 1 << ((class_4050)poses.get(i)).ordinal();
      }

      return mask;
   }

   public static boolean masked(int mask, class_4050 pose) {
      return (mask & 1 << pose.ordinal()) != 0;
   }

   public static boolean has(@Nullable class_1309 entity) {
      return PowerLookup.hasActive(entity, CANONICAL);
   }

   public record Config(
      List<ModelPartTransformation> transformations, List<class_4050> overridePose, int overridePoseMask, List<Perspective> perspectives, int perspectiveMask
   ) {
   }
}
