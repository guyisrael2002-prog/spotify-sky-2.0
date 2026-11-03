package com.spotifysky.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spotifysky.SpotifySkyClient;
import com.spotifysky.sky.SkyStyle;
import com.spotifysky.spotify.Track;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ModConfig {
    public enum Corner {
        TOP_LEFT("Top Left"), TOP_RIGHT("Top Right"),
        BOTTOM_LEFT("Bottom Left"), BOTTOM_RIGHT("Bottom Right");

        public final String label;
        Corner(String label) { this.label = label; }
        public Corner next() { return values()[(ordinal() + 1) % values().length]; }
    }

    public static final String[] SIZE_LABELS = {"Tiny", "Small", "Normal", "Wide", "Huge"};
    public static final int[] WIDTHS = {100, 130, 160, 200, 250};
    public static final int[] HEIGHTS = {30, 34, 40, 48, 56};

    // Core
    public boolean hudEnabled = true;
    public SkyStyle sky = SkyStyle.GALAXY;
    public Corner corner = Corner.TOP_LEFT;

    // Size & position
    public int sizeIndex = 2;          // 0-4
    public int offsetX = 6;
    public int offsetY = 6;
    public float scale = 1.0f;         // 0.7 - 1.4

    // Appearance
    public float opacity = 0.95f;      // 0.3 - 1.0
    public float veilStrength = 1.0f;  // multiplies the style veil alpha
    public float animSpeed = 1.0f;     // 0.3 - 2.5
    public boolean lowPower = false;
    public boolean showEqualizer = true;
    public int equalizerBars = 5;      // 3-8
    public float equalizerIntensity = 1.0f;
    public boolean textShadow = true;
    public boolean showProgress = true;
    public boolean openOnPlay = true;

    public List<Track> queue = new ArrayList<>();

    // Helpers
    public int width()  { return Math.round(WIDTHS[clamp(sizeIndex, 0, 4)] * scale); }
    public int height() { return Math.round(HEIGHTS[clamp(sizeIndex, 0, 4)] * scale); }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("spotifysky.json");
    }

    public static ModConfig load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                ModConfig c = GSON.fromJson(Files.readString(p), ModConfig.class);
                if (c != null) return c.sanitize();
            }
        } catch (Exception e) {
            SpotifySkyClient.LOGGER.warn("Could not read spotifysky.json, using defaults", e);
        }
        return new ModConfig();
    }

    private ModConfig sanitize() {
        if (queue == null) queue = new ArrayList<>();
        if (sky == null) sky = SkyStyle.GALAXY;
        if (corner == null) corner = Corner.TOP_LEFT;
        sizeIndex = clamp(sizeIndex, 0, 4);
        opacity = Math.max(0.3f, Math.min(1.0f, opacity));
        scale = Math.max(0.7f, Math.min(1.4f, scale));
        animSpeed = Math.max(0.3f, Math.min(2.5f, animSpeed));
        veilStrength = Math.max(0f, Math.min(2f, veilStrength));
        equalizerBars = clamp(equalizerBars, 3, 8);
        equalizerIntensity = Math.max(0.3f, Math.min(2f, equalizerIntensity));
        offsetX = Math.max(0, Math.min(100, offsetX));
        offsetY = Math.max(0, Math.min(100, offsetY));
        return this;
    }

    public void save() {
        try {
            Files.writeString(path(), GSON.toJson(this));
        } catch (Exception e) {
            SpotifySkyClient.LOGGER.warn("Could not save spotifysky.json", e);
        }
    }
}
