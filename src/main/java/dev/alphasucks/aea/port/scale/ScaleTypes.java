package dev.alphasucks.aea.port.scale;

import dev.alphasucks.aea.port.Apoli;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class ScaleTypes {
   private static final Map<class_2960, ScaleType> BY_ID = new HashMap();
   private static final List<ScaleType> BY_INDEX = new ArrayList();
   public static final ScaleType BASE = registerDimension("base");
   public static final ScaleType WIDTH = registerDimension("width").multipliedBy(BASE);
   public static final ScaleType HEIGHT = registerDimension("height").multipliedBy(BASE);
   public static final ScaleType HITBOX_WIDTH = registerDimension("hitbox_width").multipliedBy(WIDTH);
   public static final ScaleType HITBOX_HEIGHT = registerDimension("hitbox_height").multipliedBy(HEIGHT);
   public static final ScaleType EYE_HEIGHT = registerDimension("eye_height").multipliedBy(HEIGHT);
   public static final ScaleType MODEL_WIDTH = register("model_width").multipliedBy(WIDTH);
   public static final ScaleType MODEL_HEIGHT = register("model_height").multipliedBy(HEIGHT);
   public static final ScaleType THIRD_PERSON = register("third_person").multipliedBy(HEIGHT);
   public static final ScaleType MOTION = register("motion").multipliedBy(BASE);
   public static final ScaleType FALLING = register("falling").dividedBy(MOTION);
   public static final ScaleType STEP_HEIGHT = register("step_height").multipliedBy(MOTION);
   public static final ScaleType JUMP_HEIGHT = register("jump_height").multipliedBy(MOTION);
   public static final ScaleType VISIBILITY = register("visibility").multipliedBy(BASE);
   public static final ScaleType MINING_SPEED = register("mining_speed");
   public static final ScaleType KNOCKBACK = register("knockback");
   public static final ScaleType ATTACK = register("attack");
   public static final ScaleType DEFENSE = register("defense");
   public static final ScaleType HELD_ITEM = register("held_item");
   public static final ScaleType HELD_ITEM_MAINHAND = register("held_item_mainhand").multipliedBy(HELD_ITEM);
   public static final ScaleType HELD_ITEM_OFFHAND = register("held_item_offhand").multipliedBy(HELD_ITEM);
   public static final ScaleType DOLL = register("doll");

   private ScaleTypes() {
   }

   private static ScaleType register(String path) {
      return register(Apoli.id(path), false);
   }

   private static ScaleType registerDimension(String path) {
      return register(Apoli.id(path), true);
   }

   public static ScaleType register(class_2960 id, boolean affectsDimensions) {
      ScaleType existing = (ScaleType)BY_ID.get(id);
      if (existing != null) {
         return existing;
      }

      ScaleType type = new ScaleType(id, BY_INDEX.size(), affectsDimensions);
      BY_ID.put(id, type);
      BY_INDEX.add(type);
      return type;
   }

   @Nullable
   public static ScaleType get(@Nullable class_2960 id) {
      return id == null ? null : (ScaleType)BY_ID.get(id);
   }

   public static int count() {
      return BY_INDEX.size();
   }

   public static ScaleType byIndex(int index) {
      return (ScaleType)BY_INDEX.get(index);
   }

   public static List<ScaleType> all() {
      return Collections.unmodifiableList(BY_INDEX);
   }

   public static Iterable<class_2960> ids() {
      return BY_ID.keySet();
   }

   public static void bootstrap() {
   }
}
