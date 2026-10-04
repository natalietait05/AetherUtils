package com.aetheris.aetherutils;

import com.aetheris.aetherutils.treecapitator.Treecapitator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;

public class AetherUtils implements ModInitializer {

	public static final String MOD_ID = "aetherutils";

	public static final Logger LOGGER =
			LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		LOGGER.info("AetherUtils iniciado");

		PlayerBlockBreakEvents.BEFORE.register(
				(level, player, blockPos, blockState, blockEntity) -> {

					//check off
					if (!Treecapitator.isEnabled()) {
						return true;
					}

					//stop check
					if (Treecapitator.isCuttingTree()) {
						return true;
					}

					//tree check
					var logs = Treecapitator.detectTree(
							level,
							blockPos
					);

					if (logs.isEmpty()) {
						return true;
					}

					//break only trees
					Treecapitator.cutTree(
							level,
							player,
							blockPos,
							logs
					);

					return true;
				}
		);
		UseBlockCallback.EVENT.register(
				(player, level, hand, hitResult) -> {

					if (hand != InteractionHand.MAIN_HAND) {
						return InteractionResult.PASS;
					}

					if (!Treecapitator.isStripMode()) {
						return InteractionResult.PASS;
					}

					if (!Treecapitator.canUse(
							player.getMainHandItem()
					)) {
						return InteractionResult.PASS;
					}

					var target = hitResult.getBlockPos();

					var logs =
							Treecapitator.detectTree(
									level,
									target
							);

					if (logs.isEmpty()) {
						return InteractionResult.PASS;
					}

					/*
					 * En el cliente solo cancelamos el uso normal.
					 *
					 * SUCCESS hace que el clic llegue al servidor.
					 */
					if (level.isClientSide()) {
						return InteractionResult.SUCCESS;
					}

					/*
					 * El mundo se modifica solamente en el servidor.
					 */
					Treecapitator.stripAndCutTree(
							level,
							player,
							target,
							logs
					);

					return InteractionResult.SUCCESS;
				}
		);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(
				MOD_ID,
				path
		);
	}
}