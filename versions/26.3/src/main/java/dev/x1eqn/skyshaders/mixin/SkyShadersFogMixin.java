package dev.x1eqn.skyshaders.mixin;


import dev.x1eqn.skyshaders.modules.visual.SkyShaders;
import dev.x1eqn.skyshaders.render.sky.SkyShaderRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class SkyShadersFogMixin {
   @Inject(
      method = "setupFog(Lnet/minecraft/client/Camera;ILnet/minecraft/client/DeltaTracker;FLnet/minecraft/client/multiplayer/ClientLevel;)Lnet/minecraft/client/renderer/fog/FogData;",
      at = @At("RETURN")
   )
   private void x1eqnsky$skyFog(Camera camera, int value, DeltaTracker deltaTracker, float factor, ClientLevel clientLevel, CallbackInfoReturnable<FogData> callbackInfoReturnable) {
      FogData returnValue = (FogData)callbackInfoReturnable.getReturnValue();
      if (returnValue != null && returnValue.color != null) {
         FogType fluid = camera.getFluidInCamera();
         if (SkyShaders.matchFog() && SkyShaderRenderer.wasActiveLastFrame()
               && (fluid == FogType.NONE || fluid == FogType.ATMOSPHERIC)) {
               String preset2 = SkyShaders.preset();
               float[][] colors2 = SkyShaders.colors();
               float[] horizon2 = SkyShaders.horizon(preset2, colors2[0], colors2[1]);
               returnValue.color.set(horizon2[0], horizon2[1], horizon2[2], returnValue.color.w);
         }
      }
   }
}
