package dev.alphasucks.aea.port.loader;

import org.jetbrains.annotations.Nullable;

public enum JsonFormat {
   JSON(".json"),
   JSONC(".jsonc"),
   JSON5(".json5");

   private final String extension;

   JsonFormat(String extension) {
      this.extension = extension;
   }

   public String extension() {
      return this.extension;
   }

   public boolean allowsComments() {
      return this != JSON;
   }

   @Nullable
   public static JsonFormat of(String path) {
      int dot = path.lastIndexOf(46);
      if (dot < 0) {
         return null;
      } else {
         int length = path.length() - dot;
         if (length == 5 && path.startsWith(".json", dot)) {
            return JSON;
         } else if (length == 6 && path.startsWith(".jsonc", dot)) {
            return JSONC;
         } else {
            return length == 6 && path.startsWith(".json5", dot) ? JSON5 : null;
         }
      }
   }
}
