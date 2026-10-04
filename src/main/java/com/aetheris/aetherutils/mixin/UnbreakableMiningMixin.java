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
    private void aetherutils$preventMining(
            BlockState state,
            CallbackInfoReturnable<Float> cir
    ) {

        if (!Unbreakable.isEnabled()) {
            return;
        }

        ItemStack stack = (ItemStack) (Object) this;

        // Elytra mantiene completamente el comportamiento vanilla.
        if (stack.is(Items.ELYTRA)) {
            return;
        }

        if (!stack.isDamageableItem()) {
            return;
        }

        if (!stack.isBroken()) {
            return;
        }

        // Herramienta agotada: no puede minar.
        cir.setReturnValue(0.0F);
    }
}