package com.cvserbaada.smartinventory.util;

import javafx.scene.Scene;

public final class ThemeManager {
    public enum Theme {
        LIGHT,
        DARK
    }

    private static final String LIGHT_THEME_CLASS = "light-theme";
    private static final String DARK_THEME_CLASS = "dark-theme";
    private static Theme currentTheme = Theme.LIGHT;

    private ThemeManager() {
    }

    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    public static void applyTheme(Scene scene, Theme theme) {
        if (scene == null || scene.getRoot() == null || theme == null) {
            return;
        }

        currentTheme = theme;
        scene.getRoot().getStyleClass().removeAll(LIGHT_THEME_CLASS, DARK_THEME_CLASS);
        scene.getRoot().getStyleClass().add(theme == Theme.DARK ? DARK_THEME_CLASS : LIGHT_THEME_CLASS);
    }

    public static void applyCurrentTheme(Scene scene) {
        applyTheme(scene, currentTheme);
    }
}
