package com.aetheris.aetherutils.unbreakable;

public final class Unbreakable {

    private Unbreakable() {
    }

    public static void initialize() {

        UnbreakableConfig.load();
    }

    public static boolean isEnabled() {

        return UnbreakableConfig.isEnabled();
    }
}