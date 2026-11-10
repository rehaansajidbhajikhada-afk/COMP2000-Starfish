
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class AudioManager {

    private Clip music;
    private Clip laughter;

    private Clip load(String filename) {
        try {
            URL url = getClass().getResource("/audio/" + filename);

            if (url == null) {
                System.out.println("Audio not found: " + filename);
                return null;
            }

            try (AudioInputStream stream =
                     AudioSystem.getAudioInputStream(url)) {

                Clip clip = AudioSystem.getClip();
                clip.open(stream);
                return clip;
            }

        } catch (Exception e) {
            System.out.println(
                "Audio error (" + filename + "): " + e.getMessage()
            );
            return null;
        }
    }

    private void changeMusic(String filename) {
        if (music != null) {
            music.stop();
            music.close();
        }

        music = load(filename);

        if (music != null) {
            music.setFramePosition(0);
            music.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void playNormal() {
        changeMusic("normal.wav");
    }

    public void playEmergency() {
        changeMusic("emergency.wav");
    }

    // Game Over: do not restart emergency music.
    public void playGameOver() {
    // Stop emergency music immediately on defeat.
    if (music != null) {
        music.stop();
        music.close();
        music = null;
    }
}

    public void playLaugh() {
        if (laughter != null) {
            laughter.stop();
            laughter.close();
        }

        laughter = load("laugh.wav");

        if (laughter != null) {
            laughter.setFramePosition(0);
            laughter.start();
        }
    }

    public void stopAll() {
        if (music != null) {
            music.stop();
            music.close();
            music = null;
        }

        if (laughter != null) {
            laughter.stop();
            laughter.close();
            laughter = null;
        }
    }
}

