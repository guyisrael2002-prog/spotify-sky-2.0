package com.spotifysky.hud;

import com.spotifysky.SpotifySkyClient;
import com.spotifysky.config.ModConfig;
import com.spotifysky.gui.SpotifySkyScreen;
import com.spotifysky.spotify.SpotifyState;
import com.spotifysky.spotify.Track;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

public final class SkyHud {
    private SkyHud() {}

    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of(SpotifySkyClient.MOD_ID, "now_playing"),
                SkyHud::render);
    }

    private static void render(DrawContext ctx, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ModConfig cfg = SpotifyState.config();
        if (!cfg.hudEnabled || !SpotifyState.isPlaying()) return;
        if (mc.currentScreen instanceof SpotifySkyScreen) return;

        Track t = SpotifyState.current();
        if (t == null) return;

        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        int w = cfg.width();
        int h = cfg.height();

        boolean left = cfg.corner == ModConfig.Corner.TOP_LEFT || cfg.corner == ModConfig.Corner.BOTTOM_LEFT;
        boolean top  = cfg.corner == ModConfig.Corner.TOP_LEFT || cfg.corner == ModConfig.Corner.TOP_RIGHT;

        int x = left ? cfg.offsetX : sw - w - cfg.offsetX;
        int y = top  ? cfg.offsetY : sh - h - cfg.offsetY;

        // slight extra room under the top-right status icons
        if (cfg.corner == ModConfig.Corner.TOP_RIGHT) y += 22;

        SkyCard.render(ctx, mc.textRenderer, x, y, t, SpotifyState.progress(), true, cfg);
    }
}
