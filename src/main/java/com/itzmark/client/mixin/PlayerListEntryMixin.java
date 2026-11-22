package com.itzmark.client.mixin;

import com.itzmark.client.ModuleManager;
import com.itzmark.client.SkinManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true, require = 0)
    private void itzmark$skin(CallbackInfoReturnable<SkinTextures> cir) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || ModuleManager.SKIN == null) return;

        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler == null || handler.getPlayerListEntry(mc.player.getUuid()) != (Object) this) return;

        boolean skin = ModuleManager.SKIN.isEnabled() && SkinManager.skinLoaded;
        boolean cape = ModuleManager.CAPE.isEnabled() && SkinManager.capeLoaded;
        if (!skin && !cape) return;

        SkinTextures orig = cir.getReturnValue();

        AssetInfo.TextureAsset body = skin
                ? new AssetInfo.TextureAssetInfo(SkinManager.SKIN_ID, SkinManager.SKIN_ID)
                : orig.body();
        AssetInfo.TextureAsset capeAsset = cape
                ? new AssetInfo.TextureAssetInfo(SkinManager.CAPE_ID, SkinManager.CAPE_ID)
                : orig.cape();
        AssetInfo.TextureAsset elytraAsset = cape ? capeAsset : orig.elytra();

        cir.setReturnValue(new SkinTextures(
                body,
                capeAsset,
                elytraAsset,
                skin ? itzmark$pick(orig.model(), SkinManager.slim ? "SLIM" : "WIDE") : orig.model(),
                orig.secure()));
    }

    /** Picks an enum constant by name without importing the (renamed-often) model enum. */
    @org.spongepowered.asm.mixin.Unique
    private static <E extends Enum<E>> E itzmark$pick(E sample, String name) {
        for (E c : sample.getDeclaringClass().getEnumConstants()) {
            if (c.name().equals(name)) return c;
        }
        return sample;
    }
}
