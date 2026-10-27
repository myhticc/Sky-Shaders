package dev.x1eqn.skyshaders.qa;

import java.nio.file.Files;
import java.nio.file.Path;
import dev.x1eqn.skyshaders.config.ModMenuIntegration;
import dev.x1eqn.skyshaders.config.SkyConfig;
import dev.x1eqn.skyshaders.config.SkyConfigScreen;
import dev.x1eqn.skyshaders.render.sky.SkyShaderRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.level.material.FogType;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

/** Isolated Fabric smoke-test mod; never packaged in the release JAR. */
public final class ClientSmoke implements ClientModInitializer {
    private int ticks;
    private SkyConfigScreen screen;
    private final StringBuilder report = new StringBuilder();
    @Override public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            try {
                GLFW.glfwHideWindow(client.getWindow().getWindow());
                var config = SkyConfig.current();
                var view = new Matrix4f();
                var projection = new Matrix4f().perspective((float) Math.toRadians(75), 4f/3f, .1f, 1000);
                for (String preset : SkyConfig.PRESETS) {
                    config.preset = preset;
                    for (String resolution : new String[]{"Full", "75%", "50%"}) {
                        config.resolution = resolution;
                        SkyShaderRenderer.beginFrame(view, projection, FogType.NONE);
                        int drawFbo = GL11.glGetInteger(0x8CA6);
                        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
                        if (!SkyShaderRenderer.tryRender(false)) throw new AssertionError("Renderer failed: " + preset + "/" + resolution);
                        if (drawFbo != GL11.glGetInteger(0x8CA6) || depth != GL11.glIsEnabled(GL11.GL_DEPTH_TEST))
                            throw new AssertionError("GL state was not restored");
                        if (GL11.glGetError() != GL11.GL_NO_ERROR) throw new AssertionError("OpenGL error: " + preset);
                        report.append("PASS: Minecraft framebuffer ").append(preset).append('/').append(resolution).append('\n');
                    }
                }
                config.enabled = false;
                if (SkyShaderRenderer.tryRender(false)) throw new AssertionError("Disabled sky still rendered");
                config.enabled = true;
                SkyShaderRenderer.beginFrame(view, projection, FogType.WATER);
                if (SkyShaderRenderer.tryRender(false)) throw new AssertionError("Underwater sky replacement");
                config.inTheEnd = false;
                SkyShaderRenderer.beginFrame(view, projection, FogType.NONE);
                if (SkyShaderRenderer.tryRender(true)) throw new AssertionError("End toggle ignored");
                config.inTheEnd = true;
                if (!SkyShaderRenderer.tryRender(true)) throw new AssertionError("End rendering failed");
                report.append("PASS: enable/disable, underwater fallback, End toggle\n");
                screen = (SkyConfigScreen) new ModMenuIntegration().getModConfigScreenFactory().create(null);
                client.setScreen(screen);
                report.append("PASS: Mod Menu factory and configuration screen init\n");
            } catch (Throwable error) { fail(client, error); }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (screen == null || client.getOverlay() != null) return;
            try {
                ticks++;
                if (ticks == 5 || ticks == 10 || ticks == 15) {
                    int page = ticks / 5;
                    ((Button) screen.children().get(page)).onPress();
                    report.append("PASS: configuration page ").append(page).append('\n');
                }
                if (ticks == 20) {
                    report.append("PASS: 20 client ticks with all four settings pages\n");
                    Files.writeString(Path.of("smoke-report.txt"), report.toString());
                    client.stop();
                }
            } catch (Throwable error) { fail(client, error); }
        });
    }
    private void fail(net.minecraft.client.Minecraft client, Throwable error) {
        error.printStackTrace();
        try { Files.writeString(Path.of("smoke-report.txt"), report + "FAIL: " + error); }
        catch (Exception ignored) { }
        client.stop();
    }
}
