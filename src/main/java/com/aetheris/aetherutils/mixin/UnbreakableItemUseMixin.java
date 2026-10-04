package com.aetheris.aetherutils.mixin;

import com.aetheris.aetherutils.unbreakable.Unbreakable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class UnbreakableItemUseMixin {

    private boolean aetherutils$isBlocked() {

        if (!Unbreakable.isEnabled()) {
            return false;
        }

        ItemStack stack = (ItemStack) (Object) this;

        // Elytra mantiene completamente el comportamiento vanilla.
        if (stack.is(Items.ELYTRA)) {
            return false;
        }

        return stack.isDamageableItem() && stack.isBroken();
    }

    @Inject(
            method = "use",
            at = @At("HEAD"),
            cancellable = true
    )
    private void aetherutils$preventUse(
            Level level,
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir
    ) {

        if (aetherutils$isBlocked()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(
            method = "mineBlock",
            at = @At("HEAD"),
            cancellable = true
    )
    private void aetherutils$preventMining(
            Level level,
            BlockState state,
            BlockPos pos,
            Player player,
            CallbackInfo ci
    ) {

        if (aetherutils$isBlocked()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "hurtEnemy",
            at = @At("HEAD"),
            cancellable = true
    )
    private void aetherutils$preventAttack(
            LivingEntity target,
            LivingEntity attacker,
            CallbackInfoReturnable<Boolean> cir
    ) {

        if (aetherutils$isBlocked()) {
            cir.setReturnValue(false);
        }
    }
}