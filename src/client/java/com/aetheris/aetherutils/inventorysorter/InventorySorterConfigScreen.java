package com.aetheris.aetherutils.inventorysorter;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public final class InventorySorterConfigScreen
        extends Screen {

    private final Screen parent;

    private Button ascendingButton;
    private Button descendingButton;

//    private Button nameThenTypeButton;
//    private Button typeThenNameButton;

    public InventorySorterConfigScreen(Screen parent) {

        super(Component.literal("Inventory Sorter"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;

        int buttonWidth = 180;

        int buttonHeight = 20;

        int left = centerX - buttonWidth / 2;

        int y = 80;

        ascendingButton =
                this.addRenderableWidget(
                        Button.builder(Component.literal("A → Z"),
                                button -> {

                                    InventorySorterConfig.setDirection(InventorySortDirection.ASCENDING);

                                    updateButtons();
                                }
                        ).bounds(
                                left,
                                y,
                                buttonWidth,
                                buttonHeight
                        ).build()
                );

        descendingButton =
                this.addRenderableWidget(
                        Button.builder(
                                Component.literal("Z → A"),
                                button -> {

                                    InventorySorterConfig
                                            .setDirection(
                                                    InventorySortDirection
                                                            .DESCENDING
                                            );

                                    updateButtons();
                                }
                        ).bounds(
                                left,
                                y + 25,
                                buttonWidth,
                                buttonHeight
                        ).build()
                );
/*
        nameThenTypeButton =
                this.addRenderableWidget(
                        Button.builder(
                                Component.literal("Nombre → Tipo"),
                                button -> {

                                    InventorySorterConfig
                                            .setPriority(
                                                    InventorySortPriority
                                                            .NAME_THEN_TYPE
                                            );

                                    updateButtons();
                                }
                        ).bounds(
                                left,
                                y + 85,
                                buttonWidth,
                                buttonHeight
                        ).build()
                );

        typeThenNameButton =
                this.addRenderableWidget(
                        Button.builder(
                                Component.literal("Tipo → Nombre"),
                                button -> {

                                    InventorySorterConfig
                                            .setPriority(
                                                    InventorySortPriority
                                                            .TYPE_THEN_NAME
                                            );

                                    updateButtons();
                                }
                        ).bounds(
                                left,
                                y + 110,
                                buttonWidth,
                                buttonHeight
                        ).build()
                );
*/
        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Done"),
                        button -> {

                            InventorySorterConfig.save();

                            this.minecraft.gui.setScreen(
                                    parent
                            );
                        }
                ).bounds(
                        left,
                        y + 85, //y + 160,
                        buttonWidth,
                        buttonHeight
                ).build()
        );

        updateButtons();
    }

    private void updateButtons() {

        ascendingButton.active = InventorySorterConfig.getDirection() != InventorySortDirection.ASCENDING;

        descendingButton.active = InventorySorterConfig.getDirection() != InventorySortDirection.DESCENDING;

//        nameThenTypeButton.active = InventorySorterConfig.getPriority() != InventorySortPriority.NAME_THEN_TYPE;
//
//        typeThenNameButton.active = InventorySorterConfig.getPriority() != InventorySortPriority.TYPE_THEN_NAME;


    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {

        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.centeredText(
                this.font,
                Component.literal(
                        "AetherUtils"
                ),
                this.width / 2,
                35,
                0xFFFFFFFF
        );

        graphics.centeredText(
                this.font,
                Component.literal(
                        "Inventory Sorter"
                ),
                this.width / 2,
                50,
                0xFFFFFFFF
        );

        graphics.text(
                this.font,
                Component.literal(
                        "Order"
                ),
                this.width / 2 - 90,
                68,
                0xFFFFFFFF,
                true
        );

//        graphics.text(
//                this.font,
//                Component.literal(
//                        "Priority"
//                ),
//                this.width / 2 - 90,
//                153,
//                0xFFFFFFFF,
//                true
//        );
    }

    @Override
    public void onClose() {

        InventorySorterConfig.save();
        this.minecraft.gui.setScreen(parent);
    }
}