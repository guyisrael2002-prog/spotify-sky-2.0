package com.spotifysky.spotify;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves a Spotify track link into title / artist / duration WITHOUT any login or API key,
 * by reading the public link-preview tags of the track page (with Spotify's oEmbed as fallback).
 * Everything runs off the render thread.
 */
public final class SpotifyFetcher {
    private static final Pattern TRACK_ID = Pattern.compile("track[/:]([A-Za-z0-9]{22})");
    private static final ExecutorService POOL = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "SpotifySky-Fetch");
        t.setDaemon(true);
        return t;
    });
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(6))
            .build();

    private SpotifyFetcher() {}

    public static CompletableFuture<Track> resolve(String input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return resolveBlocking(input.trim());
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, POOL);
    }

    private static Track resolveBlocking(String in) throws Exception {
        Matcher m = TRACK_ID.matcher(in);
        if (!m.find()) {
            if (in.startsWith("http") || in.startsWith("spotify:")) {
                throw new IOException("Only track links work (open.spotify.com/track/...)");
            }
            // Manual entry: "Artist - Title" or just a title
            Track t = new Track();
            int dash = in.indexOf(" - ");
            if (dash > 0) {
                t.artist = in.substring(0, dash).trim();
                t.title = in.substring(dash + 3).trim();
            } else {
                t.title = in;
            }
            return t;
        }

        Track t = new Track();
        t.id = m.group(1);
        t.url = "https://open.spotify.com/track/" + t.id;

        try {
            String html = get(t.url);
            t.title = meta(html, "og:title");
            String desc = meta(html, "og:description");
            if (desc.startsWith("Listen to")) {
                int idx = desc.indexOf(". ");
                if (idx >= 0) desc = desc.substring(idx + 2);
            }
            String[] parts = desc.split("\\s*\u00B7\\s*");
            if (parts.length > 0) t.artist = parts[0].trim();
            String dur = meta(html, "music:duration");
            if (!dur.isEmpty()) t.durationSec = Integer.parseInt(dur.trim());
        } catch (Exception ignored) {
            // fall through to oEmbed
        }

        if (t.title.isEmpty()) {
            try {
                String body = get("https://open.spotify.com/oembed?url="
                        + URLEncoder.encode(t.url, StandardCharsets.UTF_8));
                JsonObject o = JsonParser.parseString(body).getAsJsonObject();
                t.title = o.get("title").getAsString();
            } catch (Exception ignored) {
            }
        }
        if (t.title.isEmpty()) {
            throw new IOException("Couldn't read that track (offline or bad link)");
        }
        return t;
    }

    private static String get(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) SpotifySky/1.0")
                .header("Accept-Language", "en")
                .GET().build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) throw new IOException("HTTP " + res.statusCode());
        return res.body();
    }

    private static String meta(String html, String prop) {
        String q = Pattern.quote(prop);
        Matcher a = Pattern.compile("<meta[^>]*?(?:property|name)=\"" + q + "\"[^>]*?content=\"([^\"]*)\"",
                Pattern.CASE_INSENSITIVE).matcher(html);
        if (a.find()) return unescape(a.group(1));
        Matcher b = Pattern.compile("<meta[^>]*?content=\"([^\"]*)\"[^>]*?(?:property|name)=\"" + q + "\"",
                Pattern.CASE_INSENSITIVE).matcher(html);
        if (b.find()) return unescape(b.group(1));
        return "";
    }

    private static String unescape(String in) {
        String s = in.replace("&quot;", "\"").replace("&#x27;", "'").replace("&#39;", "'")
                .replace("&lt;", "<").replace("&gt;", ">");
        Matcher m = Pattern.compile("&#(x?)([0-9a-fA-F]+);").matcher(s);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String rep = m.group(0);
            try {
                int cp = Integer.parseInt(m.group(2), m.group(1).isEmpty() ? 10 : 16);
                rep = new String(Character.toChars(cp));
            } catch (Exception ignored) {
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(rep));
        }
        m.appendTail(sb);
        return sb.toString().replace("&amp;", "&").trim();
    }
}
