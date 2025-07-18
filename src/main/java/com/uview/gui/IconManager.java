package com.uview.gui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class IconManager {

  private static final Logger LOGGER = LogManager.getLogger(IconManager.class);
  private static final Map<String, Icon> ICON_CACHE = new HashMap<>();
  private static final Icon DEFAULT_FILE_ICON;
  private static final Icon FOLDER_ICON;

  static {
    FOLDER_ICON = loadIcon("folder.svg");
    DEFAULT_FILE_ICON = loadIcon("file_default.svg");

    ICON_CACHE.put("folder", FOLDER_ICON);
    ICON_CACHE.put("file", DEFAULT_FILE_ICON);
    ICON_CACHE.put("cs", loadIcon("file_csharp.svg"));
    ICON_CACHE.put("png", loadIcon("file_image.svg"));
    ICON_CACHE.put("jpg", loadIcon("file_image.svg"));
    ICON_CACHE.put("jpeg", loadIcon("file_image.svg"));
    ICON_CACHE.put("gif", loadIcon("file_image.svg"));
    ICON_CACHE.put("tga", loadIcon("file_image.svg"));
    ICON_CACHE.put("psd", loadIcon("file_image.svg"));
    ICON_CACHE.put("bmp", loadIcon("file_image.svg"));
    ICON_CACHE.put("svg", loadIcon("file_image.svg"));
    ICON_CACHE.put("tiff", loadIcon("file_image.svg"));
    ICON_CACHE.put("ttf", loadIcon("file_font.svg"));
    ICON_CACHE.put("shader", loadIcon("file_code.svg"));
    ICON_CACHE.put("fbx", loadIcon("file_archive.svg"));
    ICON_CACHE.put("prefab", loadIcon("file_assembly.svg"));
    ICON_CACHE.put("anim", loadIcon("file_video.svg"));
    ICON_CACHE.put("mp4", loadIcon("file_video.svg"));
    ICON_CACHE.put("mov", loadIcon("file_video.svg"));
    ICON_CACHE.put("wav", loadIcon("file_audio.svg"));
    ICON_CACHE.put("mp3", loadIcon("file_audio.svg"));
    ICON_CACHE.put("txt", loadIcon("file_text.svg"));
    ICON_CACHE.put("mat", loadIcon("file_json.svg"));
    ICON_CACHE.put("unity", loadIcon("file_diagram.svg"));
    ICON_CACHE.put("json", loadIcon("file_json.svg"));
    ICON_CACHE.put("xml", loadIcon("file_xml.svg"));
    ICON_CACHE.put("controller", loadIcon("file_controller.svg"));
    ICON_CACHE.put("asmdef", loadIcon("file_assembly.svg"));
    ICON_CACHE.put("css", loadIcon("file_css.svg"));
    ICON_CACHE.put("uss", loadIcon("file_css.svg"));
    ICON_CACHE.put("html", loadIcon("file_html.svg"));
  }

  private IconManager() {}

  private static Icon loadIcon(String iconName) {
    String resourcePath = "/icons/" + iconName;
    java.net.URL resourceUrl = IconManager.class.getResource(resourcePath);

    if (resourceUrl == null) {
      LOGGER.error("Icon resource not found at path: {}", resourcePath);
      return null;
    }

    try {
      // Use the URL-based constructor for robustness
      FlatSVGIcon icon = new FlatSVGIcon(resourceUrl);

      if (icon.getIconWidth() == 0) {
        LOGGER.error(
            "Failed to parse SVG data for icon: {}. The file may be empty or corrupt.",
            resourcePath);
        return null;
      }
      return icon.derive(16, 16);
    } catch (Exception e) {
      LOGGER.error("Exception while loading icon from URL: {}", resourceUrl, e);
      return null;
    }
  }

  public static Icon getIconForFile(String filename) {
    if (filename == null || filename.isEmpty()) {
      return DEFAULT_FILE_ICON;
    }

    String extension = "";
    int i = filename.lastIndexOf('.');
    if (i > 0 && i < filename.length() - 1) {
      extension = filename.substring(i + 1).toLowerCase();
    }

    return ICON_CACHE.getOrDefault(extension, DEFAULT_FILE_ICON);
  }

  public static Icon getFolderIcon() {
    return FOLDER_ICON;
  }
}
