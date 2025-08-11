package io.github.pixelclover.uview.gui;

import io.github.pixelclover.uview.model.UnityAsset;
import java.awt.BorderLayout;
import java.nio.file.Path;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import javax.swing.JPanel;

/** A panel for playing video files using JavaFX media components. */
public class VideoPlayerPanel extends JPanel {

  private final JFXPanel jfxPanel = new JFXPanel();
  private MediaPlayer mediaPlayer;

  /**
   * Constructs a new video player panel.
   *
   * @param asset The Unity asset representing the video.
   * @param assetPath The path to the video file on disk.
   */
  public VideoPlayerPanel(UnityAsset asset, Path assetPath) {
    setLayout(new BorderLayout());
    add(jfxPanel, BorderLayout.CENTER);

    Platform.setImplicitExit(false);
    Platform.runLater(
        () -> {
          try {
            Media media = new Media(assetPath.toUri().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);

            MediaView mediaView = new MediaView(mediaPlayer);
            mediaView.setPreserveRatio(true);

            BorderPane root = new BorderPane();
            root.setCenter(mediaView);
            root.setBottom(createControls());

            Scene scene = new Scene(root);
            mediaView.fitWidthProperty().bind(scene.widthProperty());
            mediaView.fitHeightProperty().bind(scene.heightProperty().subtract(50));
            jfxPanel.setScene(scene);
          } catch (Exception e) {
            // Handle exceptions, e.g., by showing an error message
          }
        });
  }

  private HBox createControls() {
    HBox controls = new HBox(10);
    controls.setAlignment(Pos.CENTER);
    controls.setPadding(new Insets(10));

    Button playPauseButton = new Button("Pause");
    playPauseButton.setOnAction(
        e -> {
          if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playPauseButton.setText("Play");
          } else {
            mediaPlayer.play();
            playPauseButton.setText("Pause");
          }
        });

    Label timeLabel = new Label();
    Slider timeSlider = new Slider();
    HBox.setHgrow(timeSlider, Priority.ALWAYS);

    Slider volumeSlider = new Slider(0, 1, 1);
    volumeSlider.setPrefWidth(100);
    volumeSlider
        .valueProperty()
        .addListener((obs, oldVal, newVal) -> mediaPlayer.setVolume(newVal.doubleValue()));

    controls.getChildren().addAll(playPauseButton, timeSlider, timeLabel, volumeSlider);

    mediaPlayer
        .currentTimeProperty()
        .addListener(
            (obs, oldTime, newTime) -> {
              if (!timeSlider.isValueChanging()) {
                timeSlider.setValue(newTime.toSeconds());
              }
              timeLabel.setText(
                  formatDuration(newTime) + " / " + formatDuration(mediaPlayer.getTotalDuration()));
            });

    mediaPlayer.setOnReady(
        () -> {
          timeSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
          timeLabel.setText("00:00 / " + formatDuration(mediaPlayer.getTotalDuration()));
        });

    timeSlider
        .valueProperty()
        .addListener(
            (obs, oldValue, newValue) -> {
              if (timeSlider.isValueChanging()) {
                mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
              }
            });

    return controls;
  }

  private String formatDuration(Duration duration) {
    if (duration == null || duration.isUnknown()) {
      return "00:00";
    }
    long seconds = (long) duration.toSeconds();
    long minutes = seconds / 60;
    long secs = seconds % 60;
    return String.format("%02d:%02d", minutes, secs);
  }

  /** Stops the media player and releases resources. */
  public void stop() {
    if (mediaPlayer != null) {
      Platform.runLater(
          () -> {
            mediaPlayer.stop();
            mediaPlayer.dispose();
          });
    }
  }
}
