package com.aetheris.aetherutils.client.mixin;

import com.aetheris.aetherutils.inventorysorter.InventorySorter;
import com.aetheris.aetherutils.inventorysorter.InventorySorterConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class InventorySorterScreenMixin {

    @Shadow
    @Final
    protected int imageWidth;

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Shadow
    public abstract AbstractContainerMenu getMenu();

    @Inject(
            method = "init",
            at = @At("TAIL")
    )
    private void aetherutils$addInventorySorterButtons(
            CallbackInfo ci
    ) {

        AbstractContainerMenu menu = this.getMenu();

        Minecraft minecraft = Minecraft.getInstance();

        Player player = minecraft.player;

        if (player == null) {
            return;
        }

        // ignore hoppers

        if (menu instanceof HopperMenu) {
            return;
        }

        // ignore craft menus

        if (menu instanceof CraftingMenu
                || menu instanceof CrafterMenu) {

            return;
        }

        // button size

        int buttonWidth = 20;
        int buttonHeight = 18;
        int gap = 2;
        int totalWidth = buttonWidth * 2 + gap;

        // initial pos

        int x = this.leftPos + this.imageWidth + 4;

        int y = this.topPos;

        // move to left if insufficient space on right

        if (x + totalWidth
                > minecraft.getWindow().getGuiScaledWidth()) {

            x = this.leftPos - totalWidth - 4;
        }

        /*
         * ==================================================
         * first row
         * ==================================================
         */

        // Sort

        aetherutils$addButton(
                Button.builder(
                                Component.literal("↑↓"),
                                button -> InventorySorter.sort(
                                        minecraft,
                                        getMenu(),
                                        aetherutils$getSortableSlots(getMenu(), minecraft.player)
                                )
                        )
                        .tooltip(Tooltip.create(Component.literal("Sort inventory")))
                        .bounds(
                                x,
                                y,
                                buttonWidth,
                                buttonHeight
                        )
                        .build()
        );

        // Settings

        aetherutils$addButton(
                Button.builder(
                                Component.literal("⚙"),
                                button -> {

                                    Screen current =
                                            minecraft.gui.screen();

                                    minecraft.gui.setScreen(
                                            new InventorySorterConfigScreen(
                                                    current
                                            )
                                    );
                                }
                        )
                        .tooltip(
                                Tooltip.create(
                                        Component.literal(
                                                "Settings"
                                        )
                                )
                        )
                        .bounds(
                                x + buttonWidth + gap,
                                y,
                                buttonWidth,
                                buttonHeight
                        )
                        .build()
        );

        // buttons only appears in chests

        if (!aetherutils$hasExternalInventory(
                menu,
                player
        )) {
            return;
        }

        /*
         * ==================================================
         * Second Row
         * ==================================================
         */

        // Store Existing

        aetherutils$addButton(
                Button.builder(
                                Component.literal("↑="),
                                button -> InventorySorter
                                        .moveMatchingToContainer(
                                                minecraft,
                                                getMenu()
                                        )
                        )
                        .tooltip(
                                Tooltip.create(
                                        Component.literal(
                                                "Store existing items"
                                        )
                                )
                        )
                        .bounds(
                                x,
                                y + buttonHeight + gap,
                                buttonWidth,
                                buttonHeight
                        )
                        .build()
        );

        // Store All

        aetherutils$addButton(
                Button.builder(
                                Component.literal("↑*"),
                                button -> InventorySorter
                                        .moveAllToContainer(
                                                minecraft,
                                                getMenu()
                                        )
                        )
                        .tooltip(
                                Tooltip.create(
                                        Component.literal(
                                                "Store all items"
                                        )
                                )
                        )
                        .bounds(
                                x + buttonWidth + gap,
                                y + buttonHeight + gap,
                                buttonWidth,
                                buttonHeight
                        )
                        .build()
        );

        /*
         * ==================================================
         * Third Row
         * ==================================================
         */

        // Retrieve existing

        aetherutils$addButton(
                Button.builder(
                                Component.literal("↓="),
                                button -> InventorySorter
                                        .moveMatchingToPlayer(
                                                minecraft,
                                                getMenu()
                                        )
                        )
                        .tooltip(
                                Tooltip.create(
                                        Component.literal(
                                                "Retrieve existing items"
                                        )
                                )
                        )
                        .bounds(
                                x,
                                y + (buttonHeight + gap) * 2,
                                buttonWidth,
                                buttonHeight
                        )
                        .build()
        );

        // Retrieve all

        aetherutils$addButton(
                Button.builder(
                                Component.literal("↓*"),
                                button -> InventorySorter
                                        .moveAllToPlayer(
                                                minecraft,
                                                getMenu()
                                        )
                        )
                        .tooltip(
                                Tooltip.create(
                                        Component.literal(
                                                "Retrieve all items"
                                        )
                                )
                        )
                        .bounds(
                                x + buttonWidth + gap,
                                y + (buttonHeight + gap) * 2,
                                buttonWidth,
                                buttonHeight
                        )
                        .build()
        );
    }

    @Unique
    private void aetherutils$addButton(Button button) {

        ScreenAccessor accessor = (ScreenAccessor) (Object) this;

        accessor.aetherutils$addRenderableWidget(button);
    }

    @Unique
    private static List<Integer> aetherutils$getSortableSlots(AbstractContainerMenu menu, Player player) {

        List<Integer> result =
                new ArrayList<>();

        if (menu == null || player == null) {
            return result;
        }

        // only sort external inventory

        if (aetherutils$hasExternalInventory(menu, player)) {

            for (Slot slot : menu.slots) {

                if (slot.container == player.getInventory()) {
                    continue;
                }

                if (aetherutils$isCraftingSlot(slot)) {
                    continue;
                }

                result.add(slot.index);
            }

            return result;
        }

        // player inventory excluding armor, offhand and hotbar

        for (Slot slot : menu.slots) {

            if (slot.container != player.getInventory()) {
                continue;
            }

            int containerSlot = slot.getContainerSlot();

            if (containerSlot >= 9 && containerSlot <= 35) {
                result.add(slot.index);
            }
        }

        return result;
    }

    @Unique
    private static boolean aetherutils$hasExternalInventory(AbstractContainerMenu menu, Player player) {

        if (menu == null || player == null) {
            return false;
        }

        // ignore craft blocks and hoppers

        if (menu instanceof CraftingMenu || menu instanceof CrafterMenu || menu instanceof HopperMenu) {
            return false;
        }

        for (Slot slot : menu.slots) {

            if (slot.container == player.getInventory()) {
                continue;
            }

            if (aetherutils$isCraftingSlot(slot)) {
                continue;
            }

            return true;
        }

        return false;
    }

    @Unique
    private static boolean aetherutils$isCraftingSlot(Slot slot) {

        if (slot == null) {
            return false;
        }

        if (slot.container instanceof CraftingContainer) {
            return true;
        }

        if (slot.container instanceof ResultContainer) {
            return true;
        }

        String className = slot.container.getClass().getName();

        return className.contains("Crafter") || className.contains("Crafting");
    }
}