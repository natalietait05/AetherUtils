package com.aetheris.aetherutils.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class AetherUtilsKeybindScreen extends Screen {

    private final Screen parent;
    private final Component title;
    private final KeyMapping keyMapping;

    private Button keyButton;

    private boolean waitingForKey = false;

    public AetherUtilsKeybindScreen(Screen parent, Component title, KeyMapping keyMapping) {
        super(title);

        this.parent = parent;
        this.title = title;
        this.keyMapping = keyMapping;
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;

        this.keyButton = this.addRenderableWidget(
                Button.builder(
                        getKeyText(),
                        button -> {
                            waitingForKey = true;
                            button.setMessage(
                                    Component.literal("Press a key...")
                            );
                        }
                ).bounds(
                        centerX - 100,
                        this.height / 2 - 20,
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
                        this.height / 2 + 20,
                        200,
                        20
                ).build()
        );
    }

    private Component getKeyText() {
        return Component.literal("Key: " + keyMapping.getTranslatedKeyMessage().getString());
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {

        if (waitingForKey) {

            InputConstants.Key key = InputConstants.getKey(event);

            keyMapping.setKey(key);

            waitingForKey = false;

            if (keyButton != null) {
                keyButton.setMessage(getKeyText());
            }

            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(
            @NonNull GuiGraphicsExtractor graphics,
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