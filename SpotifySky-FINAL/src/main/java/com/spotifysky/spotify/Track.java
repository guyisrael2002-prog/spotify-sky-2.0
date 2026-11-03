package com.spotifysky.spotify;

public final class Track {
    public String id = "";
    public String url = "";
    public String title = "";
    public String artist = "";
    /** 0 = unknown */
    public int durationSec = 0;

    public Track() {}

    public Track(String title, String artist, int durationSec) {
        this.title = title;
        this.artist = artist;
        this.durationSec = durationSec;
    }
}
