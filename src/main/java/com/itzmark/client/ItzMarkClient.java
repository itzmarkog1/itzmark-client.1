package com.itzmark.client;

import com.itzmark.client.gui.ClickGuiScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItzMarkClient implements ClientModInitializer {
    public static final String NAME = "itzMark Client";
    public static final Logger LOG = LoggerFactory.getLogger(NAME);

    public static KeyBinding guiKey;
    public static KeyBinding zoomKey;

    @Override
    public void onInitializeClient() {
        // 1.21.9+ requires a KeyBinding.Category object (registered once).
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("itzmark", "main"));

        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.itzmark.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.itzmark.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, category));

        ModuleManager.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guiKey.wasPressed()) {
                client.setScreen(new ClickGuiScreen());
            }
        });

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.of("itzmark", "hud"), HudRenderer::render);

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            SkinManager.reload();
            ModuleManager.applyStartup();
        });

        LOG.info("{} loaded. Press Right Shift in-game to open the menu.", NAME);
    }
}
