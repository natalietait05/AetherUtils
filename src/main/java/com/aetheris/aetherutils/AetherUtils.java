package com.aetheris.aetherutils;

import com.aetheris.aetherutils.treecapitator.Treecapitator;
import com.aetheris.aetherutils.unbreakable.Unbreakable;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AetherUtils implements ModInitializer {

	public static final String MOD_ID = "aetherutils";

	public static final Logger LOGGER =
			LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		LOGGER.info("AetherUtils iniciado");
		Unbreakable.initialize();
		PlayerBlockBreakEvents.BEFORE.register(
				(level, player, blockPos, blockState, blockEntity) -> {

					if (!Treecapitator.isEnabled()) {
						return true;
					}

					if (Treecapitator.isCuttingTree()) {
						return true;
					}

					var logs = Treecapitator.detectTree(level, blockPos);

					if (logs.isEmpty()) {
						return true;
					}

					Treecapitator.cutTree(level, player, blockPos, logs);

					return true;
				}
		);
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {

					if (hand != InteractionHand.MAIN_HAND) {
						return InteractionResult.PASS;
					}

					if (!Treecapitator.isStripMode()) {
						return InteractionResult.PASS;
					}

					if (!Treecapitator.canUse(player.getMainHandItem())) {
						return InteractionResult.PASS;
					}

					var target = hitResult.getBlockPos();

					var logs = Treecapitator.detectTree(level, target);

					if (logs.isEmpty()) {
						return InteractionResult.PASS;
					}

					if (level.isClientSide()) {
						return InteractionResult.SUCCESS;
					}

					Treecapitator.stripTree(level, player, target, logs);

					return InteractionResult.SUCCESS;
				}
		);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}