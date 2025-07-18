package com.uview.gui;

import com.uview.model.UnityAsset;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

public class AssetViewerFrame extends JFrame {

  private static final Set<String> TEXT_EXTENSIONS =
      Set.of(
          "cs", "java", "js", "txt", "json", "xml", "shader", "mat", "unity", "asset", "prefab",
          "html", "css", "sql", "sh");
  private static final Set<String> IMAGE_EXTENSIONS =
      Set.of("png", "jpg", "jpeg", "gif", "tga", "bmp");
  private static final Set<String> MEDIA_EXTENSIONS = Set.of("mp4", "mov", "wav", "mp3", "ogg");
  private static final DecimalFormat FILE_SIZE_FORMAT = new DecimalFormat("#,##0.0 KB");

  public AssetViewerFrame(JFrame owner, UnityAsset asset) {
    setTitle("Asset Viewer - " + asset.assetPath());
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(900, 700);
    setLocationRelativeTo(owner);
    setLayout(new BorderLayout());

    JPanel metadataPanel = createMetadataPanel(asset);
    JPanel contentPanel = createContentPanel(asset);

    if (contentPanel == null) {
      dispose(); // Frame was closed by media handler
      return;
    }

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, metadataPanel, contentPanel);
    splitPane.setDividerLocation(300);

    add(splitPane, BorderLayout.CENTER);
  }

  private JPanel createMetadataPanel(UnityAsset asset) {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    String size = "N/A (Directory)";
    if (asset.content() != null) {
      double sizeInKb = asset.content().length / 1024.0;
      size = FILE_SIZE_FORMAT.format(sizeInKb);
    }

    addMetadataRow(panel, "Path:", asset.assetPath(), 0);
    addMetadataRow(panel, "GUID:", asset.guid(), 1);
    addMetadataRow(panel, "Size:", size, 2);

    // Add a filler component to push everything to the top
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridy = 3;
    gbc.weighty = 1.0;
    panel.add(new JLabel(), gbc);

    return panel;
  }

  private void addMetadataRow(JPanel panel, String key, String value, int gridY) {
    GridBagConstraints gbc = new GridBagConstraints();

    // Key label
    gbc.gridx = 0;
    gbc.gridy = gridY;
    gbc.anchor = GridBagConstraints.FIRST_LINE_START;
    gbc.insets = new Insets(0, 0, 10, 10);
    gbc.weightx = 0;
    JLabel keyLabel = new JLabel(key);
    keyLabel.setFont(keyLabel.getFont().deriveFont(java.awt.Font.BOLD));
    panel.add(keyLabel, gbc);

    // Value text area (for wrapping and selection)
    gbc.gridx = 1;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    JTextArea valueArea = new JTextArea(value);
    valueArea.setWrapStyleWord(true);
    valueArea.setLineWrap(true);
    valueArea.setEditable(false);
    panel.add(valueArea, gbc);
  }

  private JPanel createContentPanel(UnityAsset asset) {
    if (asset.content() == null) {
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(new JLabel("This is a directory."), BorderLayout.CENTER);
      return panel;
    }

    String extension = getFileExtension(asset.assetPath());

    if (TEXT_EXTENSIONS.contains(extension)) {
      return new SyntaxTextPanel(asset);
    }

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    if (IMAGE_EXTENSIONS.contains(extension)) {
      ImageIcon imageIcon = new ImageIcon(asset.content());
      JLabel imageLabel = new JLabel(imageIcon);
      panel.add(new JScrollPane(imageLabel), BorderLayout.CENTER);
    } else if (MEDIA_EXTENSIONS.contains(extension)) {
      handleMediaAsset(asset);
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
