package be.unamur.java_visualizer.plugin;

public enum SortMode {
    TOPDOWN,
    BOTTOMUP;

    public String toDisplayString() {
        switch (this) {
            case TOPDOWN:
                return "De haut en bas";
            case BOTTOMUP:
                return "De bas en haut";
            default:
                return this.name();
        }
    }
}
