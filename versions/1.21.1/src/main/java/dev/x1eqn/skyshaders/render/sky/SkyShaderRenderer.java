package dev.x1eqn.skyshaders.render.sky;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import dev.x1eqn.skyshaders.SkyShadersClient;

import dev.x1eqn.skyshaders.modules.visual.SkyShaders;
import dev.x1eqn.skyshaders.render.RawFramebufferUtil;
import dev.x1eqn.skyshaders.render.RawGlStateCapture;
import net.minecraft.client.Minecraft;

import net.minecraft.world.level.material.FogType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

public final class SkyShaderRenderer {
   private static final Matrix4f levelProjection = new Matrix4f();
   private static boolean haveProjection = false;
   private static final Matrix4f viewRotation = new Matrix4f();
   private static FogType cameraFog = FogType.NONE;
   private static boolean activeThisFrame = false;
   private static boolean activeLastFrame = false;
   private static final Map<String, SkyProgram> programs = new HashMap<>();
   private static final Set<String> failed = new HashSet<>();
   private static SkyProgram upscaleProgram;
   private static boolean upscaleFailed = false;
   private static boolean loggedRenderFailure = false;
   private static int noiseTex = 0;
   private static int samplerRepeat = 0;
   private static int samplerClamp = 0;
   private static int vao = 0;
   private static int vbo = 0;
   private static int lowFbo = 0;
   private static int lowTex = 0;
   private static int lowW = 0;
   private static int lowH = 0;
   private static long lastNanos = 0L;
   private static double animTime = 0.0;
   private static double celestialAngle = 0.0;
   private static final Matrix4f tmpVP = new Matrix4f();
   private static final Matrix4f tmpInv = new Matrix4f();
   private static final Matrix3f tmpCel = new Matrix3f();
   private static final float[] mat4 = new float[16];
   private static final float[] mat3 = new float[9];
   private static final Vector3f CELESTIAL_AXIS = new Vector3f(0.0F, 0.766F, 0.643F);
   private static float dirSign = 1.0F;
   private static final Matrix4f projForPixel = new Matrix4f();
   private static final Vector4f tmpA = new Vector4f();
   private static final Vector4f tmpB = new Vector4f();

   private SkyShaderRenderer() {
   }

   public static void beginFrame(Matrix4fc matrix4fc, Matrix4fc projection, FogType fogType) {
      activeLastFrame = activeThisFrame;
      activeThisFrame = false;
      if (matrix4fc != null) {
         viewRotation.set(matrix4fc);
      }

      captureProjection(projection); cameraFog = fogType;
   }

   public static void captureProjection(Matrix4fc matrix4fc) {
      levelProjection.set(matrix4fc);
      haveProjection = true;
   }

   public static boolean levelViewProjection(Matrix4f viewProj) {
      Matrix4f matrix4f = haveProjection ? levelProjection : null;
      if (matrix4f == null) {
         return false;
      }

      matrix4f.mul(viewRotation, viewProj);
      return viewProj.isFinite();
   }

   public static boolean levelProjection(Matrix4f proj) {
      Matrix4f matrix4f = haveProjection ? levelProjection : null;
      if (matrix4f == null) {
         return false;
      }

      proj.set(matrix4f);
      return proj.isFinite();
   }

   public static void cameraAxes(float[] axR, float[] axU, float[] axF) {
      axR[0] = viewRotation.m00();
      axR[1] = viewRotation.m10();
      axR[2] = viewRotation.m20();
      axU[0] = viewRotation.m01();
      axU[1] = viewRotation.m11();
      axU[2] = viewRotation.m21();
      axF[0] = -viewRotation.m02();
      axF[1] = -viewRotation.m12();
      axF[2] = -viewRotation.m22();
   }

   public static boolean isActiveThisFrame() {
      return activeThisFrame;
   }

   public static boolean wasActiveLastFrame() {
      return activeLastFrame && SkyShaders.active();
   }

   public static boolean tryRender(boolean flag) {
      if (!SkyShaders.active()) {
         return false;
      }

      if (flag && !SkyShaders.test()) {
         return false;
      }

      FogType cameraState2 = cameraFog;
      if (cameraState2 != null && cameraState2 != FogType.NONE) {
         return false;
      }

      boolean isDraw;
      try {
         isDraw = draw();
      } catch (Throwable exception) {
         if (!loggedRenderFailure) {
            SkyShadersClient.LOGGER.error("Sky rendering failed; restoring the vanilla sky", exception);
            loggedRenderFailure = true;
         }
         isDraw = false;
      }

      activeThisFrame = isDraw;
      return isDraw;
   }

   private static boolean draw() {
      RenderSystem.assertOnRenderThread();
      RenderTarget mainRenderTarget = Minecraft.getInstance().getMainRenderTarget();
      if (mainRenderTarget != null && mainRenderTarget.width > 0 && mainRenderTarget.height > 0) {
         String preset = SkyShaders.preset();
         String string = SkyShaders.getString(preset);
         if (failed.contains(string)) {
            return false;
         }

         if (!computeMatrices()) {
            return false;
         }

         tickClocks();

         try (RawGlStateCapture capture = RawGlStateCapture.capture()) {
            SkyProgram skyProgram = program(string);
            if (skyProgram != null && ensureCommon()) {
               float full = SkyShaders.getFull();
               int value2 = full < 0.999F && upscale() != null ? 1 : 0;
               int width = mainRenderTarget.width;
               int height = mainRenderTarget.height;
               int value3 = value2 != 0 ? Math.max(1, Math.round(width * full)) : width;
               int value4 = value2 != 0 ? Math.max(1, Math.round(height * full)) : height;
               GlStateManager._disableDepthTest();
               GlStateManager._depthMask(false);
               GlStateManager._disableBlend();
               GlStateManager._disableCull();
               GlStateManager._disableScissorTest();
               GL11.glColorMask(true, true, true, true);
               if (value2 != 0 && !ensureLowTarget(value3, value4)) {
                  value2 = 0;
                  value3 = width;
                  value4 = height;
               }

               if (value2 != 0) {
                  GL30.glBindFramebuffer(36160, lowFbo);
                  GL11.glDrawBuffer(36064);
                  GlStateManager._viewport(0, 0, value3, value4);
               } else if (!RawFramebufferUtil.bindDirect(mainRenderTarget)) {
                  int value5 = 0;
                  return false;
               }

               int value5 = boundSampler0();
               GlStateManager._activeTexture(33984);
               GlStateManager._bindTexture(noiseTex);
               GL33.glBindSampler(0, samplerRepeat);
               GL20.glUseProgram(skyProgram.value);
               setUniforms(skyProgram, preset, value4);
               drawTriangle();
               if (value2 != 0) {
                  SkyProgram upscale = upscale();
                  if (upscale != null && RawFramebufferUtil.bindDirect(mainRenderTarget)) {
                     GlStateManager._bindTexture(lowTex);
                     GL33.glBindSampler(0, samplerClamp);
                     GL20.glUseProgram(upscale.value);
                     upscale.set1i("uSource", 0);
                     drawTriangle();
                  }
               }

               GL33.glBindSampler(0, value5);
               int value6 = 1;
               return true;
            }

            boolean flag = false;
         }

         return false;
      } else {
         return false;
      }
   }

   private static void setUniforms(SkyProgram skyProgram, String text, int value) {
      float[][] colors = SkyShaders.colors();
      float[] floats = colors[0];
      float[] floats2 = colors[1];
      float[] horizon = SkyShaders.horizon(text, floats, floats2);
      tmpInv.get(mat4);
      skyProgram.setMat4("uInvViewProj", mat4);
      skyProgram.set1i("uNoise", 0);
      skyProgram.set1f("uTime", (float)(animTime % 10000.0));
      skyProgram.set3f("uColor1", lin(floats[0]), lin(floats[1]), lin(floats[2]));
      skyProgram.set3f("uColor2", lin(floats2[0]), lin(floats2[1]), lin(floats2[2]));
      skyProgram.set1f("uIntensity", SkyShaders.compute3());
      skyProgram.set1f("uScale", SkyShaders.compute4());
      skyProgram.set1f("uStars", SkyShaders.compute5());
      skyProgram.set1f("uBrightness", SkyShaders.compute2());
      skyProgram.set3f("uHorizon", horizon[0], horizon[1], horizon[2]);
      skyProgram.set1f("uHorizonFog", SkyShaders.compute7());
      skyProgram.set1f("uDirSign", dirSign);
      tmpCel.rotation((float)celestialAngle, CELESTIAL_AXIS).get(mat3);
      skyProgram.setMat3("uCelestial", mat3);
      float abs = Math.abs(projForPixel.m11());
      skyProgram.set1f("uPixel", abs > 1.0E-4F ? 2.0F / (abs * value) : 0.002F);
   }

   private static boolean computeMatrices() {
      Matrix4f levelProjection2;
      if (haveProjection) {
         levelProjection2 = levelProjection;
      } else {
         if (!haveProjection) {
            return false;
         }

         levelProjection2 = levelProjection;
      }

      projForPixel.set(levelProjection2);
      levelProjection2.mul(viewRotation, tmpVP);
      if (tmpVP.isFinite() && !(Math.abs(tmpVP.determinant()) < 1.0E-12F)) {
         tmpVP.invert(tmpInv);
         if (!tmpInv.isFinite()) {
            return false;
         }

         tmpInv.transform(0.0F, 0.0F, -1.0F, 1.0F, tmpA);
         tmpInv.transform(0.0F, 0.0F, 1.0F, 1.0F, tmpB);
         float diff = tmpB.x * tmpA.w - tmpA.x * tmpB.w;
         float diff2 = tmpB.y * tmpA.w - tmpA.y * tmpB.w;
         float diff3 = tmpB.z * tmpA.w - tmpA.z * tmpB.w;
         float negated = -viewRotation.m02();
         float negated2 = -viewRotation.m12();
         float negated3 = -viewRotation.m22();
         dirSign = diff * negated + diff2 * negated2 + diff3 * negated3 < 0.0F ? -1.0F : 1.0F;
         return true;
      } else {
         return false;
      }
   }

   private static void tickClocks() {
      long nanoTime = System.nanoTime();
      double quotient = lastNanos == 0L ? 0.0 : (nanoTime - lastNanos) / 1.0E9;
      lastNanos = nanoTime;
      quotient = Math.max(0.0, Math.min(quotient, 0.1));
      animTime = animTime + quotient * SkyShaders.compute();
      celestialAngle = (celestialAngle + quotient * SkyShaders.compute6() * 0.010471975511965976) % (Math.PI * 2);
   }

   private static float lin(float element) {
      return (float)Math.pow(Math.max(element, 0.0F), 2.2);
   }

   private static SkyProgram program(String text) {
      SkyProgram skyProgram = programs.get(text);
      if (skyProgram != null) {
         return skyProgram;
      }

      try {
         skyProgram = SkyProgram.preset(text);
         programs.put(text, skyProgram);
         return skyProgram;
      } catch (Throwable exception) {
         failed.add(text);
         SkyShadersClient.LOGGER.error("Cannot compile sky preset {}", text, exception);
         return null;
      }
   }

   private static SkyProgram upscale() {
      if (upscaleProgram == null && !upscaleFailed) {
         try {
            upscaleProgram = SkyProgram.upscale();
         } catch (Throwable exception) {
            upscaleFailed = true;
            SkyShadersClient.LOGGER.error("Cannot compile sky upscaler; using full resolution", exception);
         }

         return upscaleProgram;
      } else {
         return upscaleProgram;
      }
   }

   private static boolean ensureCommon() {
      if (vao == 0) {
         vao = GL30.glGenVertexArrays();
         vbo = GL15.glGenBuffers();
         GL30.glBindVertexArray(vao);
         GL15.glBindBuffer(34962, vbo);
         GL15.glBufferData(34962, new float[]{-1.0F, -1.0F, 0.0F, 3.0F, -1.0F, 0.0F, -1.0F, 3.0F, 0.0F}, 35044);
         GL20.glEnableVertexAttribArray(0);
         GL20.glVertexAttribPointer(0, 3, 5126, false, 12, 0L);
         GL30.glBindVertexArray(0);
      }

      if (samplerRepeat == 0) {
         samplerRepeat = makeSampler(10497);
         samplerClamp = makeSampler(33071);
      }

      if (noiseTex == 0) {
         noiseTex = buildNoise();
      }

      return noiseTex != 0;
   }

   private static int makeSampler(int value) {
      int glGenSamplers = GL33.glGenSamplers();
      GL33.glSamplerParameteri(glGenSamplers, 10241, 9729);
      GL33.glSamplerParameteri(glGenSamplers, 10240, 9729);
      GL33.glSamplerParameteri(glGenSamplers, 10242, value);
      GL33.glSamplerParameteri(glGenSamplers, 10243, value);
      return glGenSamplers;
   }

   private static int buildNoise() {
      byte[] bytes = new byte[65536];
      byte[] bytes2 = new byte[65536];
      byte[] bytes3 = new byte[65536];
      int value = -1640531527;

      for (int value2 = 0; value2 < 65536; value2++) {
         int value5 = value ^ value << 13;
         int value6 = value5 ^ value5 >>> 17;
         int value7 = value6 ^ value6 << 5;
         bytes[value2] = (byte)(value7 >>> 24);
         int value8 = value7 ^ value7 << 13;
         int value9 = value8 ^ value8 >>> 17;
         int value10 = value9 ^ value9 << 5;
         bytes2[value2] = (byte)(value10 >>> 24);
         int value11 = value10 ^ value10 << 13;
         int value12 = value11 ^ value11 >>> 17;
         value = value12 ^ value12 << 5;
         bytes3[value2] = (byte)(value >>> 24);
      }

      ByteBuffer memAlloc = MemoryUtil.memAlloc(262144);

      int glGenTextures;
      try {
         for (int value3 = 0; value3 < 256; value3++) {
            for (int value4 = 0; value4 < 256; value4++) {
               int sum = value3 * 256 + value4;
               int sum2 = (value3 - 17 & 0xFF) * 256 + (value4 - 37 & 0xFF);
               memAlloc.put(bytes[sum]).put(bytes[sum2]).put(bytes2[sum]).put(bytes3[sum]);
            }
         }

         memAlloc.flip();
         int glGetInteger2 = GL11.glGetInteger(35055);
         GL15.glBindBuffer(35052, 0);
         int glGetInteger3 = GL11.glGetInteger(3314);
         int glGetInteger4 = GL11.glGetInteger(3316);
         int glGetInteger5 = GL11.glGetInteger(3315);
         int glGetInteger = GL11.glGetInteger(3317);
         GL11.glPixelStorei(3314, 0);
         GL11.glPixelStorei(3316, 0);
         GL11.glPixelStorei(3315, 0);
         GL11.glPixelStorei(3317, 4);
         glGenTextures = GL11.glGenTextures();
         GlStateManager._activeTexture(33984);
         GlStateManager._bindTexture(glGenTextures);
         GL11.glTexParameteri(3553, 10241, 9729);
         GL11.glTexParameteri(3553, 10240, 9729);
         GL11.glTexParameteri(3553, 10242, 10497);
         GL11.glTexParameteri(3553, 10243, 10497);
         GL11.glTexParameteri(3553, 33085, 0);
         GL11.glTexImage2D(3553, 0, 32856, 256, 256, 0, 6408, 5121, memAlloc);
         GL11.glPixelStorei(3314, glGetInteger3);
         GL11.glPixelStorei(3316, glGetInteger4);
         GL11.glPixelStorei(3315, glGetInteger5);
         GL11.glPixelStorei(3317, glGetInteger);
         GL15.glBindBuffer(35052, glGetInteger2);
      } finally {
         MemoryUtil.memFree(memAlloc);
      }

      return glGenTextures;
   }

   private static boolean ensureLowTarget(int value, int value2) {
      if (lowFbo != 0 && lowW == value && lowH == value2) {
         return true;
      }

      if (lowTex == 0) {
         lowTex = GL11.glGenTextures();
      }

      GlStateManager._activeTexture(33984);
      GlStateManager._bindTexture(lowTex);
      GL11.glTexParameteri(3553, 10241, 9729);
      GL11.glTexParameteri(3553, 10240, 9729);
      GL11.glTexParameteri(3553, 33085, 0);
      int glGetInteger = GL11.glGetInteger(35055);
      GL15.glBindBuffer(35052, 0);
      GL11.glTexImage2D(3553, 0, 34842, value, value2, 0, 6408, 5126, (FloatBuffer)null);
      GL15.glBindBuffer(35052, glGetInteger);
      if (lowFbo == 0) {
         lowFbo = GL30.glGenFramebuffers();
      }

      GL30.glBindFramebuffer(36160, lowFbo);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, lowTex, 0);
      boolean flag = GL30.glCheckFramebufferStatus(36160) == 36053;
      lowW = flag ? value : 0;
      lowH = flag ? value2 : 0;
      return flag;
   }

   private static int boundSampler0() {
      GlStateManager._activeTexture(33984);
      return GL11.glGetInteger(35097);
   }

   private static void drawTriangle() {
      GL30.glBindVertexArray(vao);
      GL11.glDrawArrays(4, 0, 3);
      GL30.glBindVertexArray(0);
   }

   public static void dispose() {
      try {
         Iterator valueIterator = programs.values().iterator();

         while (valueIterator.hasNext()) {
            ((SkyProgram)valueIterator.next()).delete();
         }

         programs.clear();
         failed.clear();
         upscaleFailed = false;
         loggedRenderFailure = false;
         lowW = lowH = 0;
         lastNanos = 0L;
         haveProjection = false;
         cameraFog = FogType.NONE;
         activeThisFrame = activeLastFrame = false;
         if (upscaleProgram != null) {
            upscaleProgram.delete();
            upscaleProgram = null;
         }

         if (noiseTex != 0) {
            GL11.glDeleteTextures(noiseTex);
            noiseTex = 0;
         }

         if (lowTex != 0) {
            GL11.glDeleteTextures(lowTex);
            lowTex = 0;
         }

         if (lowFbo != 0) {
            GL30.glDeleteFramebuffers(lowFbo);
            lowFbo = 0;
         }

         if (samplerRepeat != 0) {
            GL33.glDeleteSamplers(samplerRepeat);
            samplerRepeat = 0;
         }

         if (samplerClamp != 0) {
            GL33.glDeleteSamplers(samplerClamp);
            samplerClamp = 0;
         }

         if (vbo != 0) {
            GL15.glDeleteBuffers(vbo);
            vbo = 0;
         }

         if (vao != 0) {
            GL30.glDeleteVertexArrays(vao);
            vao = 0;
         }
      } catch (Throwable exception) {
      }
   }

}
