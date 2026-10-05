package com.aetheris.aetherutils.treecapitator;

import com.aetheris.aetherutils.client.AetherUtilsKeybindScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TreecapitatorConfigScreen extends Screen {

    private final Screen parent;

    public TreecapitatorConfigScreen(Screen parent) {
        super(Component.literal("Treecapitator"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;
        int startY = this.height / 2 - 40;

        this.addRenderableWidget(
                Button.builder(
                        Component.literal(
                                "Key: "
                                        + TreecapitatorClient.TOGGLE_KEY
                                        .getTranslatedKeyMessage()
                                        .getString()
                        ),
                        button -> this.minecraft.gui.setScreen(
                                new AetherUtilsKeybindScreen(
                                        this,
                                        Component.literal("Treecapitator"),
                                        TreecapitatorClient.TOGGLE_KEY
                                )
                        )
                ).bounds(
                        centerX - 100,
                        startY,
                        200,
                        20
                ).build()
        );

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Done"),
                        button -> onClose()
                ).bounds(
                        centerX - 100,
                        startY + 40,
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
}