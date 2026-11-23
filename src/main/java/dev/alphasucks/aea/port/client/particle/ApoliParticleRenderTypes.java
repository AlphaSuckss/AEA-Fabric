package dev.alphasucks.aea.port.client.particle;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.alphasucks.aea.port.particle.ParticleBlend;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1060;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3999;
import net.minecraft.class_4184;
import net.minecraft.class_703;
import net.minecraft.class_757;
import net.minecraft.class_293.class_5596;

@Environment(EnvType.CLIENT)
public final class ApoliParticleRenderTypes {
   private static final Map<class_2960, class_3999> TRANSLUCENT = new HashMap();
   private static final Map<class_2960, class_3999> ADDITIVE = new HashMap();
   private static final List<class_3999> ORDER = new ArrayList(4);

   private ApoliParticleRenderTypes() {
   }

   public static class_3999 of(class_2960 texture, ParticleBlend blend) {
      Map<class_2960, class_3999> cache = blend == ParticleBlend.ADDITIVE ? ADDITIVE : TRANSLUCENT;
      class_3999 cached = (class_3999)cache.get(texture);
      if (cached != null) {
         return cached;
      }

      class_3999 created = new ApoliParticleRenderTypes.Type(texture, blend == ParticleBlend.ADDITIVE);
      cache.put(texture, created);
      if (blend == ParticleBlend.ADDITIVE) {
         ORDER.add(created);
      } else {
         ORDER.add(0, created);
      }

      return created;
   }

   public static void render(Map<class_3999, Queue<class_703>> buckets, class_1060 textureManager, class_4184 camera, float partialTick) {
      if (!ORDER.isEmpty() && !buckets.isEmpty()) {
         boolean drew = false;

         for (int i = 0; i < ORDER.size(); i++) {
            class_3999 type = (class_3999)ORDER.get(i);
            Queue<class_703> queue = (Queue<class_703>)buckets.get(type);
            if (queue != null && !queue.isEmpty()) {
               RenderSystem.setShader(class_757::method_34546);
               class_289 tesselator = class_289.method_1348();
               class_287 builder = tesselator.method_1349();
               type.method_18130(builder, textureManager);
               drew = true;

               for (class_703 particle : queue) {
                  particle.method_3074(builder, camera, partialTick);
               }

               type.method_18131(tesselator);
            }
         }

         if (drew) {
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
         }
      }
   }

   private record Type(class_2960 texture, boolean additive) implements class_3999 {
      @Override
      public void method_18130(class_287 builder, class_1060 textureManager) {
         RenderSystem.setShader(class_757::method_34546);
         RenderSystem.setShaderTexture(0, this.texture);
         RenderSystem.enableBlend();
         if (this.additive) {
            RenderSystem.blendFuncSeparate(class_4535.SRC_ALPHA, class_4534.ONE, class_4535.ONE, class_4534.ZERO);
            RenderSystem.depthMask(false);
         } else {
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
         }

         builder.method_1328(class_5596.field_27382, class_290.field_1584);
      }

      @Override
      public void method_18131(class_289 tesselator) {
         tesselator.method_1350();
      }

      public String toString() {
         return "apoli:custom[" + this.texture + (this.additive ? ",additive]" : "]");
      }
   }
}
