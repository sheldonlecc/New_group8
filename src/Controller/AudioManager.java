package Controller;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioManager {
    private static AudioManager instance;
    private Clip backgroundMusicClip;
    private boolean isMusicEnabled = true;
    
    private AudioManager() {}
    
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    public void playBackgroundMusic() {
        if (!isMusicEnabled) return;
        
        try {
            // Stop previous music
            stopBackgroundMusic();
            
            // Load audio file
            File audioFile = new File("src/resources/Audio/Ortus.wav");
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            
            // Create audio clip
            backgroundMusicClip = AudioSystem.getClip();
            backgroundMusicClip.open(audioInputStream);
            
            // Set loop playback
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            
            // Start playback
            backgroundMusicClip.start();
            
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing background music: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void stopBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
            backgroundMusicClip.close();
        }
    }
    
    public void pauseBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
        }
    }
    
    public void resumeBackgroundMusic() {
        if (backgroundMusicClip != null && !backgroundMusicClip.isRunning()) {
            backgroundMusicClip.start();
        }
    }
    
    public void setMusicEnabled(boolean enabled) {
        this.isMusicEnabled = enabled;
        if (!enabled) {
            stopBackgroundMusic();
        } else {
            playBackgroundMusic();
        }
    }
    
    public boolean isMusicEnabled() {
        return isMusicEnabled;
    }
    
    public void setVolume(float volume) {
        if (backgroundMusicClip != null) {
            FloatControl volumeControl = (FloatControl) backgroundMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            volumeControl.setValue(dB);
        }
    }
}