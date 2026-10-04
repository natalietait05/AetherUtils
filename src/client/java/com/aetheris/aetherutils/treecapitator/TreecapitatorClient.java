package com.aetheris.aetherutils.treecapitator;

import com.aetheris.aetherutils.lightlevels.LightLevelsClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
public class TreecapitatorClient implements ClientModInitializer {

    private static final KeyMapping TOGGLE_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.aetherutils.toggle_treecapitator", 84, LightLevelsClient.CATEGORY));

    @Override
    public void onInitializeClient() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (TOGGLE_KEY.consumeClick()) {

                if (client.player == null) {
                    return;
                }

                Treecapitator.cycleMode();

                String status;

                switch (Treecapitator.getMode()) {

                    case Treecapitator.ON -> status = "ON";

                    case Treecapitator.STRIP -> status = "STRIP";

                    default -> status = "OFF";
                }

                client.player.sendSystemMessage(Component.literal("Tree: " + status));
            }
        });
    }
}