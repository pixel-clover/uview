package io.github.pixelclover.uview.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SettingsManagerTest {

  @TempDir Path tempDir;
  private SettingsManager settingsManager;
  private Preferences prefs;

  @BeforeEach
  void setUp() throws BackingStoreException {
    // Use a unique preference node path just for this test.
    // This isolates the test from the actual application settings.
    prefs = Preferences.userRoot().node("/io/github/pixelclover/uview/test_prefs");
    settingsManager = new SettingsManager(prefs); // Inject the test-specific preferences

    // Clear preferences BEFORE each test to ensure a clean state.
    prefs.clear();
  }

  @AfterEach
  void tearDown() throws BackingStoreException {
    // Also clear after to clean up the last test run.
    prefs.clear();
  }

  @Test
  void getLastDirectory_shouldReturnDefaultWhenNotSet() throws IOException {
    // The fallback is `new File(".")`. We should compare its canonical path.
    File expectedDefault = new File(".");
    assertEquals(
        expectedDefault.getCanonicalPath(), settingsManager.getLastDirectory().getCanonicalPath());
  }

  @Test
  void setAndGetLastDirectory_shouldStoreAndRetrieveCorrectly() {
    File dir = tempDir.toFile();
    settingsManager.setLastDirectory(dir);
    assertEquals(dir.getAbsolutePath(), settingsManager.getLastDirectory().getAbsolutePath());
  }

  @Test
  void getLastDirectory_shouldReturnDefaultIfStoredPathIsInvalid() throws IOException {
    prefs.put("last_directory", "/a/path/that/does/not/exist");
    File expectedDefault = new File(".");
    assertEquals(
        expectedDefault.getCanonicalPath(), settingsManager.getLastDirectory().getCanonicalPath());
  }

  @Test
  void addRecentFile_shouldAddNewFileToTop() throws IOException {
    File file1 = tempDir.resolve("file1.txt").toFile();
    assertTrue(file1.createNewFile());

    settingsManager.addRecentFile(file1);

    List<File> recentFiles = settingsManager.getRecentFiles();
    assertEquals(1, recentFiles.size());
    assertEquals(file1.getAbsolutePath(), recentFiles.get(0).getAbsolutePath());
  }

  @Test
  void addRecentFile_shouldMoveExistingFileToTop() throws IOException {
    File file1 = tempDir.resolve("file1.txt").toFile();
    File file2 = tempDir.resolve("file2.txt").toFile();
    assertTrue(file1.createNewFile());
    assertTrue(file2.createNewFile());

    settingsManager.addRecentFile(file1);
    settingsManager.addRecentFile(file2);
    settingsManager.addRecentFile(file1); // Add file1 again

    List<File> recentFiles = settingsManager.getRecentFiles();
    assertEquals(2, recentFiles.size());
    assertEquals(file1.getAbsolutePath(), recentFiles.get(0).getAbsolutePath());
    assertEquals(file2.getAbsolutePath(), recentFiles.get(1).getAbsolutePath());
  }

  @Test
  void addRecentFile_shouldRespectMaxFilesLimit() throws IOException {
    for (int i = 0; i < 7; i++) {
      File file = tempDir.resolve("file" + i + ".txt").toFile();
      assertTrue(file.createNewFile());
      settingsManager.addRecentFile(file);
    }

    List<File> recentFiles = settingsManager.getRecentFiles();
    assertEquals(5, recentFiles.size()); // MAX_RECENT_FILES is 5
    // The most recent file should be "file6.txt"
    assertEquals(
        tempDir.resolve("file6.txt").toAbsolutePath().toString(),
        recentFiles.get(0).getAbsolutePath());
    // The oldest file in the list should be "file2.txt"
    assertEquals(
        tempDir.resolve("file2.txt").toAbsolutePath().toString(),
        recentFiles.get(4).getAbsolutePath());
  }

  @Test
  void getTheme_shouldReturnDefaultWhenNotSet() {
    assertEquals(SettingsManager.THEME_SYSTEM, settingsManager.getTheme());
  }

  @Test
  void setAndGetTheme_shouldStoreAndRetrieveCorrectly() {
    settingsManager.setTheme(SettingsManager.THEME_DARK);
    assertEquals(SettingsManager.THEME_DARK, settingsManager.getTheme());
  }

  @Test
  void getFontSize_shouldReturnDefaultWhenNotSet() {
    assertEquals(12, settingsManager.getFontSize());
  }

  @Test
  void setAndGetFontSize_shouldStoreAndRetrieveCorrectly() {
    settingsManager.setFontSize(16);
    assertEquals(16, settingsManager.getFontSize());
  }

  @Test
  void getFontFamily_shouldReturnDefaultWhenNotSet() {
    assertEquals("SansSerif", settingsManager.getFontFamily());
  }

  @Test
  void setAndGetFontFamily_shouldStoreAndRetrieveCorrectly() {
    settingsManager.setFontFamily("Monospaced");
    assertEquals("Monospaced", settingsManager.getFontFamily());
  }

  @Test
  void resetUiSettings_shouldResetAllUiSettings() {
    settingsManager.setTheme(SettingsManager.THEME_LIGHT);
    settingsManager.setFontSize(20);
    settingsManager.setFontFamily("Arial");

    settingsManager.resetUiSettings();

    assertEquals(SettingsManager.THEME_SYSTEM, settingsManager.getTheme());
    assertEquals(12, settingsManager.getFontSize());
    assertEquals("SansSerif", settingsManager.getFontFamily());
  }
}
