package dev.x1eqn.skyshaders.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import dev.x1eqn.skyshaders.modules.visual.SkyShaders;
import dev.x1eqn.skyshaders.render.sky.SkyShaderRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class SkyShadersLevelRendererMixin {
   @Inject(
      method = "renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;ZLnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;)V",
      at = @At("HEAD")
   )
   private void x1eqnsky$skyBeginFrame(
      GraphicsResourceAllocator graphicsResourceAllocator,
      DeltaTracker deltaTracker,
      boolean flag,
      CameraRenderState cameraRenderState,
      Matrix4fc matrix4fc,
      GpuBufferSlice gpuBufferSlice,
      Vector4f vector4f,
      boolean flag2,
      ChunkSectionsToRender chunkSectionsToRender,
      CallbackInfo callbackInfo
   ) {
      SkyShaderRenderer.beginFrame(matrix4fc, cameraRenderState);
   }

   @Inject(
      method = "addCloudsPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/CloudStatus;Lnet/minecraft/world/phys/Vec3;JFIFI)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void x1eqnsky$hideClouds(CallbackInfo callbackInfo) {
      if (SkyShaders.hideClouds()) {
         callbackInfo.cancel();
      }
   }
}
