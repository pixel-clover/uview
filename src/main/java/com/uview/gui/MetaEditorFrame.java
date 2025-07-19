package com.uview.gui;

import com.uview.core.PackageManager;
import com.uview.model.UnityAsset;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.nio.charset.StandardCharsets;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

public class MetaEditorFrame extends JDialog {

  private final RSyntaxTextArea textArea;
  private final UnityAsset asset;
  private final PackageManager packageManager;
  private final Runnable onSaveCallback;

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
