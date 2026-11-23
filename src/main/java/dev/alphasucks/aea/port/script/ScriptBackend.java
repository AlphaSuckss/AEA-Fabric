package dev.alphasucks.aea.port.script;

import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public interface ScriptBackend {
   String name();

   boolean available();

   void beginReload();

   void load(class_2960 var1, String var2);

   void endReload();

   boolean has(class_2960 var1);

   @Nullable
   Object execute(class_2960 var1, ScriptCtx var2);
}
