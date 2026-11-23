package dev.alphasucks.aea.port.scale;

import net.minecraft.class_2960;

public final class ScaleType {
   private static final ScaleType[] NO_PARENTS = new ScaleType[0];
   private final class_2960 id;
   private final int index;
   private final boolean affectsDimensions;
   private ScaleType[] multipliers = NO_PARENTS;
   private ScaleType[] divisors = NO_PARENTS;

   ScaleType(class_2960 id, int index, boolean affectsDimensions) {
      this.id = id;
      this.index = index;
      this.affectsDimensions = affectsDimensions;
   }

   public class_2960 id() {
      return this.id;
   }

   public int index() {
      return this.index;
   }

   public boolean affectsDimensions() {
      return this.affectsDimensions;
   }

   public ScaleType[] multipliers() {
      return this.multipliers;
   }

   public ScaleType[] divisors() {
      return this.divisors;
   }

   ScaleType multipliedBy(ScaleType... parents) {
      this.multipliers = parents;
      return this;
   }

   ScaleType dividedBy(ScaleType... parents) {
      this.divisors = parents;
      return this;
   }

   public String toString() {
      return this.id.toString();
   }
}
