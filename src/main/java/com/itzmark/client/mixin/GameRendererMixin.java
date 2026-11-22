package com.itzmark.client.mixin;

import com.itzmark.client.ItzMarkClient;
import com.itzmark.client.ModuleManager;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    // require = 0: if Mojang changes this method again, zoom just stops working instead of crashing the game.
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true, require = 0)
    private void itzmark$zoom(CallbackInfoReturnable<Object> cir) {
        if (ModuleManager.ZOOM == null || !ModuleManager.ZOOM.isEnabled()) return;
        if (ItzMarkClient.zoomKey == null || !ItzMarkClient.zoomKey.isPressed()) return;

        Object value = cir.getReturnValue();
        if (value instanceof Float f) {
            cir.setReturnValue(f / 4.0f);
        } else if (value instanceof Double d) {
            cir.setReturnValue(d / 4.0);
        }
    }
}
