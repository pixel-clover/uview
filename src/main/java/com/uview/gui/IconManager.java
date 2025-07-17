package com.uview.gui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Manages loading and caching of file-specific SVG icons from the FlatLaf library. */
public final class IconManager {

  private static final Logger LOGGER = LogManager.getLogger(IconManager.class);
  private static final Map<String, Icon> ICON_CACHE = new HashMap<>();
  private static final Icon DEFAULT_FILE_ICON;
  private static final Icon FOLDER_ICON;

  static {
    // Pre-load common icons into the cache.
    FOLDER_ICON = loadIcon("nodes/folder.svg");
    DEFAULT_FILE_ICON = loadIcon("fileTypes/any_type.svg");

    ICON_CACHE.put("folder", FOLDER_ICON);
    ICON_CACHE.put("file", DEFAULT_FILE_ICON);
    ICON_CACHE.put("cs", loadIcon("fileTypes/csharp.svg"));
    ICON_CACHE.put("png", loadIcon("fileTypes/image.svg"));
    ICON_CACHE.put("jpg", loadIcon("fileTypes/image.svg"));
    ICON_CACHE.put("jpeg", loadIcon("fileTypes/image.svg"));
    ICON_CACHE.put("fbx", loadIcon("fileTypes/archive.svg"));
    ICON_CACHE.put("prefab", loadIcon("nodes/ppLib.svg"));
    ICON_CACHE.put("anim", loadIcon("actions/execute.svg"));
    ICON_CACHE.put("mat", loadIcon("nodes/color.svg"));
    ICON_CACHE.put("unity", loadIcon("fileTypes/diagram.svg"));
  }

  private IconManager() {}

  private static Icon loadIcon(String relativePath) {
    String fullPath = "/com/formdev/flatlaf/extras/icons/" + relativePath;
    try {
      return new FlatSVGIcon(fullPath, IconManager.class.getClassLoader());
    } catch (Exception e) {
      LOGGER.warn("Could not load icon at path: {}", fullPath, e);
      return null;
    }
  }

  /**
   * Gets a specific icon based on a file's extension.
   *
   * @param filename The name of the file.
   * @return The corresponding Icon, or a default file icon.
   */
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

  /**
   * Gets the generic folder icon.
   *
   * @return The folder Icon.
   */
  public static Icon getFolderIcon() {
    return FOLDER_ICON;
  }
}
