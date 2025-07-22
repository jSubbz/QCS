package qcs.util;

import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * GateColorHelper manages user-customizable colors for each gate type.
 */
public class GateColorHelper {

    private static final Map<String, Color> gateColorMap = new HashMap<>();

    // Fallback colors for gates
    private static final Map<String, Color> defaultColors = Map.ofEntries(
            Map.entry("X", Color.web("#FFB9B8")),
            Map.entry("Y", Color.web("#D8FFC4")),
            Map.entry("Z", Color.web("#D8D4FF")),
            Map.entry("H", Color.web("#C4E9FF")),
            Map.entry("S", Color.web("#F7D4FF")),
            Map.entry("T", Color.web("#FFF0BA")),
            Map.entry("U", Color.web("#FFCBA6")),
            Map.entry("CX", Color.web("#FFDCD6")),
            Map.entry("CU", Color.web("#FF99FF")),
            Map.entry("CCX", Color.web("#E2CCFF")),
            Map.entry("SWAP", Color.web("#E3FFE8")),
            Map.entry("I", Color.web("#FFFFB0")),
            Map.entry("BARRIER", Color.web("#F0F1FF"))
    );

    /**
     * Sets a custom color for the given gate.
     * @param gateName the name of the gate (case-insensitive)
     * @param color the desired Color object
     */
    public static void setGateColor(String gateName, Color color) {
        if (gateName != null && color != null) {
            gateColorMap.put(gateName.toUpperCase(), color);
        }
    }

    /**
     * Retrieves the currently set color for a gate, or the default if none was set.
     * @param gateName the gate type
     * @return the appropriate color
     */
    public static Color getGateColor(String gateName) {
        if (gateName == null) return Color.GRAY;
        return gateColorMap.getOrDefault(gateName.toUpperCase(),
                defaultColors.getOrDefault(gateName.toUpperCase(), Color.LIGHTGRAY));
    }

    /**
     * Clears all custom user-defined gate colors.
     */
    public static void resetAllCustomColors() {
        gateColorMap.clear();
    }

    /**
     * Converts a Color object to its web hex string (e.g., #RRGGBB)
     * @param color Color to convert
     * @return hex string
     */
    public static String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}
