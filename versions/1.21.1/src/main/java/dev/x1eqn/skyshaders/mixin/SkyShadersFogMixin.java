package dev.x1eqn.skyshaders.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.x1eqn.skyshaders.modules.visual.SkyShaders;
import dev.x1eqn.skyshaders.render.sky.SkyShaderRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class SkyShadersFogMixin {
    @Shadow private static float fogRed;
    @Shadow private static float fogGreen;
    @Shadow private static float fogBlue;

    @Inject(method = "setupColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IF)V", at = @At("RETURN"))
    private static void x1eqnsky$fog(Camera camera, float tick, ClientLevel level, int distance, float darken, CallbackInfo ci) {
        if (SkyShaders.matchFog() && SkyShaderRenderer.wasActiveLastFrame() && camera.getFluidInCamera() == FogType.NONE) {
            var colors = SkyShaders.colors();
            var horizon = SkyShaders.horizon(SkyShaders.preset(), colors[0], colors[1]);
            fogRed = horizon[0]; fogGreen = horizon[1]; fogBlue = horizon[2];
            RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0);
            RenderSystem.setShaderFogColor(fogRed, fogGreen, fogBlue);
        }
    }
}
