package dev.alphasucks.aea.port.data.sound;

import com.mojang.serialization.Codec;
import dev.alphasucks.aea.port.Apoli;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.minecraft.class_2960;
import net.minecraft.class_5819;
import org.jetbrains.annotations.Nullable;

public final class SoundReplacements {
   private static final Pattern REGEX_METACHARACTERS = Pattern.compile("[\\\\^$|?*+()\\[\\]{}]");
   private static final Pattern PLAIN_PATH = Pattern.compile("[a-z0-9_.\\-/]+");
   private final Map<String, List<WeightedSound>> raw;
   private final Map<String, SoundReplacements.Choice> exact;
   private final List<SoundReplacements.Rule> rules;
   public static final Codec<SoundReplacements> CODEC = Codec.unboundedMap(Codec.STRING, WeightedSound.LIST_CODEC).xmap(SoundReplacements::compile, r -> r.raw);

   private SoundReplacements(Map<String, List<WeightedSound>> raw, Map<String, SoundReplacements.Choice> exact, List<SoundReplacements.Rule> rules) {
      this.raw = raw;
      this.exact = exact;
      this.rules = rules;
   }

   public boolean isEmpty() {
      return this.exact.isEmpty() && this.rules.isEmpty();
   }

   @Nullable
   public WeightedSound find(class_2960 soundId, class_5819 random) {
      SoundReplacements.Choice direct = (SoundReplacements.Choice)this.exact.get(soundId.toString());
      if (direct != null) {
         return direct.pick(random);
      }

      if (this.rules.isEmpty()) {
         return null;
      }

      String name = soundId.toString();

      for (int i = 0; i < this.rules.size(); i++) {
         SoundReplacements.Rule rule = (SoundReplacements.Rule)this.rules.get(i);
         if (!rule.choice.captureReferences) {
            if (rule.pattern.matcher(name).matches()) {
               return rule.choice.pick(random);
            }
         } else {
            Matcher matcher = rule.pattern.matcher(name);
            if (matcher.matches()) {
               WeightedSound picked = rule.choice.pick(random);

               try {
                  return picked.withId(matcher.replaceFirst(picked.id()));
               } catch (RuntimeException e) {
                  Apoli.LOGGER
                     .warn(
                        "[Apoli] Sound replacement \"{}\" for pattern \"{}\" has a bad capture group reference: {}",
                        new Object[]{picked.id(), rule.pattern.pattern(), e.getMessage()}
                     );
                  return picked;
               }
            }
         }
      }

      return null;
   }

   private static SoundReplacements compile(Map<String, List<WeightedSound>> raw) {
      Map<String, SoundReplacements.Choice> exact = new LinkedHashMap(raw.size());
      List<SoundReplacements.Rule> rules = new ArrayList();

      for (Entry<String, List<WeightedSound>> entry : raw.entrySet()) {
         String key = (String)entry.getKey();
         List<WeightedSound> options = (List<WeightedSound>)entry.getValue();
         if (!options.isEmpty()) {
            SoundReplacements.Choice choice = choice(options);
            if (!REGEX_METACHARACTERS.matcher(key).find()) {
               exact.put(namespaced(key), choice);
            } else {
               try {
                  rules.add(new SoundReplacements.Rule(Pattern.compile(key), choice));
               } catch (PatternSyntaxException e) {
                  Apoli.LOGGER.warn("[Apoli] Sound replacement key \"{}\" is not valid regex ({}); matching it literally.", key, e.getDescription());
                  exact.put(namespaced(key), choice);
               }
            }
         }
      }

      return new SoundReplacements(raw, exact, List.copyOf(rules));
   }

   private static String namespaced(String key) {
      if (key.indexOf(58) >= 0) {
         return key;
      } else {
         return PLAIN_PATH.matcher(key).matches() ? "minecraft:" + key : key;
      }
   }

   private static SoundReplacements.Choice choice(List<WeightedSound> options) {
      int[] cumulative = new int[options.size()];
      int running = 0;
      boolean captures = false;

      for (int i = 0; i < options.size(); i++) {
         WeightedSound sound = (WeightedSound)options.get(i);
         running += sound.weight();
         cumulative[i] = running;
         if (sound.hasCaptureReference()) {
            captures = true;
         }
      }

      return new SoundReplacements.Choice(List.copyOf(options), cumulative, captures);
   }

   private record Choice(List<WeightedSound> options, int[] cumulative, boolean captureReferences) {
      WeightedSound pick(class_5819 random) {
         if (this.options.size() == 1) {
            return (WeightedSound)this.options.get(0);
         }

         int total = this.cumulative[this.cumulative.length - 1];
         int roll = random.method_43048(total);

         for (int i = 0; i < this.cumulative.length; i++) {
            if (roll < this.cumulative[i]) {
               return (WeightedSound)this.options.get(i);
            }
         }

         return (WeightedSound)this.options.get(this.options.size() - 1);
      }
   }

   private record Rule(Pattern pattern, SoundReplacements.Choice choice) {
   }
}
