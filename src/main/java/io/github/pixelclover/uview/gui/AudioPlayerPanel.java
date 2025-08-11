package io.github.pixelclover.uview.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AudioPlayerPanel extends JPanel implements ActionListener, Runnable {
  private static final Logger LOGGER = LogManager.getLogger(AudioPlayerPanel.class);

  private byte[] audioData;
  private AudioFormat audioFormat;
  private final JSlider slider;
  private final JButton playPauseButton;
  private final JButton stopButton;

  private volatile boolean isPlaying = false;
  private Thread playbackThread;
  private SourceDataLine line;
  private int framePosition = 0;

  public AudioPlayerPanel(byte[] audioData)
      throws UnsupportedAudioFileException, IOException, LineUnavailableException {
    super(new BorderLayout());

    LOGGER.debug("Initializing AudioPlayerPanel...");

    try (AudioInputStream audioInputStream =
        AudioSystem.getAudioInputStream(
            new BufferedInputStream(new ByteArrayInputStream(audioData)))) {
      AudioFormat baseFormat = audioInputStream.getFormat();
      AudioFormat decodedFormat =
          new AudioFormat(
              AudioFormat.Encoding.PCM_SIGNED,
              baseFormat.getSampleRate(),
              16,
              baseFormat.getChannels(),
              baseFormat.getChannels() * 2,
              baseFormat.getSampleRate(),
              false);
      try (AudioInputStream decodedAudioInputStream =
          AudioSystem.getAudioInputStream(decodedFormat, audioInputStream)) {
        this.audioData = decodedAudioInputStream.readAllBytes();
        this.audioFormat = decodedFormat;
      }
    }

    String details =
        String.format(
            "%.1f kHz, %d-bit, %s",
            audioFormat.getSampleRate() / 1000.0,
            audioFormat.getSampleSizeInBits(),
            audioFormat.getChannels() == 1 ? "Mono" : "Stereo");
    JLabel statusLabel = new JLabel(details);
    LOGGER.debug("Audio format: {}", details);

    playPauseButton = new JButton("Play");
    playPauseButton.addActionListener(this);

    stopButton = new JButton("Stop");
    stopButton.addActionListener(this);

    slider = new JSlider(0, this.audioData.length);
    slider.setValue(0);
    slider.addChangeListener(
        e -> {
          if (!slider.getValueIsAdjusting()) {
            seek(slider.getValue());
          }
        });

    JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
    controlsPanel.add(playPauseButton);
    controlsPanel.add(stopButton);

    add(controlsPanel, BorderLayout.CENTER);
    add(slider, BorderLayout.NORTH);
    add(statusLabel, BorderLayout.SOUTH);

    LOGGER.debug("AudioPlayerPanel initialized successfully.");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == playPauseButton) {
      if (isPlaying) {
        pause();
      } else {
        play();
      }
    } else if (e.getSource() == stopButton) {
      stop();
    }
  }

  private void play() {
    if (playbackThread == null || !playbackThread.isAlive()) {
      playbackThread = new Thread(this);
      playbackThread.start();
    }
    isPlaying = true;
    playPauseButton.setText("Pause");
  }

  private void pause() {
    isPlaying = false;
    playPauseButton.setText("Play");
    if (line != null) {
      line.stop();
    }
  }

  private void stop() {
    isPlaying = false;
    playPauseButton.setText("Play");
    if (playbackThread != null) {
      playbackThread.interrupt();
    }
    framePosition = 0;
    slider.setValue(0);
  }

  private void seek(int position) {
    framePosition = position;
    if (isPlaying) {
      if (playbackThread != null) {
        playbackThread.interrupt();
      }
      play();
    } else {
      slider.setValue(position);
    }
  }

  @Override
  public void run() {
    try {
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
      line = (SourceDataLine) AudioSystem.getLine(info);
      line.open(audioFormat);
      line.start();

      int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
      byte[] buffer = new byte[bufferSize];
      int bytesRead = 0;

      try (ByteArrayInputStream bis = new ByteArrayInputStream(audioData)) {
        bis.skip(framePosition);
        while (isPlaying && (bytesRead = bis.read(buffer, 0, buffer.length)) != -1) {
          line.write(buffer, 0, bytesRead);
          framePosition += bytesRead;
          final int pos = framePosition;
          SwingUtilities.invokeLater(() -> slider.setValue(pos));
        }
      }

      line.drain();
      line.close();
      if (isPlaying) {
        SwingUtilities.invokeLater(
            () -> {
              slider.setValue(0);
              playPauseButton.setText("Play");
            });
        framePosition = 0;
        isPlaying = false;
      }

    } catch (LineUnavailableException | IOException e) {
      LOGGER.error("Error during audio playback", e);
    } finally {
      if (line != null) {
        line.close();
      }
      isPlaying = false;
      SwingUtilities.invokeLater(() -> playPauseButton.setText("Play"));
    }
  }

  public void close() {
    stop();
  }
}
