package dev.x1eqn.skyshaders.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import dev.x1eqn.skyshaders.SkyShadersClient;
import net.fabricmc.loader.api.FabricLoader;

public final class SkyConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static SkyConfig current = new SkyConfig();
    public static final List<String> PRESETS = List.of("Aurora", "Galaxy", "Black Hole", "Thunderstorm", "Nebula",
        "Synthwave", "Blood Moon", "Dreamscape", "Prism");
    public boolean enabled = true;
    public String preset = "Aurora";
    public boolean naturalColors = true;
    public String color = "#2BFF88";
    public String secondColor = "#8A4DFF";
    public float speed = 1, brightness = 1, intensity = 1, scale = 1, stars = 1, rotation = 0.3f, horizonBlend = 0.5f;
    public boolean matchFog = true, sunMoon = false, hideClouds = true, inTheEnd = true;
    public String resolution = "Full";
    public boolean useThemeColors = true;
    public String theme = "TokyoNight";
    public String accent = "#7AA2F7", accent2 = "#3D59A1";

    public static SkyConfig current() { return current; }
    public SkyConfig copy() { return GSON.fromJson(GSON.toJson(this), SkyConfig.class); }
    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("sky-shaders.json"); }

    public static void load() {
        Path path = path();
        if (!Files.exists(path)) return;
        try {
            var loaded = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), SkyConfig.class);
            if (loaded == null) throw new IllegalArgumentException("Empty configuration");
            loaded.sanitize();
            current = loaded;
        } catch (Exception ex) {
            SkyShadersClient.LOGGER.error("Cannot read sky configuration; using defaults", ex);
            try { Files.copy(path, path.resolveSibling(path.getFileName() + ".broken-" + System.currentTimeMillis())); }
            catch (java.io.IOException backupError) { SkyShadersClient.LOGGER.warn("Cannot back up sky configuration", backupError); }
        }
    }

    public static boolean save(SkyConfig draft) {
        draft.sanitize();
        Path file = path();
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(temporary, GSON.toJson(draft), StandardCharsets.UTF_8);
            try { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (java.nio.file.AtomicMoveNotSupportedException ex) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
            current = draft.copy();
            return true;
        } catch (java.io.IOException ex) {
            SkyShadersClient.LOGGER.error("Cannot save sky configuration", ex);
            return false;
        }
    }

    public void sanitize() {
        if (!PRESETS.contains(preset)) preset = "Aurora";
        if (!List.of("Full", "75%", "50%").contains(resolution == null ? "" : resolution)) resolution = "Full";
        if (!"Custom".equals(theme) && !ThemePalette.ALL.containsKey(theme)) theme = "TokyoNight";
        if (!validColor(color, true)) color = "#2BFF88";
        if (!validColor(secondColor, true)) secondColor = "#8A4DFF";
        if (!validColor(accent, false)) accent = "#7AA2F7";
        if (!validColor(accent2, false)) accent2 = "#3D59A1";
        speed = clamp(speed, 0, 5, 1); brightness = clamp(brightness, 0, 3, 1);
        intensity = clamp(intensity, 0, 3, 1); scale = clamp(scale, .25f, 3, 1);
        stars = clamp(stars, 0, 3, 1); rotation = clamp(rotation, -5, 5, .3f);
        horizonBlend = clamp(horizonBlend, 0, 1, .5f);
    }

    private static float clamp(float value, float min, float max, float fallback) {
        return Float.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
    }

    public static boolean validColor(String text, boolean allowTheme) {
        return text != null && (text.trim().matches("(?i)#?(?:[0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})")
            || allowTheme && text.trim().matches("(?i)theme2?"));
    }

    @SuppressWarnings("unchecked")
    public <T> T getSetting(String key, T fallback) {
        Object value = switch (key) {
            case "Preset" -> preset; case "Natural Colors" -> naturalColors;
            case "Color" -> color; case "Second Color" -> secondColor;
            case "Speed" -> speed; case "Brightness" -> brightness; case "Intensity" -> intensity;
            case "Scale" -> scale; case "Stars" -> stars; case "Sky Rotation" -> rotation;
            case "Horizon Blend" -> horizonBlend; case "Match Fog" -> matchFog;
            case "Sun & Moon" -> sunMoon; case "Hide Clouds" -> hideClouds;
            case "In The End" -> inTheEnd; case "Resolution" -> resolution;
            default -> fallback;
        };
        return (T) value;
    }
}
