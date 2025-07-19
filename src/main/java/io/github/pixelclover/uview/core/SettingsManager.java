package io.github.pixelclover.uview.core;

import io.github.pixelclover.uview.App;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

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

  public SettingsManager() {
    this(Preferences.userNodeForPackage(App.class));
  }

  public SettingsManager(Preferences prefs) {
    this.prefs = prefs;
  }

  public boolean isDarkTheme() {
    return prefs.getBoolean(THEME_IS_DARK_KEY, false);
  }

  public void setDarkTheme(boolean isDark) {
    prefs.putBoolean(THEME_IS_DARK_KEY, isDark);
  }

  public int getFontSize() {
    return prefs.getInt(FONT_SIZE_KEY, DEFAULT_FONT_SIZE);
  }

  public void setFontSize(int size) {
    prefs.putInt(FONT_SIZE_KEY, size);
  }

  public String getFontFamily() {
    return prefs.get(FONT_FAMILY_KEY, DEFAULT_FONT_FAMILY);
  }

  public void setFontFamily(String fontFamily) {
    prefs.put(FONT_FAMILY_KEY, fontFamily);
  }

  public void resetUiSettings() {
    prefs.remove(THEME_IS_DARK_KEY);
    prefs.remove(FONT_SIZE_KEY);
    prefs.remove(FONT_FAMILY_KEY);
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
