package dev.x1eqn.skyshaders.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;

/** Minecraft 1.21.4 exposes its existing OpenGL framebuffer directly. */
public final class RawFramebufferUtil {
    private RawFramebufferUtil() { }

    public static boolean bindDirect(RenderTarget target) {
        RenderSystem.assertOnRenderThread();
        if (target == null || target.width <= 0 || target.height <= 0 || target.frameBufferId < 0) return false;
        target.bindWrite(true);
        return true;
    }

    // No additional framebuffer is allocated on this Minecraft version.
    public static void dispose() { }
}
