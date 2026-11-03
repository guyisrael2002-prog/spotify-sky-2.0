package com.spotifysky.sky;

import net.minecraft.client.gui.DrawContext;

import java.util.Random;

/**
 * Procedural animated skies. Pure GUI fills/gradients only.
 * Low-power mode dramatically reduces draw calls.
 */
public final class SkyRenderer {
    private static final int STARS = 64;
    private static final float[] SX = new float[STARS];
    private static final float[] SY = new float[STARS];
    private static final float[] SPH = new float[STARS];
    private static final float[] SSP = new float[STARS];

    static {
        Random r = new Random(7331L);
        for (int i = 0; i < STARS; i++) {
            SX[i] = r.nextFloat();
            SY[i] = r.nextFloat();
            SPH[i] = r.nextFloat() * 6.2831f;
            SSP[i] = 0.7f + r.nextFloat() * 2.4f;
        }
    }

    private static DrawContext ctx;
    private static int ox, oy, w, h;
    private static float alpha;

    private SkyRenderer() {}

    public static void draw(DrawContext context, SkyStyle style, int x, int y, int width, int height,
                            float t, boolean low, float opacity, float veilMul) {
        ctx = context;
        ox = x; oy = y; w = width; h = height; alpha = opacity;

        switch (style) {
            case GALAXY   -> galaxy(t, low);
            case AURORA   -> aurora(t, low);
            case COMIC    -> comic(t, low);
            case SUNSET   -> sunset(t, low);
            case MIDNIGHT -> midnight(t, low);
            case NEBULA   -> nebula(t, low);
            case CYBER    -> cyber(t, low);
            case PAPER    -> paper(t, low);
            case OCEAN    -> ocean(t, low);
            case FOREST   -> forest(t, low);
        }

        // readability veil
        if (style.veil != 0 && veilMul > 0.01f) {
            int a = (int) (((style.veil >>> 24) & 0xFF) * veilMul * opacity);
            int c = (a << 24) | (style.veil & 0xFFFFFF);
            ctx.fill(x, y, x + width, y + height, c);
        }
        ctx = null;
    }

    // ------------------------------------------------------------------ styles

    private static void galaxy(float t, boolean low) {
        gradient(0, 0, w, h, 0xFF07041A, 0xFF1A0B3A);
        int blobs = low ? 1 : 3;
        for (int b = 0; b < blobs; b++) {
            float cx = w * (0.25f + b * 0.28f) + sin(t * 0.18f + b * 1.7f) * 8f;
            float cy = h * (0.35f + (b % 2) * 0.3f) + cos(t * 0.14f + b) * 4f;
            int rgb = switch (b % 3) { case 0 -> 0xB02FD0; case 1 -> 0x2F7BE0; default -> 0xE040A0; };
            int layers = low ? 2 : 4;
            for (int l = 0; l < layers; l++) {
                float s = 1f - l * 0.22f;
                ellipse(cx, cy, w * 0.32f * s, h * 0.38f * s, ((18 + l * 12) << 24) | rgb, low ? 4 : 2);
            }
        }
        stars(t, low ? 18 : STARS, 0xFFFFFF);
        shootingStar(t);
    }

    private static void aurora(float t, boolean low) {
        gradient(0, 0, w, h, 0xFF041018, 0xFF0A2A28);
        int bands = low ? 2 : 4;
        for (int i = 0; i < bands; i++) {
            float phase = t * 0.35f + i * 1.3f;
            int y0 = (int) (h * (0.15f + i * 0.18f + 0.04f * sin(phase)));
            int y1 = y0 + (int) (h * 0.22f);
            int col = switch (i % 3) {
                case 0 -> 0xFF4DFFB0;
                case 1 -> 0xFF40C4FF;
                default -> 0xFFB388FF;
            };
            for (int yy = y0; yy < y1; yy += low ? 3 : 2) {
                float f = (yy - y0) / (float) (y1 - y0);
                int a = (int) (40 + 90 * (1f - Math.abs(f - 0.5f) * 2f));
                int wave = (int) (sin(phase + yy * 0.08f) * w * 0.08f);
                fill(wave, yy, w + wave, yy + (low ? 3 : 2), (a << 24) | (col & 0xFFFFFF));
            }
        }
        stars(t, low ? 12 : 28, 0xE0FFFF);
    }

    private static void comic(float t, boolean low) {
        gradient(0, 0, w, h, 0xFFFFF8E1, 0xFFFFECB3);
        // halftone dots
        int step = low ? 8 : 5;
        for (int yy = 0; yy < h; yy += step) {
            for (int xx = (yy / step) % 2 == 0 ? 0 : step / 2; xx < w; xx += step) {
                fill(xx, yy, xx + 2, yy + 2, 0x33E8344E);
            }
        }
        // speed lines
        if (!low) {
            for (int i = 0; i < 5; i++) {
                float p = (t * 1.8f + i * 0.4f) % 1.4f - 0.2f;
                int lx = (int) (w * p);
                fill(lx, 0, lx + 3, h, 0x28E8344E);
            }
        }
    }

    private static void sunset(float t, boolean low) {
        gradient(0, 0, w, h, 0xFFFF8A65, 0xFF4A148C);
        int sunX = w / 2 + (int) (sin(t * 0.1f) * 6);
        int sunY = (int) (h * 0.55f);
        int layers = low ? 2 : 4;
        for (int l = 0; l < layers; l++) {
            float s = 1f - l * 0.25f;
            ellipse(sunX, sunY, w * 0.22f * s, h * 0.28f * s, ((30 + l * 20) << 24) | 0xFFD54A, low ? 3 : 2);
        }
        // soft clouds
        if (!low) {
            for (int i = 0; i < 3; i++) {
                float cx = w * (0.2f + i * 0.3f) + sin(t * 0.12f + i) * 10;
                ellipse(cx, h * 0.3f, w * 0.18f, h * 0.12f, 0x33FFFFFF, 3);
            }
        }
    }

    private static void midnight(float t, boolean low) {
        gradient(0, 0, w, h, 0xFF02040A, 0xFF0A1020);
        stars(t, low ? 20 : STARS, 0xC0D8FF);
        // faint nebula wash
        if (!low) {
            ellipse(w * 0.7f, h * 0.3f, w * 0.4f, h * 0.35f, 0x182A4060, 4);
            ellipse(w * 0.25f, h * 0.7f, w * 0.3f, h * 0.25f, 0x18204060, 4);
        }
        shootingStar(t);
    }

    private static void nebula(float t, boolean low) {
        gradient(0, 0, w, h, 0xFF120018, 0xFF2A0040);
        int blobs = low ? 2 : 4;
        for (int b = 0; b < blobs; b++) {
            float cx = w * (0.2f + b * 0.22f) + sin(t * 0.15f + b) * 9;
            float cy = h * (0.3f + (b % 2) * 0.35f) + cos(t * 0.11f + b * 1.3f) * 5;
            int rgb = switch (b % 3) { case 0 -> 0xFF6EC7; case 1 -> 0xC77DFF; default -> 0xFF9E80; };
            for (int l = 0; l < (low ? 2 : 3); l++) {
                float s = 1f - l * 0.28f;
                ellipse(cx, cy, w * 0.28f * s, h * 0.32f * s, ((20 + l * 14) << 24) | rgb, low ? 4 : 2);
            }
        }
        stars(t, low ? 14 : 40, 0xFFE0FF);
    }

    private static void cyber(float t, boolean low) {
        gradient(0, 0, w, h, 0xFF000A12, 0xFF001820);
        // grid
        int step = low ? 14 : 9;
        int gridA = 0x1800E5FF;
        for (int x = 0; x < w; x += step) fill(x, 0, x + 1, h, gridA);
        for (int y = 0; y < h; y += step) fill(0, y, w, y + 1, gridA);
        // moving scan line
        int sy = (int) ((t * 40f) % (h + 20)) - 10;
        fill(0, sy, w, sy + 3, 0x4000FFCC);
        // neon blobs
        if (!low) {
            ellipse(w * 0.3f + sin(t * 0.4f) * 8, h * 0.5f, w * 0.2f, h * 0.25f, 0x2800E5FF, 3);
            ellipse(w * 0.75f + cos(t * 0.3f) * 6, h * 0.4f, w * 0.18f, h * 0.22f, 0x2800FFAA, 3);
        }
    }

    private static void paper(float t, boolean low) {
        gradient(0, 0, w, h, 0xFFF5E6C8, 0xFFE8D5A8);
        // paper grain
        if (!low) {
            Random r = new Random((long) (t * 3) + 42);
            for (int i = 0; i < 40; i++) {
                int px = r.nextInt(w);
                int py = r.nextInt(h);
                fill(px, py, px + 1, py + 1, 0x182A2A2A);
            }
        }
        // soft corner shadow
        fill(0, h - 6, w, h, 0x18000000);
    }

    private static void ocean(float t, boolean low) {
        gradient(0, 0, w, h, 0xFF001830, 0xFF003050);
        int waves = low ? 2 : 4;
        for (int i = 0; i < waves; i++) {
            float phase = t * 0.6f + i * 1.1f;
            int baseY = (int) (h * (0.4f + i * 0.15f));
            for (int x = 0; x < w; x += low ? 4 : 2) {
                int dy = (int) (sin(phase + x * 0.07f) * 4);
                fill(x, baseY + dy, x + (low ? 4 : 2), baseY + dy + 2, 0x304FC3F7);
            }
        }
        stars(t, low ? 8 : 20, 0xA0E0FF);
    }

    private static void forest(float t, boolean low) {
        gradient(0, 0, w, h, 0xFF0A1808, 0xFF1A3010);
        // soft canopy blobs
        int trees = low ? 2 : 4;
        for (int i = 0; i < trees; i++) {
            float cx = w * (0.15f + i * 0.25f) + sin(t * 0.08f + i) * 5;
            ellipse(cx, h * 0.35f, w * 0.22f, h * 0.4f, 0x287CB342, low ? 4 : 3);
        }
        // fireflies
        if (!low) {
            for (int i = 0; i < 8; i++) {
                float tw = 0.4f + 0.6f * sin(t * 2.2f + i * 1.7f);
                int a = (int) (120 * tw);
                int px = (int) (SX[i + 10] * w);
                int py = (int) (SY[i + 10] * h * 0.7f + h * 0.2f);
                fill(px, py, px + 2, py + 2, (a << 24) | 0xC6FF00);
            }
        }
    }

    // ------------------------------------------------------------------ helpers

    private static void stars(float t, int count, int rgb) {
        for (int i = 0; i < count; i++) {
            float tw = 0.45f + 0.55f * sin(t * SSP[i] + SPH[i]);
            int a = (int) (60 + 195 * tw);
            int px = (int) (SX[i] * w);
            int py = (int) (SY[i] * h);
            int s = (i % 7 == 0) ? 2 : 1;
            fill(px, py, px + s, py + s, (a << 24) | (rgb & 0xFFFFFF));
            if (i % 11 == 0 && tw > 0.65f) {
                int fa = ((a / 3) << 24) | (rgb & 0xFFFFFF);
                fill(px - 1, py, px, py + 1, fa);
                fill(px + s, py, px + s + 1, py + 1, fa);
                fill(px, py - 1, px + 1, py, fa);
                fill(px, py + s, px + 1, py + s + 1, fa);
            }
        }
    }

    private static void shootingStar(float t) {
        float cyc = (t % 9f) / 9f;
        if (cyc < 0.12f) {
            float p = cyc / 0.12f;
            int sx = (int) (w * (0.95f - 0.7f * p));
            int sy = (int) (h * (0.15f + 0.4f * p));
            int len = 18;
            for (int i = 0; i < len; i++) {
                int a = (int) (200 * (1f - i / (float) len));
                fill(sx + i, sy + i / 3, sx + i + 1, sy + i / 3 + 1, (a << 24) | 0xFFFFFF);
            }
        }
    }

    private static void ellipse(float cx, float cy, float rx, float ry, int color, int step) {
        int y0 = (int) (cy - ry), y1 = (int) (cy + ry);
        for (int yy = y0; yy < y1; yy += step) {
            float f = ((yy + step * 0.5f) - cy) / ry;
            if (f < -1f || f > 1f) continue;
            float hw = rx * (float) Math.sqrt(1f - f * f);
            fill((int) (cx - hw), yy, (int) (cx + hw), yy + step, color);
        }
    }

    private static float sin(float x) { return (float) Math.sin(x); }
    private static float cos(float x) { return (float) Math.cos(x); }

    private static int scale(int color) {
        int a = (int) (((color >>> 24) & 0xFF) * alpha);
        return (a << 24) | (color & 0xFFFFFF);
    }

    private static void fill(int x1, int y1, int x2, int y2, int color) {
        x1 = Math.max(0, x1); y1 = Math.max(0, y1);
        x2 = Math.min(w, x2); y2 = Math.min(h, y2);
        if (x2 <= x1 || y2 <= y1) return;
        ctx.fill(ox + x1, oy + y1, ox + x2, oy + y2, scale(color));
    }

    private static void gradient(int x1, int y1, int x2, int y2, int top, int bottom) {
        x1 = Math.max(0, x1); y1 = Math.max(0, y1);
        x2 = Math.min(w, x2); y2 = Math.min(h, y2);
        if (x2 <= x1 || y2 <= y1) return;
        ctx.fillGradient(ox + x1, oy + y1, ox + x2, oy + y2, scale(top), scale(bottom));
    }
}
