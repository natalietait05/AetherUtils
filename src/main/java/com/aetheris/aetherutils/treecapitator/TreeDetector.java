package com.aetheris.aetherutils.treecapitator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public final class TreeDetector {

    private static final int MAX_LOGS = 256;
    private static final int MAX_NETHER_BLOCKS = 512;
    private static final int LEAF_SEARCH_RADIUS = 2;

    private TreeDetector() {
    }

    public static Set<BlockPos> findConnectedLogs(Level level, BlockPos origin) {

        Set<BlockPos> logs = new HashSet<>();
        Set<BlockPos> visited = new HashSet<>();

        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        ArrayDeque<Integer> distances = new ArrayDeque<>();

        BlockState originState = level.getBlockState(origin);

        if (!isTreeTrunk(originState)) {
            return logs;
        }

        queue.add(origin);
        distances.add(0);
        visited.add(origin);
        logs.add(origin);

        while (!queue.isEmpty() && logs.size() < MAX_LOGS) {

            BlockPos current = queue.removeFirst();

            int distance = distances.removeFirst();

            for (BlockPos neighbor : getNeighbors(current)) {

                if (visited.contains(neighbor)) {
                    continue;
                }

                BlockState state = level.getBlockState(neighbor);

                if (isTreeTrunk(state)) {

                    if (sameTreeType(originState, state)) {

                        visited.add(neighbor);
                        logs.add(neighbor);

                        queue.addLast(neighbor);
                        distances.addLast(distance + 1);

                        if (logs.size() >= MAX_LOGS) {
                            break;
                        }
                    }

                    continue;
                }

                if (state.is(net.minecraft.tags.BlockTags.LEAVES)) {

                    if (distance >= 3) {
                        continue;
                    }

                    visited.add(neighbor);

                    queue.addLast(neighbor);
                    distances.addLast(distance + 1);
                }
            }
        }

        return logs;
    }

    public static boolean isTreeTrunk(BlockState state) {

        return state.is(net.minecraft.tags.BlockTags.LOGS)
                || state.is(Blocks.CRIMSON_STEM)
                || state.is(Blocks.WARPED_STEM)
                || state.is(Blocks.STRIPPED_CRIMSON_STEM)
                || state.is(Blocks.STRIPPED_WARPED_STEM)
                || isMangroveRoot(state);
    }

    public static boolean isMangroveRoot(BlockState state) {
        return state.is(Blocks.MANGROVE_ROOTS);
               // || state.is(Blocks.MUDDY_MANGROVE_ROOTS);

    }

    public static boolean sameTreeType(BlockState origin, BlockState other
    ) {

        if (isCrimsonStem(origin)) {
            return isCrimsonStem(other);
        }

        if (isWarpedStem(origin)) {
            return isWarpedStem(other);
        }

        if (origin.is(Blocks.MANGROVE_LOG) || origin.is(Blocks.STRIPPED_MANGROVE_LOG) || isMangroveRoot(origin)) {

            return other.is(Blocks.MANGROVE_LOG) || other.is(Blocks.STRIPPED_MANGROVE_LOG) || isMangroveRoot(other);
        }

        if (origin.is(net.minecraft.tags.BlockTags.LOGS)) {
            return other.is(net.minecraft.tags.BlockTags.LOGS) && other.getBlock() == origin.getBlock();
        }

        return false;
    }

    public static boolean isCrimsonStem(BlockState state) {

        return state.is(Blocks.CRIMSON_STEM) || state.is(Blocks.STRIPPED_CRIMSON_STEM);
    }

    public static boolean isWarpedStem(BlockState state) {

        return state.is(Blocks.WARPED_STEM) || state.is(Blocks.STRIPPED_WARPED_STEM);
    }

    public static boolean isStrippedStem(BlockState state) {

        return state.is(Blocks.STRIPPED_CRIMSON_STEM) || state.is(Blocks.STRIPPED_WARPED_STEM);
    }

    public static boolean hasNearbyLeaves(Level level, Set<BlockPos> logs) {

        for (BlockPos log : logs) {

            for (int x = -LEAF_SEARCH_RADIUS;
                 x <= LEAF_SEARCH_RADIUS;
                 x++) {

                for (int y = -LEAF_SEARCH_RADIUS;
                     y <= LEAF_SEARCH_RADIUS;
                     y++) {

                    for (int z = -LEAF_SEARCH_RADIUS;
                         z <= LEAF_SEARCH_RADIUS;
                         z++) {

                        BlockPos pos = log.offset(x, y, z);

                        if (level.getBlockState(pos).is(net.minecraft.tags.BlockTags.LEAVES)) {

                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public static boolean hasNetherGround(Level level, Set<BlockPos> stems) {

        for (BlockPos stem : stems) {

            BlockState below = level.getBlockState(stem.below());

            if (below.is(Blocks.NETHERRACK)) {
                return true;
            }

            if (isCrimsonStem(level.getBlockState(stem)) && below.is(Blocks.CRIMSON_NYLIUM)) {

                return true;
            }

            if (isWarpedStem(level.getBlockState(stem)) && below.is(Blocks.WARPED_NYLIUM)) {

                return true;
            }
        }

        return false;
    }

    public static Set<BlockPos> findNetherVegetation(Level level, Set<BlockPos> stems, boolean crimson) {

        Set<BlockPos> vegetation = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();

        for (BlockPos stem : stems) {

            for (BlockPos neighbor : getNeighbors(stem)) {

                if (isValidNetherVegetation(level.getBlockState(neighbor), crimson)) {

                    if (vegetation.add(neighbor)) {
                        queue.addLast(neighbor);
                    }
                }
            }
        }

        while (!queue.isEmpty() && vegetation.size() < MAX_NETHER_BLOCKS) {

            BlockPos current = queue.removeFirst();

            for (BlockPos neighbor : getNeighbors(current)) {

                if (vegetation.contains(neighbor)) {
                    continue;
                }

                BlockState state = level.getBlockState(neighbor);

                if (!isValidNetherVegetation(state, crimson)) {
                    continue;
                }

                vegetation.add(neighbor);
                queue.addLast(neighbor);

                if (vegetation.size() >= MAX_NETHER_BLOCKS) {

                    break;
                }
            }
        }

        return vegetation;
    }

    private static boolean isValidNetherVegetation(BlockState state, boolean crimson) {

        if (state.is(Blocks.SHROOMLIGHT)) {
            return true;
        }

        if (crimson) {
            return state.is(Blocks.NETHER_WART_BLOCK);
        }

        return state.is(Blocks.WARPED_WART_BLOCK);
    }

    private static BlockPos[] getNeighbors(BlockPos pos) {

        return new BlockPos[]{
                pos.above(),
                pos.below(),
                pos.north(),
                pos.south(),
                pos.east(),
                pos.west()
        };
    }
}