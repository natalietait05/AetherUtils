package com.aetheris.aetherutils.unbreakable;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class UnbreakableConfig {

    private static final boolean DEFAULT_ENABLED = true;

    private static boolean enabled = DEFAULT_ENABLED;

    private UnbreakableConfig() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static void load() {

        Path configPath = getConfigPath();

        if (!Files.exists(configPath)) {
            enabled = DEFAULT_ENABLED;
            return;
        }

        Properties properties = new Properties();

        try (InputStream input = Files.newInputStream(configPath)) {

            properties.load(input);

            enabled = Boolean.parseBoolean(
                    properties.getProperty(
                            "enabled",
                            Boolean.toString(DEFAULT_ENABLED)
                    )
            );

        } catch (IOException e) {

            enabled = DEFAULT_ENABLED;
        }
    }

    public static void save() {

        Path configPath = getConfigPath();

        Properties properties = new Properties();

        properties.setProperty(
                "enabled",
                Boolean.toString(enabled)
        );

        try {

            Files.createDirectories(configPath.getParent());

            try (OutputStream output = Files.newOutputStream(configPath)) {

                properties.store(
                        output,
                        "AetherUtils Unbreakable configuration"
                );
            }

        } catch (IOException e) {

            System.err.println(
                    "Could not save AetherUtils Unbreakable configuration"
            );

            e.printStackTrace();
        }
    }

    private static Path getConfigPath() {

        return FabricLoader.getInstance()
                .getConfigDir()
                .resolve("aetherutils-unbreakable.properties");
    }
}