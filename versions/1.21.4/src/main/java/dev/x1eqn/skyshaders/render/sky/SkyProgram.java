package dev.x1eqn.skyshaders.render.sky;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.opengl.GL20;

final class SkyProgram {
   private static final String DIR = "/assets/x1eqnsky/shaders/sky/";
   final int value;
   private final Map<String, Integer> locations = new HashMap<>();

   private SkyProgram(int value) {
      this.value = value;
   }

   static SkyProgram preset(String text) throws IOException {
      String text2 = "#version 330 core\n" + read("common.glsl") + "\n" + read(text + ".fsh");
      return link(read("sky.vsh"), text2, text);
   }

   static SkyProgram upscale() throws IOException {
      return link(read("upscale.vsh"), read("upscale.fsh"), "upscale");
   }

   private static SkyProgram link(String read, String read2, String text) {
      int compile = compile(35633, read, text + ".vsh");

      int compile2;
      try {
         compile2 = compile(35632, read2, text + ".fsh");
      } catch (RuntimeException runtimeException) {
         GL20.glDeleteShader(compile);
         throw runtimeException;
      }

      int glCreateProgram2 = GL20.glCreateProgram();
      GL20.glAttachShader(glCreateProgram2, compile);
      GL20.glAttachShader(glCreateProgram2, compile2);
      GL20.glBindAttribLocation(glCreateProgram2, 0, "Position");
      GL20.glLinkProgram(glCreateProgram2);
      GL20.glDetachShader(glCreateProgram2, compile);
      GL20.glDetachShader(glCreateProgram2, compile2);
      GL20.glDeleteShader(compile);
      GL20.glDeleteShader(compile2);
      if (GL20.glGetProgrami(glCreateProgram2, 35714) == 0) {
         String glGetProgramInfoLog = GL20.glGetProgramInfoLog(glCreateProgram2);
         GL20.glDeleteProgram(glCreateProgram2);
         throw new RuntimeException("link failed (" + text + "): " + glGetProgramInfoLog);
      } else {
         return new SkyProgram(glCreateProgram2);
      }
   }

   private static int compile(int value, String text, String text2) {
      int glCreateShader = GL20.glCreateShader(value);
      GL20.glShaderSource(glCreateShader, text);
      GL20.glCompileShader(glCreateShader);
      if (GL20.glGetShaderi(glCreateShader, 35713) == 0) {
         String glGetShaderInfoLog = GL20.glGetShaderInfoLog(glCreateShader);
         GL20.glDeleteShader(glCreateShader);
         throw new RuntimeException("compile failed (" + text2 + "): " + glGetShaderInfoLog);
      } else {
         return glCreateShader;
      }
   }

   private static String read(String text) throws IOException {
      try (InputStream resourceAsStream = SkyProgram.class.getResourceAsStream("/assets/x1eqnsky/shaders/sky/" + text)) {
         if (resourceAsStream == null) {
            throw new IOException("missing shader /assets/x1eqnsky/shaders/sky/" + text);
         } else {
            return new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8);
         }
      }
   }

   int loc(String text) {
      Integer integer = this.locations.get(text);
      if (integer == null) {
         integer = GL20.glGetUniformLocation(this.value, text);
         this.locations.put(text, integer);
      }

      return integer;
   }

   void set1i(String text, int value) {
      int loc = this.loc(text);
      if (loc >= 0) {
         GL20.glUniform1i(loc, value);
      }
   }

   void set1f(String text, float quotient) {
      int loc = this.loc(text);
      if (loc >= 0) {
         GL20.glUniform1f(loc, quotient);
      }
   }

   void set3f(String text, float lin, float lin2, float lin3) {
      int loc = this.loc(text);
      if (loc >= 0) {
         GL20.glUniform3f(loc, lin, lin2, lin3);
      }
   }

   void setMat3(String text, float[] mat3) {
      int loc = this.loc(text);
      if (loc >= 0) {
         GL20.glUniformMatrix3fv(loc, false, mat3);
      }
   }

   void setMat4(String text, float[] mat4) {
      int loc = this.loc(text);
      if (loc >= 0) {
         GL20.glUniformMatrix4fv(loc, false, mat4);
      }
   }

   void delete() {
      GL20.glDeleteProgram(this.value);
   }
}
