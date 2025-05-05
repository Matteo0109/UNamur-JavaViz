package be.unamur.java_visualizer.plugin;

import com.intellij.ide.util.PropertiesComponent;

public class PluginSettings {
    private static final String SORT_MODE_KEY = "java_visualizer.sort_mode";
    private static String typeMode = "Simplifié";


    public static SortMode getSortMode() {
        String modeStr = PropertiesComponent.getInstance().getValue(SORT_MODE_KEY, "TOPDOWN");
        return SortMode.valueOf(modeStr);
    }

    public static void setSortMode(SortMode mode) {
        PropertiesComponent.getInstance().setValue(SORT_MODE_KEY, mode.name());
        JavaVisualizerManager.getInstance().forceRefreshVisualizer();
    }


    public static String getTypeMode() {
        return typeMode;
    }

    public static void setTypeMode(String mode) {
        typeMode = mode;
        PropertiesComponent.getInstance().setValue("java_visualizer.type_mode", mode);
    }

    public static String simplifyTypeName(String typeName) {
        // Simplification du nom du type
        if (typeName.contains(".")) {
            String[] parts = typeName.split("\\.");
            typeName = parts[parts.length - 1];
        }
        return typeName;
    }

}
