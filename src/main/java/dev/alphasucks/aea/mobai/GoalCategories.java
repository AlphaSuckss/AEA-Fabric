package dev.alphasucks.aea.mobai;

import java.util.Locale;
import net.minecraft.class_1338;
import net.minecraft.class_1347;
import net.minecraft.class_1350;
import net.minecraft.class_1352;
import net.minecraft.class_1361;
import net.minecraft.class_1366;
import net.minecraft.class_1374;
import net.minecraft.class_1376;
import net.minecraft.class_1379;
import net.minecraft.class_1380;
import net.minecraft.class_1381;
import net.minecraft.class_1383;
import net.minecraft.class_1399;

/**
 * The "remove" names of aea:mob_ai. Vanilla goals are matched by class (their names are obfuscated in game);
 * goals from other mods (and AEA's clones) by words in their class name. An entry with a '.' or '$' is an exact
 * class name.
 */
final class GoalCategories {
    private GoalCategories() {
    }

    static boolean isClassName(String category) {
        return category.indexOf('.') >= 0 || category.indexOf('$') >= 0;
    }

    private static boolean named(class_1352 goal, String... words) {
        String name = goal.getClass().getName();
        for (String word : words) {
            if (name.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /** Does this category remove the goal from the mob's goal selector? */
    static boolean matchesGoal(String category, class_1352 goal) {
        if (isClassName(category)) {
            return matchesClass(category, goal);
        }
        return switch (category.toLowerCase(Locale.ROOT)) {
            case "all", "goals" -> true;
            case "wander" -> goal instanceof class_1379 || named(goal, "Wander", "Stroll");
            case "look_around" -> goal instanceof class_1376 || named(goal, "LookAround", "RandomLook");
            case "look_at" -> goal instanceof class_1361 || named(goal, "LookAt", "LookAtPlayer");
            case "melee" -> goal instanceof class_1366 || named(goal, "Melee");
            case "ranged" -> goal instanceof class_1380 || goal instanceof class_1381 || goal instanceof class_1383
                || named(goal, "Bow", "Crossbow", "Ranged", "Projectile");
            case "follow_owner" -> goal instanceof class_1350 || named(goal, "FollowOwner");
            case "panic" -> goal instanceof class_1374 || named(goal, "Panic", "EscapeDanger");
            case "flee" -> goal instanceof class_1338 || named(goal, "Flee", "Avoid");
            case "swim" -> goal instanceof class_1347 || named(goal, "Swim", "Float");
            default -> false;
        };
    }

    /** Does this category remove the goal from the mob's target selector? */
    static boolean matchesTarget(String category, class_1352 goal) {
        if (isClassName(category)) {
            return matchesClass(category, goal);
        }
        return switch (category.toLowerCase(Locale.ROOT)) {
            case "all", "targets" -> true;
            case "revenge" -> goal instanceof class_1399 || named(goal, "Revenge", "HurtBy");
            default -> false;
        };
    }

    private static boolean matchesClass(String category, class_1352 goal) {
        String name = goal.getClass().getName();
        return name.equals(category) || name.endsWith("." + category) || name.endsWith("$" + category);
    }
}
