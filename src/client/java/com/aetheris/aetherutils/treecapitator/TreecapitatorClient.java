package com.aetheris.aetherutils.treecapitator;

import com.aetheris.aetherutils.client.AetherUtilsClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

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

                String status;

                switch (Treecapitator.getMode()) {

                    case Treecapitator.ON ->
                            status = "ON";

                    case Treecapitator.STRIP ->
                            status = "STRIP";

                    default ->
                            status = "OFF";
                }

                client.player.sendSystemMessage(
                        Component.literal(
                                "Tree: " + status
                        )
                );
            }
        });
    }
}