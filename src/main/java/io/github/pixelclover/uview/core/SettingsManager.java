package io.github.pixelclover.uview.core;

import io.github.pixelclover.uview.App;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Manages application settings using the Java Preferences API. This class provides a persistent,
 * cross-platform way to store user settings like theme, font preferences, and recently opened
 * files.
 */
public class SettingsManager {

  private static final int MAX_RECENT_FILES = 5;
  private static final String LAST_DIRECTORY_KEY = "last_directory";
  private static final String RECENT_FILE_PREFIX = "recent_file_";

  private static final String THEME_IS_DARK_KEY = "theme_is_dark";
  private static final String FONT_SIZE_KEY = "font_size";
  private static final String FONT_FAMILY_KEY = "font_family";
  private static final int DEFAULT_FONT_SIZE = 12;
  private static final String DEFAULT_FONT_FAMILY = Font.SANS_SERIF;

  private final Preferences prefs;

  /** Constructs a SettingsManager using the default application preferences node. */
  public SettingsManager() {
    this(Preferences.userNodeForPackage(App.class));
  }

  /**
   * Constructs a SettingsManager with a specific preferences node. Used for testing.
   *
   * @param prefs The preferences node to use for storage.
   */
  public SettingsManager(Preferences prefs) {
    this.prefs = prefs;
  }

  /**
   * Checks if the dark theme is currently enabled.
   *
   * @return true if dark theme is set, false otherwise.
   */
  public boolean isDarkTheme() {
    return prefs.getBoolean(THEME_IS_DARK_KEY, false);
  }

  /**
   * Sets the theme preference.
   *
   * @param isDark true to enable dark theme, false for light theme.
   */
  public void setDarkTheme(boolean isDark) {
    prefs.putBoolean(THEME_IS_DARK_KEY, isDark);
  }

  /**
   * Gets the global font size for the UI.
   *
   * @return The stored font size, or a default value if not set.
   */
  public int getFontSize() {
    return prefs.getInt(FONT_SIZE_KEY, DEFAULT_FONT_SIZE);
  }

  /**
   * Sets the global font size for the UI.
   *
   * @param size The new font size.
   */
  public void setFontSize(int size) {
    prefs.putInt(FONT_SIZE_KEY, size);
  }

  /**
   * Gets the global font family for the UI.
   *
   * @return The stored font family name, or a default value if not set.
   */
  public String getFontFamily() {
    return prefs.get(FONT_FAMILY_KEY, DEFAULT_FONT_FAMILY);
  }

  /**
   * Sets the global font family for the UI.
   *
   * @param fontFamily The name of the new font family.
   */
  public void setFontFamily(String fontFamily) {
    prefs.put(FONT_FAMILY_KEY, fontFamily);
  }

  /** Resets all UI-related settings (theme and font) to their default states. */
  public void resetUiSettings() {
    prefs.remove(THEME_IS_DARK_KEY);
    prefs.remove(FONT_SIZE_KEY);
    prefs.remove(FONT_FAMILY_KEY);
  }

  /**
   * Gets the last directory accessed by the user via a file chooser.
   *
   * @return The last used directory, or the current working directory if none is stored or valid.
   */
  public File getLastDirectory() {
    String path = prefs.get(LAST_DIRECTORY_KEY, null);
    if (path != null) {
      File dir = new File(path);
      if (dir.exists() && dir.isDirectory()) {
        return dir;
      }
    }
    return new File(".");
  }

  /**
   * Stores the last directory accessed by the user.
   *
   * @param directory The directory to store.
   */
  public void setLastDirectory(File directory) {
    if (directory != null && directory.isDirectory()) {
      prefs.put(LAST_DIRECTORY_KEY, directory.getAbsolutePath());
    }
  }

  /**
   * Gets the list of recently opened package files.
   *
   * @return A list of {@link File} objects, ordered from most to least recent.
   */
  public List<File> getRecentFiles() {
    List<File> recentFiles = new ArrayList<>();
    for (int i = 0; i < MAX_RECENT_FILES; i++) {
      String path = prefs.get(RECENT_FILE_PREFIX + i, null);
      if (path != null) {
        recentFiles.add(new File(path));
      }
    }
    return recentFiles;
  }

  /**
   * Adds a file to the top of the recent files list. If the file is already in the list, it is
   * moved to the top. If the list exceeds its maximum size, the oldest entry is removed.
   *
   * @param file The file to add to the recent list.
   */
  public void addRecentFile(File file) {
    List<File> recentFiles = getRecentFiles();
    recentFiles.remove(file);
    recentFiles.add(0, file);

    for (int i = 0; i < MAX_RECENT_FILES; i++) {
      if (i < recentFiles.size()) {
        prefs.put(RECENT_FILE_PREFIX + i, recentFiles.get(i).getAbsolutePath());
      } else {
        prefs.remove(RECENT_FILE_PREFIX + i);
      }
    }
  }
}
