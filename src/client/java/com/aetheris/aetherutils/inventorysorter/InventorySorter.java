package com.aetheris.aetherutils.inventorysorter;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


// Player inventory:
//
// 0 - 8   = hotbar
// 9 - 35  = main inventory
// 36 - 39 = armor
// 40      = offhand



public final class InventorySorter {

    private static final Logger LOGGER = LogUtils.getLogger();

    private InventorySorter() {
    }

    /*
     * ============================================================
     * SORT
     * ============================================================
     */

    public static void sort(Minecraft client, AbstractContainerMenu menu, List<Integer> slotIndexes) {

        if (client == null || client.player == null || client.gameMode == null || menu == null || slotIndexes == null || slotIndexes.size() < 2) {
            return;
        }

        if (client.player.containerMenu != menu) {
            return;
        }

        List<Slot> slots = new ArrayList<>();

        for (int index : slotIndexes) {

            if (index < 0 || index >= menu.slots.size()) {
                continue;
            }

            Slot slot = menu.slots.get(index);

            if (!slot.hasItem() || slot.mayPickup(client.player)) {
                slots.add(slot);
            }
        }

        if (slots.size() < 2) {
            return;
        }

        stackItems(client, menu, slots);

        // after stacking re-read inventory content

        List<ItemStack> current = new ArrayList<>();

        for (Slot slot : slots) {
            current.add(slot.getItem().copy());
        }

        // copy sorted inventory

        List<ItemStack> sorted = new ArrayList<>(current);

        sorted.sort(createComparator());

        // if nothing changes, continue

        if (isSameArrangement(current, sorted)) {
            return;
        }

        // re-order stacks

        performReordering(client, menu, slots, current, sorted);

    }

    private static Comparator<ItemStack> createComparator() {

        Comparator<ItemStack> itemComparator = Comparator.comparing(InventorySorter::nameKey, String.CASE_INSENSITIVE_ORDER);


        itemComparator = itemComparator.thenComparing(InventorySorter::typeKey, String.CASE_INSENSITIVE_ORDER);

        if (InventorySorterConfig.getDirection() == InventorySortDirection.DESCENDING) {

            itemComparator = itemComparator.reversed();
        }

        return Comparator.comparing(InventorySorter::isEmpty).thenComparing(itemComparator);
    }

    private static boolean isEmpty(ItemStack stack) {
        return stack.isEmpty();
    }

    private static String nameKey(ItemStack stack) {

        if (stack.isEmpty()) {
            return "";
        }

        return stack.getHoverName().getString();
    }

    private static String typeKey(ItemStack stack) {

        if (stack.isEmpty()) {
            return "";
        }

        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
    }

    private static boolean isSameArrangement(List<ItemStack> current, List<ItemStack> sorted) {

        if (current.size() != sorted.size()) {
            return false;
        }

        for (int i = 0; i < current.size(); i++) {

            if (!ItemStack.matches(current.get(i), sorted.get(i))) {
                return false;
            }
        }

        return true;
    }

    private static void performReordering(Minecraft client, AbstractContainerMenu menu, List<Slot> slots, List<ItemStack> current, List<ItemStack> sorted) {

        int safetyLimit = slots.size() * slots.size() * 4;

        int operations = 0;

        for (int targetIndex = 0; targetIndex < slots.size(); targetIndex++) {

            if (operations >= safetyLimit) {

                LOGGER.warn("Inventory Sorter reached safety limit");

                break;
            }

            ItemStack desired = sorted.get(targetIndex);

            ItemStack actual = current.get(targetIndex);

            if (ItemStack.matches(actual, desired)) {
                continue;
            }

            int sourceIndex = findMatchingStack(current, desired, targetIndex + 1);

            if (sourceIndex == -1) {
                continue;
            }

            Slot targetSlot = slots.get(targetIndex);

            Slot sourceSlot = slots.get(sourceIndex);

            swapSlots(client, menu, sourceSlot, targetSlot);

            ItemStack temporary = current.get(targetIndex);

            current.set(targetIndex, current.get(sourceIndex));

            current.set(sourceIndex, temporary);

            operations += 3;
        }
    }

    public static void moveMatchingToPlayer(Minecraft client, AbstractContainerMenu menu) {

        if (!isValidMenu(client, menu)) {
            return;
        }

        List<Slot> playerSlots = getPlayerMainInventorySlots(menu, client.player);

        List<Slot> containerSlots = getExternalInventorySlots(menu, client.player);

        if (playerSlots.isEmpty() || containerSlots.isEmpty()) {

            return;
        }

        List<Slot> sourceSlots = new ArrayList<>(containerSlots);

        for (Slot containerSlot : sourceSlots) {

            if (!containerSlot.hasItem()) {
                continue;
            }

            ItemStack containerStack = containerSlot.getItem();

            if (containerStack.isEmpty()) {
                continue;
            }

            boolean exists = containerContainsSameItem(playerSlots, containerStack);

            if (!exists) {
                continue;
            }

            moveStackToPlayer(client, menu, containerSlot, playerSlots);
        }

    }

    public static void moveAllToPlayer(Minecraft client, AbstractContainerMenu menu) {

        if (!isValidMenu(client, menu)) {
            return;
        }

        List<Slot> playerSlots = getPlayerMainInventorySlots(menu, client.player);

        List<Slot> containerSlots = getExternalInventorySlots(menu, client.player);

        if (playerSlots.isEmpty() || containerSlots.isEmpty()) {

            return;
        }

        List<Slot> sourceSlots = new ArrayList<>(containerSlots);

        for (Slot containerSlot : sourceSlots) {

            if (!containerSlot.hasItem()) {
                continue;
            }

            moveStackToPlayer(client, menu, containerSlot, playerSlots);
        }

    }

    private static void moveStackToPlayer(Minecraft client, AbstractContainerMenu menu, Slot source, List<Slot> playerSlots) {

        if (source == null || !source.hasItem()) {
            return;
        }

        // complete existing stacks in player inventory

        for (Slot target : playerSlots) {

            if (!source.hasItem()) {
                return;
            }

            if (!target.hasItem()) {
                continue;
            }

            if (!target.mayPlace(source.getItem())) {
                continue;
            }

            ItemStack sourceStack = source.getItem();

            ItemStack targetStack = target.getItem();

            if (!ItemStack.isSameItemSameComponents(sourceStack, targetStack)) {
                continue;
            }

            if (targetStack.getCount() >= targetStack.getMaxStackSize()) {

                continue;
            }

            moveUsingPickup(client, menu, source, target);
        }

        // check if there is empty slots in player inventory

        for (Slot target : playerSlots) {

            if (!source.hasItem()) {
                return;
            }

            if (target.hasItem()) {
                continue;
            }

            if (!target.mayPlace(source.getItem())) {
                continue;
            }

            moveUsingPickup(client, menu, source, target);
        }
    }

    private static int findMatchingStack(List<ItemStack> stacks, ItemStack wanted, int startIndex) {

        for (int i = startIndex; i < stacks.size(); i++) {

            if (ItemStack.matches(stacks.get(i), wanted)) {

                return i;
            }
        }

        return -1;
    }

    private static void swapSlots(Minecraft client, AbstractContainerMenu menu, Slot first, Slot second) {

        if (first == second) {
            return;
        }

        click(client, menu, first);

        click(client, menu, second);

        click(client, menu, first);
    }

    // MOVE MATCHING


    private static void stackItems(Minecraft client, AbstractContainerMenu menu, List<Slot> slots) {

        for (int i = 0; i < slots.size(); i++) {

            Slot target = slots.get(i);

            if (!target.hasItem()) {
                continue;
            }

            ItemStack targetStack = target.getItem();

            if (targetStack.getCount() >= targetStack.getMaxStackSize()) {
                continue;
            }

            for (int j = i + 1; j < slots.size(); j++) {

                Slot source = slots.get(j);

                if (!source.hasItem()) {
                    continue;
                }

                ItemStack sourceStack = source.getItem();

                if (!ItemStack.isSameItemSameComponents(targetStack, sourceStack)) {
                    continue;
                }

                int space = targetStack.getMaxStackSize() - targetStack.getCount();

                if (space <= 0) {
                    break;
                }

                click(client, menu, source);

                click(client, menu, target);

                click(client, menu, source);

                targetStack = target.getItem();

                if (!target.hasItem() || targetStack.getCount() >= targetStack.getMaxStackSize()) {
                    break;
                }
            }
        }
    }

    public static void moveMatchingToContainer(Minecraft client, AbstractContainerMenu menu) {

        if (!isValidMenu(client, menu)) {
            return;
        }

        List<Slot> playerSlots = getPlayerMainInventorySlots(menu, client.player);

        List<Slot> containerSlots = getExternalInventorySlots(menu, client.player);

        if (playerSlots.isEmpty() || containerSlots.isEmpty()) {
            return;
        }

        List<Slot> sourceSlots = new ArrayList<>(playerSlots);

        for (Slot playerSlot : sourceSlots) {

            if (!playerSlot.hasItem()) {
                continue;
            }

            ItemStack playerStack = playerSlot.getItem();

            if (playerStack.isEmpty()) {
                continue;
            }

            boolean exists = containerContainsSameItem(containerSlots, playerStack);

            if (!exists) {
                continue;
            }

            moveStackToContainer(client, menu, playerSlot, containerSlots);
        }


    }

    // Move All to player

    public static void moveAllToContainer(Minecraft client, AbstractContainerMenu menu) {

        if (!isValidMenu(client, menu)) {
            return;
        }

        List<Slot> playerSlots = getPlayerMainInventorySlots(menu, client.player);

        List<Slot> containerSlots = getExternalInventorySlots(menu, client.player);

        if (playerSlots.isEmpty() || containerSlots.isEmpty()) {

            return;
        }

        List<Slot> sourceSlots = new ArrayList<>(playerSlots);

        for (Slot playerSlot : sourceSlots) {

            if (!playerSlot.hasItem()) {
                continue;
            }

            moveStackToContainer(client, menu, playerSlot, containerSlots);
        }


    }

    // Define inventory without hotbar/armor/offhand

    private static List<Slot> getPlayerMainInventorySlots(AbstractContainerMenu menu, Player player) {

        List<Slot> result = new ArrayList<>();

        if (menu == null || player == null) {

            return result;
        }

        for (Slot slot : menu.slots) {

            if (slot.container != player.getInventory()) {

                continue;
            }

            int containerSlot =
                    slot.getContainerSlot();

            if (containerSlot >= 9 && containerSlot <= 35) {

                result.add(slot);
            }
        }

        return result;
    }

    private static List<Slot> getExternalInventorySlots(AbstractContainerMenu menu, Player player) {

        List<Slot> result = new ArrayList<>();

        if (menu == null || player == null) {

            return result;
        }

        if (menu instanceof net.minecraft.world.inventory.HopperMenu) {
            return result;
        }

        if (menu instanceof net.minecraft.world.inventory.CraftingMenu || menu instanceof net.minecraft.world.inventory.CrafterMenu) {
            return result;
        }

        for (Slot slot : menu.slots) {

            if (slot.container == player.getInventory()) {

                continue;
            }

            String className = slot.container.getClass().getName();

            if (className.contains("Crafting") || className.contains("Crafter")) {
                continue;
            }

            if (slot.container instanceof net.minecraft.world.inventory.CraftingContainer) {
                continue;
            }

            if (slot.container instanceof net.minecraft.world.inventory.ResultContainer) {
                continue;
            }

            result.add(slot);
        }

        return result;
    }

    private static boolean containerContainsSameItem(List<Slot> containerSlots, ItemStack stack) {

        for (Slot slot : containerSlots) {

            if (!slot.hasItem()) {
                continue;
            }

            ItemStack containerStack =
                    slot.getItem();

            if (ItemStack.isSameItemSameComponents(stack, containerStack)) {

                return true;
            }
        }

        return false;
    }

    // Move Stack

    private static void moveStackToContainer(Minecraft client, AbstractContainerMenu menu, Slot source, List<Slot> targets) {

        if (source == null || !source.hasItem()) {

            return;
        }

        for (Slot target : targets) {

            if (!source.hasItem()) {
                return;
            }

            if (!target.hasItem()) {
                continue;
            }

            if (!target.mayPlace(source.getItem())) {
                continue;
            }

            ItemStack sourceStack = source.getItem();

            ItemStack targetStack = target.getItem();

            if (!ItemStack.isSameItemSameComponents(sourceStack, targetStack)) {
                continue;
            }

            if (targetStack.getCount() >= targetStack.getMaxStackSize()) {
                continue;
            }

            moveUsingPickup(client, menu, source, target);
        }

        for (Slot target : targets) {

            if (!source.hasItem()) {
                return;
            }

            if (target.hasItem()) {
                continue;
            }

            if (!target.mayPlace(source.getItem())) {
                continue;
            }

            moveUsingPickup(client, menu, source, target);
        }
    }

    private static void moveUsingPickup(Minecraft client, AbstractContainerMenu menu, Slot source, Slot target) {

        if (source == target) {
            return;
        }

        if (!source.hasItem()) {
            return;
        }

        if (!target.mayPlace(source.getItem())) {
            return;
        }

        click(client, menu, source);

        click(client, menu, target);

        click(client, menu, source);
    }

    // simulate click

    private static void click(Minecraft client, AbstractContainerMenu menu, Slot slot) {

        if (client == null || client.gameMode == null || client.player == null || menu == null || slot == null) {
            return;
        }

        client.gameMode.handleContainerInput(
                menu.containerId,
                slot.index,
                0,
                ContainerInput.PICKUP,
                client.player
        );
    }

    // menu validation

    private static boolean isValidMenu(Minecraft client, AbstractContainerMenu menu
    ) {

        if (client == null || client.player == null || client.gameMode == null || menu == null) {
            return false;
        }

        return client.player.containerMenu == menu;
    }

}