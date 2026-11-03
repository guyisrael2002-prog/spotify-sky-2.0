package com.spotifysky.gui;

import com.spotifysky.config.ModConfig;
import com.spotifysky.hud.SkyCard;
import com.spotifysky.spotify.SpotifyFetcher;
import com.spotifysky.spotify.SpotifyState;
import com.spotifysky.spotify.Track;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

public class SpotifySkyScreen extends Screen {
    private static final int ROWS = 5;
    private static final Track DEMO = new Track("Your song here", "Paste a Spotify link below", 0);

    private final Screen parent;
    private TextFieldWidget input;
    private String inputText = "";
    private String status = "Paste Spotify track link or type: Artist - Title";
    private int page = 0;

    public SpotifySkyScreen(Screen parent) {
        super(Text.literal("Spotify Sky"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ModConfig cfg = SpotifyState.config();
        int cx = width / 2;
        int x0 = cx - 160;
        int x1 = cx + 10;

        // Input row
        input = new TextFieldWidget(textRenderer, x0, 58, 240, 20, Text.literal("Spotify link"));
        input.setMaxLength(300);
        input.setPlaceholder(Text.literal("open.spotify.com/track/..."));
        input.setText(inputText);
        input.setChangedListener(s -> inputText = s);
        addDrawableChild(input);
        setInitialFocus(input);

        addDrawableChild(ButtonWidget.builder(Text.literal("Add"), b -> addTrack())
                .dimensions(cx + 90, 58, 70, 20).build());

        // Queue controls
        int pages = Math.max(1, (cfg.queue.size() + ROWS - 1) / ROWS);
        page = Math.min(page, pages - 1);

        for (int r = 0; r < ROWS; r++) {
            int idx = page * ROWS + r;
            if (idx >= cfg.queue.size()) break;
            int ry = 90 + r * 18;
            final int fi = idx;
            addDrawableChild(ButtonWidget.builder(Text.literal("▶"), b -> {
                SpotifyState.play(fi, cfg.openOnPlay);
                clearAndInit();
            }).dimensions(x0 + 90, ry, 20, 16).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("✕"), b -> {
                SpotifyState.remove(fi);
                clearAndInit();
            }).dimensions(x0 + 112, ry, 20, 16).build());
        }

        // Playback
        addDrawableChild(ButtonWidget.builder(Text.literal("⏮ Prev"), b -> { SpotifyState.prev(); clearAndInit(); })
                .dimensions(x1, 90, 70, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("⏹ Stop"), b -> { SpotifyState.stop(); clearAndInit(); })
                .dimensions(x1 + 74, 90, 70, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Next ⏭"), b -> { SpotifyState.next(); clearAndInit(); })
                .dimensions(x1, 112, 144, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Open Spotify: " + (cfg.openOnPlay ? "ON" : "OFF")), b -> {
            cfg.openOnPlay = !cfg.openOnPlay;
            cfg.save();
            clearAndInit();
        }).dimensions(x1, 134, 144, 18).build());

        // Pagination
        ButtonWidget prevPage = ButtonWidget.builder(Text.literal("<"), b -> { page--; clearAndInit(); })
                .dimensions(x1, 156, 30, 18).build();
        ButtonWidget nextPage = ButtonWidget.builder(Text.literal(">"), b -> { page++; clearAndInit(); })
                .dimensions(x1 + 114, 156, 30, 18).build();
        prevPage.active = page > 0;
        nextPage.active = page < pages - 1;
        addDrawableChild(prevPage);
        addDrawableChild(nextPage);
        addDrawableChild(ButtonWidget.builder(Text.literal("Page " + (page + 1) + "/" + pages), b -> {})
                .dimensions(x1 + 32, 156, 80, 18).build()).active = false;

        // Appearance toggles - row 1
        int y = 190;
        setting(x0, y, 100, "HUD: " + onOff(cfg.hudEnabled), () -> cfg.hudEnabled = !cfg.hudEnabled);
        setting(x0 + 105, y, 100, "Sky: " + cfg.sky.label, () -> cfg.sky = cfg.sky.next());
        setting(x0 + 210, y, 100, "Pos: " + cfg.corner.label, () -> cfg.corner = cfg.corner.next());

        // row 2
        y = 212;
        setting(x0, y, 100, "Size: " + ModConfig.SIZE_LABELS[cfg.sizeIndex], () -> cfg.sizeIndex = (cfg.sizeIndex + 1) % 5);
        setting(x0 + 105, y, 100, "Opacity: " + Math.round(cfg.opacity * 100) + "%", () -> {
            cfg.opacity += 0.15f;
            if (cfg.opacity > 1.01f) cfg.opacity = 0.4f;
        });
        setting(x0 + 210, y, 100, "Low Power: " + onOff(cfg.lowPower), () -> cfg.lowPower = !cfg.lowPower);

        // row 3
        y = 234;
        setting(x0, y, 100, "Anim: " + String.format("%.1fx", cfg.animSpeed), () -> {
            cfg.animSpeed += 0.3f;
            if (cfg.animSpeed > 2.6f) cfg.animSpeed = 0.4f;
        });
        setting(x0 + 105, y, 100, "EQ: " + onOff(cfg.showEqualizer), () -> cfg.showEqualizer = !cfg.showEqualizer);
        setting(x0 + 210, y, 100, "Progress: " + onOff(cfg.showProgress), () -> cfg.showProgress = !cfg.showProgress);

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(cx - 50, height - 28, 100, 20).build());
    }

    private void setting(int x, int y, int w, String label, Runnable change) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> {
            change.run();
            SpotifyState.config().save();
            clearAndInit();
        }).dimensions(x, y, w, 18).build());
    }

    private static String onOff(boolean v) { return v ? "ON" : "OFF"; }

    private void addTrack() {
        String s = inputText.trim();
        if (s.isEmpty()) return;
        status = "Looking it up…";
        SpotifyFetcher.resolve(s).whenComplete((track, err) -> MinecraftClient.getInstance().execute(() -> {
            if (err != null) {
                Throwable root = err.getCause() != null ? err.getCause() : err;
                status = "Failed: " + (root.getMessage() == null ? "unknown" : root.getMessage());
            } else {
                SpotifyState.config().queue.add(track);
                SpotifyState.config().save();
                inputText = "";
                status = "Added: " + track.title;
            }
            if (MinecraftClient.getInstance().currentScreen == this) clearAndInit();
        }));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ModConfig cfg = SpotifyState.config();
        int cx = width / 2;
        int x0 = cx - 160;

        ctx.drawCenteredTextWithShadow(textRenderer, "Spotify Sky", cx, 8, 0xFFFFFF);

        // live preview
        boolean playing = SpotifyState.isPlaying();
        Track shown = playing ? SpotifyState.current() : DEMO;
        int cardW = cfg.width();
        int previewX = cx - cardW / 2;
        SkyCard.render(ctx, textRenderer, previewX, 26, shown,
                playing ? SpotifyState.progress() : -1f, playing, cfg);

        ctx.drawText(textRenderer, trim(status, 320), x0, 78, 0xFFB0B0B0, false);

        // queue list
        List<Track> q = cfg.queue;
        if (q.isEmpty()) {
            ctx.drawText(textRenderer, "Queue empty – add a track above", x0, 95, 0xFF808080, false);
        } else {
            for (int r = 0; r < ROWS; r++) {
                int idx = page * ROWS + r;
                if (idx >= q.size()) break;
                int ry = 92 + r * 18;
                boolean now = playing && idx == SpotifyState.currentIndex();
                if (now) ctx.fill(x0 - 2, ry - 1, x0 + 88, ry + 15, 0x33FFFFFF);
                Track t = q.get(idx);
                String line = (now ? "▶ " : "") + t.title;
                ctx.drawText(textRenderer, trim(line, 85), x0, ry + 3, now ? 0xFFFFFF : 0xFFCCCCCC, false);
            }
        }
    }

    private String trim(String s, int maxW) {
        if (textRenderer.getWidth(s) <= maxW) return s;
        return textRenderer.trimToWidth(s, maxW - textRenderer.getWidth("…")) + "…";
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}
