package dev.x1eqn.skyshaders.render.sky;

import java.nio.ByteBuffer;
import java.util.Arrays;
import dev.x1eqn.skyshaders.config.SkyConfig;
import dev.x1eqn.skyshaders.config.ThemePalette;
import dev.x1eqn.skyshaders.modules.visual.SkyShaders;
import dev.x1eqn.skyshaders.util.ThemeColors;
import org.joml.Matrix4f;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

/** Offline regression checks and actual GPU compilation of every shipped shader. */
public final class Validation {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        validateMixinTargets();
        var config = new SkyConfig();
        config.preset = "invalid"; config.theme = null; config.resolution = null;
        config.scale = Float.NaN; config.speed = Float.POSITIVE_INFINITY;
        config.rotation = -100; config.brightness = 100; config.color = "invalid";
        config.sanitize();
        check(config.preset.equals("Aurora") && config.theme.equals("TokyoNight"), "Preset/theme recovery");
        check(config.scale == 1 && config.speed == 1 && config.rotation == -5 && config.brightness == 3, "Numeric bounds");
        check(config.resolution.equals("Full") && config.color.equals("#2BFF88"), "Invalid input recovery");
        check(ThemePalette.ALL.size() == 31, "All 31 themes are present");
        for (var palette : ThemePalette.ALL.values()) {
            for (var color : new String[]{palette.accent(), palette.accent2(), palette.background(), palette.surface(), palette.border()}) {
                check(SkyConfig.validColor(color, false), "Palette color: " + color);
            }
        }
        check(ThemeColors.parseHexRgb("#888", 0) == 0x888888, "Original short hex themes");
        check(ThemeColors.parseHexRgb("#ff123456", 0) == 0x123456, "ARGB support");
        check(SkyConfig.validColor("theme2", true) && !SkyConfig.validColor("theme2", false), "Theme references");
        new SkyShaders();
        for (String preset : SkyConfig.PRESETS) {
            SkyConfig.current().preset = preset;
            float[][] colors = SkyShaders.colors();
            for (float value : SkyShaders.horizon(preset, colors[0], colors[1])) check(Float.isFinite(value), "Finite horizon");
            String resource = "/assets/x1eqnsky/shaders/sky/" + SkyShaders.getString(preset) + ".fsh";
            check(Validation.class.getResource(resource) != null, "Resource for " + preset);
        }
        SkyConfig.current().preset = "Prism";
        float[][] before = SkyShaders.getFloatArray();
        SkyConfig.current().theme = "Custom";
        SkyConfig.current().accent = "#ffffff";
        SkyConfig.current().accent2 = "#ffffff";
        Thread.sleep(30);
        float[][] smooth = SkyShaders.getFloatArray();
        check(smooth[0][0] > before[0][0], "Prism approaches new theme rather than diverging");
        check(Arrays.stream(new double[]{smooth[0][0], smooth[0][1], smooth[0][2]}).allMatch(v -> v >= 0 && v <= 1), "Stable Prism interpolation");
        SkyConfig.current().naturalColors = false;
        SkyConfig.current().useThemeColors = false;
        SkyConfig.current().color = "#123456";
        check(Math.abs(SkyShaders.colors()[0][0] - 0x12 / 255f) < .0001, "Manual color reaches renderer");
        SkyConfig.current().useThemeColors = true;
        check(SkyShaders.colors()[0][0] == 1, "Theme color reaches renderer");
        System.out.println("PASS: config, all 31 palettes, 9 presets, manual/theme colors, Prism interpolation");

        check(SDLInit.SDL_Init(SDLInit.SDL_INIT_VIDEO), "SDL initialization");
        SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_MAJOR_VERSION, 3);
        SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_MINOR_VERSION, 3);
        SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_PROFILE_MASK, SDLVideo.SDL_GL_CONTEXT_PROFILE_CORE);
        long window = SDLVideo.SDL_CreateWindow("Shader validation", 64, 64, SDLVideo.SDL_WINDOW_OPENGL | SDLVideo.SDL_WINDOW_HIDDEN);
        check(window != 0, "Hidden OpenGL window");
        long context = SDLVideo.SDL_GL_CreateContext(window);
        check(context != 0, "OpenGL context");
        try {
            check(SDLVideo.SDL_GL_MakeCurrent(window, context), "Current OpenGL context");
            GL.createCapabilities();
            System.out.println("GPU: " + GL11.glGetString(GL11.GL_RENDERER));
            int vao = GL30.glGenVertexArrays(); GL30.glBindVertexArray(vao);
            int vbo = GL15.glGenBuffers(); GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, new float[]{-1,-1,0,3,-1,0,-1,3,0}, GL15.GL_STATIC_DRAW);
            GL20.glEnableVertexAttribArray(0); GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 12, 0L);
            int noise = GL11.glGenTextures(); GL11.glBindTexture(GL11.GL_TEXTURE_2D, noise);
            ByteBuffer pixels = MemoryUtil.memAlloc(256 * 256 * 4);
            var random = new java.util.Random(1234);
            for (int i = 0; i < pixels.capacity(); i++) pixels.put((byte) random.nextInt(256));
            pixels.flip();
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, 256, 256, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            MemoryUtil.memFree(pixels);
            ByteBuffer sample = MemoryUtil.memAlloc(64 * 64 * 4);
            for (String preset : SkyConfig.PRESETS) {
                SkyProgram program = SkyProgram.preset(SkyShaders.getString(preset));
                GL20.glUseProgram(program.value);
                var inv = new Matrix4f().perspective((float) Math.toRadians(75), 1, .1f, 1000).invert();
                program.setMat4("uInvViewProj", inv.get(new float[16]));
                program.setMat3("uCelestial", new org.joml.Matrix3f().get(new float[9]));
                program.set1i("uNoise", 0); program.set1f("uTime", 5);
                program.set3f("uColor1", .05f, .8f, .4f); program.set3f("uColor2", .3f, .1f, .8f);
                program.set1f("uIntensity", 1); program.set1f("uScale", 1);
                program.set1f("uStars", 1); program.set1f("uBrightness", 1);
                program.set3f("uHorizon", .1f, .1f, .2f); program.set1f("uHorizonFog", .5f);
                program.set1f("uDirSign", 1); program.set1f("uPixel", .02f);
                GL11.glViewport(0, 0, 64, 64); GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
                GL11.glReadPixels(0, 0, 64, 64, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, sample);
                check(GL11.glGetError() == GL11.GL_NO_ERROR, "OpenGL error: " + preset);
                int lit = 0;
                for (int i = 0; i < sample.capacity(); i += 4) lit += (sample.get(i) & 255) + (sample.get(i+1) & 255) + (sample.get(i+2) & 255);
                check(lit > 0, "Nonempty rendered sky: " + preset);
                program.delete();
                System.out.println("PASS: compile, link, draw, readback " + preset);
            }
            SkyProgram.upscale().delete();
            System.out.println("PASS: original upscale shader");
            MemoryUtil.memFree(sample);
            GL20.glUseProgram(0); GL15.glDeleteBuffers(vbo); GL30.glDeleteVertexArrays(vao); GL11.glDeleteTextures(noise);
        } finally {
            SDLVideo.SDL_GL_DestroyContext(context);
            SDLVideo.SDL_DestroyWindow(window);
            SDLInit.SDL_Quit();
        }
    }

    private static void validateMixinTargets() throws Exception {
        String[] mixins = {"GameRendererSkyAccessor", "SkyShadersProjectionMixin", "SkyShadersLevelRendererMixin",
            "SkyShadersSkyRendererMixin", "SkyShadersCloudMixin", "SkyShadersFogMixin"};
        int injections = 0;
        for (String name : mixins) {
            var mixin = readClass("dev/x1eqn/skyshaders/mixin/" + name);
            var annotations = new java.util.ArrayList<org.objectweb.asm.tree.AnnotationNode>();
            if (mixin.visibleAnnotations != null) annotations.addAll(mixin.visibleAnnotations);
            if (mixin.invisibleAnnotations != null) annotations.addAll(mixin.invisibleAnnotations);
            org.objectweb.asm.tree.ClassNode target = null;
            for (var annotation : annotations) {
                if (annotation.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")) {
                    for (int i = 0; i < annotation.values.size(); i += 2) {
                        if (annotation.values.get(i).equals("value")) {
                            var targets = (java.util.List<?>) annotation.values.get(i + 1);
                            target = readClass(((org.objectweb.asm.Type) targets.getFirst()).getInternalName());
                        }
                    }
                }
            }
            check(target != null, "Mixin target: " + name);
            for (var method : mixin.methods) {
                var methodAnnotations = new java.util.ArrayList<org.objectweb.asm.tree.AnnotationNode>();
                if (method.visibleAnnotations != null) methodAnnotations.addAll(method.visibleAnnotations);
                if (method.invisibleAnnotations != null) methodAnnotations.addAll(method.invisibleAnnotations);
                for (var annotation : methodAnnotations) {
                    if (annotation.desc.endsWith("/Inject;")) {
                        for (int i = 0; i < annotation.values.size(); i += 2) {
                            if (annotation.values.get(i).equals("method")) {
                                for (var signature : (java.util.List<?>) annotation.values.get(i + 1)) {
                                    boolean found = false;
                                    for (var candidate : target.methods) {
                                        if ((candidate.name + candidate.desc).equals(signature)) {
                                            found = true;
                                            var actual = org.objectweb.asm.Type.getArgumentTypes(candidate.desc);
                                            var callback = org.objectweb.asm.Type.getArgumentTypes(method.desc);
                                            if (callback.length > 1) {
                                                check(callback.length == actual.length + 1, "Callback arity: " + name);
                                                for (int p = 0; p < actual.length; p++) check(actual[p].equals(callback[p]), "Callback type: " + name);
                                            }
                                            injections++;
                                        }
                                    }
                                    check(found, "Minecraft 26.3 injection target: " + signature);
                                }
                            }
                        }
                    } else if (annotation.desc.endsWith("/Accessor;")) {
                        String field = (String) annotation.values.get(1);
                        boolean found = false;
                        for (var candidate : target.fields) if (candidate.name.equals(field)) found = true;
                        check(found, "Minecraft projection accessor");
                    }
                }
            }
        }
        check(injections == 14, "All 14 renderer injection points");
        System.out.println("PASS: 6 mixins, projection accessor, all 14 Minecraft 26.3 injection signatures");
    }

    private static org.objectweb.asm.tree.ClassNode readClass(String internalName) throws Exception {
        try (var input = Validation.class.getResourceAsStream("/" + internalName + ".class")) {
            check(input != null, "Class: " + internalName);
            var node = new org.objectweb.asm.tree.ClassNode();
            new org.objectweb.asm.ClassReader(input).accept(node, org.objectweb.asm.ClassReader.SKIP_CODE);
            return node;
        }
    }
}
