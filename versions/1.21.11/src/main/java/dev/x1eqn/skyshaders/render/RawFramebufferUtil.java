package dev.x1eqn.skyshaders.render;

import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import java.lang.reflect.Method;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public final class RawFramebufferUtil {
   private static int fbo = 0;
   private static boolean loggedBadTexture = false;
   private static boolean loggedIncomplete = false;

   private RawFramebufferUtil() {
   }

   public static void copy(RenderTarget renderTarget, RenderTarget renderTarget2, boolean flag) {
      RenderSystem.assertOnRenderThread();
      if (isUsable(renderTarget) && isUsable(renderTarget2)) {
         CommandEncoder createCommandEncoder = RenderSystem.getDevice().createCommandEncoder();
         int min = Math.min(renderTarget.width, renderTarget2.width);
         int min2 = Math.min(renderTarget.height, renderTarget2.height);
         GpuTexture colorTexture = renderTarget.getColorTexture();
         GpuTexture colorTexture2 = renderTarget2.getColorTexture();
         if (colorTexture != null && colorTexture2 != null) {
            createCommandEncoder.copyTextureToTexture(colorTexture, colorTexture2, 0, 0, 0, 0, 0, min, min2);
         }

         if (flag) {
            GpuTexture depthTexture = renderTarget.getDepthTexture();
            GpuTexture depthTexture2 = renderTarget2.getDepthTexture();
            if (depthTexture != null && depthTexture2 != null) {
               createCommandEncoder.copyTextureToTexture(depthTexture, depthTexture2, 0, 0, 0, 0, 0, min, min2);
            }
         }
      }
   }

   public static void clear(RenderTarget renderTarget) {
      RenderSystem.assertOnRenderThread();
      if (isUsable(renderTarget)) {
         CommandEncoder createCommandEncoder = RenderSystem.getDevice().createCommandEncoder();
         GpuTexture colorTexture = renderTarget.getColorTexture();
         if (colorTexture != null && !colorTexture.isClosed()) {
            createCommandEncoder.clearColorTexture(colorTexture, 0);
         }
      }
   }

   public static void clearColorAndDepth(RenderTarget renderTarget) {
      RenderSystem.assertOnRenderThread();
      if (isUsable(renderTarget)) {
         CommandEncoder createCommandEncoder = RenderSystem.getDevice().createCommandEncoder();
         GpuTexture colorTexture = renderTarget.getColorTexture();
         GpuTexture depthTexture = renderTarget.getDepthTexture();
         if (colorTexture != null && !colorTexture.isClosed()) {
            if (depthTexture != null && !depthTexture.isClosed()) {
               createCommandEncoder.clearColorAndDepthTextures(colorTexture, 0, depthTexture, 1.0);
            } else {
               createCommandEncoder.clearColorTexture(colorTexture, 0);
            }
         }
      }
   }

   public static boolean bindDirect(RenderTarget maskFbo) {
      RenderSystem.assertOnRenderThread();
      if (!isUsable(maskFbo)) {
         return false;
      }

      int glGetInteger = GL11.glGetInteger(36006);
      int glId = getGlId(maskFbo.getColorTexture());
      GpuTexture depthTexture = maskFbo.getDepthTexture();
      int glId2 = depthTexture != null && !depthTexture.isClosed() ? getGlId(depthTexture) : 0;
      if (glId == 0 || !GL11.glIsTexture(glId) || glId2 != 0 && !GL11.glIsTexture(glId2)) {
         if (!loggedBadTexture) {
            loggedBadTexture = true;
         }

         return false;
      } else {
         for (int value = 0; value < 2; value++) {
            ensureFbo();
            attach(glId, glId2);
            int glCheckFramebufferStatus = GL30.glCheckFramebufferStatus(36160);
            if (glCheckFramebufferStatus == 36053) {
               GL11.glDrawBuffer(36064);
               GL11.glViewport(0, 0, maskFbo.width, maskFbo.height);
               return true;
            }

            if (!loggedIncomplete) {
               loggedIncomplete = true;
            }

            deleteFbo();
         }

         GL30.glBindFramebuffer(36160, GL30.glIsFramebuffer(glGetInteger) ? glGetInteger : 0);
         return false;
      }
   }

   private static void attach(int value, int value2) {
      GL30.glBindFramebuffer(36160, fbo);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, value, 0);
      GL30.glFramebufferTexture2D(36160, 36096, 3553, value2, 0);
   }

   public static void invalidate() {
      if (fbo != 0) {
         int glGetInteger = GL11.glGetInteger(36006);
         int glGetInteger2 = GL11.glGetInteger(36010);
         GL30.glBindFramebuffer(36160, fbo);
         GL30.glFramebufferTexture2D(36160, 36064, 3553, 0, 0);
         GL30.glFramebufferTexture2D(36160, 36096, 3553, 0, 0);
         GL30.glBindFramebuffer(36009, glGetInteger == fbo ? 0 : glGetInteger);
         GL30.glBindFramebuffer(36008, glGetInteger2 == fbo ? 0 : glGetInteger2);
      }
   }

   public static void dispose() {
      deleteFbo();
   }

   private static void deleteFbo() {
      if (fbo != 0) {
         if (GL11.glGetInteger(36006) == fbo) {
            GL30.glBindFramebuffer(36009, 0);
         }

         if (GL11.glGetInteger(36010) == fbo) {
            GL30.glBindFramebuffer(36008, 0);
         }

         GL30.glDeleteFramebuffers(fbo);
         fbo = 0;
      }
   }

   public static void unbind() {
      GL30.glBindFramebuffer(36160, 0);
   }

   public static int getColorTextureId(RenderTarget renderTarget) {
      return renderTarget == null ? 0 : getGlId(renderTarget.getColorTexture());
   }

   public static int getDepthTextureId(RenderTarget renderTarget) {
      return renderTarget == null ? 0 : getGlId(renderTarget.getDepthTexture());
   }

   private static boolean isUsable(RenderTarget renderTarget) {
      return renderTarget != null && renderTarget.width > 0 && renderTarget.height > 0 && renderTarget.getColorTexture() != null;
   }

   private static void ensureFbo() {
      if (fbo == 0) {
         fbo = GL30.glGenFramebuffers();
      }
   }

   public static int getGlId(GpuTexture colorTexture) {
      if (colorTexture == null || colorTexture.isClosed()) {
         return 0;
      } else if (colorTexture instanceof GlTexture glTexture) {
         return glTexture.glId();
      } else {
         try {
            Method method = colorTexture.getClass().getMethod("glId");
            return (Integer)method.invoke(colorTexture);
         } catch (Exception exception) {
            return 0;
         }
      }
   }
}
