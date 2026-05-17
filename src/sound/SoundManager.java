package sound;

import javax.sound.sampled.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Sound manager for the game.
 * Supports: opening music, BG music, bomb explosion, pickup item, win, lose.
 */
public class SoundManager {

    // Sound types
    public enum SoundType {
        OPENING,      // Menu music (lobby)
        BGMUSIC,      // Background music when playing
        BOMB,         // Bomb explosion
        PICKUP,       // Item pickup
        WIN,          // Player wins
        LOSE          // Player loses
    }

    private Map<SoundType, Clip> clips = new HashMap<>();
    private Map<SoundType, String> soundPaths = new HashMap<>();


    private Map<SoundType, Float> volumes = new HashMap<>();

    private SoundType currentMusic = null;
    private boolean isMuted = false;
    private boolean winLosePlayed = false;

    public SoundManager() {
        initSoundPaths();
        initVolumes();
        loadAllSounds();
    }

    private void initSoundPaths() {
        soundPaths.put(SoundType.OPENING, "/sound/opening.wav");
        soundPaths.put(SoundType.BGMUSIC, "/sound/bgmusic.wav");
        soundPaths.put(SoundType.BOMB,    "/sound/bomb.wav");
        soundPaths.put(SoundType.PICKUP,  "/sound/pickup.wav");
        soundPaths.put(SoundType.WIN,     "/sound/win.wav");
        soundPaths.put(SoundType.LOSE,    "/sound/lose.wav");
    }

    private void initVolumes() {
        volumes.put(SoundType.OPENING, 0.7f);
        volumes.put(SoundType.BGMUSIC, 0.6f);
        volumes.put(SoundType.BOMB,    0.7f);
        volumes.put(SoundType.PICKUP,  1.0f);
        volumes.put(SoundType.WIN,     1.0f);
        volumes.put(SoundType.LOSE,    1.0f);
    }

    private void loadAllSounds() {
        for (Map.Entry<SoundType, String> entry : soundPaths.entrySet()) {
            try {
                URL url = getClass().getResource(entry.getValue());
                if (url == null) {
                    System.err.println("Sound not found: " + entry.getValue());
                    continue;
                }

                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);

                // Set volume
                setVolume(clip, volumes.getOrDefault(entry.getKey(), 0.7f));

                clips.put(entry.getKey(), clip);

            } catch (Exception e) {
                System.err.println("Failed to load sound: " + entry.getKey());
            }
        }
    }


    private void setVolume(Clip clip, float volume) {
        if (clip == null) return;

        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        } catch (Exception e) {
        }
    }

    /**
     * Play a sound effect (non-looping)
     */
    public void play(SoundType type) {
        if (isMuted) return;

        Clip clip = clips.get(type);
        if (clip == null) return;

        // Reset volume (trong case clip bị reset)
        setVolume(clip, volumes.getOrDefault(type, 0.7f));

        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();
    }

    /**
     * Play music (looping)
     */
    public void playMusic(SoundType type) {
        if (isMuted) return;

        // Don't restart same music
        if (currentMusic == type && isMusicPlaying()) {
            return;
        }

        // Stop current music if different
        if (currentMusic != null && currentMusic != type) {
            stopMusic();
        }

        Clip clip = clips.get(type);
        if (clip == null) return;

        // Reset volume
        setVolume(clip, volumes.getOrDefault(type, 0.6f));

        currentMusic = type;
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    /**
     * Stop current music completely
     */
    public void stopMusic() {
        if (currentMusic != null) {
            Clip clip = clips.get(currentMusic);
            if (clip != null && clip.isRunning()) {
                clip.stop();
                clip.setFramePosition(0);  // Reset về đầu
            }
            currentMusic = null;
        }
    }

    /**
     * Stop all sounds immediately
     */
    public void stopAll() {
        stopMusic();
        for (Clip clip : clips.values()) {
            if (clip.isRunning()) {
                clip.stop();
                clip.setFramePosition(0);
            }
        }
    }

    /**
     * Check if music is playing
     */
    public boolean isMusicPlaying() {
        if (currentMusic == null) return false;
        Clip clip = clips.get(currentMusic);
        return clip != null && clip.isRunning();
    }

    /**
     * Check if a sound is playing
     */
    public boolean isPlaying(SoundType type) {
        Clip clip = clips.get(type);
        return clip != null && clip.isRunning();
    }

    /**
     * Reset win/lose flag (call when game starts/restarts)
     */
    public void resetWinLoseFlag() {
        winLosePlayed = false;
    }

    /**
     * Play win sound (only once)
     */
    public void playWinOnce() {
        if (!winLosePlayed && !isMuted) {
            winLosePlayed = true;
            play(SoundType.WIN);
        }
    }

    /**
     * Play lose sound (only once)
     */
    public void playLoseOnce() {
        if (!winLosePlayed && !isMuted) {
            winLosePlayed = true;
            play(SoundType.LOSE);
        }
    }

    /**
     */
    public void setMusicVolume(float volume) {
        if (currentMusic != null) {
            Clip clip = clips.get(currentMusic);
            if (clip != null) {
                setVolume(clip, volume);
            }
        }
        volumes.put(SoundType.BGMUSIC, volume);
    }

    public void setMuted(boolean muted) {
        this.isMuted = muted;
        if (muted) {
            stopAll();
        }
    }

    public boolean isMuted() {
        return isMuted;
    }
}