package com.spotifysky.sky;

public enum SkyStyle {
    // label, text color, subText color, accent color, veil color
    GALAXY   ("Galaxy",    0xFFFFFFFF, 0xFFC9B8FF, 0xFFB388FF, 0x48000000),
    AURORA   ("Aurora",    0xFFFFFFFF, 0xFFB8FFE6, 0xFF4DFFB0, 0x38000000),
    COMIC    ("Comic",     0xFF1A1A1A, 0xFF3A3A3A, 0xFFE8344E, 0x50FFF6CC),
    SUNSET   ("Sunset",    0xFFFFFFFF, 0xFFFFE2C4, 0xFFFFD27A, 0x55000000),
    MIDNIGHT ("Midnight",  0xFFFFFFFF, 0xFFB0B8C8, 0xFF5EEAD4, 0x30000000),
    NEBULA   ("Nebula",    0xFFFFFFFF, 0xFFFFB8E8, 0xFFFF6EC7, 0x45000000),
    CYBER    ("Cyber",     0xFF00FFCC, 0xFF80FFE0, 0xFF00E5FF, 0x50000A1A),
    PAPER    ("Paper",     0xFF2A2A2A, 0xFF555555, 0xFFD45B2A, 0x40F5E6C8),
    OCEAN    ("Ocean",     0xFFFFFFFF, 0xFFA8D8FF, 0xFF4FC3F7, 0x40001830),
    FOREST   ("Forest",    0xFFE8FFE0, 0xFFB8E0A8, 0xFF7CB342, 0x40081000);

    public final String label;
    public final int text, subText, accent, veil;

    SkyStyle(String label, int text, int subText, int accent, int veil) {
        this.label = label;
        this.text = text;
        this.subText = subText;
        this.accent = accent;
        this.veil = veil;
    }

    public SkyStyle next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public SkyStyle prev() {
        int i = ordinal() - 1;
        return values()[i < 0 ? values().length - 1 : i];
    }
}
