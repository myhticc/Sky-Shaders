package dev.x1eqn.skyshaders.mixin;

import dev.x1eqn.skyshaders.modules.visual.SkyShaders;
import dev.x1eqn.skyshaders.render.sky.SkyShaderRenderer;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class SkyShadersLevelRendererMixin {
    @Inject(method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V", at = @At("HEAD"))
    private void x1eqnsky$beginFrame(PoseStack view, float tick, long limit, boolean outline, Camera camera, GameRenderer renderer,
            LightTexture light, Matrix4f projection, CallbackInfo ci) {
        SkyShaderRenderer.beginFrame(view.last().pose(), projection, camera.getFluidInCamera());
    }

    @Inject(method = "renderClouds(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FDDD)V", at = @At("HEAD"), cancellable = true)
    private void x1eqnsky$hideClouds(CallbackInfo ci) {
        if (SkyShaders.hideClouds()) ci.cancel();
    }
}
