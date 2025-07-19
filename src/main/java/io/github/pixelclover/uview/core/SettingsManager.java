package io.github.pixelclover.uview.core;

import io.github.pixelclover.uview.App;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Manages persistent application settings using the Java Preferences API. This class handles
 * storing and retrieving user preferences like the last used directory and recently opened files.
 */
public class SettingsManager {

  private static final int MAX_RECENT_FILES = 5;
  private static final String LAST_DIRECTORY_KEY = "last_directory";
  private static final String RECENT_FILE_PREFIX = "recent_file_";

  // --- ADDED: Keys for persistent UI settings ---
  private static final String THEME_IS_DARK_KEY = "theme_is_dark";
  private static final String FONT_SIZE_KEY = "font_size";
  private static final int DEFAULT_FONT_SIZE = 12;

  private final Preferences prefs;

  public SettingsManager() {
    this(Preferences.userNodeForPackage(App.class));
  }

  public SettingsManager(Preferences prefs) {
    this.prefs = prefs;
  }

  // --- ADDED: Methods for theme persistence ---
  public boolean isDarkTheme() {
    return prefs.getBoolean(THEME_IS_DARK_KEY, false); // Default to light theme
  }

  public void setDarkTheme(boolean isDark) {
    prefs.putBoolean(THEME_IS_DARK_KEY, isDark);
  }

  // --- ADDED: Methods for font size persistence ---
  public int getFontSize() {
    return prefs.getInt(FONT_SIZE_KEY, DEFAULT_FONT_SIZE);
  }

  public void setFontSize(int size) {
    prefs.putInt(FONT_SIZE_KEY, size);
  }

  // --- ADDED: Method to reset UI settings to default ---
  public void resetUiSettings() {
    prefs.remove(THEME_IS_DARK_KEY);
    prefs.remove(FONT_SIZE_KEY);
  }

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

  public void setLastDirectory(File directory) {
    if (directory != null && directory.isDirectory()) {
      prefs.put(LAST_DIRECTORY_KEY, directory.getAbsolutePath());
    }
  }

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
