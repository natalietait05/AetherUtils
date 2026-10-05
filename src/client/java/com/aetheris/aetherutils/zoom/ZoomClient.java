package com.aetheris.aetherutils.zoom;

import com.aetheris.aetherutils.client.AetherUtilsClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public final class ZoomClient {

    public static KeyMapping ZOOM_KEY;

    private ZoomClient() {
    }

    public static void initialize() {

        ZOOM_KEY = new KeyMapping(
                "key.aetherutils.zoom",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_C,
                AetherUtilsClient.CATEGORY
        );

        ZoomConfig.load();
    }

    public static boolean isZooming() {

        return ZOOM_KEY != null
                && ZOOM_KEY.isDown();
    }
}