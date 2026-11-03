# Spotify Sky – Fabric 1.21.11

Beautiful, lightweight, fully customizable now-playing HUD for Spotify tracks.

**Keybind:** K (rebindable in Options → Controls → Key Binds)

### Features
- 10 animated sky styles (Galaxy, Aurora, Comic, Sunset, Midnight, Nebula, Cyber, Paper, Ocean, Forest)
- Extremely customizable: size, opacity, animation speed, equalizer, position, offsets, veil strength, etc.
- Paste Spotify track links or type “Artist - Title”
- Queue system with play / prev / next / stop
- Opens the real Spotify app when you press Play (legal)
- Low-power mode for maximum FPS
- Mod Menu support
- Pure procedural rendering – no heavy assets

### How to get the .jar (drag into mods folder)

**Easiest – GitHub Actions (recommended)**
1. Create a free GitHub repo
2. Upload everything in this folder (including the `.github` folder)
3. Go to Actions → “Build mod jar” → Run workflow
4. Download the artifact → put the .jar in your mods folder
5. Also install Fabric API for 1.21.11

**Or build locally**
```
# Requires JDK 21
./gradlew build
```
Jar appears in `build/libs/spotify-sky-1.0.0.jar`

Then drop it into your CurseForge / Prism / MultiMC mods folder together with Fabric API.
