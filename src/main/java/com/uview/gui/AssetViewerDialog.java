package com.uview.gui;

import com.uview.model.UnityAsset;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

public class AssetViewerDialog extends JDialog {

  private static final Set<String> TEXT_EXTENSIONS =
      Set.of("cs", "txt", "json", "xml", "shader", "mat", "unity", "asset");
  private static final Set<String> IMAGE_EXTENSIONS =
      Set.of("png", "jpg", "jpeg", "gif", "tga", "bmp");
  private static final Set<String> MEDIA_EXTENSIONS = Set.of("mp4", "mov", "wav", "mp3", "ogg");
  private static final DecimalFormat FILE_SIZE_FORMAT = new DecimalFormat("#,##0.0 KB");

  public AssetViewerDialog(JFrame owner, UnityAsset asset) {
    super(owner, "Asset Viewer - " + asset.assetPath(), true);
    setSize(800, 600);
    setLocationRelativeTo(owner);
    setLayout(new BorderLayout());

    JPanel metadataPanel = createMetadataPanel(asset);
    JPanel contentPanel = createContentPanel(asset);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, metadataPanel, contentPanel);
    splitPane.setDividerLocation(250);

    add(splitPane, BorderLayout.CENTER);
  }

  private JPanel createMetadataPanel(UnityAsset asset) {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JTextArea metadataArea = new JTextArea();
    metadataArea.setEditable(false);
    metadataArea.setFont(new JTextArea().getFont().deriveFont(14f));

    StringBuilder sb = new StringBuilder();
    sb.append("Path:\n").append(asset.assetPath()).append("\n\n");
    sb.append("GUID:\n").append(asset.guid()).append("\n\n");

    if (asset.content() != null) {
      double sizeInKb = asset.content().length / 1024.0;
      sb.append("Size:\n").append(FILE_SIZE_FORMAT.format(sizeInKb));
    } else {
      sb.append("Size:\nN/A (Directory)");
    }

    metadataArea.setText(sb.toString());
    panel.add(new JScrollPane(metadataArea), BorderLayout.CENTER);
    return panel;
  }

  private JPanel createContentPanel(UnityAsset asset) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    if (asset.content() == null) {
      panel.add(new JLabel("This is a directory."), BorderLayout.CENTER);
      return panel;
    }

    String extension = getFileExtension(asset.assetPath());

    if (TEXT_EXTENSIONS.contains(extension)) {
      JTextArea textArea = new JTextArea(new String(asset.content(), StandardCharsets.UTF_8));
      textArea.setEditable(false);
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);
      panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
    } else if (IMAGE_EXTENSIONS.contains(extension)) {
      ImageIcon imageIcon = new ImageIcon(asset.content());
      JLabel imageLabel = new JLabel(imageIcon);
      panel.add(new JScrollPane(imageLabel), BorderLayout.CENTER);
    } else if (MEDIA_EXTENSIONS.contains(extension)) {
      handleMediaAsset(asset);
      dispose(); // Close the dialog as the media will open externally
      return null;
    } else {
      panel.add(new JLabel("Binary content cannot be previewed."), BorderLayout.CENTER);
    }

    return panel;
  }

  private void handleMediaAsset(UnityAsset asset) {
    try {
      File tempFile =
          Files.createTempFile(
                  "uview-preview-", asset.assetPath().replaceAll("[^a-zA-Z0-9.-]", "_"))
              .toFile();
      tempFile.deleteOnExit();
      Files.write(tempFile.toPath(), asset.content());

      if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(tempFile);
      } else {
        JOptionPane.showMessageDialog(
            this,
            "Cannot open media file automatically.\nIt has been saved to:\n"
                + tempFile.getAbsolutePath(),
            "Desktop Action Not Supported",
            JOptionPane.WARNING_MESSAGE);
      }
    } catch (IOException e) {
      JOptionPane.showMessageDialog(
          this,
          "Failed to create temporary file for preview: " + e.getMessage(),
          "Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private String getFileExtension(String filename) {
    int i = filename.lastIndexOf('.');
    if (i > 0 && i < filename.length() - 1) {
      return filename.substring(i + 1).toLowerCase();
    }
    return "";
  }
}
