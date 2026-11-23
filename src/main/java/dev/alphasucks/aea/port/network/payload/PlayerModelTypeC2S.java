package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.data.PlayerModelType;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record PlayerModelTypeC2S(PlayerModelType modelType) {
   public static final class_2960 CHANNEL = Apoli.internal("player_model_type");

   public void write(class_2540 buf) {
      buf.writeBoolean(this.modelType == PlayerModelType.SLIM);
   }

   public static PlayerModelTypeC2S read(class_2540 buf) {
      return new PlayerModelTypeC2S(buf.readBoolean() ? PlayerModelType.SLIM : PlayerModelType.WIDE);
   }
}
