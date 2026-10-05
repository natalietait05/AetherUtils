package com.aetheris.aetherutils.lightlevels;

import com.aetheris.aetherutils.client.AetherUtilsKeybindScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class LightLevelsConfigScreen extends Screen {

    private final Screen parent;

    private RadiusSlider radiusSlider;

    public LightLevelsConfigScreen(Screen parent) {
        super(Component.literal("LightLevels"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;
        int startY = this.height / 2 - 50;

        this.addRenderableWidget(
                Button.builder(
                        Component.literal(
                                "Key: "
                                        + LightLevelsClient.TOGGLE_KEY
                                        .getTranslatedKeyMessage()
                                        .getString()
                        ),
                        button -> this.minecraft.gui.setScreen(
                                new AetherUtilsKeybindScreen(
                                        this,
                                        Component.literal("LightLevels"),
                                        LightLevelsClient.TOGGLE_KEY
                                )
                        )
                ).bounds(
                        centerX - 100,
                        startY,
                        200,
                        20
                ).build()
        );

        radiusSlider = this.addRenderableWidget(
                new RadiusSlider(
                        centerX - 100,
                        startY + 30,
                        200,
                        20,
                        LightLevelsConfig.getRadius()
                )
        );

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Done"),
                        button -> onClose()
                ).bounds(
                        centerX - 100,
                        startY + 70,
                        200,
                        20
                ).build()
        );
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                delta
        );

        graphics.centeredText(
                this.font,
                this.title,
                this.width / 2,
                40,
                0xFFFFFFFF
        );
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }

    private static class RadiusSlider extends AbstractSliderButton {

        private static final int MIN_RADIUS = LightLevelsRenderer.MIN_RADIUS;
        private static final int MAX_RADIUS = LightLevelsRenderer.MAX_RADIUS;

        public RadiusSlider(
                int x,
                int y,
                int width,
                int height,
                int radius
        ) {
            super(
                    x,
                    y,
                    width,
                    height,
                    Component.empty(),
                    radiusToValue(radius)
            );

            updateMessage();
        }

        private static double radiusToValue(int radius) {

            return (double) (radius - MIN_RADIUS)
                    / (MAX_RADIUS - MIN_RADIUS);
        }

        private static int valueToRadius(double value) {

            return MIN_RADIUS
                    + (int) Math.round(
                    value * (MAX_RADIUS - MIN_RADIUS)
            );
        }

        @Override
        protected void updateMessage() {

            setMessage(
                    Component.literal(
                            "Radius: " + valueToRadius(this.value)
                    )
            );
        }

        @Override
        protected void applyValue() {

            int radius = valueToRadius(this.value);

            LightLevelsConfig.setRadius(radius);
            LightLevelsConfig.save();

            updateMessage();
        }
    }
}