package dev.alphasucks.aea.port.scale;

import org.jetbrains.annotations.Nullable;

public interface ScaleHolder {
   @Nullable
   ScaleState apoli$scales();

   ScaleState apoli$scalesOrCreate();
}
