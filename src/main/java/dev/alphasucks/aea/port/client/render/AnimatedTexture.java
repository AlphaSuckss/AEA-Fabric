package dev.alphasucks.aea.port.client.render;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1044;
import net.minecraft.class_1061;
import net.minecraft.class_2960;
import net.minecraft.class_3300;
import net.minecraft.class_7764;
import net.minecraft.class_7766;
import net.minecraft.class_7768;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class AnimatedTexture extends class_1044 implements class_1061 {
   private static final int IDLE_TICKS = 40;
   private final class_2960 source;
   @Nullable
   private class_7764 contents;
   @Nullable
   private class_7768 ticker;
   private int idle;

   public AnimatedTexture(class_2960 source) {
      this.source = source;
   }

   @Override
   public void method_4625(class_3300 manager) throws IOException {
      class_7764 loaded = class_7766.method_45829(this.source, manager.getResourceOrThrow(this.source));
      if (loaded == null) {
         throw new IOException("Could not read animated texture " + this.source);
      }

      this.release();
      this.contents = loaded;
      this.ticker = loaded.method_45818();
      if (RenderSystem.isOnRenderThreadOrInit()) {
         this.upload();
      } else {
         RenderSystem.recordRenderCall(this::upload);
      }
   }

   private void upload() {
      class_7764 current = this.contents;
      if (current != null) {
         TextureUtil.prepareImage(this.method_4624(), current.method_45807(), current.method_45815());
         current.method_45809(0, 0);
      }
   }

   public void markUsed() {
      this.idle = 0;
   }

   @Override
   public void method_4622() {
      class_7768 current = this.ticker;
      if (current != null && this.idle++ <= 40) {
         this.method_23207();
         current.method_45824(0, 0);
      }
   }

   @Override
   public void close() {
      this.release();
      super.close();
   }

   private void release() {
      if (this.ticker != null) {
         this.ticker.close();
         this.ticker = null;
      }

      if (this.contents != null) {
         this.contents.close();
         this.contents = null;
      }
   }
}
