package io.github.pixelclover.uview.gui;

import java.awt.FlowLayout;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** A panel with simple controls to play an audio clip. */
public class AudioPlayerPanel extends JPanel implements LineListener {

  private final Clip clip;
  private final JButton playStopButton;

  public AudioPlayerPanel(byte[] audioData)
      throws UnsupportedAudioFileException, IOException, LineUnavailableException {
    super(new FlowLayout(FlowLayout.CENTER, 20, 20));

    JLabel statusLabel;
    try (AudioInputStream audioInputStream =
        AudioSystem.getAudioInputStream(
            new BufferedInputStream(new ByteArrayInputStream(audioData)))) {
      this.clip = AudioSystem.getClip();

      // --- ADDED: Register this panel to listen for audio events ---
      this.clip.addLineListener(this);
      this.clip.open(audioInputStream);

      AudioFormat format = audioInputStream.getFormat();
      String details =
          String.format(
              "%.1f kHz, %d-bit, %s",
              format.getSampleRate() / 1000.0,
              format.getSampleSizeInBits(),
              format.getChannels() == 1 ? "Mono" : "Stereo");
      statusLabel = new JLabel(details);
    }

    this.playStopButton = new JButton("Play");
    setupActionListener();

    add(playStopButton);
    add(statusLabel);
  }

  // --- MODIFIED: The click listener is now simpler ---
  private void setupActionListener() {
    playStopButton.addActionListener(
        e -> {
          if (clip.isRunning()) {
            clip.stop(); // The LineListener will handle the UI update.
          } else {
            clip.start();
            playStopButton.setText("Stop");
          }
        });
  }

  /** Closes the audio clip to release system resources. */
  public void close() {
    if (clip != null) {
      clip.close();
    }
  }

  // --- ADDED: This method handles events from the audio clip ---
  @Override
  public void update(LineEvent event) {
    // This event is fired when playback finishes naturally OR is manually stopped.
    if (event.getType() == LineEvent.Type.STOP) {
      // All UI updates must happen on the Swing Event Dispatch Thread (EDT).
      SwingUtilities.invokeLater(
          () -> {
            playStopButton.setText("Play");
            clip.setFramePosition(0); // Always rewind the clip when it stops.
          });
    }
  }
}
