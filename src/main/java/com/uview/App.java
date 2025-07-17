package com.uview;

import com.uview.gui.MainWindow;
import javax.swing.*;
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
    SwingUtilities.invokeLater(
        () -> {
          try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
          } catch (Exception e) {
            LOGGER.warn("Could not set system look and feel.", e);
          }
          MainWindow frame = new MainWindow();
          frame.setVisible(true);
        });
  }
}
