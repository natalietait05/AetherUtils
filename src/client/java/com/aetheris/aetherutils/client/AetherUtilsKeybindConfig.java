package com.aetheris.aetherutils.client;

import com.aetheris.aetherutils.lightlevels.LightLevelsClient;
import com.aetheris.aetherutils.treecapitator.TreecapitatorClient;
import com.aetheris.aetherutils.zoom.ZoomClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AetherUtilsKeybindConfig {

    private static final String TREECAPITATOR_KEY = "treecapitator";
    private static final String LIGHT_LEVELS_KEY = "light_levels";
    private static final String ZOOM_KEY = "zoom";

    private AetherUtilsKeybindConfig() {
    }

    public static void load() {

        Path path = getConfigPath();

        if (!Files.exists(path)) {
            return;
        }

        Properties properties = new Properties();

        try (InputStream input = Files.newInputStream(path)) {

            properties.load(input);

            loadKey(properties, TREECAPITATOR_KEY, TreecapitatorClient.TOGGLE_KEY);

            loadKey(properties, LIGHT_LEVELS_KEY, LightLevelsClient.TOGGLE_KEY);

            loadKey(properties, ZOOM_KEY, ZoomClient.ZOOM_KEY);

        } catch (IOException e) {

            System.err.println("Could not load AetherUtils keybind configuration");

            e.printStackTrace();
        }
    }

    private static void loadKey(Properties properties, String property, KeyMapping keyMapping) {

        if (keyMapping == null) {
            return;
        }

        String value = properties.getProperty(property);

        if (value == null || value.isBlank()) {
            return;
        }

        try {

            InputConstants.Key key = InputConstants.getKey(value);

            keyMapping.setKey(key);

        } catch (Exception e) {

            System.err.println("Could not load keybind: " + property);
        }
    }

    public static void setTreecapitatorKey(InputConstants.Key key) {

        if (TreecapitatorClient.TOGGLE_KEY != null && key != null) {

            TreecapitatorClient.TOGGLE_KEY.setKey(key);
        }
    }

    public static void setLightLevelsKey(InputConstants.Key key) {

        if (LightLevelsClient.TOGGLE_KEY != null && key != null) {

            LightLevelsClient.TOGGLE_KEY.setKey(key);
        }
    }

    public static void setZoomKey(InputConstants.Key key) {

        if (ZoomClient.ZOOM_KEY != null && key != null) {

            ZoomClient.ZOOM_KEY.setKey(key);
        }
    }

    public static void save() {

        Path path = getConfigPath();

        Properties properties = new Properties();

        saveKey(properties, TREECAPITATOR_KEY, TreecapitatorClient.TOGGLE_KEY);

        saveKey(properties, LIGHT_LEVELS_KEY, LightLevelsClient.TOGGLE_KEY);

        saveKey(properties, ZOOM_KEY, ZoomClient.ZOOM_KEY);

        try {

            Files.createDirectories(path.getParent());

            try (OutputStream output = Files.newOutputStream(path)) {

                properties.store(output, "AetherUtils Keybind configuration");
            }

        } catch (IOException e) {

            System.err.println("Could not save AetherUtils keybind configuration");

            e.printStackTrace();
        }
    }

    private static void saveKey(Properties properties, String property, KeyMapping keyMapping) {

        if (keyMapping == null) {
            return;
        }

        properties.setProperty(property, keyMapping.saveString());
    }

    private static Path getConfigPath() {

        return FabricLoader.getInstance().getConfigDir().resolve("aetherutils-keybinds.properties");
    }
}