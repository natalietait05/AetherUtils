package com.aetheris.aetherutils.mixin;

import com.aetheris.aetherutils.unbreakable.Unbreakable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class UnbreakableMiningMixin {

    @Inject(
            method = "getDestroySpeed",
            at = @At("HEAD"),
            cancellable = true
    )
    private void aetherutils$preventMining(BlockState state, CallbackInfoReturnable<Float> cir) {

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

        if (!stack.isBroken()) {
            return;
        }

        cir.setReturnValue(0.0F);
    }
}