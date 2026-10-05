package com.aetheris.aetherutils.client;

import com.aetheris.aetherutils.AetherUtils;
import com.aetheris.aetherutils.lightlevels.LightLevelsClient;
import com.aetheris.aetherutils.treecapitator.TreecapitatorClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.platform.InputConstants;

public class AetherUtilsClient implements ClientModInitializer {

	public static KeyMapping.Category CATEGORY;

	public static KeyMapping CONFIG_KEY;

	@Override
	public void onInitializeClient() {

		CATEGORY = KeyMapping.Category.register(
				AetherUtils.id("controls")
		);

		CONFIG_KEY = KeyMappingHelper.registerKeyMapping(
				new KeyMapping(
						"key.aetherutils.config",
						InputConstants.KEY_K,
						CATEGORY
				)
		);

		TreecapitatorClient.initialize();
		LightLevelsClient.initialize();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			while (CONFIG_KEY.consumeClick()) {

				Screen parent = client.gui.screen();

				client.gui.setScreen(
						new AetherUtilsConfigScreen(parent)
				);
			}
		});
	}
}