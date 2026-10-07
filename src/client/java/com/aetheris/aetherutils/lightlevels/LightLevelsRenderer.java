package com.aetheris.aetherutils.lightlevels;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.ArrayList;
import java.util.List;

public final class LightLevelsRenderer {

    public static final int MIN_RADIUS = 4;
    public static final int MAX_RADIUS = 64;

    private static int radius = 16;

    private static List<LightEntry> lightEntries = List.of();

    private LightLevelsRenderer() {
    }

    public static void setRadius(int newRadius) {

        radius = Math.clamp(newRadius, MIN_RADIUS, MAX_RADIUS);
    }

    public static void initialize() {

        LevelRenderEvents.COLLECT_SUBMITS.register(LightLevelsRenderer::render);
    }

    public static void update(Level level, BlockPos center, int mode) {

        if (mode == LightLevelsClient.OFF) {
            lightEntries = List.of();
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null) {
            lightEntries = List.of();
            return;
        }

        LightLayer lightLayer = mode == LightLevelsClient.BLOCK_LIGHT ? LightLayer.BLOCK : LightLayer.SKY;

        Vec3 cameraPos = player.getEyePosition();

        List<LightEntry> found = new ArrayList<>();

        int radiusSquared = radius * radius;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    if (x * x + y * y + z * z > radiusSquared) {
                        continue;
                    }

                    BlockPos pos = center.offset(x, y, z);

                    if (!level.isInWorldBounds(pos)) {
                        continue;
                    }

                    BlockState blockState = level.getBlockState(pos);

                    if (!isFullSolidBlock(level, pos, blockState)) {
                        continue;
                    }

                    BlockPos lightPos = pos.above();
                    BlockState aboveState = level.getBlockState(lightPos);

                    if (aboveState.getFluidState().is(net.minecraft.tags.FluidTags.WATER)) {
                        continue;
                    }

                    if (!aboveState.getCollisionShape(level, lightPos).isEmpty()) {
                        continue;
                    }

                    int lightLevel = level.getBrightness(lightLayer, lightPos);

                    if (mode == LightLevelsClient.BLOCK_LIGHT && lightLevel > 0){
                        continue;
                    }

                    if (!isVisible(level, cameraPos, pos)) {
                        continue;
                    }

                    found.add(new LightEntry(pos.immutable(), lightLevel));
                }
            }
        }
        lightEntries = List.copyOf(found);
    }

    private static boolean isFullSolidBlock(BlockGetter level, BlockPos pos, BlockState state) {

        if (state.isAir()) {
            return false;
        }

        if (state.is(BlockTags.LEAVES)) {
            return false;
        }

        if (state.propagatesSkylightDown()) {
            return false;
        }

        return state.isCollisionShapeFullBlock(level, pos);
    }


    private static boolean isVisible(Level level, Vec3 cameraPos, BlockPos target) {

        double x = target.getX();
        double y = target.getY();
        double z = target.getZ();

        double e = 0.02;

        double minX = x + e;
        double maxX = x + 1.0 - e;

        double minY = y + e;
        double maxY = y + 1.0 - e;

        double minZ = z + e;
        double maxZ = z + 1.0 - e;

        Vec3[] points = {

                // ABOVE
                new Vec3(x + 0.5, maxY, z + 0.5),
                new Vec3(minX, maxY, minZ),
                new Vec3(maxX, maxY, minZ),
                new Vec3(minX, maxY, maxZ),
                new Vec3(maxX, maxY, maxZ),

                // BELOW
                new Vec3(x + 0.5, minY, z + 0.5),
                new Vec3(minX, minY, minZ),
                new Vec3(maxX, minY, minZ),
                new Vec3(minX, minY, maxZ),
                new Vec3(maxX, minY, maxZ),

                // NORTH
                new Vec3(x + 0.5, y + 0.5, minZ),
                new Vec3(minX, minY, minZ),
                new Vec3(maxX, minY, minZ),
                new Vec3(minX, maxY, minZ),
                new Vec3(maxX, maxY, minZ),

                // SOUTH
                new Vec3(x + 0.5, y + 0.5, maxZ),
                new Vec3(minX, minY, maxZ),
                new Vec3(maxX, minY, maxZ),
                new Vec3(minX, maxY, maxZ),
                new Vec3(maxX, maxY, maxZ),

                // WEST
                new Vec3(minX, y + 0.5, z + 0.5),
                new Vec3(minX, minY, minZ),
                new Vec3(minX, minY, maxZ),
                new Vec3(minX, maxY, minZ),
                new Vec3(minX, maxY, maxZ),

                // EAST
                new Vec3(maxX, y + 0.5, z + 0.5),
                new Vec3(maxX, minY, minZ),
                new Vec3(maxX, minY, maxZ),
                new Vec3(maxX, maxY, minZ),
                new Vec3(maxX, maxY, maxZ)
        };

        for (Vec3 point : points) {

            BlockHitResult hit = level.clip(new ClipContext(cameraPos, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));

            if (hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(target)) {
                return true;
            }
        }

        return false;
    }

    private static void render(LevelRenderContext context) {

        if (lightEntries.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        Font font = minecraft.font;

        Vec3 camera = context.levelState().cameraRenderState.pos;

        SubmitNodeCollector queue = context.submitNodeCollector();

        PoseStack poseStack = new PoseStack();

        for (LightEntry entry : lightEntries) {

            BlockPos pos = entry.pos();

            poseStack.pushPose();

            // TEXT POSITION
            poseStack.translate(
                    pos.getX() + 0.5 - camera.x,
                    pos.getY() + 1.01 - camera.y,
                    pos.getZ() + 0.5 - camera.z
            );

            // TEXT ROTATION
            poseStack.rotate(Axis.XP.rotationDegrees(90));

            // TEXT SIZE
            poseStack.scale(
                    1.0f / 18.0f,
                    1.0f / 18.0f,
                    1.0f / 18.0f
            );

            String lightText = entry.light() == 0
                            ? "/"
                            : Integer.toString(entry.light());

            var text = net.minecraft.network.chat.Component.literal(lightText).getVisualOrderText();

            float width = font.width(text);

            queue.submitText(
                    poseStack,
                    -width / 2.0f,
                    -4.0f,
                    text,
                    true,
                    Font.DisplayMode.NORMAL,
                    0xF000F0,
                    0xFFFFFFFF,
                    0,
                    0
            );

            poseStack.popPose();
        }
    }
    private record LightEntry(BlockPos pos, int light) { }
}