package dev.x1eqn.skyshaders.mixin;

import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import dev.x1eqn.skyshaders.modules.visual.SkyShaders;
import dev.x1eqn.skyshaders.render.sky.SkyShaderRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class SkyShadersSkyRendererMixin {
    @Shadow private ClientLevel level;
    @Shadow protected abstract boolean doesMobEffectBlockSky(Camera camera);

    @Inject(method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
    private void x1eqnsky$sky(PoseStack view, Matrix4f projection, float tick, Camera camera,
            boolean foggy, Runnable fogSetup, CallbackInfo ci) {
        if (foggy || level == null || doesMobEffectBlockSky(camera)) return;
        var type = level.effects().skyType();
        boolean end = type == DimensionSpecialEffects.SkyType.END;
        if (!end && type != DimensionSpecialEffects.SkyType.NORMAL) return;
        if (SkyShaderRenderer.tryRender(end) && (end || !SkyShaders.keepSunMoon())) ci.cancel();
    }

    // In 1.21/1.21.1 the disc, stars and dark disc share this draw call.
    @Redirect(method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;drawWithShader(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/ShaderInstance;)V"), require = 3)
    private void x1eqnsky$skipVanillaDiscsAndStars(VertexBuffer buffer, Matrix4f view, Matrix4f projection, ShaderInstance shader) {
        if (!SkyShaderRenderer.isActiveThisFrame()) buffer.drawWithShader(view, projection, shader);
    }

    // The first immediate mesh is sunrise; leave the sun and moon meshes intact.
    @Redirect(method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/BufferBuilder$RenderedBuffer;)V", ordinal = 0))
    private void x1eqnsky$skipVanillaSunrise(BufferBuilder.RenderedBuffer mesh) {
        if (SkyShaderRenderer.isActiveThisFrame()) mesh.release();
        else BufferUploader.drawWithShader(mesh);
    }
}
