package com.aetheris.aetherutils.mixin;

import com.aetheris.aetherutils.unbreakable.Unbreakable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class UnbreakableItemStackMixin {

	@Inject(
			method = "applyDamage",
			at = @At("HEAD"),
			cancellable = true
	)
	private void aetherutils$preventBreaking(int newDamage, ServerPlayer player, Consumer<ItemStack> onBreak, CallbackInfo ci) {

		if (!Unbreakable.isEnabled()) {
			return;
		}

		ItemStack stack = (ItemStack) (Object) this;

		if (stack.is(Items.ELYTRA)) {
			return;
		}

		if (!stack.isDamageableItem()) {
			return;
		}

		if (newDamage < stack.getMaxDamage()) {
			return;
		}

		stack.setDamageValue(stack.getMaxDamage());

		player.level().playSound(
				null,
				player.getX(),
				player.getY(),
				player.getZ(),
				SoundEvents.ITEM_BREAK,
				player.getSoundSource(),
				1.0F,
				1.0F
		);

		ci.cancel();
	}
}