package com.itzmark.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.itzmark.client.Module.Category;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.option.SimpleOption;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ModuleManager {
    public static final List<Module> ALL = new ArrayList<>();

    public static Module FPS, CPS, KEYS, COORDS, ZOOM, SKIN, CAPE;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;
    private static boolean loading;

    private ModuleManager() {}

    public static void init() {
        configPath = FabricLoader.getInstance().getConfigDir().resolve("itzmark-client.json");

        // ---------- HUD ----------
        FPS = add(new Module("FPS Counter", "Shows your current frames per second", Category.HUD));
        CPS = add(new Module("CPS Counter", "Shows left / right clicks per second", Category.HUD));
        KEYS = add(new Module("Keystrokes", "WASD, space and mouse buttons on screen", Category.HUD));
        COORDS = add(new Module("Coordinates", "Shows your XYZ position", Category.HUD));

        // ---------- FPS BOOST ----------
        // Each toggle applies a lighter setting; turning it off restores the vanilla default.
        List<Module> perf = new ArrayList<>();
        Module master = add(new Module("FPS Boost (All)", "Turns every FPS boost option on or off",
                Category.PERFORMANCE,
                () -> perf.forEach(m -> m.setEnabled(true)),
                () -> perf.forEach(m -> m.setEnabled(false))));
        perf.add(add(opt("Fast Graphics", "Fast instead of Fancy graphics",
                GameOptions::getGraphicsMode, GraphicsMode.FAST, GraphicsMode.FANCY)));
        perf.add(add(opt("Minimal Particles", "Fewer particles",
                GameOptions::getParticles, ParticlesMode.MINIMAL, ParticlesMode.ALL)));
        perf.add(add(opt("No Clouds", "Disables cloud rendering",
                GameOptions::getCloudRenderMode, CloudRenderMode.OFF, CloudRenderMode.FANCY)));
        perf.add(add(opt("No Entity Shadows", "Disables entity shadows",
                GameOptions::getEntityShadows, false, true)));
        perf.add(add(opt("Flat Lighting", "Turns smooth lighting off",
                GameOptions::getAo, false, true)));
        perf.add(add(opt("Render Distance 8", "Lowers render distance (default 12 when off)",
                GameOptions::getViewDistance, 8, 12)));
        perf.add(add(opt("Simulation Distance 6", "Lowers simulation distance (default 12 when off)",
                GameOptions::getSimulationDistance, 6, 12)));
        perf.add(add(opt("No Biome Blend", "Disables biome color blending",
                GameOptions::getBiomeBlendRadius, 0, 2)));

        // ---------- PLAYER ----------
        ZOOM = add(new Module("Zoom", "Hold C to zoom in", Category.PLAYER));
        SKIN = add(new Module("Skin Changer", "Uses .minecraft/itzmark/skin.png (client-side, only you see it)", Category.PLAYER));
        CAPE = add(new Module("Custom Cape", "Uses .minecraft/itzmark/cape.png (client-side, only you see it)", Category.PLAYER));

        load();
    }

    private static Module add(Module m) {
        ALL.add(m);
        return m;
    }

    private static <T> Module opt(String name, String desc, Function<GameOptions, SimpleOption<T>> getter, T on, T off) {
        return new Module(name, desc, Category.PERFORMANCE,
                () -> getter.apply(MinecraftClient.getInstance().options).setValue(on),
                () -> getter.apply(MinecraftClient.getInstance().options).setValue(off));
    }

    public static void applyStartup() {
        for (Module m : ALL) m.reapply();
    }

    private static void load() {
        loading = true;
        try {
            if (Files.exists(configPath)) {
                Map<String, Boolean> map = GSON.fromJson(Files.readString(configPath),
                        new TypeToken<Map<String, Boolean>>() {}.getType());
                if (map != null) {
                    for (Module m : ALL) {
                        if (map.containsKey(m.name)) m.setEnabledQuiet(map.get(m.name));
                    }
                    SkinManager.slim = map.getOrDefault("_slimSkin", false);
                }
            }
        } catch (Exception e) {
            ItzMarkClient.LOG.warn("Could not read config", e);
        }
        loading = false;
    }

    public static void save() {
        if (loading || configPath == null) return;
        try {
            Map<String, Boolean> map = new LinkedHashMap<>();
            for (Module m : ALL) map.put(m.name, m.isEnabled());
            map.put("_slimSkin", SkinManager.slim);
            Files.writeString(configPath, GSON.toJson(map));
        } catch (Exception e) {
            ItzMarkClient.LOG.warn("Could not save config", e);
        }
    }
}
