package io.github.pixelclover.uview;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import io.github.pixelclover.uview.gui.FontManager;
import io.github.pixelclover.uview.gui.MainWindow;
import javax.swing.*;

public final class App {
  private App() {}

  public static void main(String[] args) {
    FlatLightLaf.setup();
    FlatInspector.install("ctrl shift alt X");
    FlatUIDefaultsInspector.install("ctrl shift alt Y");

    // Load custom fonts on startup
    FontManager.loadAndRegisterFonts();

    SwingUtilities.invokeLater(
        () -> {
          MainWindow frame = new MainWindow();
          frame.setVisible(true);
        });
  }
}
