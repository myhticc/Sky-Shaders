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
   // Draw before Minecraft opens its render pass; retain vanilla celestial bodies below.
   @Inject(method = "render(Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Lnet/minecraft/client/renderer/state/level/SkyRenderState;)V", at = @At("HEAD"))
   private void x1eqnsky$drawSky(com.mojang.renderpearl.api.buffers.GpuBufferSlice fog,
      net.minecraft.client.renderer.state.level.SkyRenderState state, CallbackInfo ci) {
      SkyShaderRenderer.tryRender(state.skybox == net.minecraft.world.level.dimension.DimensionType.Skybox.END);
   }
   @Inject(method = "renderSkyDisc(Lcom/mojang/renderpearl/api/commands/RenderPass;Lorg/joml/Vector3fc;)V", at = @At("HEAD"), cancellable = true)
   private void x1eqnsky$skyDisc(CallbackInfo callbackInfo) {
      if (SkyShaderRenderer.isActiveThisFrame()) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "renderEndSky(Lcom/mojang/renderpearl/api/commands/RenderPass;)V", at = @At("HEAD"), cancellable = true)
   private void x1eqnsky$endSky(CallbackInfo callbackInfo) {
      if (SkyShaderRenderer.isActiveThisFrame()) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "renderDarkDisc(Lcom/mojang/renderpearl/api/commands/RenderPass;)V", at = @At("HEAD"), cancellable = true)
   private void x1eqnsky$darkDisc(CallbackInfo callbackInfo) {
      if (SkyShaderRenderer.isActiveThisFrame()) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "renderSunriseAndSunset(Lcom/mojang/renderpearl/api/commands/RenderPass;Lcom/mojang/blaze3d/vertex/PoseStack;FLorg/joml/Vector4fc;)V", at = @At("HEAD"), cancellable = true)
   private void x1eqnsky$sunrise(CallbackInfo callbackInfo) {
      if (SkyShaderRenderer.isActiveThisFrame()) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = "renderSunMoonAndStars(Lcom/mojang/renderpearl/api/commands/RenderPass;Lcom/mojang/blaze3d/vertex/PoseStack;FFFLnet/minecraft/world/level/MoonPhase;FF)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void x1eqnsky$sunMoonStars(CallbackInfo callbackInfo) {
      if (SkyShaderRenderer.isActiveThisFrame() && !SkyShaders.keepSunMoon()) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "renderStars(Lcom/mojang/renderpearl/api/commands/RenderPass;FLcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("HEAD"), cancellable = true)
   private void x1eqnsky$stars(CallbackInfo callbackInfo) {
      if (SkyShaderRenderer.isActiveThisFrame()) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "renderEndFlash(Lcom/mojang/renderpearl/api/commands/RenderPass;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V", at = @At("HEAD"), cancellable = true)
   private void x1eqnsky$endFlash(CallbackInfo callbackInfo) {
      if (SkyShaderRenderer.isActiveThisFrame()) {
         callbackInfo.cancel();
      }
   }
}
