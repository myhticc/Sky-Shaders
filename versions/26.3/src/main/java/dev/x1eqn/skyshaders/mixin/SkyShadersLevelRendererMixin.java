package dev.x1eqn.skyshaders.mixin;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import dev.x1eqn.skyshaders.render.sky.SkyShaderRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class SkyShadersLevelRendererMixin {
   @Inject(
      method = "render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Lorg/joml/Vector4f;ZZ)V",
      at = @At("HEAD")
   )
   private void x1eqnsky$skyBeginFrame(
      GraphicsResourceAllocator graphicsResourceAllocator,
      boolean flag,
      CameraRenderState cameraRenderState,
      GpuBufferSlice gpuBufferSlice,
      Vector4f vector4f,
      boolean flag2,
      boolean flag3,
      CallbackInfo callbackInfo
   ) {
      SkyShaderRenderer.beginFrame(cameraRenderState.viewRotationMatrix, cameraRenderState);
   }

}
