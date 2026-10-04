package com.aetheris.aetherutils.treecapitator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;


import java.util.Set;

public final class Treecapitator {

    public static final int OFF = 0;
    public static final int ON = 1;
    public static final int STRIP = 2;

    private static int mode = ON;

    /*
     * Evita que el Treecapitator se ejecute recursivamente
     * mientras destruye los demás bloques.
     */
    private static boolean cuttingTree = false;

    private Treecapitator() {
    }

    public static ItemStack findHoe(Player player) {

        /*
         * Primero buscamos en la mano secundaria.
         */
        ItemStack offhand = player.getOffhandItem();

        if (offhand.is(net.minecraft.tags.ItemTags.HOES)) {
            return offhand;
        }

        /*
         * Después buscamos en todo el inventario.
         */
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {

            ItemStack stack =
                    player.getInventory().getItem(i);

            if (stack.is(net.minecraft.tags.ItemTags.HOES)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    public static boolean breakNetherVegetation(
            Level level,
            Player player,
            Set<BlockPos> vegetation
    ) {

        if (vegetation.isEmpty()) {
            return false;
        }

        ItemStack hoe =
                findHoe(player);

        if (hoe.isEmpty()) {
            return false;
        }

        if (!player.isCreative()) {

            int durabilityRemaining =
                    hoe.getMaxDamage()
                            - hoe.getDamageValue();

            if (durabilityRemaining < vegetation.size()) {
                return false;
            }
        }

        for (BlockPos pos : vegetation) {

            BlockState state =
                    level.getBlockState(pos);

            if (!state.is(Blocks.SHROOMLIGHT)
                    && !state.is(Blocks.NETHER_WART_BLOCK)
                    && !state.is(Blocks.WARPED_WART_BLOCK)) {

                continue;
            }

            boolean destroyed =
                    level.destroyBlock(
                            pos,
                            true,
                            player
                    );

            if (destroyed && !player.isCreative()) {

                hoe.hurtAndBreak(
                        1,
                        player,
                        player.getUsedItemHand()
                );
            }
        }

        return true;
    }

    public static int getMode() {
        return mode;
    }

    public static void cycleMode() {

        mode++;

        if (mode > STRIP) {
            mode = OFF;
        }
    }

    public static boolean isEnabled() {
        return mode != OFF;
    }

    public static boolean isStripMode() {
        return mode == STRIP;
    }

    public static boolean isCuttingTree() {
        return cuttingTree;
    }

    /*
     * Compatibilidad con el sistema anterior.
     */
    public static void toggle() {

        if (mode == OFF) {
            mode = ON;
        } else {
            mode = OFF;
        }
    }

    public static boolean canUse(ItemStack tool) {
        return tool.is(ItemTags.AXES);
    }

    /**
     * Detecta un árbol válido.
     */
    public static Set<BlockPos> detectTree(
            Level level,
            BlockPos origin
    ) {

        BlockState originState =
                level.getBlockState(origin);

        if (!TreeDetector.isTreeTrunk(originState)) {
            return Set.of();
        }

        Set<BlockPos> logs =
                TreeDetector.findConnectedLogs(
                        level,
                        origin
                );

        if (logs.isEmpty()) {
            return Set.of();
        }

        /*
         * Árbol del Nether.
         */
        if (TreeDetector.isCrimsonStem(originState)
                || TreeDetector.isWarpedStem(originState)) {

            if (!TreeDetector.hasNetherGround(
                    level,
                    logs
            )) {
                return Set.of();
            }

            return logs;
        }

        /*
         * Árbol del Overworld.
         */
        if (!TreeDetector.hasNearbyLeaves(
                level,
                logs
        )) {
            return Set.of();
        }

        return logs;
    }

    /**
     * Treecapitator normal.
     *
     * Rompe todos los troncos conectados.
     */
    public static boolean cutTree(
            Level level,
            Player player,
            BlockPos origin,
            Set<BlockPos> logs
    ) {

        if (cuttingTree) {
            return false;
        }

        if (logs.isEmpty()) {
            return false;
        }

        ItemStack tool =
                player.getMainHandItem();

        if (!canUse(tool)) {
            return false;
        }

        /*
         * Comprobación de durabilidad.
         */
        if (!player.isCreative()) {

            int durabilityRemaining =
                    tool.getMaxDamage()
                            - tool.getDamageValue();

            if (durabilityRemaining < logs.size()) {
                return false;
            }
        }

        cuttingTree = true;

        try {
            boolean netherTree =
                    TreeDetector.isCrimsonStem(
                            level.getBlockState(origin)
                    )
                            || TreeDetector.isWarpedStem(
                            level.getBlockState(origin)
                    );

            if (netherTree) {

                boolean crimson =
                        TreeDetector.isCrimsonStem(
                                level.getBlockState(origin)
                        );

                Set<BlockPos> vegetation =
                        TreeDetector.findNetherVegetation(
                                level,
                                logs,
                                crimson
                        );

                breakNetherVegetation(
                        level,
                        player,
                        vegetation
                );
            }
            for (BlockPos log : logs) {

                /*
                 * El bloque original lo rompe Minecraft
                 * mediante el clic del jugador.
                 */
                if (log.equals(origin)) {
                    continue;
                }

                BlockState state =
                        level.getBlockState(log);

                if (!TreeDetector.isTreeTrunk(state)) {
                    continue;
                }

                boolean destroyed =
                        level.destroyBlock(
                                log,
                                true,
                                player
                        );

                if (destroyed && !player.isCreative()) {

                    tool.hurtAndBreak(
                            1,
                            player,
                            player.getUsedItemHand()
                    );
                }
            }

            return true;

        } finally {
            cuttingTree = false;
        }
    }

    /**
     * STRIP:
     *
     * 1. Convierte todos los troncos/stems.
     * 2. Después los rompe.
     */
    public static boolean stripAndCutTree(
            Level level,
            Player player,
            BlockPos origin,
            Set<BlockPos> logs
    ) {

        if (cuttingTree) return false;
        if (logs.isEmpty()) return false;

        ItemStack tool = player.getMainHandItem();

        if (!canUse(tool)) {
            return false;
        }

        int blocksToStrip = 0;

        for (BlockPos log : logs) {

            BlockState state =
                    level.getBlockState(log);

            if (!TreeDetector.isTreeTrunk(state)) {
                continue;
            }

            if (!TreeDetector.isStrippedStem(state)
                    && getStrippedState(state) != null) {

                blocksToStrip++;
            }
        }

        if (!player.isCreative()) {

            /*
             * Los bloques que ya estaban stripped solamente
             * necesitan ser destruidos.
             */
            int requiredUses =
                    blocksToStrip + logs.size();

            int durabilityRemaining =
                    tool.getMaxDamage()
                            - tool.getDamageValue();

            if (durabilityRemaining < requiredUses) {
                return false;
            }
        }

        cuttingTree = true;

        try {

            /*
             * PRIMERA FASE:
             * quitar la corteza.
             */
            for (BlockPos log : logs) {

                BlockState state =
                        level.getBlockState(log);

                if (!TreeDetector.isTreeTrunk(state)) {
                    continue;
                }

                /*
                 * Si ya estaba stripped, no hacemos nada.
                 */
                if (TreeDetector.isStrippedStem(state)) {
                    continue;
                }

                BlockState stripped =
                        getStrippedState(state);

                if (stripped == null) {
                    continue;
                }

                level.setBlock(
                        log,
                        stripped,
                        3
                );

                if (!player.isCreative()) {

                    tool.hurtAndBreak(
                            1,
                            player,
                            player.getUsedItemHand()
                    );
                }
            }
            //segunda fase
            boolean netherTree =
                    TreeDetector.isCrimsonStem(
                            level.getBlockState(origin)
                    )
                            || TreeDetector.isWarpedStem(
                            level.getBlockState(origin)
                    );

            if (netherTree) {

                boolean crimson =
                        TreeDetector.isCrimsonStem(
                                level.getBlockState(origin)
                        );

                Set<BlockPos> vegetation =
                        TreeDetector.findNetherVegetation(
                                level,
                                logs,
                                crimson
                        );

                breakNetherVegetation(
                        level,
                        player,
                        vegetation
                );
            }
            /*
             * TERCERA FASE:
             * romper todos los troncos.
             */
            for (BlockPos log : logs) {

                BlockState state =
                        level.getBlockState(log);

                if (!TreeDetector.isTreeTrunk(state)) {
                    continue;
                }

                boolean destroyed =
                        level.destroyBlock(
                                log,
                                true,
                                player
                        );

                if (destroyed && !player.isCreative()) {

                    tool.hurtAndBreak(
                            1,
                            player,
                            player.getUsedItemHand()
                    );
                }
            }

            return true;

        } finally {
            cuttingTree = false;
        }
    }

    /**
     * Obtiene la variante stripped del bloque.
     *
     * No utilizamos ResourceLocation para evitar
     * problemas de mappings.
     */
    private static BlockState getStrippedState(
            BlockState state
    ) {

        var block = state.getBlock();

        var strippedBlock =
                (net.minecraft.world.level.block.Block) null;

        if (block == Blocks.OAK_LOG) {
            strippedBlock = Blocks.STRIPPED_OAK_LOG;

        } else if (block == Blocks.SPRUCE_LOG) {
            strippedBlock = Blocks.STRIPPED_SPRUCE_LOG;

        } else if (block == Blocks.BIRCH_LOG) {
            strippedBlock = Blocks.STRIPPED_BIRCH_LOG;

        } else if (block == Blocks.JUNGLE_LOG) {
            strippedBlock = Blocks.STRIPPED_JUNGLE_LOG;

        } else if (block == Blocks.ACACIA_LOG) {
            strippedBlock = Blocks.STRIPPED_ACACIA_LOG;

        } else if (block == Blocks.DARK_OAK_LOG) {
            strippedBlock = Blocks.STRIPPED_DARK_OAK_LOG;

        } else if (block == Blocks.MANGROVE_LOG) {
            strippedBlock = Blocks.STRIPPED_MANGROVE_LOG;

        } else if (block == Blocks.CHERRY_LOG) {
            strippedBlock = Blocks.STRIPPED_CHERRY_LOG;

        } else if (block == Blocks.PALE_OAK_LOG) {
            strippedBlock = Blocks.STRIPPED_PALE_OAK_LOG;

        } else if (block == Blocks.CRIMSON_STEM) {
            strippedBlock = Blocks.STRIPPED_CRIMSON_STEM;

        } else if (block == Blocks.WARPED_STEM) {
            strippedBlock = Blocks.STRIPPED_WARPED_STEM;
        }

        /*
         * Si ya está stripped, devolvemos su propio estado.
         * Esto permite que STRIP pueda romperlo después.
         */
        if (block == Blocks.STRIPPED_OAK_LOG
                || block == Blocks.STRIPPED_SPRUCE_LOG
                || block == Blocks.STRIPPED_BIRCH_LOG
                || block == Blocks.STRIPPED_JUNGLE_LOG
                || block == Blocks.STRIPPED_ACACIA_LOG
                || block == Blocks.STRIPPED_DARK_OAK_LOG
                || block == Blocks.STRIPPED_MANGROVE_LOG
                || block == Blocks.STRIPPED_CHERRY_LOG
                || block == Blocks.STRIPPED_PALE_OAK_LOG
                || block == Blocks.STRIPPED_CRIMSON_STEM
                || block == Blocks.STRIPPED_WARPED_STEM) {

            return state;
        }

        if (strippedBlock == null) {
            return null;
        }

        BlockState strippedState =
                strippedBlock.defaultBlockState();

        if (state.hasProperty(RotatedPillarBlock.AXIS)
                && strippedState.hasProperty(
                RotatedPillarBlock.AXIS
        )) {

            Direction.Axis axis =
                    state.getValue(
                            RotatedPillarBlock.AXIS
                    );

            strippedState =
                    strippedState.setValue(
                            RotatedPillarBlock.AXIS,
                            axis
                    );
        }

        return strippedState;
    }
}