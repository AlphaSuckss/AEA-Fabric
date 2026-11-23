package dev.alphasucks.aea.port.power;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.HudRender;
import java.util.OptionalInt;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import org.jetbrains.annotations.Nullable;

public abstract class PowerType<C> {
   public abstract MapCodec<C> configCodec();

   public void onAdded(class_2960 powerId, C cfg, PowerContainer holder, class_2960 source) {
   }

   public void onRemoved(class_2960 powerId, C cfg, PowerContainer holder, class_2960 source) {
   }

   public void onSuppressed(class_2960 powerId, C cfg, PowerContainer holder) {
   }

   public void onUnsuppressed(class_2960 powerId, C cfg, PowerContainer holder) {
   }

   public void tick(class_2960 powerId, C cfg, PowerContainer holder) {
   }

   public void tickStored(class_2960 powerId, C cfg, PowerContainer holder) {
   }

   public boolean isActive(class_2960 powerId, C cfg, EntityCtx ctx) {
      return true;
   }

   public boolean ticksNonLivingEntities() {
      return false;
   }

   public boolean isCooldown() {
      return false;
   }

   @Nullable
   public HudRender hudRender(C cfg) {
      return cfg instanceof HudRendered rendered ? rendered.hudRender() : null;
   }

   public boolean resolvesForClient() {
      return false;
   }

   public int clientTarget(class_2960 powerId, C cfg, class_3222 player, int current) {
      return -1;
   }

   public OptionalInt readResource(class_2960 powerId, C cfg, PowerContainer holder) {
      return OptionalInt.empty();
   }

   public OptionalInt writeResource(class_2960 powerId, C cfg, PowerContainer holder, int value) {
      return OptionalInt.empty();
   }

   public OptionalInt resourceBound(class_2960 powerId, C cfg, PowerContainer holder, boolean max) {
      return OptionalInt.empty();
   }

   public int resourceSize(class_2960 powerId, C cfg, PowerContainer holder) {
      return 0;
   }

   public OptionalInt readResourceAt(class_2960 powerId, C cfg, PowerContainer holder, int slot) {
      return slot == 0 ? this.readResource(powerId, cfg, holder) : OptionalInt.empty();
   }

   public OptionalInt writeResourceAt(class_2960 powerId, C cfg, PowerContainer holder, int slot, int value) {
      return slot == 0 ? this.writeResource(powerId, cfg, holder, value) : OptionalInt.empty();
   }

   public int resourceIndexOf(class_2960 powerId, C cfg, PowerContainer holder, int value) {
      OptionalInt only = this.readResource(powerId, cfg, holder);
      return only.isPresent() && only.getAsInt() == value ? 0 : -1;
   }
}
