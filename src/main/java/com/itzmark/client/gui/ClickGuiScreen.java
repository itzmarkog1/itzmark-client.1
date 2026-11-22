package com.itzmark.client.gui;

import com.itzmark.client.Module;
import com.itzmark.client.ModuleManager;
import com.itzmark.client.SkinManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Dark red / black mod menu. Open with Right Shift.
 * Input is read straight from GLFW inside render() so the screen does not depend on
 * Minecraft's input event classes (which changed in 1.21.9).
 */
public class ClickGuiScreen extends Screen {
    private static final int BG = 0xCC050000;
    private static final int PANEL = 0xF0101010;
    private static final int HEADER = 0xFF7A0010;
    private static final int RED = 0xFFE10600;
    private static final int ROW_ON = 0xFF6E0010;
    private static final int ROW_HOVER = 0xFF2A0A0A;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFAAAAAA;

    private static final int PW = 130, HH = 22, RH = 18, GAP = 8, TOP = 56;

    private record Row(Supplier<String> label, BooleanSupplier on, Runnable click, String tip) {}

    private record Panel(String title, List<Row> rows) {}

    private final List<Panel> panels = new ArrayList<>();
    private boolean lastMouse = true;
    private boolean lastShift = true;

    public ClickGuiScreen() {
        super(Text.literal("itzMark Client"));
    }

    @Override
    protected void init() {
        panels.clear();
        for (Module.Category c : Module.Category.values()) {
            List<Row> rows = new ArrayList<>();
            for (Module m : ModuleManager.ALL) {
                if (m.category == c) {
                    rows.add(new Row(() -> m.name, m::isEnabled, m::toggle, m.description));
                }
            }
            panels.add(new Panel(c.title, rows));
        }
        panels.add(new Panel("Skins", List.of(
                new Row(() -> "Reload skin / cape", () -> false, SkinManager::reload,
                        "Re-reads skin.png and cape.png from the itzmark folder"),
                new Row(() -> "Arms: " + (SkinManager.slim ? "Slim" : "Wide"), () -> SkinManager.slim,
                        () -> { SkinManager.slim = !SkinManager.slim; ModuleManager.save(); },
                        "Arm style for your custom skin"),
                new Row(() -> "Open itzmark folder", () -> false, SkinManager::openFolder,
                        "Put skin.png (64x64) and cape.png (64x32) here"))));

        // Treat whatever is held right now as "already pressed" so opening the menu never counts as a click.
        long win = client.getWindow().getHandle();
        lastMouse = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        lastShift = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private int panelX(int i) {
        int total = panels.size() * PW + (panels.size() - 1) * GAP;
        return (width - total) / 2 + i * (PW + GAP);
    }

    private void handleClick(int mx, int my) {
        for (int i = 0; i < panels.size(); i++) {
            Panel p = panels.get(i);
            int x = panelX(i);
            for (int r = 0; r < p.rows().size(); r++) {
                int ry = TOP + HH + 2 + r * RH;
                if (mx >= x && mx < x + PW && my >= ry && my < ry + RH) {
                    p.rows().get(r).click().run();
                    return;
                }
            }
        }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        long win = client.getWindow().getHandle();

        boolean mouse = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (mouse && !lastMouse) handleClick(mx, my);
        lastMouse = mouse;

        boolean shift = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (shift && !lastShift) {
            close();
            return;
        }
        lastShift = shift;

        ctx.fill(0, 0, width, height, BG);
        ctx.fill(0, 0, width, 3, RED);

        // Title
        Text a = Text.literal("itzMark").formatted(Formatting.BOLD);
        Text b = Text.literal(" CLIENT").formatted(Formatting.BOLD);
        int tw = textRenderer.getWidth(a) + textRenderer.getWidth(b);
        int tx = (width - tw) / 2;
        ctx.drawTextWithShadow(textRenderer, a, tx, 28, RED);
        ctx.drawTextWithShadow(textRenderer, b, tx + textRenderer.getWidth(a), 28, WHITE);

        String tip = null;
        for (int i = 0; i < panels.size(); i++) {
            Panel p = panels.get(i);
            int x = panelX(i);
            int h = HH + p.rows().size() * RH + 4;

            ctx.fill(x - 1, TOP - 1, x + PW + 1, TOP + h + 1, RED);
            ctx.fill(x, TOP, x + PW, TOP + h, PANEL);
            ctx.fill(x, TOP, x + PW, TOP + HH, HEADER);
            ctx.drawTextWithShadow(textRenderer, p.title().toUpperCase(), x + 8, TOP + 7, WHITE);

            for (int r = 0; r < p.rows().size(); r++) {
                Row row = p.rows().get(r);
                int ry = TOP + HH + 2 + r * RH;
                boolean hover = mx >= x && mx < x + PW && my >= ry && my < ry + RH;
                boolean on = row.on().getAsBoolean();

                if (on) {
                    ctx.fill(x + 2, ry, x + PW - 2, ry + RH - 1, ROW_ON);
                    ctx.fill(x + 2, ry, x + 4, ry + RH - 1, RED);
                } else if (hover) {
                    ctx.fill(x + 2, ry, x + PW - 2, ry + RH - 1, ROW_HOVER);
                }
                ctx.drawTextWithShadow(textRenderer, row.label().get(), x + 9, ry + 5, on ? WHITE : GRAY);
                if (hover) tip = row.tip();
            }
        }

        if (tip != null) {
            int w = textRenderer.getWidth(tip);
            ctx.drawTextWithShadow(textRenderer, tip, (width - w) / 2, height - 34, GRAY);
        }
        String hint = "Right Shift / Esc: close   |   C: zoom";
        ctx.drawTextWithShadow(textRenderer, hint, (width - textRenderer.getWidth(hint)) / 2, height - 18, RED);
    }
}
