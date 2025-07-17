package com.uview;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import com.uview.gui.MainWindow;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class App {
  private static final Logger LOGGER = LogManager.getLogger(App.class);

  private App() {}

  public static void main(String[] args) {
    FlatLightLaf.setup();
    FlatInspector.install("ctrl shift alt X");
    FlatUIDefaultsInspector.install("ctrl shift alt Y");

    SwingUtilities.invokeLater(
        () -> {
          MainWindow frame = new MainWindow();
          frame.setVisible(true);
        });
  }
}
