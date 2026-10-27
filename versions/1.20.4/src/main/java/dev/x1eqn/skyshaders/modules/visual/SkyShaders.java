package dev.x1eqn.skyshaders.modules.visual;

import java.util.Locale;
import dev.x1eqn.skyshaders.config.SkyConfig;

import dev.x1eqn.skyshaders.util.ThemeColors;

public class SkyShaders  {
   public static final String[] stringArray;
   private static SkyShaders instance;
   private static float[] floatArray;
   private static float[] floatArray2;
   private static long time = 0L;

   public SkyShaders() { instance = this; }

   public boolean isEnabled() { return SkyConfig.current().enabled; }

   public <T> T getSetting(String key, T fallback) { return SkyConfig.current().getSetting(key, fallback); }

   public static boolean active() {
      SkyShaders skyShaders = instance;
      if (skyShaders != null) {
         boolean isEnabled = skyShaders.isEnabled();
         if (isEnabled) {
            return true;
         }
      }

      return false;
   }

   public static String preset() {
      String setting;
      if (instance != null) {
         setting = instance.getSetting("Preset", "Aurora");
      } else {
         setting = "Aurora";
      }

      String setting2 = setting;
      String[] stringArray2 = stringArray;
      int length = stringArray2.length;
      int value = 0;

      while (true) {
         int value2 = value;
         if (value2 >= length) {
            return "Aurora";
         }

         int value3 = value;
         String text = stringArray2[value3];
         boolean flag = text.equals(setting2);
         if (flag) {
            return text;
         }

         value++;
      }
   }

   public static String getString(String text) {
      Locale rOOT = Locale.ROOT;
      return text.toLowerCase(rOOT).replace(" ", "");
   }

   public static float compute() {
      Float floatValue = 1.0F;
      float factor = getObject("Speed", floatValue);
      return computeFloat(factor, 0.0F, 5.0F);
   }

   public static float compute2() {
      Float floatValue = 1.0F;
      float factor = getObject("Brightness", floatValue);
      return computeFloat(factor, 0.0F, 3.0F);
   }

   public static float compute3() {
      Float floatValue = 1.0F;
      float factor = getObject("Intensity", floatValue);
      return computeFloat(factor, 0.0F, 3.0F);
   }

   public static float compute4() {
      Float floatValue = 1.0F;
      float factor = getObject("Scale", floatValue);
      return computeFloat(factor, 0.25F, 3.0F);
   }

   public static float compute5() {
      Float floatValue = 1.0F;
      float factor = getObject("Stars", floatValue);
      return computeFloat(factor, 0.0F, 3.0F);
   }

   public static float compute6() {
      Float floatValue = 0.3F;
      float factor = getObject("Sky Rotation", floatValue);
      return computeFloat(factor, -5.0F, 5.0F);
   }

   public static float compute7() {
      Float floatValue = 0.5F;
      float factor = getObject("Horizon Blend", floatValue);
      return computeFloat(factor, 0.0F, 1.0F);
   }

   public static boolean matchFog() {
      Boolean booleanValue = true;
      return getObject("Match Fog", booleanValue);
   }

   public static boolean keepSunMoon() {
      Boolean booleanValue = false;
      return getObject("Sun & Moon", booleanValue);
   }

   public static boolean hideClouds() {
      boolean isActive = active();
      if (isActive) {
         Boolean booleanValue = true;
         boolean flag = getObject("Hide Clouds", booleanValue);
         if (flag) {
            return true;
         }
      }

      return false;
   }

   public static boolean test() {
      Boolean booleanValue = true;
      return getObject("In The End", booleanValue);
   }

   public static float getFull() {
      String setting;
      if (instance != null) {
         setting = instance.getSetting("Resolution", "Full");
      } else {
         setting = "Full";
      }

      String setting2 = setting;
      int value = -1;
      int value2 = setting2.hashCode();
      switch (value2) {
         case 52458:
            boolean flag2 = setting2.equals("50%");
            if (flag2) {
               value = 1;
            }
            break;
         case 54535:
            boolean flag = setting2.equals("75%");
            if (flag) {
               value = 0;
            }
      }

      int value3 = value;

      return switch (value3) {
         case 0 -> 0.75F;
         case 1 -> 0.5F;
         default -> 1.0F;
      };
   }

   private static int[] getIntArray(String text) {
      int value = -1;
      int value2 = text.hashCode();
      switch (value2) {
         case -1965582497:
            boolean flag6 = text.equals("Nebula");
            if (flag6) {
               value = 3;
            }
            break;
         case -1710645595:
            boolean flag4 = text.equals("Thunderstorm");
            if (flag4) {
               value = 2;
            }
            break;
         case -1315838013:
            boolean flag3 = text.equals("Dreamscape");
            if (flag3) {
               value = 6;
            }
            break;
         case -1301352427:
            boolean flag5 = text.equals("Synthwave");
            if (flag5) {
               value = 4;
            }
            break;
         case 77382433:
            boolean flag8 = text.equals("Prism");
            if (flag8) {
               value = 7;
            }
            break;
         case 296012929:
            boolean flag = text.equals("Black Hole");
            if (flag) {
               value = 1;
            }
            break;
         case 785408423:
            boolean flag7 = text.equals("Blood Moon");
            if (flag7) {
               value = 5;
            }
            break;
         case 2125565744:
            boolean flag2 = text.equals("Galaxy");
            if (flag2) {
               value = 0;
            }
      }

      int value3 = value;
      int[] ints;
      switch (value3) {
         case 0:
            ints = new int[]{16763274, 9413375};
            break;
         case 1:
            ints = new int[]{16767392, 16738847};
            break;
         case 2:
            ints = new int[]{13227775, 5923712};
            break;
         case 3:
            ints = new int[]{16727450, 3065343};
            break;
         case 4:
            ints = new int[]{16722541, 383464};
            break;
         case 5:
            ints = new int[]{16726831, 5900810};
            break;
         case 6:
            ints = new int[]{8300543, 16757703};
            break;
         case 7:
            ints = new int[2];
            int accentRgb = ThemeColors.getAccentRgb();
            ints[0] = accentRgb;
            int accent2Rgb = ThemeColors.getAccent2Rgb();
            ints[1] = accent2Rgb;
            break;
         default:
            ints = new int[]{2883464, 9063935};
      }

      return ints;
   }

   public static float[][] colors() {
      String preset = preset();
      boolean flag = "Prism".equals(preset);
      if (flag) {
         Boolean booleanValue = true;
         boolean flag2 = getObject("Natural Colors", booleanValue);
         if (flag2) {
            return getFloatArray();
         }
      }

      int[] intArray = getIntArray(preset);
      int element = intArray[0];
      int element2 = element;
      int element3 = intArray[1];
      int element32 = element3;
      Boolean booleanValue2 = true;
      boolean flag3 = getObject("Natural Colors", booleanValue2);
      if (!flag3 && instance != null) {
         Object setting = instance.getSetting("Color", null);
         Object setting2 = instance.getSetting("Second Color", null);
         int element22 = element2;
         int resolveRgb = ThemeColors.resolveRgb(setting, element22, false);
         element2 = resolveRgb;
         int element322 = element32;
         int resolveRgb2 = ThemeColors.resolveRgb(setting2, element322, true);
         element32 = resolveRgb2;
      }

      float[][] floatsGrid = new float[2][];
      int element23 = element2;
      float[] floatArray3 = getFloatArray(element23);
      floatsGrid[0] = floatArray3;
      int element323 = element32;
      float[] floatArray4 = getFloatArray(element323);
      floatsGrid[1] = floatArray4;
      return floatsGrid;
   }

   public static float[] horizon(String text, float[] factors, float[] factors2) {
      int value = -1;
      int value2 = text.hashCode();
      switch (value2) {
         case -1965582497:
            boolean flag = text.equals("Nebula");
            if (flag) {
               value = 3;
            }
            break;
         case -1710645595:
            boolean flag6 = text.equals("Thunderstorm");
            if (flag6) {
               value = 2;
            }
            break;
         case -1315838013:
            boolean flag7 = text.equals("Dreamscape");
            if (flag7) {
               value = 6;
            }
            break;
         case -1301352427:
            boolean flag2 = text.equals("Synthwave");
            if (flag2) {
               value = 4;
            }
            break;
         case 77382433:
            boolean flag8 = text.equals("Prism");
            if (flag8) {
               value = 7;
            }
            break;
         case 296012929:
            boolean flag5 = text.equals("Black Hole");
            if (flag5) {
               value = 1;
            }
            break;
         case 785408423:
            boolean flag4 = text.equals("Blood Moon");
            if (flag4) {
               value = 5;
            }
            break;
         case 2125565744:
            boolean flag3 = text.equals("Galaxy");
            if (flag3) {
               value = 0;
            }
      }

      int value3 = value;

      return switch (value3) {
         case 0 -> getFloatArray(getFloatArray(790300), factors2, 0.1F);
         case 1 -> getFloatArray(getFloatArray(657936), factors2, 0.06F);
         case 2 -> getFloatArray(factors2, 0.45F);
         case 3 -> {
            float[] floatArray5 = getFloatArray(789014);
            float[] floatArray6 = getFloatArray(factors, factors2, 0.5F);
            yield getFloatArray(floatArray5, floatArray6, 0.08F);
         }
         case 4 -> getFloatArray(factors, 0.8F);
         case 5 -> getFloatArray(factors2, 0.8F);
         case 6 -> {
            float[] floatArray4 = getFloatArray(16769216);
            yield getFloatArray(factors2, floatArray4, 0.25F);
         }
         case 7 -> {
            float[] floatArray7 = getFloatArray(460815);
            float[] floatArray8 = getFloatArray(factors, factors2, 0.5F);
            yield getFloatArray(floatArray7, floatArray8, 0.14F);
         }
         default -> getFloatArray(getFloatArray(726562), factors, 0.08F);
      };
   }

   public static synchronized float[][] getFloatArray() {
      int accentRgb = ThemeColors.getAccentRgb();
      float[] floatArray3 = getFloatArray(accentRgb);
      int accent2Rgb = ThemeColors.getAccent2Rgb();
      float[] floatArray4 = getFloatArray(accent2Rgb);
      long nanoTime = System.nanoTime();
      if (floatArray == null) {
         floatArray = (float[])floatArray3.clone();
         floatArray2 = (float[])floatArray4.clone();
      }

      long time2 = time;
      float factor;
      if (time2 == 0L) {
         factor = 0.0F;
      } else {
         long time3 = time;
         long diff = nanoTime - time3;
         float factor2 = (float)diff;
         float quotient = factor2 / 1.0E9F;
         factor = Math.min(0.1F, quotient);
      }

      float factor3 = factor;
      time = nanoTime;
      float product = factor3 * 3.2F;
      double amount = product;
      double amount2 = Math.exp(-amount);
      float factor4 = (float)amount2;
      float diff2 = 1.0F - factor4;
      floatArray = getFloatArray(floatArray, floatArray3, diff2);
      floatArray2 = getFloatArray(floatArray2, floatArray4, diff2);
      float[][] floatsGrid = new float[2][];
      float[] f = (float[])floatArray.clone();
      floatsGrid[0] = f;
      float[] f2 = (float[])floatArray2.clone();
      floatsGrid[1] = f2;
      return floatsGrid;
   }

   private static <T> T getObject(String text, T obj) {
      SkyShaders skyShaders = instance;
      if (skyShaders == null) {
         return (T)obj;
      }

      Object setting = skyShaders.getSetting(text, obj);
      boolean isInstance = obj instanceof Float;
      if (isInstance) {
         boolean isInstance2 = setting instanceof Number;
         if (isInstance2) {
            float factor = ((Number)setting).floatValue();
            return (T)(Object)factor;
         }
      }

      boolean isInstance3 = obj instanceof Boolean;
      if (isInstance3) {
         boolean isInstance4 = setting instanceof Boolean;
         if (!isInstance4) {
            return (T)obj;
         }
      }

      return (T)setting;
   }

   private static float computeFloat(float factor, float factor2, float factor3) {
      boolean isNaN = Float.isNaN(factor);
      if (isNaN) {
         return factor2;
      }

      float min = Math.min(factor3, factor);
      return Math.max(factor2, min);
   }

   private static float[] getFloatArray(int value) {
      float[] floats = new float[3];
      int shifted = value >> 16;
      int bits = shifted & 0xFF;
      float factor = bits;
      float quotient = factor / 255.0F;
      floats[0] = quotient;
      int shifted2 = value >> 8;
      int bits2 = shifted2 & 0xFF;
      float factor2 = bits2;
      float quotient2 = factor2 / 255.0F;
      floats[1] = quotient2;
      int bits3 = value & 0xFF;
      float factor3 = bits3;
      float quotient3 = factor3 / 255.0F;
      floats[2] = quotient3;
      return floats;
   }

   private static float[] getFloatArray(float[] floatArray3, float[] factors, float factor) {
      float[] floats = new float[3];
      float element = floatArray3[0];
      float element2 = factors[0];
      float element3 = floatArray3[0];
      float diff = element2 - element3;
      float product = diff * factor;
      float sum = element + product;
      floats[0] = sum;
      float element4 = floatArray3[1];
      float element5 = factors[1];
      float element6 = floatArray3[1];
      float diff2 = element5 - element6;
      float product2 = diff2 * factor;
      float sum2 = element4 + product2;
      floats[1] = sum2;
      float element7 = floatArray3[2];
      float element8 = factors[2];
      float element9 = floatArray3[2];
      float diff3 = element8 - element9;
      float product3 = diff3 * factor;
      float sum3 = element7 + product3;
      floats[2] = sum3;
      return floats;
   }

   private static float[] getFloatArray(float[] factors, float factor) {
      float[] floats = new float[3];
      float element = factors[0];
      float product = element * factor;
      floats[0] = product;
      float element2 = factors[1];
      float product2 = element2 * factor;
      floats[1] = product2;
      float element3 = factors[2];
      float product3 = element3 * factor;
      floats[2] = product3;
      return floats;
   }

   static {
      String[] strings = new String[]{"Aurora", "Galaxy", "Black Hole", "Thunderstorm", "Nebula", "Synthwave", "Blood Moon", "Dreamscape", "Prism"};
      stringArray = strings;
   }
}
