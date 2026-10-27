package dev.x1eqn.skyshaders.util;

import dev.x1eqn.skyshaders.config.SkyConfig;
import dev.x1eqn.skyshaders.config.ThemePalette;

public final class ThemeColors {
    private ThemeColors() { }
    public static int getAccentRgb() {
        return parseHexRgb(ThemePalette.current(SkyConfig.current()).accent(), 0x7aa2f7);
    }
    public static int getAccent2Rgb() {
        return parseHexRgb(ThemePalette.current(SkyConfig.current()).accent2(), 0x3d59a1);
    }
    public static int resolveRgb(Object setting, int fallback, boolean secondary) {
        String value = setting == null ? "" : setting.toString().trim();
        if (value.equalsIgnoreCase("theme")) return getAccentRgb();
        if (value.equalsIgnoreCase("theme2")) return getAccent2Rgb();
        if (SkyConfig.current().useThemeColors) return secondary ? getAccent2Rgb() : getAccentRgb();
        return parseHexRgb(value, fallback);
    }
    public static int parseHexRgb(Object value, int fallback) {
        if (value == null) return fallback;
        String hex = value.toString().trim().replaceFirst("^#", "");
        if (hex.matches("(?i)[0-9a-f]{3}")) {
            hex = "" + hex.charAt(0) + hex.charAt(0) + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2);
        }
        if (!hex.matches("(?i)([0-9a-f]{6}|[0-9a-f]{8})")) return fallback;
        return (int) Long.parseLong(hex, 16) & 0xffffff;
    }
}
