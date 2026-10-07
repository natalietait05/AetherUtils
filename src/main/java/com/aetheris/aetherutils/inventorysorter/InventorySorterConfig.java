package com.aetheris.aetherutils.inventorysorter;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class InventorySorterConfig {

    private static final InventorySortDirection DEFAULT_DIRECTION = InventorySortDirection.ASCENDING;

    private static final InventorySortPriority DEFAULT_PRIORITY = InventorySortPriority.NAME_THEN_TYPE;

    private static InventorySortDirection direction = DEFAULT_DIRECTION;

    private static InventorySortPriority priority = DEFAULT_PRIORITY;

    private InventorySorterConfig() {
    }

    public static InventorySortDirection getDirection() {
        return direction;
    }

    public static void setDirection(InventorySortDirection newDirection) {

        if (newDirection == null) {
            return;
        }

        direction = newDirection;
    }

    public static InventorySortPriority getPriority() {
        return priority;
    }

    public static void setPriority(InventorySortPriority newPriority) {

        if (newPriority == null) {
            return;
        }

        priority = newPriority;
    }

    public static void load() {

        Path path = getConfigPath();

        if (!Files.exists(path)) {

            direction = DEFAULT_DIRECTION;
            priority = DEFAULT_PRIORITY;

            return;
        }

        Properties properties = new Properties();

        try (InputStream input = Files.newInputStream(path)) {

            properties.load(input);

            try {

                direction = InventorySortDirection.valueOf(properties.getProperty("direction", DEFAULT_DIRECTION.name()));

            } catch (IllegalArgumentException e) {

                direction = DEFAULT_DIRECTION;
            }

            try {

                priority = InventorySortPriority.valueOf(properties.getProperty("priority", DEFAULT_PRIORITY.name()));

            } catch (IllegalArgumentException e) {

                priority = DEFAULT_PRIORITY;
            }

        } catch (IOException e) {

            direction = DEFAULT_DIRECTION;
            priority = DEFAULT_PRIORITY;

            System.err.println("Could not load AetherUtils Inventory Sorter configuration");

            e.printStackTrace();
        }
    }

    public static void save() {

        Path path = getConfigPath();

        Properties properties = new Properties();

        properties.setProperty("direction", direction.name());

        properties.setProperty("priority", priority.name());

        try {

            Files.createDirectories(path.getParent());

            try (OutputStream output = Files.newOutputStream(path)) {

                properties.store(output, "AetherUtils Inventory Sorter configuration");
            }

        } catch (IOException e) {

            System.err.println("Could not save AetherUtils Inventory Sorter configuration");

            e.printStackTrace();
        }
    }

    private static Path getConfigPath() {

        return FabricLoader.getInstance().getConfigDir().resolve("aetherutils-inventory-sorter.properties");
    }
}