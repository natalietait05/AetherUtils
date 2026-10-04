package com.aetheris.aetherutils.lightlevels;

import com.aetheris.aetherutils.AetherUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public class LightLevelsClient implements ClientModInitializer {

    public static final int OFF = 0;
    public static final int BLOCK_LIGHT = 1;
    public static final int SKY_LIGHT = 2;
    private static int state = OFF;

    // category section in game
    public static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(
                    AetherUtils.id("lightlevels")
            );

    // default keys
    private static final KeyMapping TOGGLE_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.aetherutils.light_levels", 76, CATEGORY));
    private static final KeyMapping CONFIG_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.aetherutils.lightlevels_config", 75, CATEGORY));

    @Override
    public void onInitializeClient() {

        int[] refreshCounter = {0};
        LightLevelsConfig.load();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (CONFIG_KEY.consumeClick()) {

                client.gui.setScreen(new LightLevelsConfigScreen(client.gui.screen()));
            }
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
                    case BLOCK_LIGHT -> message = "Light Levels: BLOCK LIGHT";
                    case SKY_LIGHT -> message = "Light Levels: SKY LIGHT";
                    default -> message = "Light Levels: OFF";
                }

                client.player.sendSystemMessage(Component.literal(message));

                if (client.level != null && state != OFF) {
                    LightLevelsRenderer.update(client.level, client.player.blockPosition(), state);
                } else {
                    LightLevelsRenderer.update(null, null, OFF);
                }
            }

            if (state != OFF && client.player != null && client.level != null) {

                refreshCounter[0]++;

                if (refreshCounter[0] >= 5) {
                    refreshCounter[0] = 0;

                    LightLevelsRenderer.update(client.level, client.player.blockPosition(), state);
                }
            }
        });
        LightLevelsRenderer.initialize();
    }
}