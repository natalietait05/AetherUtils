package com.aetheris.aetherutils.lightlevels;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class LightLevelsConfig {

    private static final int DEFAULT_RADIUS = 16;

    private static int radius = DEFAULT_RADIUS;

    private LightLevelsConfig() {
    }

    public static int getRadius() {
        return radius;
    }

    public static void setRadius(int newRadius) {
        radius = Math.clamp(newRadius, LightLevelsRenderer.MIN_RADIUS, LightLevelsRenderer.MAX_RADIUS);
        LightLevelsRenderer.setRadius(radius);
    }

    public static void load() {

        Path configPath = getConfigPath();

        if (!Files.exists(configPath)) {
            setRadius(DEFAULT_RADIUS);
            return;
        }

        Properties properties = new Properties();

        try (InputStream input = Files.newInputStream(configPath)) {

            properties.load(input);

            int loadedRadius =
                    Integer.parseInt(properties.getProperty("radius", Integer.toString(DEFAULT_RADIUS)));

            setRadius(loadedRadius);

        } catch (IOException | NumberFormatException e) {

            setRadius(DEFAULT_RADIUS);
        }
    }

    public static void save() {

        Path configPath = getConfigPath();

        Properties properties = new Properties();

        properties.setProperty("radius", Integer.toString(radius));

        try {

            Files.createDirectories(configPath.getParent());

            try (OutputStream output = Files.newOutputStream(configPath)) {

                properties.store(output, "AetherUtils LightLevels configuration");
            }

        } catch (IOException e) {

            System.err.println("Could not save AetherUtils LightLevels configuration");
            e.printStackTrace();
        }
    }

    private static Path getConfigPath() {

        return Minecraft.getInstance()
                .gameDirectory
                .toPath()
                .resolve("config")
                .resolve("aetherutils-lightlevels.properties");
    }
}
