package dev.x1eqn.skyshaders.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import dev.x1eqn.skyshaders.util.ThemeColors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SkyConfigScreen extends Screen {
    private final Screen parent;
    private SkyConfig draft = SkyConfig.current().copy();
    private int page, x, y, controlWidth, spacing;
    private Button save;
    private final List<Label> labels = new ArrayList<>();
    private record Label(Component text, int y) { }

    public SkyConfigScreen(Screen parent) {
        super(Component.literal("Sky Shaders"));
        this.parent = parent;
    }

    private static Component tr(String key) { return Component.translatable("x1eqnsky." + key); }

    @Override
    protected void init() {
        labels.clear();
        controlWidth = Math.min(420, width - 24);
        x = (width - controlWidth) / 2;
        spacing = Math.min(26, Math.max(21, (height - 92) / 7));
        y = 57;
        String[] tabs = {"appearance", "motion", "world", "themes"};
        int tabWidth = controlWidth / tabs.length;
        for (int i = 0; i < tabs.length; i++) {
            int selected = i;
            var tab = addRenderableWidget(Button.builder(tr(tabs[i]), b -> {
                page = selected;
                rebuildWidgets();
            }).bounds(x + i * tabWidth, 29, tabWidth - 2, 20).build());
            tab.active = page != i;
        }

        switch (page) {
            case 0 -> {
                toggle("enabled", draft.enabled, v -> draft.enabled = v);
                cycle("preset", draft.preset, SkyConfig.PRESETS, v -> draft.preset = v);
                toggle("naturalColors", draft.naturalColors, v -> draft.naturalColors = v);
                color("color", draft.color, true, v -> draft.color = v);
                color("secondColor", draft.secondColor, true, v -> draft.secondColor = v);
                toggle("useThemeColors", draft.useThemeColors, v -> draft.useThemeColors = v);
            }
            case 1 -> {
                slider("speed", draft.speed, 0, 5, v -> draft.speed = (float) v);
                slider("brightness", draft.brightness, 0, 3, v -> draft.brightness = (float) v);
                slider("intensity", draft.intensity, 0, 3, v -> draft.intensity = (float) v);
                slider("scale", draft.scale, .25, 3, v -> draft.scale = (float) v);
                slider("stars", draft.stars, 0, 3, v -> draft.stars = (float) v);
                slider("rotation", draft.rotation, -5, 5, v -> draft.rotation = (float) v);
                slider("horizonBlend", draft.horizonBlend, 0, 1, v -> draft.horizonBlend = (float) v);
            }
            case 2 -> {
                toggle("matchFog", draft.matchFog, v -> draft.matchFog = v);
                toggle("sunMoon", draft.sunMoon, v -> draft.sunMoon = v);
                toggle("hideClouds", draft.hideClouds, v -> draft.hideClouds = v);
                toggle("inTheEnd", draft.inTheEnd, v -> draft.inTheEnd = v);
                cycle("resolution", draft.resolution, List.of("Full", "75%", "50%"), v -> draft.resolution = v);
            }
            case 3 -> {
                var names = new ArrayList<>(ThemePalette.ALL.keySet());
                names.add("Custom");
                cycle("theme", draft.theme, names, v -> draft.theme = v);
                color("accent", draft.accent, false, v -> draft.accent = v);
                color("accent2", draft.accent2, false, v -> draft.accent2 = v);
            }
        }
        int buttonWidth = (controlWidth - 8) / 3;
        addRenderableWidget(Button.builder(tr("reset"), b -> {
            draft = new SkyConfig();
            rebuildWidgets();
        }).bounds(x, height - 26, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(tr("cancel"), b -> onClose())
            .bounds(x + buttonWidth + 4, height - 26, buttonWidth, 20).build());
        save = addRenderableWidget(Button.builder(tr("save"), b -> {
            if (SkyConfig.save(draft)) onClose();
            else {
                b.setMessage(tr("saveFailed"));
                b.setTooltip(Tooltip.create(tr("saveFailed.tip")));
            }
        }).bounds(x + 2 * (buttonWidth + 4), height - 26, buttonWidth, 20).build());
        validate();
    }

    private void validate() {
        if (save == null) return;
        save.active = SkyConfig.validColor(draft.color, true) && SkyConfig.validColor(draft.secondColor, true)
            && SkyConfig.validColor(draft.accent, false) && SkyConfig.validColor(draft.accent2, false);
        save.setTooltip(save.active ? null : Tooltip.create(tr("invalidColor")));
    }

    private void toggle(String name, boolean value, Consumer<Boolean> setter) {
        var widget = CycleButton.onOffBuilder(value).create(x, y, controlWidth, 20, tr(name), (b, v) -> setter.accept(v));
        widget.setTooltip(Tooltip.create(tr(name + ".tip")));
        addRenderableWidget(widget);
        y += spacing;
    }

    private void cycle(String name, String value, List<String> values, Consumer<String> setter) {
        var widget = CycleButton.<String>builder(Component::literal).withValues(values).withInitialValue(value)
            .create(x, y, controlWidth, 20, tr(name), (b, v) -> setter.accept(v));
        widget.setTooltip(Tooltip.create(tr(name + ".tip")));
        addRenderableWidget(widget);
        y += spacing;
    }

    private void color(String name, String value, boolean allowTheme, Consumer<String> setter) {
        labels.add(new Label(tr(name), y + 6));
        int labelWidth = Math.min(controlWidth / 2, 170);
        var input = new EditBox(font, x + labelWidth, y, controlWidth - labelWidth, 20, tr(name));
        input.setMaxLength(16);
        input.setValue(value);
        input.setTooltip(Tooltip.create(tr(name + ".tip")));
        input.setTextColor(SkyConfig.validColor(value, allowTheme) ? 0xffeeeeee : 0xffff5555);
        input.setResponder(text -> {
            setter.accept(text);
            input.setTextColor(SkyConfig.validColor(text, allowTheme) ? 0xffeeeeee : 0xffff5555);
            validate();
        });
        addRenderableWidget(input);
        y += spacing;
    }

    private void slider(String name, double initial, double min, double max, DoubleConsumer setter) {
        var slider = new AbstractSliderButton(x, y, controlWidth, 20, tr(name), (initial - min) / (max - min)) {
            { updateMessage(); }
            private double amount() { return Math.round((min + value * (max - min)) * 20.0) / 20.0; }
            @Override protected void updateMessage() {
                setMessage(Component.literal(tr(name).getString() + ": " + String.format(Locale.ROOT, "%.2f", amount())));
            }
            @Override protected void applyValue() { setter.accept(amount()); }
        };
        slider.setTooltip(Tooltip.create(tr(name + ".tip")));
        addRenderableWidget(slider);
        y += spacing;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        var palette = ThemePalette.current(draft);
        int accent = 0xff000000 | ThemeColors.parseHexRgb(palette.accent(), 0x7aa2f7);
        graphics.drawCenteredString(font, title, width / 2, 10, accent);
        for (var label : labels) graphics.drawString(font, label.text(), x, label.y(), 0xffeeeeee);
        if (page == 3) {
            int[] colors = {ThemeColors.parseHexRgb(palette.accent(), 0), ThemeColors.parseHexRgb(palette.accent2(), 0),
                ThemeColors.parseHexRgb(palette.background(), 0), ThemeColors.parseHexRgb(palette.surface(), 0),
                ThemeColors.parseHexRgb(palette.border(), 0)};
            for (int i = 0; i < colors.length; i++) {
                int left = x + i * controlWidth / colors.length;
                graphics.fill(left, y + 5, left + controlWidth / colors.length - 3, y + 26, 0xff000000 | colors[i]);
            }
            graphics.drawCenteredString(font, tr("themeHint"), width / 2, y + 35, 0xffcccccc);
        }
    }

    @Override public void onClose() { minecraft.setScreen(parent); }
}
