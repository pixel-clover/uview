package com.uview.gui;

import com.uview.model.UnityAsset;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.Set;
import javax.swing.*;

public class AssetViewerFrame extends JFrame {

  private static final Set<String> TEXT_EXTENSIONS =
      Set.of(
          "cs",
          "java",
          "js",
          "txt",
          "json",
          "asmdef",
          "xml",
          "shader",
          "mat",
          "unity",
          "asset",
          "prefab",
          "html",
          "css",
          "uss",
          "sql",
          "sh",
          "yaml",
          "md",
          "controller",
          "meta");
  private static final Set<String> IMAGE_EXTENSIONS =
      Set.of("png", "jpg", "jpeg", "gif", "tga", "bmp", "webp", "svg", "ico", "avif", "tiff");
  private static final Set<String> MEDIA_EXTENSIONS = Set.of("mp4", "mov", "wav", "mp3", "ogg");
  private static final DecimalFormat FILE_SIZE_FORMAT = new DecimalFormat("#,##0.0 KB");

  public AssetViewerFrame(JFrame owner, UnityAsset asset) {
    setTitle("Asset Viewer - " + asset.assetPath());
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(900, 700);
    setLocationRelativeTo(owner);
    setLayout(new BorderLayout());

    JPanel contentPanel = createContentPanel(asset);
    if (contentPanel == null) {
      dispose(); // Frame was closed by media handler
      return;
    }

    add(contentPanel, BorderLayout.CENTER);
    add(createFooterPanel(asset), BorderLayout.SOUTH);
  }

  private JPanel createFooterPanel(UnityAsset asset) {
    JPanel footer = new JPanel();
    footer.setLayout(new BoxLayout(footer, BoxLayout.X_AXIS));
    footer.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

    // Path
    JLabel pathLabel = new JLabel(asset.assetPath());
    pathLabel.setToolTipText(asset.assetPath()); // Show full path on hover

    // Size
    String size = "N/A (Directory)";
    if (asset.content() != null) {
      double sizeInKb = asset.content().length / 1024.0;
      size = FILE_SIZE_FORMAT.format(sizeInKb);
    }
    JLabel sizeLabel = new JLabel(size);

    // GUID
    JLabel guidLabel = new JLabel("GUID: " + asset.guid());

    footer.add(pathLabel);
    footer.add(Box.createHorizontalGlue());
    footer.add(guidLabel);
    footer.add(Box.createRigidArea(new Dimension(15, 0)));
    footer.add(sizeLabel);

    return footer;
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
      assert asset.content() != null;
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
