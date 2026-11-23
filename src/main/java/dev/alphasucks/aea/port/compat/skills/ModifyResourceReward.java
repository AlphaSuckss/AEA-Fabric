package dev.alphasucks.aea.port.compat.skills;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.PowerContainerAttachment;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerResources;
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

public final class ModifyResourceReward implements Reward {
   public static final class_2960 ID = Apoli.id("modify_resource");
   private final class_2960 resource;
   private final ModifyResourceReward.Operation operation;
   private final int value;
   private final int base;

   private ModifyResourceReward(class_2960 resource, ModifyResourceReward.Operation operation, int value, int base) {
      this.resource = resource;
      this.operation = operation;
      this.value = value;
      this.base = base;
   }

   static void register() {
      SkillsAPI.registerReward(ID, ModifyResourceReward::parse);
   }

   private static Result<ModifyResourceReward, Problem> parse(RewardConfigContext context) {
      return context.getData().andThen(JsonElement::getAsObject).andThen(rootObject -> rootObject.noUnused(ModifyResourceReward::parse));
   }

   private static Result<ModifyResourceReward, Problem> parse(JsonObject rootObject) {
      ArrayList<Problem> problems = new ArrayList();
      Optional<class_2960> optResource = rootObject.get("resource").andThen(BuiltinJson::parseIdentifier).ifFailure(problems::add).getSuccess();
      Optional<Integer> optValue = rootObject.getInt("value").ifFailure(problems::add).getSuccess();
      ModifyResourceReward.Operation operation = (ModifyResourceReward.Operation)rootObject.get("operation")
         .getSuccess()
         .flatMap(element -> parseOperation(element).ifFailure(problems::add).getSuccess())
         .orElse(ModifyResourceReward.Operation.ADD);
      Integer base = (Integer)rootObject.getInt("base").getSuccess().orElse(0);
      return !problems.isEmpty()
         ? Result.failure(Problem.combine(problems))
         : Result.success(new ModifyResourceReward((class_2960)optResource.orElseThrow(), operation, (Integer)optValue.orElseThrow(), base));
   }

   private static Result<ModifyResourceReward.Operation, Problem> parseOperation(JsonElement element) {
      return element.getAsString().andThen(string -> {
         return switch (string) {
            case "add" -> Result.success(ModifyResourceReward.Operation.ADD);
            case "set" -> Result.success(ModifyResourceReward.Operation.SET);
            default -> Result.failure(element.getPath().createProblem("Expected operation `add` or `set`, but got `" + string + "`"));
         };
      });
   }

   public void update(RewardUpdateContext context) {
      this.apply(context.getPlayer(), context.getCount());
   }

   public void dispose(RewardDisposeContext context) {
      for (class_3222 player : context.getServer().method_3760().method_14571()) {
         this.apply(player, 0);
      }
   }

   private void apply(class_3222 player, int count) {
      PowerContainer container = PowerContainerAttachment.getOrCreate(player);
      if (container != null) {
         int target = this.operation == ModifyResourceReward.Operation.ADD ? this.base + this.value * count : (count > 0 ? this.value : this.base);
         PowerResources.write(container, this.resource, target);
      }
   }

   private enum Operation {
      ADD,
      SET;
   }
}
