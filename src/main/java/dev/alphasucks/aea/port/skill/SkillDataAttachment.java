package dev.alphasucks.aea.port.skill;

import dev.alphasucks.aea.port.Apoli;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.class_1657;

public final class SkillDataAttachment {
   public static final AttachmentType<SkillData> TYPE = AttachmentRegistry.<SkillData>builder()
      .persistent(SkillData.CODEC)
      .copyOnDeath()
      .buildAndRegister(Apoli.id("skill_data"));

   private SkillDataAttachment() {
   }

   /**
    * AEA: forces TYPE to register during mod init. It used to register lazily (first skill action/command),
    * so after a restart players' saved skill data loaded before it existed: Fabric logged
    * "Unknown attachment type apoli:skill_data found when deserializing, skipping" and the data was lost.
    */
   public static void init() {
      if (TYPE == null) {
         throw new IllegalStateException("apoli:skill_data attachment failed to register");
      }
   }

   public static SkillData get(class_1657 player) {
      return (SkillData)((AttachmentTarget)player).getAttachedOrCreate(TYPE, SkillData::new);
   }
}
