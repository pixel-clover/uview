package io.github.pixelclover.uview.gui;

import io.github.pixelclover.uview.core.PackageManager;
import io.github.pixelclover.uview.model.UnityAsset;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import javax.swing.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * A dialog for editing the YAML-based .meta file associated with a {@link UnityAsset}. It provides
 * a syntax-highlighted text area for editing.
 */
public class MetaEditorFrame extends JDialog {

  private final RSyntaxTextArea textArea;
  private final UnityAsset asset;
  private final PackageManager packageManager;
  private final Runnable onSaveCallback;

  /**
   * Constructs a MetaEditorFrame.
   *
   * @param owner The parent frame.
   * @param asset The asset whose meta file is to be edited.
   * @param packageManager The package manager to handle the update.
   * @param onSaveCallback A callback to execute after saving changes.
   */
  public MetaEditorFrame(
      JFrame owner, UnityAsset asset, PackageManager packageManager, Runnable onSaveCallback) {
    super(owner, "Edit Meta: " + asset.assetPath(), true);
    this.asset = asset;
    this.packageManager = packageManager;
    this.onSaveCallback = onSaveCallback;

    setSize(600, 400);
    setLocationRelativeTo(owner);
    setLayout(new BorderLayout(0, 5));

    textArea = createTextArea();
    add(new RTextScrollPane(textArea), BorderLayout.CENTER);
    add(createButtonPanel(), BorderLayout.SOUTH);
  }

  private RSyntaxTextArea createTextArea() {
    RSyntaxTextArea rSyntaxTextArea = new RSyntaxTextArea();
    rSyntaxTextArea.setEditable(true);

    if (asset.metaContent() != null) {
      rSyntaxTextArea.setText(new String(asset.metaContent(), StandardCharsets.UTF_8));
    } else {
      String defaultMeta = String.format("fileFormatVersion: 2\nguid: %s\n", asset.guid());
      rSyntaxTextArea.setText(defaultMeta);
    }
    rSyntaxTextArea.setCaretPosition(0);

    // Apply syntax highlighting and theme
    rSyntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_YAML);
    try {
      Theme theme =
          Theme.load(
              getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
      theme.apply(rSyntaxTextArea);
    } catch (Exception e) {
      // Ignore, fallback to default theme
    }
    return rSyntaxTextArea;
  }

  private JPanel createButtonPanel() {
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());
    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(e -> saveChanges());

    buttonPanel.add(cancelButton);
    buttonPanel.add(saveButton);
    return buttonPanel;
  }

  private void saveChanges() {
    String newMetaText = textArea.getText();
    packageManager.updateAssetMeta(asset.assetPath(), newMetaText.getBytes(StandardCharsets.UTF_8));
    onSaveCallback.run();
    dispose();
  }
}
