package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.RadialMenuEntry;
import dev.alphasucks.aea.port.network.payload.RadialMenuOpenS2C.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_2960;
import net.minecraft.class_3222;

public final class RadialMenuAction implements ActionType<EntityCtx, RadialMenuAction.Cfg> {
   @Override
   public MapCodec<RadialMenuAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         instance -> instance.group(
               Codec.list(RadialMenuEntry.CODEC).fieldOf("entries").forGetter(RadialMenuAction.Cfg::entries),
               IdCodecs.ID.optionalFieldOf("sprite_location").forGetter(RadialMenuAction.Cfg::sprite)
            )
            .apply(instance, RadialMenuAction.Cfg::new)
      );
   }

   public void run(RadialMenuAction.Cfg cfg, EntityCtx ctx) {
      if (ctx.entity() instanceof class_3222 player) {
         ArrayList var8 = new ArrayList(cfg.entries.size());
         ArrayList actions = new ArrayList(cfg.entries.size());

         for (RadialMenuEntry entry : cfg.entries) {
            if (!entry.condition().isPresent() || ((EntityCondition)entry.condition().get()).test(ctx)) {
               actions.add(entry.entityAction());
               var8.add(
                  new Entry(
                     entry.item(),
                     entry.buttonTexture(),
                     entry.icon(),
                     entry.highlightIcon(),
                     entry.highlightButtonTexture(),
                     entry.tooltip(),
                     entry.distance(),
                     entry.velocity(),
                     entry.buttonWidth(),
                     entry.buttonHeight(),
                     entry.iconWidth(),
                     entry.iconHeight(),
                     entry.itemWidth(),
                     entry.itemHeight(),
                     entry.offsetX(),
                     entry.offsetY(),
                     entry.angle()
                  )
               );
            }
         }

         if (!var8.isEmpty()) {
            ApoliNetwork.openRadialMenu(player, cfg.sprite, var8, actions);
         }
      }
   }

   public record Cfg(List<RadialMenuEntry> entries, Optional<class_2960> sprite) {
   }
}
