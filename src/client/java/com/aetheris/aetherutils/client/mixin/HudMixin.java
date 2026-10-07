package com.aetheris.aetherutils.client.mixin;

import com.aetheris.aetherutils.client.AetherUtilsToast;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {

    @Inject(
            method = "extractRenderState",
            at = @At("TAIL")
    )
    private void aetherutils$renderToast(
            GuiGraphicsExtractor graphics,
            DeltaTracker deltaTracker,
            CallbackInfo ci
    ) {

        AetherUtilsToast.render(graphics);
    }
}