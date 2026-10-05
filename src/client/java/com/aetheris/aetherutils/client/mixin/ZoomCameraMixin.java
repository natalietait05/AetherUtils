package com.aetheris.aetherutils.client.mixin;

import com.aetheris.aetherutils.zoom.ZoomClient;
import com.aetheris.aetherutils.zoom.ZoomConfig;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class ZoomCameraMixin {

    @Inject(
            method = "calculateFov",
            at = @At("RETURN"),
            cancellable = true
    )
    private void aetherutils$applyZoom(
            float partialTicks,
            CallbackInfoReturnable<Float> cir
    ) {
        if (!ZoomClient.isZooming()) {
            return;
        }

        float fov = cir.getReturnValue();

        cir.setReturnValue(
                (float) (fov / ZoomConfig.getZoom())
        );
    }
}