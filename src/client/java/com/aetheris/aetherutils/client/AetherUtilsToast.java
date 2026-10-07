package com.aetheris.aetherutils.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class AetherUtilsToast {

    private static final long DISPLAY_TIME = 2500L;

    private static String message = null;
    private static long messageEndTime = 0L;

    private AetherUtilsToast() {
    }

    public static void show(String newMessage) {

        if (newMessage == null || newMessage.isEmpty()) {
            return;
        }

        message = newMessage;
        messageEndTime = System.currentTimeMillis() + DISPLAY_TIME;
    }

    public static void render(GuiGraphicsExtractor graphics) {

        if (message == null) {
            return;
        }

        long remaining = messageEndTime - System.currentTimeMillis();

        if (remaining <= 0L) {
            message = null;
            return;
        }

        Minecraft client = Minecraft.getInstance();

        if (client.player == null) {
            return;
        }

        int screenWidth = graphics.guiWidth();

        int screenHeight = graphics.guiHeight();

        int y = screenHeight - 70;

        float fade = Math.clamp(remaining / 500.0f, 0.0f, 1.0f);

        int alpha = (int) (fade * 255.0f);

        int color = (alpha << 24) | 0xFFFFFF;

        graphics.centeredText(
                client.font,
                Component.literal(message),
                screenWidth / 2,
                y,
                color
        );
    }
}