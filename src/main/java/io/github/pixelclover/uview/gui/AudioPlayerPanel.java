package io.github.pixelclover.uview.gui;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.sound.sampled.*;
import javax.swing.*;

/**
 * A panel with simple controls to play an audio clip. It implements {@link LineListener} to react
 * to audio events, such as when playback stops, to update the UI correctly.
 */
public class AudioPlayerPanel extends JPanel implements LineListener {

  private final Clip clip;
  private final JButton playStopButton;

  /**
   * Constructs an AudioPlayerPanel.
   *
   * @param audioData The byte array containing the audio data.
   * @throws UnsupportedAudioFileException if the audio format is not supported.
   * @throws IOException if an I/O error occurs.
   * @throws LineUnavailableException if a Line cannot be opened because it is unavailable.
   */
  public AudioPlayerPanel(byte[] audioData)
      throws UnsupportedAudioFileException, IOException, LineUnavailableException {
    super(new FlowLayout(FlowLayout.CENTER, 20, 20));

    JLabel statusLabel;
    try (AudioInputStream audioInputStream =
        AudioSystem.getAudioInputStream(
            new BufferedInputStream(new ByteArrayInputStream(audioData)))) {
      this.clip = AudioSystem.getClip();
      this.clip.addLineListener(this); // Listen for events like STOP
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

  /**
   * Closes the audio clip to release system resources. This should be called when the panel is no
   * longer needed.
   */
  public void close() {
    if (clip != null) {
      clip.close();
    }
  }

  /**
   * Handles events from the audio line. This method is called when the clip's status changes (e.g.,
   * starts, stops).
   *
   * @param event The LineEvent that occurred.
   */
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
