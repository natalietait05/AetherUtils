package com.aetheris.aetherutils.client;

import com.aetheris.aetherutils.lightlevels.LightLevelsClient;
import com.aetheris.aetherutils.lightlevels.LightLevelsConfig;
import com.aetheris.aetherutils.lightlevels.LightLevelsRenderer;
import com.aetheris.aetherutils.treecapitator.TreecapitatorClient;
import com.aetheris.aetherutils.unbreakable.UnbreakableConfig;
import com.aetheris.aetherutils.zoom.ZoomClient;
import com.aetheris.aetherutils.zoom.ZoomConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public class AetherUtilsConfigScreen extends Screen {

    private final Screen parent;

    private Button treecapitatorKeyButton;
    private Button lightLevelsKeyButton;
    private Button zoomKeyButton;

    private Button unbreakableButton;
    private Button smoothZoomButton;

    private KeyMapping waitingForKey;

    public AetherUtilsConfigScreen(Screen parent) {

        super(Component.literal("AetherUtils"));

        this.parent = parent;
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;

        int buttonWidth = 140;
        int gap = 8;

        int leftX =
                centerX - buttonWidth - gap / 2;

        int rightX =
                centerX + gap / 2;

        int startY = 65;

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Treecapitator"),
                        button -> {
                        }
                ).bounds(
                        leftX,
                        startY,
                        buttonWidth,
                        20
                ).build()
        );

        treecapitatorKeyButton =
                this.addRenderableWidget(
                        Button.builder(
                                getKeyText(
                                        TreecapitatorClient.TOGGLE_KEY
                                ),
                                button ->
                                        startKeySelection(
                                                TreecapitatorClient.TOGGLE_KEY,
                                                button
                                        )
                        ).bounds(
                                rightX,
                                startY,
                                buttonWidth,
                                20
                        ).build()
                );

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Light Levels"),
                        button -> {
                        }
                ).bounds(
                        leftX,
                        startY + 30,
                        buttonWidth,
                        20
                ).build()
        );

        lightLevelsKeyButton =
                this.addRenderableWidget(
                        Button.builder(
                                getKeyText(
                                        LightLevelsClient.TOGGLE_KEY
                                ),
                                button ->
                                        startKeySelection(
                                                LightLevelsClient.TOGGLE_KEY,
                                                button
                                        )
                        ).bounds(
                                rightX,
                                startY + 30,
                                buttonWidth,
                                20
                        ).build()
                );

        this.addRenderableWidget(
                new RadiusSlider(
                        centerX - 70,
                        startY + 60,
                        140,
                        20,
                        LightLevelsConfig.getRadius()
                )
        );

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Zoom"),
                        button -> {
                        }
                ).bounds(
                        leftX,
                        startY + 90,
                        buttonWidth,
                        20
                ).build()
        );

        zoomKeyButton =
                this.addRenderableWidget(
                        Button.builder(
                                getKeyText(
                                        ZoomClient.ZOOM_KEY
                                ),
                                button ->
                                        startKeySelection(
                                                ZoomClient.ZOOM_KEY,
                                                button
                                        )
                        ).bounds(
                                rightX,
                                startY + 90,
                                buttonWidth,
                                20
                        ).build()
                );

        this.addRenderableWidget(
                new ZoomSlider(
                        centerX - 70,
                        startY + 120,
                        140,
                        20,
                        ZoomConfig.getZoom()
                )
        );

        smoothZoomButton =
                this.addRenderableWidget(
                        Button.builder(
                                getSmoothZoomText(),
                                button -> {

                                    ZoomConfig.setSmooth(
                                            !ZoomConfig.isSmooth()
                                    );

                                    ZoomConfig.save();

                                    button.setMessage(
                                            getSmoothZoomText()
                                    );
                                }
                        ).bounds(
                                leftX,
                                startY + 150,
                                buttonWidth,
                                20
                        ).build()
                );

        unbreakableButton =
                this.addRenderableWidget(
                        Button.builder(
                                getUnbreakableText(),
                                button -> {

                                    UnbreakableConfig.setEnabled(
                                            !UnbreakableConfig.isEnabled()
                                    );

                                    UnbreakableConfig.save();

                                    button.setMessage(
                                            getUnbreakableText()
                                    );
                                }
                        ).bounds(
                                rightX,
                                startY + 150,
                                buttonWidth,
                                20
                        ).build()
                );

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Done"),
                        button -> onClose()
                ).bounds(
                        centerX - 100,
                        startY + 190,
                        200,
                        20
                ).build()
        );
    }

    private void startKeySelection(
            KeyMapping keyMapping,
            Button button
    ) {

        waitingForKey = keyMapping;

        button.setMessage(
                Component.literal("Press a key...")
        );
    }

    private Component getKeyText(
            KeyMapping keyMapping
    ) {

        if (keyMapping == null) {
            return Component.literal("Key: NONE");
        }

        return Component.literal(
                "Key: "
                        + keyMapping
                        .getTranslatedKeyMessage()
                        .getString()
        );
    }

    private Component getSmoothZoomText() {

        return Component.literal(
                "Smooth Zoom: "
                        + (
                        ZoomConfig.isSmooth()
                                ? "ON"
                                : "OFF"
                )
        );
    }

    private Component getUnbreakableText() {

        return Component.literal(
                "Unbreakable: "
                        + (
                        UnbreakableConfig.isEnabled()
                                ? "ON"
                                : "OFF"
                )
        );
    }

    @Override
    public boolean keyPressed(
            KeyEvent event
    ) {

        if (waitingForKey != null) {

            if (event.isEscape()) {

                waitingForKey = null;

                treecapitatorKeyButton.setMessage(
                        getKeyText(
                                TreecapitatorClient.TOGGLE_KEY
                        )
                );

                lightLevelsKeyButton.setMessage(
                        getKeyText(
                                LightLevelsClient.TOGGLE_KEY
                        )
                );

                zoomKeyButton.setMessage(
                        getKeyText(
                                ZoomClient.ZOOM_KEY
                        )
                );

                return true;
            }

            InputConstants.Key key =
                    InputConstants.getKey(event);

            if (waitingForKey
                    == TreecapitatorClient.TOGGLE_KEY) {

                AetherUtilsKeybindConfig.setTreecapitatorKey(
                        key
                );

                treecapitatorKeyButton.setMessage(
                        getKeyText(
                                TreecapitatorClient.TOGGLE_KEY
                        )
                );

            } else if (waitingForKey
                    == LightLevelsClient.TOGGLE_KEY) {

                AetherUtilsKeybindConfig.setLightLevelsKey(
                        key
                );

                lightLevelsKeyButton.setMessage(
                        getKeyText(
                                LightLevelsClient.TOGGLE_KEY
                        )
                );

            } else if (waitingForKey
                    == ZoomClient.ZOOM_KEY) {

                AetherUtilsKeybindConfig.setZoomKey(
                        key
                );

                zoomKeyButton.setMessage(
                        getKeyText(
                                ZoomClient.ZOOM_KEY
                        )
                );
            }

            AetherUtilsKeybindConfig.save();

            waitingForKey = null;

            return true;
        }

        return super.keyPressed(event);
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

        if (this.minecraft != null) {

            this.minecraft.gui.setScreen(parent);
        }
    }

    private static class RadiusSlider
            extends AbstractSliderButton {

        private static final int MIN_RADIUS =
                LightLevelsRenderer.MIN_RADIUS;

        private static final int MAX_RADIUS =
                LightLevelsRenderer.MAX_RADIUS;

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

        private static double radiusToValue(
                int radius
        ) {

            return (double) (
                    radius - MIN_RADIUS
            ) / (
                    MAX_RADIUS - MIN_RADIUS
            );
        }

        private static int valueToRadius(
                double value
        ) {

            return MIN_RADIUS
                    + (int) Math.round(
                    value * (
                            MAX_RADIUS - MIN_RADIUS
                    )
            );
        }

        @Override
        protected void updateMessage() {

            setMessage(
                    Component.literal(
                            "Radius: "
                                    + valueToRadius(
                                    this.value
                            )
                    )
            );
        }

        @Override
        protected void applyValue() {

            int radius =
                    valueToRadius(
                            this.value
                    );

            LightLevelsConfig.setRadius(radius);
            LightLevelsConfig.save();

            updateMessage();
        }
    }

    private static class ZoomSlider
            extends AbstractSliderButton {

        private static final double MIN_ZOOM = 2.0;
        private static final double MAX_ZOOM = 6.0;

        public ZoomSlider(
                int x,
                int y,
                int width,
                int height,
                double zoom
        ) {

            super(
                    x,
                    y,
                    width,
                    height,
                    Component.empty(),
                    zoomToValue(zoom)
            );

            updateMessage();
        }

        private static double zoomToValue(
                double zoom
        ) {

            return (
                    zoom - MIN_ZOOM
            ) / (
                    MAX_ZOOM - MIN_ZOOM
            );
        }

        private static double valueToZoom(
                double value
        ) {

            return MIN_ZOOM
                    + value * (
                    MAX_ZOOM - MIN_ZOOM
            );
        }

        @Override
        protected void updateMessage() {

            double zoom =
                    valueToZoom(this.value);

            setMessage(
                    Component.literal(
                            String.format(
                                    "Zoom: %.1fx",
                                    zoom
                            )
                    )
            );
        }

        @Override
        protected void applyValue() {

            double zoom =
                    valueToZoom(this.value);

            ZoomConfig.setZoom(zoom);
            ZoomConfig.save();

            updateMessage();
        }
    }
}