package dev.x1eqn.skyshaders.mixin;

import dev.x1eqn.skyshaders.modules.visual.SkyShaders;
import dev.x1eqn.skyshaders.render.sky.SkyShaderRenderer;
import net.minecraft.client.renderer.SkyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class SkyShadersSkyRendererMixin {
    @Inject(method = "renderSkyDisc(FFF)V", at = @At("HEAD"), cancellable = true)
    private void x1eqnsky$skyDisc(CallbackInfo ci) {
        if (SkyShaderRenderer.tryRender(false)) ci.cancel();
    }

    @Inject(method = "renderEndSky()V", at = @At("HEAD"), cancellable = true)
    private void x1eqnsky$endSky(CallbackInfo ci) {
        if (SkyShaderRenderer.tryRender(true)) ci.cancel();
    }

    @Inject(method = "renderDarkDisc(Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("HEAD"), cancellable = true)
    private void x1eqnsky$darkDisc(CallbackInfo ci) {
        if (SkyShaderRenderer.isActiveThisFrame()) ci.cancel();
    }

    @Inject(method = "renderSunriseAndSunset(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;FI)V", at = @At("HEAD"), cancellable = true)
    private void x1eqnsky$sunrise(CallbackInfo ci) {
        if (SkyShaderRenderer.isActiveThisFrame()) ci.cancel();
    }

    @Inject(method = "renderSunMoonAndStars(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;FIFFLnet/minecraft/client/renderer/FogParameters;)V", at = @At("HEAD"), cancellable = true)
    private void x1eqnsky$sunMoonStars(CallbackInfo ci) {
        if (SkyShaderRenderer.isActiveThisFrame() && !SkyShaders.keepSunMoon()) ci.cancel();
    }

    @Inject(method = "renderStars(Lnet/minecraft/client/renderer/FogParameters;FLcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("HEAD"), cancellable = true)
    private void x1eqnsky$stars(CallbackInfo ci) {
        if (SkyShaderRenderer.isActiveThisFrame()) ci.cancel();
    }
}
