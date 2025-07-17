package com.uview;

import com.formdev.flatlaf.FlatLightLaf;
import com.uview.gui.MainWindow;
import javax.swing.SwingUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** The main entry point for the UView desktop application. */
public final class App {
  private static final Logger LOGGER = LogManager.getLogger(App.class);

  private App() {}

  /**
   * Initializes and runs the Swing GUI.
   *
   * @param args Command line arguments (not used).
   */
  public static void main(String[] args) {
    // Set up the FlatLaf Look and Feel
    FlatLightLaf.setup();

    SwingUtilities.invokeLater(
        () -> {
          MainWindow frame = new MainWindow();
          frame.setVisible(true);
        });
  }
}
