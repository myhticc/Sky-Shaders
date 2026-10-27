package dev.x1eqn.skyshaders.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.x1eqn.skyshaders.modules.visual.SkyShaders;
import dev.x1eqn.skyshaders.render.sky.SkyShaderRenderer;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class SkyShadersSkyRendererMixin {
   @Inject(method = "renderSkyDisc(I)V", at = @At("HEAD"), cancellable = true)
   private void x1eqnsky$skyDisc(int value, CallbackInfo callbackInfo) {
      if (SkyShaderRenderer.tryRender(false)) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "renderEndSky()V", at = @At("HEAD"), cancellable = true)
   private void x1eqnsky$endSky(CallbackInfo callbackInfo) {
      if (SkyShaderRenderer.tryRender(true)) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "renderDarkDisc()V", at = @At("HEAD"), cancellable = true)
   private void x1eqnsky$darkDisc(CallbackInfo callbackInfo) {
      if (SkyShaderRenderer.isActiveThisFrame()) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "renderSunriseAndSunset(Lcom/mojang/blaze3d/vertex/PoseStack;FI)V", at = @At("HEAD"), cancellable = true)
   private void x1eqnsky$sunrise(PoseStack poseStack, float factor, int value, CallbackInfo callbackInfo) {
      if (SkyShaderRenderer.isActiveThisFrame()) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = "renderSunMoonAndStars(Lcom/mojang/blaze3d/vertex/PoseStack;FFFLnet/minecraft/world/level/MoonPhase;FF)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void x1eqnsky$sunMoonStars(PoseStack poseStack, float factor, float factor2, float factor3, MoonPhase moonPhase, float factor4, float factor5, CallbackInfo callbackInfo) {
      if (SkyShaderRenderer.isActiveThisFrame() && !SkyShaders.keepSunMoon()) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "renderStars(FLcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("HEAD"), cancellable = true)
   private void x1eqnsky$stars(float factor, PoseStack poseStack, CallbackInfo callbackInfo) {
      if (SkyShaderRenderer.isActiveThisFrame()) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "renderEndFlash(Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V", at = @At("HEAD"), cancellable = true)
   private void x1eqnsky$endFlash(PoseStack poseStack, float factor, float factor2, float factor3, CallbackInfo callbackInfo) {
      if (SkyShaderRenderer.isActiveThisFrame()) {
         callbackInfo.cancel();
      }
   }
}
