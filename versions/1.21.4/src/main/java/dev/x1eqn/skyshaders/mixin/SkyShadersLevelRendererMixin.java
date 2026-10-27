package dev.x1eqn.skyshaders.mixin;

import net.minecraft.client.renderer.GameRenderer;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import dev.x1eqn.skyshaders.modules.visual.SkyShaders;
import dev.x1eqn.skyshaders.render.sky.SkyShaderRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class SkyShadersLevelRendererMixin {
    @Inject(method = "renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V", at = @At("HEAD"))
    private void x1eqnsky$skyBeginFrame(GraphicsResourceAllocator allocator, DeltaTracker delta,
            boolean outline, Camera camera, GameRenderer gameRenderer, Matrix4f viewRotation, Matrix4f projection,
            CallbackInfo ci) {
        SkyShaderRenderer.beginFrame(viewRotation, projection, camera.getFluidInCamera());
    }

    @Inject(method = "addCloudsPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/CloudStatus;Lnet/minecraft/world/phys/Vec3;FIF)V", at = @At("HEAD"), cancellable = true)
    private void x1eqnsky$hideClouds(CallbackInfo ci) {
        if (SkyShaders.hideClouds()) ci.cancel();
    }
}
