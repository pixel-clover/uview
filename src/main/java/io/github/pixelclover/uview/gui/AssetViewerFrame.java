package io.github.pixelclover.uview.gui;

import io.github.pixelclover.uview.core.PackageManager;
import io.github.pixelclover.uview.model.UnityAsset;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.Set;
import javax.swing.*;

/**
 * A frame for viewing and potentially editing a single {@link UnityAsset}. It determines the
 * appropriate viewer (text editor, image viewer, etc.) based on the asset's file extension.
 */
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
          "meta",
          "lighting");
  private static final Set<String> IMAGE_EXTENSIONS =
      Set.of(
          "png", "jpg", "jpeg", "gif", "tga", "bmp", "webp", "svg", "ico", "avif", "tiff", "tif");
  private static final Set<String> AUDIO_EXTENSIONS = Set.of("wav", "mp3", "ogg");
  private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "mov");
  private static final Set<String> PDF_EXTENSIONS = Set.of("pdf");
  private static final DecimalFormat FILE_SIZE_FORMAT = new DecimalFormat("#,##0.0 KB");

  private final PackageManager packageManager;
  private final Runnable onSaveCallback;
  private PdfViewerPanel pdfPanel;
  private AudioPlayerPanel audioPanel;

  /**
   * Constructs a new AssetViewerFrame.
   *
   * @param owner The parent frame.
   * @param asset The asset to display.
   * @param packageManager The package manager instance for handling updates.
   * @param onSaveCallback A callback to execute when the asset's content is saved.
   */
  public AssetViewerFrame(
      JFrame owner, UnityAsset asset, PackageManager packageManager, Runnable onSaveCallback) {
    this.packageManager = packageManager;
    this.onSaveCallback = onSaveCallback;

    setTitle("Asset Viewer - " + asset.assetPath());
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(900, 700);
    setLocationRelativeTo(owner);
    setLayout(new BorderLayout());

    JPanel contentPanel = createContentPanel(asset);
    if (contentPanel == null) {
      dispose();
      return;
    }

    add(contentPanel, BorderLayout.CENTER);
    add(createFooterPanel(asset), BorderLayout.SOUTH);

    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosed(WindowEvent e) {
            // Clean up resources used by viewers.
            if (pdfPanel != null) {
              try {
                pdfPanel.close();
              } catch (IOException ex) {
                System.err.println("Failed to close PDF document: " + ex.getMessage());
              }
            }
            if (audioPanel != null) {
              audioPanel.close();
            }
          }
        });
  }

  private JPanel createFooterPanel(UnityAsset asset) {
    JPanel footer = new JPanel();
    footer.setLayout(new BoxLayout(footer, BoxLayout.X_AXIS));
    footer.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

    JLabel pathLabel = new JLabel(asset.assetPath());
    pathLabel.setToolTipText(asset.assetPath());

    String size = "N/A (Directory)";
    if (asset.content() != null) {
      double sizeInKb = asset.content().length / 1024.0;
      size = FILE_SIZE_FORMAT.format(sizeInKb);
    }
    JLabel sizeLabel = new JLabel(size);

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
      return createTextEditorPanel(asset);
    }

    JPanel contentWrapperPanel = new JPanel(new BorderLayout());

    if (IMAGE_EXTENSIONS.contains(extension)) {
      ImageIcon imageIcon = new ImageIcon(asset.content());
      JLabel imageLabel = new JLabel(imageIcon);
      contentWrapperPanel.add(new JScrollPane(imageLabel), BorderLayout.CENTER);
    } else if (PDF_EXTENSIONS.contains(extension)) {
      try {
        this.pdfPanel = new PdfViewerPanel(asset.content());
        contentWrapperPanel.add(this.pdfPanel, BorderLayout.CENTER);
      } catch (IOException e) {
        JLabel errorLabel = new JLabel("Failed to load PDF: " + e.getMessage());
        errorLabel.setHorizontalAlignment(JLabel.CENTER);
        contentWrapperPanel.add(errorLabel, BorderLayout.CENTER);
      }
    } else if (AUDIO_EXTENSIONS.contains(extension)) {
      try {
        this.audioPanel = new AudioPlayerPanel(asset.content());
        contentWrapperPanel.add(this.audioPanel, BorderLayout.CENTER);
      } catch (Exception e) {
        JLabel errorLabel = new JLabel("Failed to load audio: " + e.getMessage());
        errorLabel.setHorizontalAlignment(JLabel.CENTER);
        contentWrapperPanel.add(errorLabel, BorderLayout.CENTER);
      }
    } else if (VIDEO_EXTENSIONS.contains(extension)) {
      handleMediaAsset(asset);
      return null;
    } else {
      contentWrapperPanel.add(
          new JLabel("Binary content cannot be previewed."), BorderLayout.CENTER);
    }
    return contentWrapperPanel;
  }

  private JPanel createTextEditorPanel(UnityAsset asset) {
    JPanel editorPanel = new JPanel(new BorderLayout(0, 5));

    JButton saveButton = new JButton("Save");
    saveButton.setEnabled(false);
    JButton revertButton = new JButton("Revert");
    revertButton.setEnabled(false);

    SyntaxTextPanel syntaxTextPanel =
        new SyntaxTextPanel(
            asset,
            isDirty -> {
              saveButton.setEnabled(isDirty);
              revertButton.setEnabled(isDirty);
            });

    saveButton.addActionListener(
        e -> {
          byte[] newContent = syntaxTextPanel.getText().getBytes(StandardCharsets.UTF_8);
          packageManager.updateAssetContent(asset.assetPath(), newContent);
          syntaxTextPanel.markAsSaved();
          onSaveCallback.run();
        });

    revertButton.addActionListener(e -> syntaxTextPanel.revert());

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(revertButton);
    buttonPanel.add(saveButton);

    editorPanel.add(syntaxTextPanel, BorderLayout.CENTER);
    editorPanel.add(buttonPanel, BorderLayout.SOUTH);

    return editorPanel;
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
