package com.aetheris.aetherutils.unbreakable;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class UnbreakableConfigScreen extends Screen {

    private final Screen parent;

    public UnbreakableConfigScreen(Screen parent) {
        super(Component.literal("AetherUtils - Unbreakable"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;

        this.addRenderableWidget(
                Button.builder(
                                getToggleText(),
                                button -> {

                                    UnbreakableConfig.setEnabled(
                                            !UnbreakableConfig.isEnabled()
                                    );

                                    UnbreakableConfig.save();

                                    button.setMessage(
                                            getToggleText()
                                    );
                                }
                        )
                        .bounds(
                                centerX - 100,
                                this.height / 2 - 10,
                                200,
                                20
                        )
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(
                                Component.literal("Done"),
                                button -> onClose()
                        )
                        .bounds(
                                centerX - 100,
                                this.height / 2 + 30,
                                200,
                                20
                        )
                        .build()
        );
    }

    private Component getToggleText() {

        return Component.literal(
                "Unbreakable: "
                        + (UnbreakableConfig.isEnabled()
                        ? "ON"
                        : "OFF")
        );
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {

        this.extractMenuBackground(graphics);

        graphics.centeredText(
                this.font,
                this.title,
                this.width / 2,
                40,
                0xFFFFFF
        );

        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                delta
        );
    }

    @Override
    public void onClose() {

        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(parent);
        }
    }
}