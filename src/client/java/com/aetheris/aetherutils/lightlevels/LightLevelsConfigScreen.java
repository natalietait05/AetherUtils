package com.aetheris.aetherutils.lightlevels;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class LightLevelsConfigScreen extends Screen {

    private static final Component TITLE = Component.literal("LightLevels");
    private static final int SLIDER_WIDTH = 300;
    private static final int SLIDER_HEIGHT = 20;
    private final Screen parent;

    public LightLevelsConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;
        int sliderY = this.height / 2 - 20;

        RadiusSlider radiusSlider = new RadiusSlider(centerX - SLIDER_WIDTH / 2, sliderY, SLIDER_WIDTH, SLIDER_HEIGHT);

        this.addRenderableWidget(radiusSlider);

        this.addRenderableWidget(Button.builder(Component.literal("Done"),
                                button -> {
                                    LightLevelsConfig.save();
                                    if (this.minecraft != null) {
                                        this.minecraft.gui.setScreen(parent);
                                    }
                                }
                        )
                        .bounds(centerX - 100, sliderY + 45, 200, 20)
                        .build()
        );
    }

    @Override
    public void onClose() {
        LightLevelsConfig.save();
        this.minecraft.gui.setScreen(parent);
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        graphics.text(this.font, Component.literal("Light display distance"),
                this.width / 2 - 100,
                this.height / 2 - 55,
                0xFFFFFFFF
        );

        graphics.text(this.font, Component.literal("Higher values consume more resources"),
                this.width / 2 - 100,
                this.height / 2 + 35,
                0xFFAAAAAA
        );
    }

    private static class RadiusSlider extends AbstractSliderButton {

        private RadiusSlider(int x, int y, int width, int height) {

            super(x, y, width, height, Component.empty(), getInitialValue());
            updateMessage();
        }

        private static double getInitialValue() {
            int radius = LightLevelsConfig.getRadius();

            return (radius - LightLevelsRenderer.MIN_RADIUS) / (double) (LightLevelsRenderer.MAX_RADIUS - LightLevelsRenderer.MIN_RADIUS);
        }

        private int getRadius() {

            return LightLevelsRenderer.MIN_RADIUS + (int) Math.round(value * (LightLevelsRenderer.MAX_RADIUS - LightLevelsRenderer.MIN_RADIUS)
            );
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal("Distance: " + getRadius() + " blocks"));
        }

        @Override
        protected void applyValue() {
            LightLevelsConfig.setRadius(getRadius());
        }
    }
}