package io.github.pixelclover.uview.gui;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.GraphicsEnvironment;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class FontManagerTest {

  @Test
  void loadAndRegisterFontsDoesNotThrowException() {
    // This test can only run in a headful environment.
    Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());

    // The test passes if this method executes without throwing any exceptions.
    assertDoesNotThrow(
        FontManager::loadAndRegisterFonts,
        "FontManager.loadAndRegisterFonts() should not throw any exceptions.");
  }
}
