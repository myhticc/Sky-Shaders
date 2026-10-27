package dev.x1eqn.skyshaders.config;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public record ThemePalette(String accent, String accent2, String background, String surface, String border) {
    public static final Map<String, ThemePalette> ALL = load();

    private static Map<String, ThemePalette> load() {
        var result = new LinkedHashMap<String, ThemePalette>();
        try (var stream = ThemePalette.class.getResourceAsStream("/assets/x1eqnsky/themes.json")) {
            if (stream == null) throw new IllegalStateException("Missing x1eqn themes");
            var root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            root.entrySet().forEach(entry -> {
                var a = entry.getValue().getAsJsonArray();
                result.put(entry.getKey(), new ThemePalette(a.get(0).getAsString(), a.get(1).getAsString(),
                    a.get(2).getAsString(), a.get(3).getAsString(), a.get(4).getAsString()));
            });
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("Cannot load x1eqn themes", ex);
        }
        return java.util.Collections.unmodifiableMap(result);
    }

    public static ThemePalette current(SkyConfig config) {
        if ("Custom".equals(config.theme)) {
            return new ThemePalette(config.accent, config.accent2, "#0b0e17", "#161b26", "#2a3142");
        }
        return ALL.getOrDefault(config.theme, ALL.get("TokyoNight"));
    }
}
