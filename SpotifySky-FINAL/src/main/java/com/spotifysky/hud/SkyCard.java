package com.spotifysky.hud;

import com.spotifysky.config.ModConfig;
import com.spotifysky.sky.SkyRenderer;
import com.spotifysky.sky.SkyStyle;
import com.spotifysky.spotify.Track;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class SkyCard {
    private static final long START = System.nanoTime();

    private SkyCard() {}

    public static float time(float speed) {
        return (float) (((System.nanoTime() - START) / 1_000_000_000.0) * speed % 7200.0);
    }

    public static void render(DrawContext ctx, TextRenderer tr, int x, int y,
                              Track track, float progress, boolean playing, ModConfig cfg) {
        int w = cfg.width();
        int h = cfg.height();
        SkyStyle style = cfg.sky;
        float t = time(cfg.animSpeed);

        // background sky
        SkyRenderer.draw(ctx, style, x, y, w, h, t, cfg.lowPower, cfg.opacity, cfg.veilStrength);

        // equalizer
        if (cfg.showEqualizer) {
            int bars = cfg.equalizerBars;
            int bottom = y + h - Math.max(8, h / 5);
            int barW = Math.max(2, w / 40);
            int gap = barW + 1;
            int totalW = bars * gap;
            int startX = x + 6;
            for (int i = 0; i < bars; i++) {
                float level = playing
                        ? 0.25f + 0.75f * Math.abs((float) Math.sin(t * (1.8f + i * 0.7f) + i * 1.4f)) * cfg.equalizerIntensity
                        : 0.1f;
                int bh = Math.max(2, Math.round((h * 0.45f) * level));
                int bx = startX + i * gap;
                ctx.fill(bx, bottom - bh, bx + barW, bottom, style.accent);
            }
        }

        // text
        int textX = x + (cfg.showEqualizer ? 8 + cfg.equalizerBars * 6 : 8);
        int maxW = w - (textX - x) - 6;
        boolean shadow = cfg.textShadow && style != SkyStyle.COMIC && style != SkyStyle.PAPER;

        String title = track.title == null || track.title.isEmpty() ? "Unknown" : track.title;
        String artist = track.artist == null || track.artist.isEmpty() ? "Spotify" : track.artist;

        int titleY = y + Math.max(5, h / 6);
        int artistY = titleY + tr.fontHeight + 1;

        ctx.drawText(tr, fit(tr, title, maxW), textX, titleY, style.text, shadow);
        ctx.drawText(tr, fit(tr, artist, maxW), textX, artistY, style.subText, false);

        // progress bar
        if (cfg.showProgress) {
            int barX = x + 6;
            int barLen = w - 12;
            int barY = y + h - 5;
            int bg = (style == SkyStyle.COMIC || style == SkyStyle.PAPER) ? 0x55000000 : 0x44FFFFFF;
            ctx.fill(barX, barY, barX + barLen, barY + 2, bg);

            if (progress >= 0f) {
                int filled = Math.round(barLen * Math.min(1f, progress));
                if (filled > 0) ctx.fill(barX, barY, barX + filled, barY + 2, style.accent);
            } else if (playing) {
                // indeterminate sliding segment
                int seg = Math.max(12, barLen / 6);
                int s = ((int) (t * 35f)) % (barLen + seg) - seg;
                int a = Math.max(0, s);
                int b = Math.min(barLen, s + seg);
                if (b > a) ctx.fill(barX + a, barY, barX + b, barY + 2, style.accent);
            }
        }
    }

    private static String fit(TextRenderer tr, String s, int maxW) {
        if (tr.getWidth(s) <= maxW) return s;
        return tr.trimToWidth(s, maxW - tr.getWidth("…")) + "…";
    }
}
