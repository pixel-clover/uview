package com.uview.core;

import com.uview.App;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class SettingsManager {

  private static final int MAX_RECENT_FILES = 5;
  private static final String LAST_DIRECTORY_KEY = "last_directory";
  private static final String RECENT_FILE_PREFIX = "recent_file_";

  private final Preferences prefs;

  public SettingsManager() {
    this.prefs = Preferences.userNodeForPackage(App.class);
  }

  public File getLastDirectory() {
    String path = prefs.get(LAST_DIRECTORY_KEY, null);
    if (path != null) {
      File dir = new File(path);
      if (dir.exists() && dir.isDirectory()) {
        return dir;
      }
    }
    // Fallback to the application's current working directory
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
    recentFiles.remove(file); // Remove if it already exists to avoid duplicates
    recentFiles.add(0, file); // Add to the top of the list

    // Trim the list and save back to preferences
    for (int i = 0; i < MAX_RECENT_FILES; i++) {
      if (i < recentFiles.size()) {
        prefs.put(RECENT_FILE_PREFIX + i, recentFiles.get(i).getAbsolutePath());
      } else {
        prefs.remove(RECENT_FILE_PREFIX + i);
      }
    }
  }
}
