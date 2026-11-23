package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.client.render.model.CentaurPlayerModel;
import dev.alphasucks.aea.port.client.render.model.DigiLegsPlayerModel;
import dev.alphasucks.aea.port.client.render.model.FourArmsPlayerModel;
import dev.alphasucks.aea.port.client.render.model.SixArmsPlayerModel;
import dev.alphasucks.aea.port.client.render.model.StinkFlyPlayerModel;
import dev.alphasucks.aea.port.power.builtin.ModifyPlayerModelPower;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_2960;
import net.minecraft.class_591;
import net.minecraft.class_742;
import net.minecraft.class_5617.class_5618;

public final class ApoliPlayerModels {
   public static final class_2960 FOUR_ARMS = Apoli.id("four_arms");
   public static final class_2960 STINKFLY = Apoli.id("stinkfly");
   public static final class_2960 DIGI_LEGS = Apoli.id("digi_legs");
   public static final class_2960 SIX_ARMS = Apoli.id("six_arms");
   public static final class_2960 CENTAUR = Apoli.id("centaur");
   private static final Map<class_2960, ApoliPlayerModels.ModelFactory> FACTORIES = new HashMap();
   private static final Map<class_2960, class_591<class_742>> BAKED_WIDE = new HashMap();
   private static final Map<class_2960, class_591<class_742>> BAKED_SLIM = new HashMap();

   private ApoliPlayerModels() {
   }

   public static void register(class_2960 id, ApoliPlayerModels.ModelFactory factory) {
      FACTORIES.put(id, factory);
   }

   public static boolean isRegistered(class_2960 id) {
      return ModifyPlayerModelPower.VANILLA_MODEL.equals(id) || FACTORIES.containsKey(id);
   }

   public static void bake(class_5618 ctx, boolean slim) {
      Map<class_2960, class_591<class_742>> target = slim ? BAKED_SLIM : BAKED_WIDE;
      target.clear();
      FACTORIES.forEach((id, factory) -> target.put(id, factory.create(ctx, slim)));
   }

   public static class_591<class_742> override(class_742 player, class_591<class_742> original, boolean slim) {
      class_2960 id = ModifyPlayerModelPower.firstActiveModel(player);
      if (id != null && !ModifyPlayerModelPower.VANILLA_MODEL.equals(id)) {
         class_591<class_742> model = (class_591<class_742>)(slim ? BAKED_SLIM : BAKED_WIDE).get(id);
         return model != null ? model : original;
      } else {
         return original;
      }
   }

   static {
      register(FOUR_ARMS, (ctx, slim) -> new FourArmsPlayerModel(FourArmsPlayerModel.createLayer(slim).method_32109(), slim));
      register(SIX_ARMS, (ctx, slim) -> new SixArmsPlayerModel(SixArmsPlayerModel.createLayer(slim).method_32109(), slim));
      register(STINKFLY, (ctx, slim) -> new StinkFlyPlayerModel(StinkFlyPlayerModel.createLayer(slim).method_32109(), slim));
      register(DIGI_LEGS, (ctx, slim) -> new DigiLegsPlayerModel(DigiLegsPlayerModel.createRoot(slim), slim));
      register(CENTAUR, (ctx, slim) -> new CentaurPlayerModel(CentaurPlayerModel.createLayer(slim).method_32109(), slim));
   }

   @FunctionalInterface
   public interface ModelFactory {
      class_591<class_742> create(class_5618 var1, boolean var2);
   }
}
