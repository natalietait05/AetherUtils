package com.aetheris.aetherutils.treecapitator;

import com.aetheris.aetherutils.client.AetherUtilsClient;
import com.aetheris.aetherutils.client.AetherUtilsToast;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;


public final class TreecapitatorClient {

    public static KeyMapping TOGGLE_KEY;

    private TreecapitatorClient() {
    }

    public static void initialize() {

        TOGGLE_KEY = new KeyMapping(
                "key.aetherutils.toggle_treecapitator",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_T,
                AetherUtilsClient.CATEGORY
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (TOGGLE_KEY.consumeClick()) {

                if (client.player == null) {
                    return;
                }
                Treecapitator.cycleMode();
                String message;
                switch (Treecapitator.getMode()) {

                    case Treecapitator.ON ->
                            message = "Treecapitator: ON";
                    case Treecapitator.STRIP ->
                            message = "Treecapitator: STRIP";
                    default ->
                            message = "Treecapitator: OFF";
                }
                AetherUtilsToast.show(message);
            }
        });
    }
}