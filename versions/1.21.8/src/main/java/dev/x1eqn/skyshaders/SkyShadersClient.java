package dev.x1eqn.skyshaders;

import dev.x1eqn.skyshaders.config.SkyConfig;
import dev.x1eqn.skyshaders.modules.visual.SkyShaders;
import dev.x1eqn.skyshaders.render.RawFramebufferUtil;
import dev.x1eqn.skyshaders.render.sky.SkyShaderRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SkyShadersClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("x1eqn_sky_shaders");

    @Override
    public void onInitializeClient() {
        SkyConfig.load();
        new SkyShaders();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            SkyShaderRenderer.dispose();
            RawFramebufferUtil.dispose();
        });
    }
}
