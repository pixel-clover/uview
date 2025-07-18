package com.uview.gui;

import com.uview.model.UnityAsset;
import java.awt.BorderLayout;
import java.nio.charset.StandardCharsets;
import javax.swing.JPanel;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

public class SyntaxTextPanel extends JPanel {

  public SyntaxTextPanel(UnityAsset asset) {
    super(new BorderLayout());

    RSyntaxTextArea textArea = new RSyntaxTextArea();
    textArea.setEditable(false);
    assert asset.content() != null;
    textArea.setText(new String(asset.content(), StandardCharsets.UTF_8));
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

    RTextScrollPane scrollPane = new RTextScrollPane(textArea);
    add(scrollPane, BorderLayout.CENTER);
  }

  private void setSyntaxStyle(RSyntaxTextArea textArea, String extension) {
    String style =
        switch (extension) {
          case "cs" -> SyntaxConstants.SYNTAX_STYLE_CSHARP;
          case "java" -> SyntaxConstants.SYNTAX_STYLE_JAVA;
          case "js" -> SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
          case "json" -> SyntaxConstants.SYNTAX_STYLE_JSON;
          case "unity", "asset", "prefab", "mat" -> SyntaxConstants.SYNTAX_STYLE_YAML;
          case "html" -> SyntaxConstants.SYNTAX_STYLE_HTML;
          case "css" -> SyntaxConstants.SYNTAX_STYLE_CSS;
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
