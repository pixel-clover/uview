package io.github.pixelclover.uview.gui;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IconManagerTest {
  @ParameterizedTest
  @ValueSource(
      strings = {
        "cs",
        "png",
        "jpg",
        "jpeg",
        "gif",
        "tga",
        "psd",
        "bmp",
        "svg",
        "tiff",
        "tif",
        "ttf",
        "shader",
        "fbx",
        "prefab",
        "anim",
        "mp4",
        "mov",
        "wav",
        "mp3",
        "txt",
        "mat",
        "unity",
        "json",
        "xml",
        "controller",
        "asmdef",
        "css",
        "uss",
        "html",
        "pdf"
      })
  void supportedIconsLoadAndPaint(String extension) throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      Icon icon = IconManager.getIconForFile("sample." + extension);
      assertNotNull(icon);
      assertNotSame(IconManager.getIconForFile("unknown.extension"), icon);
      assertEquals(16, icon.getIconWidth());
      assertEquals(16, icon.getIconHeight());
      assertSame(
          icon,
          IconManager.getIconForFile("sample." + extension.toUpperCase(java.util.Locale.ROOT)));
      BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
      var graphics = image.createGraphics();
      try {
        icon.paintIcon(null, graphics, 0, 0);
      } finally {
        graphics.dispose();
      }
      boolean painted = false;
      for (int y = 0; y < 16; y++) {
        for (int x = 0; x < 16; x++) {
          painted |= (image.getRGB(x, y) >>> 24) != 0;
        }
      }
      assertTrue(painted, "Icon must paint visible pixels");
    });
  }

  @Test
  void missingOrUnknownExtensionsUseTheDefaultIcon() {
    Icon fallback = IconManager.getIconForFile("unknown.extension");
    assertNotNull(fallback);
    for (String name : new String[] {null, "", "README", "file.", ".gitignore"}) {
      assertSame(fallback, IconManager.getIconForFile(name));
    }
    assertNotNull(IconManager.getFolderIcon());
    assertNotSame(fallback, IconManager.getFolderIcon());
  }
}
