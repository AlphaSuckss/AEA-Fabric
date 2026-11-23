package dev.alphasucks.aea.port.client.particle;

import dev.alphasucks.aea.port.data.ColorCodecs;
import dev.alphasucks.aea.port.particle.CustomParticleOptions;
import dev.alphasucks.aea.port.particle.ParticleFacing;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_3940;
import net.minecraft.class_3999;
import net.minecraft.class_4184;
import net.minecraft.class_4588;
import net.minecraft.class_638;
import net.minecraft.class_703;
import net.minecraft.class_707;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class CustomParticle extends class_3940 {
   private static final int FULL_BRIGHT = 15728880;
   private static final float COLLISION_HALF_WIDTH = 0.1F;
   private static final double MAX_COLLISION_SPEED_SQR = 10000.0;
   private final CustomParticleOptions options;
   private final class_3999 renderType;
   private final ParticleSheet sheet;
   private final boolean loopFrames;
   private final float startSize;
   private final float endSize;
   private final float halfHeight;
   private final float rollStep;
   private final float startRed;
   private final float startGreen;
   private final float startBlue;
   private final float startAlpha;
   private final float endRed;
   private final float endGreen;
   private final float endBlue;
   private final float endAlpha;
   private int cell;
   private boolean stopped;

   protected CustomParticle(class_638 level, CustomParticleOptions options, double x, double y, double z, double xd, double yd, double zd) {
      super(level, x, y, z);
      this.options = options;
      class_2960 texture = ParticleTextures.resolve(options.texture());
      this.renderType = ApoliParticleRenderTypes.of(ParticleTextures.bound(texture, options.alphaBleed()), options.blend());
      this.sheet = ParticleSheet.of(texture, options.frameLayout(), options.frames(), options.frameTime());
      this.loopFrames = (Boolean)options.loopFrames().orElseGet(this.sheet::loopsByDefault);
      this.field_3847 = Math.max(1, options.lifetime() + (options.lifetimeVariation() > 0 ? this.field_3840.method_43048(options.lifetimeVariation() + 1) : 0));
      this.field_3844 = options.gravity();
      this.field_28786 = options.friction();
      this.field_3862 = options.physics();
      this.field_3852 = xd;
      this.field_3869 = yd;
      this.field_3850 = zd;
      float variation = options.sizeVariation() > 0.0F ? this.field_3840.method_43057() * options.sizeVariation() : 0.0F;
      this.startSize = options.size() + variation;
      this.endSize = options.size() > 0.0F ? options.endSizeOr() * (this.startSize / options.size()) : options.endSizeOr() + variation;
      this.field_17867 = this.startSize;
      this.halfHeight = Math.max(this.startSize, this.endSize);
      this.method_3067(new class_238(x - 0.1F, y - this.halfHeight, z - 0.1F, x + 0.1F, y + this.halfHeight, z + 0.1F));
      this.field_3839 = (float)options.roll().eval((class_1297)null) * (float) (Math.PI / 180.0);
      this.field_3857 = this.field_3839;
      this.rollStep = (float)options.rollSpeed().eval((class_1297)null) * (float) (Math.PI / 180.0);
      this.cell = this.sheet.cellAt(0, this.field_3847, this.loopFrames);
      int from = options.color();
      int to = options.endColorOr();
      if (options.hueVariation() != 0.0F) {
         float degrees = (this.field_3840.method_43057() * 2.0F - 1.0F) * options.hueVariation();
         from = ColorCodecs.rotateHue(from, degrees);
         to = ColorCodecs.rotateHue(to, degrees);
      }

      if (options.colorVariation() != 0.0F) {
         float spread = options.colorVariation();
         float dRed = (this.field_3840.method_43057() * 2.0F - 1.0F) * spread;
         float dGreen = (this.field_3840.method_43057() * 2.0F - 1.0F) * spread;
         float dBlue = (this.field_3840.method_43057() * 2.0F - 1.0F) * spread;
         from = ColorCodecs.offsetRgb(from, dRed, dGreen, dBlue);
         to = ColorCodecs.offsetRgb(to, dRed, dGreen, dBlue);
      }

      this.startRed = ColorCodecs.red(from);
      this.startGreen = ColorCodecs.green(from);
      this.startBlue = ColorCodecs.blue(from);
      this.startAlpha = ColorCodecs.alpha(from);
      this.endRed = ColorCodecs.red(to);
      this.endGreen = ColorCodecs.green(to);
      this.endBlue = ColorCodecs.blue(to);
      this.endAlpha = ColorCodecs.alpha(to);
      this.tint(0.0F);
   }

   @Override
   public void method_3070() {
      this.field_3857 = this.field_3839;
      super.method_3070();
      if (!this.field_3843) {
         this.field_3839 = this.field_3839 + this.rollStep;
         if (this.sheet.animated()) {
            this.cell = this.sheet.cellAt(this.field_3866, this.field_3847, this.loopFrames);
         }

         this.tint((float)this.field_3866 / this.field_3847);
      }
   }

   @Override
   public void method_3069(double dx, double dy, double dz) {
      if (!this.stopped) {
         double wantedX = dx;
         double wantedY = dy;
         double wantedZ = dz;
         if (this.field_3862 && (dx != 0.0 || dy != 0.0 || dz != 0.0) && dx * dx + dy * dy + dz * dz < 10000.0) {
            class_243 allowed = class_1297.method_20736(null, new class_243(dx, dy, dz), this.method_3064(), this.field_3851, List.of());
            dx = allowed.field_1352;
            dy = allowed.field_1351;
            dz = allowed.field_1350;
         }

         if (dx != 0.0 || dy != 0.0 || dz != 0.0) {
            this.method_3067(this.method_3064().method_989(dx, dy, dz));
            this.field_3874 += dx;
            this.field_3854 += dy;
            this.field_3871 += dz;
         }

         if (Math.abs(wantedY) >= 1.0E-5F && Math.abs(dy) < 1.0E-5F) {
            this.stopped = true;
         }

         this.field_3845 = wantedY != dy && wantedY < 0.0;
         if (wantedX != dx) {
            this.field_3852 = 0.0;
         }

         if (wantedZ != dz) {
            this.field_3850 = 0.0;
         }
      }
   }

   private void tint(float progress) {
      float eased = this.options.easing().apply(progress);
      this.field_3861 = class_3532.method_16439(eased, this.startRed, this.endRed);
      this.field_3842 = class_3532.method_16439(eased, this.startGreen, this.endGreen);
      this.field_3859 = class_3532.method_16439(eased, this.startBlue, this.endBlue);
      this.field_3841 = class_3532.method_16439(eased, this.startAlpha, this.endAlpha);
   }

   @Override
   public void method_3074(class_4588 consumer, class_4184 camera, float partialTick) {
      class_243 cameraPos = camera.method_19326();
      float x = (float)(class_3532.method_16436(partialTick, this.field_3858, this.field_3874) - cameraPos.method_10216());
      float y = (float)(class_3532.method_16436(partialTick, this.field_3838, this.field_3854) - cameraPos.method_10214());
      float z = (float)(class_3532.method_16436(partialTick, this.field_3856, this.field_3871) - cameraPos.method_10215());
      Quaternionf rotation = this.options.facing() == ParticleFacing.VERTICAL
         ? new Quaternionf(0.0F, camera.method_23767().y, 0.0F, camera.method_23767().w)
         : new Quaternionf(camera.method_23767());
      if (this.field_3839 != 0.0F) {
         rotation.rotateZ(class_3532.method_16439(partialTick, this.field_3857, this.field_3839));
      }

      Vector3f[] corners = new Vector3f[]{
         new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)
      };
      float size = this.method_18132(partialTick);

      for (int i = 0; i < 4; i++) {
         corners[i].rotate(rotation).mul(size).add(x, y, z);
      }

      float u0 = this.method_18133();
      float u1 = this.method_18134();
      float v0 = this.method_18135();
      float v1 = this.method_18136();
      int light = this.method_3068(partialTick);
      this.vertex(consumer, corners[0], u1, v1, light);
      this.vertex(consumer, corners[1], u1, v0, light);
      this.vertex(consumer, corners[2], u0, v0, light);
      this.vertex(consumer, corners[3], u0, v1, light);
   }

   private void vertex(class_4588 consumer, Vector3f corner, float u, float v, int light) {
      consumer.method_22912(corner.x(), corner.y(), corner.z())
         .method_22913(u, v)
         .method_22915(this.field_3861, this.field_3842, this.field_3859, this.field_3841)
         .method_22916(light)
         .method_1344();
   }

   @Override
   public float method_18132(float partialTick) {
      if (this.startSize == this.endSize) {
         return this.startSize;
      }

      float progress = class_3532.method_15363((this.field_3866 + partialTick) / this.field_3847, 0.0F, 1.0F);
      return class_3532.method_16439(this.options.easing().apply(progress), this.startSize, this.endSize);
   }

   @Override
   protected int method_3068(float partialTick) {
      return this.options.emissive() ? 15728880 : super.method_3068(partialTick);
   }

   @Override
   public class_3999 method_18122() {
      return this.renderType;
   }

   @Override
   protected float method_18133() {
      return this.sheet.u0(this.cell);
   }

   @Override
   protected float method_18134() {
      return this.sheet.u1(this.cell);
   }

   @Override
   protected float method_18135() {
      return this.sheet.v0(this.cell);
   }

   @Override
   protected float method_18136() {
      return this.sheet.v1(this.cell);
   }

   @Environment(EnvType.CLIENT)
   public static final class Provider implements class_707<CustomParticleOptions> {
      // original carried a javac bridge from the intermediary name
      @Override
      public class_703 method_3090(CustomParticleOptions options, class_638 level, double x, double y, double z, double xd, double yd, double zd) {
         return this.createParticle(options, level, x, y, z, xd, yd, zd);
      }

      public class_703 createParticle(CustomParticleOptions options, class_638 level, double x, double y, double z, double xd, double yd, double zd) {
         return new CustomParticle(level, options, x, y, z, xd, yd, zd);
      }
   }
}
