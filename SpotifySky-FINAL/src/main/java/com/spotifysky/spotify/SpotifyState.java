package com.spotifysky.spotify;

import com.spotifysky.SpotifySkyClient;
import com.spotifysky.config.ModConfig;
import net.minecraft.util.Util;

import java.net.URI;
import java.util.List;

/** Queue + "now playing" timer. Render/client thread only. */
public final class SpotifyState {
    private static final ModConfig CONFIG = ModConfig.load();
    private static int current = -1;
    private static long startMs;
    private static boolean playing;

    private SpotifyState() {}

    public static ModConfig config() { return CONFIG; }
    public static boolean isPlaying() { return playing && current() != null; }
    public static int currentIndex() { return current; }

    public static Track current() {
        List<Track> q = CONFIG.queue;
        return current >= 0 && current < q.size() ? q.get(current) : null;
    }

    public static void play(int index, boolean openInSpotify) {
        if (index < 0 || index >= CONFIG.queue.size()) return;
        current = index;
        startMs = System.currentTimeMillis();
        playing = true;
        if (openInSpotify) open(CONFIG.queue.get(index));
    }

    public static void stop() {
        playing = false;
        current = -1;
    }

    public static void next() {
        int i = current + 1;
        if (i < CONFIG.queue.size()) play(i, CONFIG.openOnPlay); else stop();
    }

    public static void prev() {
        if (CONFIG.queue.isEmpty()) return;
        play(Math.max(0, current - 1), CONFIG.openOnPlay);
    }

    public static void remove(int index) {
        if (index < 0 || index >= CONFIG.queue.size()) return;
        CONFIG.queue.remove(index);
        if (index == current) stop();
        else if (index < current) current--;
        CONFIG.save();
    }

    /** 0..1, or -1 when unknown. */
    public static float progress() {
        Track t = current();
        if (t == null || !playing || t.durationSec <= 0) return -1f;
        float p = (System.currentTimeMillis() - startMs) / (t.durationSec * 1000f);
        return Math.min(1f, Math.max(0f, p));
    }

    public static void tick() {
        if (!playing) return;
        Track t = current();
        if (t == null) { playing = false; return; }
        if (t.durationSec > 0 && System.currentTimeMillis() - startMs >= t.durationSec * 1000L) {
            if (current + 1 < CONFIG.queue.size()) play(current + 1, false); else stop();
        }
    }

    private static void open(Track t) {
        if (t.url == null || t.url.isEmpty()) return;
        try {
            Util.getOperatingSystem().open(URI.create(t.url));
        } catch (Exception e) {
            SpotifySkyClient.LOGGER.warn("Could not open link", e);
        }
    }
}
