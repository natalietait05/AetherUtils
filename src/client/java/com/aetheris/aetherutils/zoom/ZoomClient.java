package com.aetheris.aetherutils.zoom;

import com.aetheris.aetherutils.client.AetherUtilsClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.network.chat.Component;

public final class ZoomClient {

    private static boolean wasZooming = false;
    public static KeyMapping ZOOM_KEY;

    private ZoomClient() {
    }

    public static void initialize() {

        ZOOM_KEY = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.aetherutils.zoom",
                        InputConstants.KEY_C,
                        AetherUtilsClient.CATEGORY
                )
        );

        ZoomConfig.load();

//        ClientTickEvents.END_CLIENT_TICK.register(client -> {
//            boolean zooming = isZooming();
//
//            if (zooming && !wasZooming && client.player != null) {
//                client.player.sendSystemMessage(
//                        Component.literal("Zoom: tecla C detectada")
//                );
//            }
//
//            wasZooming = zooming;
//        });
    }

    public static boolean isZooming() {
        return ZOOM_KEY != null
                && ZOOM_KEY.isDown();
    }
}