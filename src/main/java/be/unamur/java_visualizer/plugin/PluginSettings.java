package be.unamur.java_visualizer.plugin;

import com.intellij.ide.util.PropertiesComponent;

public class PluginSettings {
    private static final String SORT_MODE_KEY = "java_visualizer.sort_mode";

    public static SortMode getSortMode() {
        String modeStr = PropertiesComponent.getInstance().getValue(SORT_MODE_KEY, "FIFO");
        return SortMode.valueOf(modeStr);
    }

    public static void setSortMode(SortMode mode) {
        PropertiesComponent.getInstance().setValue(SORT_MODE_KEY, mode.name());
    }
}
