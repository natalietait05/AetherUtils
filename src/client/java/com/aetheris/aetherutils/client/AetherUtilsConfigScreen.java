package com.aetheris.aetherutils.client;

import com.aetheris.aetherutils.lightlevels.LightLevelsConfigScreen;
import com.aetheris.aetherutils.treecapitator.TreecapitatorClient;
import com.aetheris.aetherutils.treecapitator.TreecapitatorConfigScreen;
import com.aetheris.aetherutils.unbreakable.UnbreakableConfigScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AetherUtilsConfigScreen extends Screen {

    private final Screen parent;

    public AetherUtilsConfigScreen(Screen parent) {
        super(Component.literal("AetherUtils"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;
        int startY = this.height / 2 - 55;

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Treecapitator"),
                        button -> this.minecraft.gui.setScreen(
                                new TreecapitatorConfigScreen(this)
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
                        Component.literal("LightLevels"),
                        button -> this.minecraft.gui.setScreen(
                                new LightLevelsConfigScreen(this)
                        )
                ).bounds(
                        centerX - 100,
                        startY + 30,
                        200,
                        20
                ).build()
        );

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Unbreakable"),
                        button -> this.minecraft.gui.setScreen(
                                new UnbreakableConfigScreen(this)
                        )
                ).bounds(
                        centerX - 100,
                        startY + 60,
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
                        startY + 100,
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

        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(parent);
        }
    }
}