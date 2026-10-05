package com.aetheris.aetherutils.zoom;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ZoomConfig {

    private static final double DEFAULT_ZOOM = 3.0;
    private static final boolean DEFAULT_SMOOTH = true;

    private static double zoom = DEFAULT_ZOOM;
    private static boolean smooth = DEFAULT_SMOOTH;

    private ZoomConfig() {
    }

    public static double getZoom() {
        return zoom;
    }

    public static void setZoom(double value) {
        zoom = Math.clamp(value, 2.0, 6.0);
    }

    public static boolean isSmooth() {
        return smooth;
    }

    public static void setSmooth(boolean value) {
        smooth = value;
    }

    public static void load() {
        Path configPath = getConfigPath();

        if (!Files.exists(configPath)) {
            zoom = DEFAULT_ZOOM;
            smooth = DEFAULT_SMOOTH;
            return;
        }

        Properties properties = new Properties();

        try (InputStream input = Files.newInputStream(configPath)) {
            properties.load(input);

            setZoom(
                    Double.parseDouble(
                            properties.getProperty(
                                    "com/aetheris/aetherutils/zoom",
                                    Double.toString(DEFAULT_ZOOM)
                            )
                    )
            );

            setSmooth(
                    Boolean.parseBoolean(
                            properties.getProperty(
                                    "smooth",
                                    Boolean.toString(DEFAULT_SMOOTH)
                            )
                    )
            );

        } catch (IOException | NumberFormatException e) {
            zoom = DEFAULT_ZOOM;
            smooth = DEFAULT_SMOOTH;
        }
    }

    public static void save() {
        Path configPath = getConfigPath();

        Properties properties = new Properties();

        properties.setProperty(
                "com/aetheris/aetherutils/zoom",
                Double.toString(zoom)
        );

        properties.setProperty(
                "smooth",
                Boolean.toString(smooth)
        );

        try {
            Files.createDirectories(configPath.getParent());

            try (OutputStream output = Files.newOutputStream(configPath)) {
                properties.store(
                        output,
                        "AetherUtils Zoom configuration"
                );
            }

        } catch (IOException e) {
            System.err.println(
                    "Could not save AetherUtils Zoom configuration"
            );

            e.printStackTrace();
        }
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance()
                .getConfigDir()
                .resolve("aetherutils-zoom.properties");
    }
}