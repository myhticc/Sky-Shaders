package dev.x1eqn.skyshaders.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import dev.x1eqn.skyshaders.modules.visual.SkyShaders;
import dev.x1eqn.skyshaders.render.sky.SkyShaderRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProjectionMatrixBuffer.class)
public abstract class SkyShadersProjectionMixin {
   @Unique
   private static final Matrix4f x1eqnsky$tmp = new Matrix4f();

   @Unique
   private boolean x1eqnsky$isLevelBuffer() {
      Minecraft mc = Minecraft.getInstance();
      return mc != null && mc.gameRenderer != null ? ((GameRendererSkyAccessor)mc.gameRenderer).x1eqnsky$getLevelProjectionMatrixBuffer() == (Object)this : false;
   }

   @Inject(method = "getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;", at = @At("HEAD"))
   private void x1eqnsky$captureMatrix(Matrix4f matrix4f, CallbackInfoReturnable<GpuBufferSlice> callbackInfoReturnable) {
      if (matrix4f != null && this.x1eqnsky$isLevelBuffer()) {
         SkyShaderRenderer.captureProjection(matrix4f);
      }
   }

   @Inject(method = "getBuffer(Lnet/minecraft/client/renderer/Projection;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;", at = @At("HEAD"))
   private void x1eqnsky$captureProjection(Projection projection, CallbackInfoReturnable<GpuBufferSlice> callbackInfoReturnable) {
      if (projection != null && this.x1eqnsky$isLevelBuffer()) {
         SkyShaderRenderer.captureProjection(projection.getMatrix(x1eqnsky$tmp));
      }
   }
}
