package com.uview.gui;

import com.uview.model.UnityAsset;
import java.awt.BorderLayout;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

public class SyntaxTextPanel extends JPanel {

  private final RSyntaxTextArea textArea;
  private final String initialContent;
  private boolean isDirty = false;

  public SyntaxTextPanel(UnityAsset asset, Consumer<Boolean> onDirtyStateChange) {
    super(new BorderLayout());

    textArea = new RSyntaxTextArea();
    textArea.setEditable(true);

    assert asset.content() != null;
    initialContent = new String(asset.content(), StandardCharsets.UTF_8);
    textArea.setText(initialContent);
    textArea.setCaretPosition(0); // Scroll to the top

    setSyntaxStyle(textArea, getFileExtension(asset.assetPath()));

    try {
      Theme theme =
          Theme.load(
              getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
      theme.apply(textArea);
    } catch (Exception e) {
      // Fallback to default theme if there's an issue
    }

    textArea
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent e) {
                updateDirtyState();
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                updateDirtyState();
              }

              @Override
              public void changedUpdate(DocumentEvent e) {
                updateDirtyState();
              }

              private void updateDirtyState() {
                boolean wasDirty = isDirty;
                isDirty = !textArea.getText().equals(initialContent);
                if (isDirty != wasDirty) {
                  onDirtyStateChange.accept(isDirty);
                }
              }
            });

    RTextScrollPane scrollPane = new RTextScrollPane(textArea);
    add(scrollPane, BorderLayout.CENTER);
  }

  public String getText() {
    return textArea.getText();
  }

  public void revert() {
    textArea.setText(initialContent);
  }

  private void setSyntaxStyle(RSyntaxTextArea textArea, String extension) {
    String style =
        switch (extension) {
          case "cs" -> SyntaxConstants.SYNTAX_STYLE_CSHARP;
          case "java" -> SyntaxConstants.SYNTAX_STYLE_JAVA;
          case "js" -> SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
          case "json", "asmdef" -> SyntaxConstants.SYNTAX_STYLE_JSON;
          case "unity", "asset", "prefab", "mat", "controller" -> SyntaxConstants.SYNTAX_STYLE_YAML;
          case "html" -> SyntaxConstants.SYNTAX_STYLE_HTML;
          case "css", "uss" -> SyntaxConstants.SYNTAX_STYLE_CSS;
          case "sql" -> SyntaxConstants.SYNTAX_STYLE_SQL;
          case "sh" -> SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
          case "shader" -> SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS;
          case "xml" -> SyntaxConstants.SYNTAX_STYLE_XML;
          case "md" -> SyntaxConstants.SYNTAX_STYLE_MARKDOWN;
          default -> SyntaxConstants.SYNTAX_STYLE_NONE;
        };
    textArea.setSyntaxEditingStyle(style);
  }

  private String getFileExtension(String filename) {
    int i = filename.lastIndexOf('.');
    if (i > 0 && i < filename.length() - 1) {
      return filename.substring(i + 1).toLowerCase();
    }
    return "";
  }
}
