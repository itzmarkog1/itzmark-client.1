package com.itzmark.client;

public class Module {
    public enum Category {
        HUD("HUD"),
        PERFORMANCE("FPS Boost"),
        PLAYER("Player");

        public final String title;

        Category(String title) {
            this.title = title;
        }
    }

    public final String name;
    public final String description;
    public final Category category;
    private final Runnable onEnable;
    private final Runnable onDisable;
    private boolean enabled;

    public Module(String name, String description, Category category) {
        this(name, description, category, null, null);
    }

    public Module(String name, String description, Category category, Runnable onEnable, Runnable onDisable) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.onEnable = onEnable;
        this.onDisable = onDisable;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean value) {
        if (enabled == value) return;
        enabled = value;
        Runnable r = value ? onEnable : onDisable;
        if (r != null) r.run();
        ModuleManager.save();
    }

    /** Used when loading the config: sets the state without running callbacks. */
    void setEnabledQuiet(boolean value) {
        enabled = value;
    }

    /** Re-applies the enabled state (used once at startup). */
    void reapply() {
        if (enabled && onEnable != null) onEnable.run();
    }
}
