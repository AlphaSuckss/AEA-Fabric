package dev.alphasucks.aea.port.client.particle;

import dev.alphasucks.aea.port.client.render.ModelPartAnimator;
import dev.alphasucks.aea.port.data.BodyPart;
import dev.alphasucks.aea.port.data.BodyParts;
import dev.alphasucks.aea.port.data.ModelPartAnchor;
import dev.alphasucks.aea.port.data.ParticlePlacement;
import dev.alphasucks.aea.port.data.Space;
import dev.alphasucks.aea.port.data.Vector;
import dev.alphasucks.aea.port.data.ModelPartAnchor.Frame;
import dev.alphasucks.aea.port.network.payload.AnchoredParticlesS2C;
import java.util.ArrayDeque;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_638;

@Environment(EnvType.CLIENT)
public final class AnchoredParticleClient {
   private static final int MAX_PENDING = 256;
   private static final ArrayDeque<AnchoredParticlesS2C> PENDING = new ArrayDeque();

   private AnchoredParticleClient() {
   }

   public static void enqueue(AnchoredParticlesS2C payload) {
      if (PENDING.size() >= 256) {
         PENDING.pollFirst();
      }

      PENDING.addLast(payload);
   }

   public static void beginFrame() {
      RenderedPoses.nextFrame();
      if (!PENDING.isEmpty()) {
         class_310 mc = class_310.method_1551();
         class_638 level = mc.field_1687;
         if (level == null) {
            PENDING.clear();
            RenderedPoses.clear();
         } else {
            float partialTick = ModelPartAnimator.partialTick();

            AnchoredParticlesS2C payload;
            while ((payload = (AnchoredParticlesS2C)PENDING.pollFirst()) != null) {
               spawn(mc, level, payload, partialTick);
            }
         }
      }
   }

   private static void spawn(class_310 mc, class_638 level, AnchoredParticlesS2C payload, float partialTick) {
      class_1297 entity = level.method_8469(payload.entityId());
      if (entity != null) {
         RenderedPoses.watch(entity);
         BodyPart part = BodyParts.lookupOrCustom(payload.part());
         Optional<Space> space = payload.spaceValue();
         class_243 base = entity.method_30950(partialTick);
         RenderedPoses.Pose rendered = RenderedPoses.recent(entity);
         Frame frame = rendered == null ? null : rendered.frame(part.sided(entity), base);
         if (frame == null) {
            frame = FirstPersonHandAnchor.frame(mc, entity, part, base, partialTick);
         }

         if (frame == null) {
            frame = ModelPartAnchor.frameOf(entity, part, ModelPartAnimator.timeline(), ModelPartAnimator.now(entity), partialTick);
         }

         class_243 origin = ParticlePlacement.origin(base, entity, frame, space, payload.offsetX(), payload.offsetY(), payload.offsetZ());
         class_243 velocity = ParticlePlacement.velocity(entity, frame, space, new class_243(payload.velocityX(), payload.velocityY(), payload.velocityZ()));
         ParticlePlacement.spawn(
            level,
            level.field_9229,
            payload.particle(),
            payload.force(),
            origin,
            velocity,
            payload.count(),
            new Vector(payload.spreadX(), payload.spreadY(), payload.spreadZ()),
            payload.speed(),
            entity,
            frame,
            space
         );
      }
   }
}
