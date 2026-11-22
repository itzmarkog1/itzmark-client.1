package com.itzmark.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/** Loads skin.png and cape.png from .minecraft/itzmark/ (client-side only). */
public final class SkinManager {
    public static final Identifier SKIN_ID = Identifier.of("itzmark", "custom_skin");
    public static final Identifier CAPE_ID = Identifier.of("itzmark", "custom_cape");

    public static boolean slim;
    public static boolean skinLoaded;
    public static boolean capeLoaded;

    private SkinManager() {}

    public static Path dir() {
        Path p = FabricLoader.getInstance().getGameDir().resolve("itzmark");
        try {
            Files.createDirectories(p);
        } catch (IOException e) {
            ItzMarkClient.LOG.warn("Could not create itzmark folder", e);
        }
        return p;
    }

    public static void reload() {
        skinLoaded = load(dir().resolve("skin.png"), SKIN_ID);
        capeLoaded = load(dir().resolve("cape.png"), CAPE_ID);
    }

    public static void openFolder() {
        Util.getOperatingSystem().open(dir().toFile());
    }

    private static boolean load(Path file, Identifier id) {
        if (!Files.exists(file)) return false;
        try (InputStream in = Files.newInputStream(file)) {
            NativeImage image = NativeImage.read(in);
            MinecraftClient.getInstance().getTextureManager()
                    .registerTexture(id, new NativeImageBackedTexture(() -> "itzmark/" + id.getPath(), image));
            return true;
        } catch (IOException e) {
            ItzMarkClient.LOG.warn("Could not load {}", file, e);
            return false;
        }
    }
}
