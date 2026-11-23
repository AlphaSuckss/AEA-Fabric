package dev.alphasucks.aea.port.compat.skills;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.PowerContainerAttachment;
import dev.alphasucks.aea.port.power.PowerContainer;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.reward.Reward;
import net.puffish.skillsmod.api.reward.RewardConfigContext;
import net.puffish.skillsmod.api.reward.RewardDisposeContext;
import net.puffish.skillsmod.api.reward.RewardUpdateContext;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

public final class PowerReward implements Reward {
   public static final class_2960 ID = Apoli.id("power");
   private final class_2960 power;
   private final PowerReward.Operation operation;
   private final class_2960 source;

   private PowerReward(class_2960 power, PowerReward.Operation operation, class_2960 source) {
      this.power = power;
      this.operation = operation;
      this.source = source;
   }

   static void register() {
      SkillsAPI.registerReward(ID, PowerReward::parse);
   }

   private static Result<PowerReward, Problem> parse(RewardConfigContext context) {
      return context.getData().andThen(JsonElement::getAsObject).andThen(rootObject -> rootObject.noUnused(PowerReward::parse));
   }

   private static Result<PowerReward, Problem> parse(JsonObject rootObject) {
      ArrayList<Problem> problems = new ArrayList();
      Optional<class_2960> optPower = rootObject.get("power").andThen(BuiltinJson::parseIdentifier).ifFailure(problems::add).getSuccess();
      PowerReward.Operation operation = (PowerReward.Operation)rootObject.get("operation")
         .getSuccess()
         .flatMap(element -> parseOperation(element).ifFailure(problems::add).getSuccess())
         .orElse(PowerReward.Operation.ADD);
      return !problems.isEmpty()
         ? Result.failure(Problem.combine(problems))
         : Result.success(new PowerReward((class_2960)optPower.orElseThrow(), operation, SkillsCompat.newSource()));
   }

   private static Result<PowerReward.Operation, Problem> parseOperation(JsonElement element) {
      return element.getAsString().andThen(string -> {
         return switch (string) {
            case "add" -> Result.success(PowerReward.Operation.ADD);
            case "remove" -> Result.success(PowerReward.Operation.REMOVE);
            default -> Result.failure(element.getPath().createProblem("Expected operation `add` or `remove`, but got `" + string + "`"));
         };
      });
   }

   public void update(RewardUpdateContext context) {
      this.apply(context.getPlayer(), context.getCount() > 0);
   }

   public void dispose(RewardDisposeContext context) {
      for (class_3222 player : context.getServer().method_3760().method_14571()) {
         this.apply(player, false);
      }
   }

   private void apply(class_3222 player, boolean unlocked) {
      PowerContainer container = PowerContainerAttachment.getOrCreate(player);
      if (container != null) {
         if (this.operation == PowerReward.Operation.ADD == unlocked) {
            container.addPower(this.power, this.source);
         } else {
            container.removePower(this.power, this.source);
         }
      }
   }

   private enum Operation {
      ADD,
      REMOVE;
   }
}
