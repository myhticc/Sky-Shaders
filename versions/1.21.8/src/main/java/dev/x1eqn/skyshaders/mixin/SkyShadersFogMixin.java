package dev.x1eqn.skyshaders.mixin;

import dev.x1eqn.skyshaders.modules.visual.SkyShaders;
import dev.x1eqn.skyshaders.render.sky.SkyShaderRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class SkyShadersFogMixin {
    // Adjust before setupFog writes the color into the GPU uniform buffers.
    @Inject(method = "computeFogColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IFZ)Lorg/joml/Vector4f;", at = @At("RETURN"))
    private void x1eqnsky$skyFog(Camera camera, float partialTick, ClientLevel level, int distance,
            float darken, boolean thickFog, CallbackInfoReturnable<Vector4f> cir) {
        Vector4f color = cir.getReturnValue();
        FogType fluid = camera.getFluidInCamera();
        if (color != null && SkyShaders.matchFog() && SkyShaderRenderer.wasActiveLastFrame()
                && (fluid == FogType.NONE || fluid == FogType.ATMOSPHERIC)) {
            float[][] colors = SkyShaders.colors();
            float[] horizon = SkyShaders.horizon(SkyShaders.preset(), colors[0], colors[1]);
            color.set(horizon[0], horizon[1], horizon[2], color.w);
        }
    }
}
