package io.github.pixelclover.uview.gui;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** A utility class to handle loading and registering custom fonts from application resources. */
public final class FontManager {

  private static final Logger LOGGER = LogManager.getLogger(FontManager.class);

  private FontManager() {}

  /** Loads custom fonts from the classpath and registers them with the graphics environment. */
  public static void loadAndRegisterFonts() {
    // We only need to load the main variants for the family to be recognized.
    String[] fontPaths = {
      "/fonts/jetbrains/ttf/JetBrainsMono-Regular.ttf",
      "/fonts/jetbrains/ttf/JetBrainsMono-Bold.ttf",
      "/fonts/jetbrains/ttf/JetBrainsMono-Italic.ttf",
      "/fonts/jetbrains/ttf/JetBrainsMono-BoldItalic.ttf"
    };

    for (String path : fontPaths) {
      try (InputStream is = FontManager.class.getResourceAsStream(path)) {
        if (is == null) {
          LOGGER.error("Custom font not found at resource path: {}", path);
          continue;
        }
        Font customFont = Font.createFont(Font.TRUETYPE_FONT, new BufferedInputStream(is));
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
        LOGGER.info("Successfully registered font: {}", customFont.getFontName());
      } catch (IOException | FontFormatException e) {
        LOGGER.error("Failed to load and register font from path: {}", path, e);
      }
    }
  }
}
