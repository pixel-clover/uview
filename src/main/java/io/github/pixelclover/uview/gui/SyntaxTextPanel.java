package io.github.pixelclover.uview.gui;

import io.github.pixelclover.uview.model.UnityAsset;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;

public class SyntaxTextPanel extends JPanel {

  private final RSyntaxTextArea textArea;
  private final Consumer<Boolean> onDirtyStateChange;
  private DocumentListener dirtyStateListener;
  private String savedContent;

  public SyntaxTextPanel(UnityAsset asset, Consumer<Boolean> onDirtyStateChange) {
    super(new BorderLayout());
    this.onDirtyStateChange = onDirtyStateChange;

    textArea = new RSyntaxTextArea();
    textArea.setEditable(true);

    assert asset.content() != null;
    this.savedContent = new String(asset.content(), StandardCharsets.UTF_8);
    textArea.setText(savedContent);
    textArea.setCaretPosition(0);

    textArea.discardAllEdits();

    setSyntaxStyle(textArea, getFileExtension(asset.assetPath()));

    try {
      Theme theme =
          Theme.load(
              getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
      theme.apply(textArea);
      customizeHighlighting(textArea);
    } catch (Exception e) {
      // Fallback to default theme if there's an issue
    }

    addDirtyStateListener();
    RTextScrollPane scrollPane = new RTextScrollPane(textArea);
    add(scrollPane, BorderLayout.CENTER);
  }

  private void addDirtyStateListener() {
    if (dirtyStateListener == null) {
      dirtyStateListener =
          new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
              handleFirstEdit();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
              handleFirstEdit();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
              handleFirstEdit();
            }
          };
    }
    textArea.getDocument().addDocumentListener(dirtyStateListener);
  }

  private void handleFirstEdit() {
    onDirtyStateChange.accept(true);
    textArea.getDocument().removeDocumentListener(dirtyStateListener);
  }

  private void customizeHighlighting(RSyntaxTextArea textArea) {
    if (SyntaxConstants.SYNTAX_STYLE_CSHARP.equals(textArea.getSyntaxEditingStyle())) {
      SyntaxScheme scheme = textArea.getSyntaxScheme();

      Color defaultForegroundColor = scheme.getStyle(Token.IDENTIFIER).foreground;
      Color defaultBackgroundColor = textArea.getBackground();

      Style separatorStyle = scheme.getStyle(Token.SEPARATOR);

      separatorStyle.foreground = defaultForegroundColor;
      separatorStyle.background = defaultBackgroundColor;
    }
  }

  public String getText() {
    return textArea.getText();
  }

  public void markAsSaved() {
    this.savedContent = textArea.getText();
    textArea.discardAllEdits();
    onDirtyStateChange.accept(false);
    addDirtyStateListener();
  }

  public void revert() {
    textArea.getDocument().removeDocumentListener(dirtyStateListener);
    textArea.setText(this.savedContent);
    textArea.discardAllEdits();
    onDirtyStateChange.accept(false);
    addDirtyStateListener();
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
