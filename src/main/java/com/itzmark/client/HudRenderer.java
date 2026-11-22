package com.itzmark.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

public final class HudRenderer {
    private static final int RED = 0xFFE10600;
    private static final int PRESSED = 0xDD8B0000;
    private static final int DARK = 0x99000000;
    private static final int WHITE = 0xFFFFFFFF;

    private static final Deque<Long> LEFT = new ArrayDeque<>();
    private static final Deque<Long> RIGHT = new ArrayDeque<>();
    private static boolean lastLeft, lastRight;

    private HudRenderer() {}

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;

        pollClicks(mc);

        int x = 6;
        int y = 6;
        if (ModuleManager.FPS.isEnabled()) {
            y = line(ctx, mc, "FPS: " + mc.getCurrentFps(), x, y);
        }
        if (ModuleManager.CPS.isEnabled()) {
            y = line(ctx, mc, "CPS: " + LEFT.size() + " | " + RIGHT.size(), x, y);
        }
        if (ModuleManager.COORDS.isEnabled()) {
            y = line(ctx, mc, String.format("XYZ: %.0f %.0f %.0f",
                    mc.player.getX(), mc.player.getY(), mc.player.getZ()), x, y);
        }
        if (ModuleManager.KEYS.isEnabled()) {
            keystrokes(ctx, mc, x, y + 4);
        }
    }

    private static void pollClicks(MinecraftClient mc) {
        long window = mc.getWindow().getHandle();
        long now = System.currentTimeMillis();

        boolean l = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (l && !lastLeft) LEFT.add(now);
        if (r && !lastRight) RIGHT.add(now);
        lastLeft = l;
        lastRight = r;

        while (!LEFT.isEmpty() && now - LEFT.peekFirst() > 1000) LEFT.pollFirst();
        while (!RIGHT.isEmpty() && now - RIGHT.peekFirst() > 1000) RIGHT.pollFirst();
    }

    private static int line(DrawContext ctx, MinecraftClient mc, String text, int x, int y) {
        int w = mc.textRenderer.getWidth(text) + 10;
        ctx.fill(x, y, x + w, y + 14, DARK);
        ctx.fill(x, y, x + 2, y + 14, RED);
        ctx.drawTextWithShadow(mc.textRenderer, text, x + 6, y + 3, WHITE);
        return y + 16;
    }

    private static void keystrokes(DrawContext ctx, MinecraftClient mc, int bx, int by) {
        var o = mc.options;
        key(ctx, mc, bx + 24, by, 22, 22, "W", o.forwardKey.isPressed());
        key(ctx, mc, bx, by + 24, 22, 22, "A", o.leftKey.isPressed());
        key(ctx, mc, bx + 24, by + 24, 22, 22, "S", o.backKey.isPressed());
        key(ctx, mc, bx + 48, by + 24, 22, 22, "D", o.rightKey.isPressed());
        key(ctx, mc, bx, by + 48, 34, 22, "LMB", o.attackKey.isPressed());
        key(ctx, mc, bx + 36, by + 48, 34, 22, "RMB", o.useKey.isPressed());
        key(ctx, mc, bx, by + 72, 70, 14, "SPACE", o.jumpKey.isPressed());
    }

    private static void key(DrawContext ctx, MinecraftClient mc, int x, int y, int w, int h, String label, boolean down) {
        ctx.fill(x, y, x + w, y + h, down ? PRESSED : DARK);
        ctx.fill(x, y + h - 1, x + w, y + h, RED);
        int tx = x + (w - mc.textRenderer.getWidth(label)) / 2;
        int ty = y + (h - 8) / 2;
        ctx.drawTextWithShadow(mc.textRenderer, label, tx, ty, WHITE);
    }
}
