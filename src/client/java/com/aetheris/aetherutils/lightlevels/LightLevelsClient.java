package com.aetheris.aetherutils.lightlevels;

import com.aetheris.aetherutils.client.AetherUtilsClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public final class LightLevelsClient {

    public static final int OFF = 0;
    public static final int BLOCK_LIGHT = 1;
    public static final int SKY_LIGHT = 2;

    private static int state = OFF;

    public static KeyMapping TOGGLE_KEY;

    private LightLevelsClient() {
    }

    public static void initialize() {

        TOGGLE_KEY = new KeyMapping(
                "key.aetherutils.light_levels",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_L,
                AetherUtilsClient.CATEGORY
        );

        LightLevelsConfig.load();

        int[] refreshCounter = {0};

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (TOGGLE_KEY.consumeClick()) {

                state++;

                if (state > SKY_LIGHT) {
                    state = OFF;
                }

                refreshCounter[0] = 0;

                if (client.player == null) {
                    continue;
                }

                String message;

                switch (state) {

                    case BLOCK_LIGHT ->
                            message =
                                    "Light Levels: BLOCK LIGHT";

                    case SKY_LIGHT ->
                            message =
                                    "Light Levels: SKY LIGHT";

                    default ->
                            message =
                                    "Light Levels: OFF";
                }

                client.player.sendSystemMessage(
                        Component.literal(message)
                );

                if (client.level != null
                        && state != OFF) {

                    LightLevelsRenderer.update(
                            client.level,
                            client.player.blockPosition(),
                            state
                    );

                } else {

                    LightLevelsRenderer.update(
                            null,
                            null,
                            OFF
                    );
                }
            }

            if (state != OFF
                    && client.player != null
                    && client.level != null) {

                refreshCounter[0]++;

                if (refreshCounter[0] >= 5) {

                    refreshCounter[0] = 0;

                    LightLevelsRenderer.update(
                            client.level,
                            client.player.blockPosition(),
                            state
                    );
                }
            }
        });

        LightLevelsRenderer.initialize();
    }
}